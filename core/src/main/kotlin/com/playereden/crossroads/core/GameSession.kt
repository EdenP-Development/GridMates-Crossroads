package com.playereden.crossroads.core

import kotlin.random.Random

/**
 * Truthful, reconstructed finish receipt for Lab v5 only.
 *
 * This is derived from authoritative action replay; it is NOT a serialized
 * Snapshot/checkpoint field, so prior board identities and save formats stay intact.
 */
data class FairFinishReceipt(
    val firstFinisherName: String,
    val winnerName: String,
    val winningUnusedSteps: Int,
    val finishers: Int,
    val tiedOnWinningSteps: Boolean,
    val laterSeatsGotFinalChance: Boolean
)

/**
 * Small deterministic local rules engine. No sockets, UI, account or Android dependency.
 * Dice/events are sourced from one seeded PRNG owned by the session.
 */
class GameSession(
    val board: BoardDefinition,
    private val catalogue: EventCatalogue,
    seats: Int = 2,
    seed: Int = 1234,
    humanIndices: Set<Int> = setOf(0),
    val botPolicy: BotPolicyVersion = BotPolicyVersion.LEGACY_V1,
    botSeatPolicies: Map<Int, BotPolicyVersion> = emptyMap()
) {
    private val random = Random(seed)
    private val initialSeed = seed
    val playtestRules: Boolean = DecisionLab.enabled(board)
    // Reconstructed entirely from actions during checkpoint replay. Neither
    // this transient guard nor UI animation enters the saved snapshot.
    private var steadyFooting = false
    private var pushedThisTurn = false
    // V5 finish-line contenders are reconstructed solely from actions on checkpoint replay.
    // There is no new serialized schema field and older rule versions never touch them.
    private var equalRoundLeader: Int? = null
    private var equalRoundLeadExcess: Int = -1
    private var equalRoundFinishers: Int = 0
    private var equalRoundFirstFinisher: Int? = null
    private var equalRoundBestExcessTies: Int = 0

    /** Present only after a completed v5 game; always restored by action replay. */
    val fairFinishReceipt: FairFinishReceipt? get() {
        if (!DecisionLab.isV5(board) || phase != Phase.FINISHED) return null
        val first = equalRoundFirstFinisher ?: return null
        val leader = equalRoundLeader ?: return null
        return FairFinishReceipt(
            firstFinisherName = players[first].name,
            winnerName = players[leader].name,
            winningUnusedSteps = equalRoundLeadExcess,
            finishers = equalRoundFinishers,
            tiedOnWinningSteps = equalRoundBestExcessTies > 1,
            laterSeatsGotFinalChance = first < players.lastIndex
        )
    }
    val finalRoundPending: Boolean get() =
        DecisionLab.finishRoundEqually(board) && equalRoundLeader != null && phase != Phase.FINISHED
    /** Read-only movement assessment; no random draws or state changes. */
    internal fun directFinishOpportunity(): FinishOpportunityV1? {
        if (!playtestRules || phase != Phase.ROLL) return null
        return BotFinishOpportunityV1.assess(board, players[currentIndex].position,
            if (finalRoundPending) equalRoundLeadExcess else null)
    }

    val leadingFinisherName: String? get() =
        if (finalRoundPending) equalRoundLeader?.let { players[it].name } else null
    /** Live, replay-derived travel stance; used only to describe Lab route choices. */
    val steadyTravelChosen: Boolean get() = playtestRules && steadyFooting
    /** At most one on-entry encounter (and v2 shelter bonus) per entire turn. */
    val routeEncounterAvailable: Boolean get() = playtestRules && !eventTriggeredThisTurn
    /** Accurate preview for the currently pending v4 hazard; no RNG or effect yet. */
    val pendingPushHazardBonus: Int get() =
        if (phase == Phase.ENCOUNTER && pendingEncounterPool == "hazard")
            DecisionLab.pushHazardSuccessBonus(board, pushedThisTurn) else 0
    private val originalHumanIndices = humanIndices.toSortedSet()
    private val seatPolicyOverrides = botSeatPolicies.toSortedMap()
    fun policyForBotSeat(seat: Int): BotPolicyVersion {
        require(seat in players.indices && players[seat].isBot)
        return seatPolicyOverrides[seat] ?: botPolicy
    }
    private val acceptedActions = mutableListOf<MatchAction>()
    // A bot turn executes one nested legal roll/route/target internally; the
    // checkpoint records only the public bot command, so replay consumes the
    // exact same random draws.
    private var insideBotCommand = false
    // Diagnostic receipt only: never serialized, never read by a policy or rules.
    internal var lastBotDecision: MatchAction? = null
        private set
    private fun record(action: MatchAction) {
        if (insideBotCommand) lastBotDecision = action
        if (!insideBotCommand) {
            check(acceptedActions.size < MatchCheckpointCodec.MAX_ACTIONS) {
                "Match exceeded supported checkpoint action count"
            }
            acceptedActions.add(action)
        }
    }
    val players: List<Player>
    var phase: Phase = Phase.ROLL
        private set
    var currentIndex: Int = 0
        private set
    var remainingSteps: Int = 0
        private set
    var die: Int? = null
        private set
    var winnerIndex: Int? = null
        private set
    var turn: Int = 1
        private set
    var revision: Int = 0
        private set
    var lastEventId: String? = null
        private set
    var lastMessage: String = "Reach FINISH first. The path is yours to choose!"
        private set
    private var eventTriggeredThisTurn = false
    private var pendingPrank = 0
    private var pendingPrankName = ""
    private var pendingEncounterPool = ""
    private var pendingEncounterRegion = ""
    private var pendingEncounterRoute = ""
    private fun spacesText(count: Int): String = "$count ${if (count == 1) "space" else "spaces"}"
    private val encounterLog = mutableListOf<EncounterNote>()
    private fun recordEncounter(title: String, outcome: String, kind: String) {
        encounterLog.add(EncounterNote(turn, players[currentIndex].name, title, outcome, kind))
        if (encounterLog.size > 16) encounterLog.removeAt(0)
    }

    init {
        require(seats in 2..4)
        require(seatPolicyOverrides.keys.all { it in 0 until seats && it !in humanIndices })
        require(board.start != board.finish)
        require(board.nodes.map { it.id }.toSet().size == board.nodes.size)
        require(board.nodes.any { it.id == board.start } && board.nodes.any { it.id == board.finish })
        players = (0 until seats).map { seat ->
            val isBot = seat !in humanIndices
            val name = when {
                isBot -> "AI $seat"
                humanIndices.size == 1 -> "You"
                else -> "Player ${seat + 1}"
            }
            Player(name, isBot, board.start, mutableListOf(board.start))
        }
    }

    /**
     * Produce a verified replay checkpoint. UI timing/camera and completed-match
     * statistics are deliberately not part of rules state.
     */
    fun checkpoint(matchId: String, startedAtEpochMillis: Long): MatchCheckpoint {
        require(matchId.isNotBlank() && matchId.length <= 100)
        require(startedAtEpochMillis > 0)
        return MatchCheckpoint(
            formatVersion = MatchCheckpointCodec.FORMAT_VERSION,
            matchId = matchId,
            startedAtEpochMillis = startedAtEpochMillis,
            boardId = board.id,
            contentFingerprint = MatchCheckpointCodec.fingerprint(board, catalogue),
            seed = initialSeed,
            seats = players.size,
            humanIndices = originalHumanIndices.toList(),
            actions = acceptedActions.toList(),
            expected = snapshot(),
            botPolicy = botPolicy,
            botSeatPolicies = seatPolicyOverrides.toMap()
        )
    }

    companion object {
        /** Rejects corrupted or incompatible local saves instead of partial restores. */
        fun restore(
            board: BoardDefinition,
            catalogue: EventCatalogue,
            checkpoint: MatchCheckpoint
        ): GameSession {
            require(checkpoint.formatVersion == MatchCheckpointCodec.FORMAT_VERSION) {
                "Unsupported match checkpoint version"
            }
            require(checkpoint.matchId.isNotBlank() && checkpoint.matchId.length <= 100)
            require(checkpoint.startedAtEpochMillis > 0)
            require(checkpoint.boardId == board.id)
            require(checkpoint.contentFingerprint == MatchCheckpointCodec.fingerprint(board, catalogue)) {
                "Board or event definitions changed"
            }
            require(checkpoint.seats in 2..4)
            require(checkpoint.humanIndices.distinct().size == checkpoint.humanIndices.size)
            require(checkpoint.humanIndices.all { it in 0 until checkpoint.seats })
            require(checkpoint.actions.size <= MatchCheckpointCodec.MAX_ACTIONS)

            val game = GameSession(
                board, catalogue, checkpoint.seats, checkpoint.seed,
                humanIndices = checkpoint.humanIndices.toSet(),
                botPolicy = checkpoint.botPolicy,
                botSeatPolicies = checkpoint.botSeatPolicies
            )
            checkpoint.actions.forEach { action ->
                when (action.kind) {
                    MatchActionKind.ROLL -> {
                        require(action.routeId == null && action.targetIndex == null)
                        game.roll()
                    }
                    MatchActionKind.STEADY_ROLL -> {
                        require(action.routeId == null && action.targetIndex == null)
                        game.rollSteady()
                    }
                    MatchActionKind.BOLD_ROLL -> {
                        require(action.routeId == null && action.targetIndex == null)
                        game.rollBold()
                    }
                    MatchActionKind.ENCOUNTER -> {
                        require(action.routeId != null && action.targetIndex == null)
                        game.chooseEncounter(action.routeId)
                    }
                    MatchActionKind.ROUTE -> {
                        require(action.routeId != null && action.targetIndex == null)
                        game.chooseRoute(action.routeId)
                    }
                    MatchActionKind.TARGET -> {
                        require(action.routeId == null && action.targetIndex != null)
                        game.chooseTarget(action.targetIndex)
                    }
                    MatchActionKind.BOT -> {
                        require(action.routeId == null && action.targetIndex == null)
                        game.botStep()
                    }
                }
            }
            require(game.acceptedActions == checkpoint.actions) {
                "Action replay was incomplete"
            }
            require(game.snapshot() == checkpoint.expected) {
                "Recovered rules state differs from the saved match"
            }
            return game
        }
    }

    /**
     * Start a fresh race with the exact completed course, rules version and
     * human seats. A new seed is supplied by the caller. Never reuse match
     * progress or reinterpret a restored match through home preferences.
     */
    fun freshRematch(seed: Int): GameSession {
        require(phase == Phase.FINISHED) { "Finish the current race before rematching" }
        return GameSession(board, catalogue, players.size, seed, originalHumanIndices, botPolicy, seatPolicyOverrides)
    }

    fun snapshot(): Snapshot = Snapshot(
        revision, phase, currentIndex,
        players.map { it.copy(traversed = it.traversed.toMutableList()) },
        remainingSteps, die, lastMessage, winnerIndex, turn, lastEventId,
        encounterLog.toList()
    )

    fun roll() {
        require(!playtestRules) { "Decision Lab requires a travel choice" }
        require(phase == Phase.ROLL) { "Not waiting for a roll" }
        die = random.nextInt(1, 7)
        remainingSteps = requireNotNull(die)
        lastEventId = null
        lastMessage = "${players[currentIndex].name} rolled $die."
        progress()
        revision++
        record(MatchAction(MatchActionKind.ROLL))
    }

    /**
     * Decision Lab: steady has predictable movement and protects against one
     * setback encountered THIS turn. Bold can move 5–8, but die 1–2
     * stumbles move only 1 space. Not available on legacy/LAN.
     */
    fun rollSteady() = travelRoll(steady = true)
    fun rollBold() = travelRoll(steady = false)

    private fun travelRoll(steady: Boolean) {
        require(playtestRules) { "Travel tactics are not available in classic rules" }
        require(phase == Phase.ROLL) { "Not waiting for a roll" }
        steadyFooting = steady
        pushedThisTurn = !steady
        if (steady) {
            die = 4
            remainingSteps = 4
            lastMessage = "${players[currentIndex].name} chose Steady: 4 steps and firm footing."
        } else {
            die = random.nextInt(1, 7)
            remainingSteps = if (requireNotNull(die) <= 2) 1 else requireNotNull(die) + 2
            lastMessage = if (requireNotNull(die) <= 2)
                "${players[currentIndex].name} pushed too hard and stumbled: 1 step."
            else "${players[currentIndex].name} pushed ahead: $remainingSteps steps."
        }
        lastEventId = null
        progress()
        revision++
        record(MatchAction(
            if (steady) MatchActionKind.STEADY_ROLL else MatchActionKind.BOLD_ROLL
        ))
    }

    fun availableRoutes(): List<BoardNode> =
        if (phase == Phase.ROUTE) board.node(players[currentIndex].position).next.map(board::node)
        else emptyList()

    fun chooseRoute(id: String) {
        require(phase == Phase.ROUTE) { "Not at a crossroads" }
        require(id in board.node(players[currentIndex].position).next) { "Invalid route" }
        val previous = players[currentIndex].position
        val choice = board.forkAt(previous)?.choices?.firstOrNull { it.to == id }
        stepForward(id)
        remainingSteps--
        val label = choice?.label ?: board.node(id).region
        lastMessage = "${players[currentIndex].name} chose $label."
        if (playtestRules && !eventTriggeredThisTurn && choice != null) {
            resolveRouteCommitment(choice, board.node(id))
        } else progress()
        revision++
        record(MatchAction(MatchActionKind.ROUTE, routeId = id))
    }

    /** Optional Lab v3/v4 response. Never surfaced for older rule variants. */
    fun availableEncounterChoices(): List<String> {
        if (!DecisionLab.hasInteractiveEncounter(board) || phase != Phase.ENCOUNTER) return emptyList()
        return when (pendingEncounterPool) {
            "shelter" -> listOf("supplies", "cover")
            "hazard" -> listOf("gamble", "brace")
            else -> emptyList()
        }
    }

    fun chooseEncounter(choice: String) {
        require(phase == Phase.ENCOUNTER && DecisionLab.hasInteractiveEncounter(board)) {
            "No encounter decision is pending"
        }
        require(choice in availableEncounterChoices()) { "Unavailable encounter choice" }
        val kind = pendingEncounterPool
        val region = pendingEncounterRegion
        val route = pendingEncounterRoute
        pendingEncounterPool = ""
        pendingEncounterRegion = ""
        pendingEncounterRoute = ""

        when (choice) {
            "supplies" -> {
                remainingSteps += 2
                lastEventId = "lab_v3_shelter_supplies"
                lastMessage = "${players[currentIndex].name} collected supplies on $route: +2 steps."
                recordEncounter("Shelter supplies", "Advance 2 spaces", "benefit")
                progress()
            }
            "cover" -> {
                remainingSteps += 1
                players[currentIndex].shield = true
                lastEventId = "lab_v3_shelter_cover"
                lastMessage = "${players[currentIndex].name} took cover on $route: shield and +1 step."
                recordEncounter("Shelter cover", "Shield and 1 step", "benefit")
                progress()
            }
            "brace" -> {
                players[currentIndex].shield = true
                lastEventId = "lab_v3_hazard_brace"
                lastMessage = "${players[currentIndex].name} braced on $route: shield gained, move ended."
                recordEncounter("Hazard avoided", "Shield gained; stopped safely", "benefit")
                finishTurn()
            }
            "gamble" -> {
                val pool = catalogue.events.filter {
                    region in it.regions && "hazard" in it.pools && it.weight > 0
                }
                require(pool.isNotEmpty()) { "No regional hazard event" }
                var ticket = random.nextInt(pool.sumOf { it.weight })
                val event = pool.first { ticket -= it.weight; ticket < 0 }
                lastEventId = event.id
                val title = event.description.ifBlank { event.id.replace('_', ' ') }
                when (event.effect.type) {
                    "advance" -> {
                        val successBonus = DecisionLab.pushHazardSuccessBonus(board, pushedThisTurn)
                        val gained = event.effect.spaces + successBonus
                        remainingSteps += gained
                        val bonusCopy = if (successBonus > 0) " (Push risk bonus +$successBonus)" else ""
                        lastMessage = "${players[currentIndex].name} risked $route: $title! +$gained steps$bonusCopy."
                        recordEncounter(title, "Risk succeeded: +$gained steps$bonusCopy", "benefit")
                        progress()
                    }
                    "retreat" -> {
                        if (steadyFooting) {
                            steadyFooting = false
                            lastMessage = "${players[currentIndex].name} steadied through $title."
                            recordEncounter(title, "Steady blocked the setback", "benefit")
                            progress()
                        } else {
                            moveBack(players[currentIndex], event.effect.spaces)
                            lastMessage = "${players[currentIndex].name} risked $route: $title! Back ${event.effect.spaces}."
                            recordEncounter(title, "Risk failed: back ${event.effect.spaces}", "setback")
                            finishTurn()
                        }
                    }
                    else -> error("Unsupported hazard outcome ${event.effect.type}")
                }
            }
            else -> error("Unexpected encounter choice")
        }
        revision++
        record(MatchAction(MatchActionKind.ENCOUNTER, routeId = choice))
    }

    fun availableTargets(): List<Int> =
        if (phase == Phase.TARGET) players.indices.filter { it != currentIndex && players[it].position != board.finish }
        else emptyList()

    fun chooseTarget(index: Int) {
        require(phase == Phase.TARGET) { "No mischief target pending" }
        require(index in availableTargets()) { "Invalid target" }
        val target = players[index]
        if (target.shield) {
            target.shield = false
            lastMessage = "${target.name}'s shield blocked a prank."
        } else {
            moveBack(target, pendingPrank)
            lastMessage = "${players[currentIndex].name} pranked ${target.name}: back $pendingPrank!"
        }
        recordEncounter(pendingPrankName.ifBlank { "Woodland Mischief" }, lastMessage, "mischief")
        pendingPrank = 0
        pendingPrankName = ""
        finishTurn()
        revision++
        record(MatchAction(MatchActionKind.TARGET, targetIndex = index))
    }

    /** Bot submits exactly one regular legal action. UI may animate between actions. */
    fun botStep() {
        lastBotDecision = null
        when (policyForBotSeat(currentIndex)) {
        BotPolicyVersion.LEGACY_V1 -> legacyBotStep()
        BotPolicyVersion.ADAPTIVE_V2_R1 -> adaptiveBotStep()
        BotPolicyVersion.ADAPTIVE_V2_R2 -> adaptiveRouteBotStep()
        BotPolicyVersion.ADAPTIVE_V2_R3 -> adaptiveEncounterBotStep()
        BotPolicyVersion.ADAPTIVE_V2_R4 -> adaptiveTargetBotStep()
        BotPolicyVersion.GAMBLER_V1 -> gamblerBotStep()
        BotPolicyVersion.GAMBLER_V2 -> gamblerBotStep(refined = true)
        BotPolicyVersion.GUARDIAN_V1 -> guardianBotStep()
        BotPolicyVersion.STRATEGIST_V1 -> strategistBotStep()
        BotPolicyVersion.SCOUT_RELAXED_V1 -> scoutDifficultyBotStep(BotDifficultyV1.RELAXED)
        BotPolicyVersion.SCOUT_ADVANCED_V1 -> scoutDifficultyBotStep(BotDifficultyV1.ADVANCED)
        }
    }

    // R1 is intentionally narrow: adaptive travel only; all other phases retain V1.
    // Never mutate this version once saved matches exist; future revisions get new IDs.
    private fun adaptiveBotStep() {
        require(players[currentIndex].isBot && phase != Phase.FINISHED)
        if (phase != Phase.ROLL || !playtestRules) {
            legacyBotStep()
            return
        }
        val awareness = BotRaceAssessment.assess(currentIndex,
            players.mapIndexed { seat, player ->
                BotRacerView(seat, distanceToFinish(player.position), player.shield)
            }, distanceToFinish(board.start), finalRoundPending)
        insideBotCommand = true
        try {
            if (AdaptiveBotTravelR1.shouldPush(awareness)) rollBold() else rollSteady()
        } finally {
            insideBotCommand = false
        }
        record(MatchAction(MatchActionKind.BOT))
    }

    private fun gamblerBotStep(refined: Boolean = false) {
        require(players[currentIndex].isBot && phase != Phase.FINISHED)
        if (!playtestRules || (phase != Phase.ROLL && phase != Phase.ENCOUNTER)) {
            adaptiveTargetBotStep()
            return
        }
        val awareness = BotRaceAssessment.assess(currentIndex,
            players.mapIndexed { seat, player ->
                BotRacerView(seat, distanceToFinish(player.position), player.shield)
            }, distanceToFinish(board.start), finalRoundPending)
        insideBotCommand = true
        try {
            if (phase == Phase.ROLL) {
                if (GamblerPersonalityV1.shouldPush(awareness)) rollBold() else rollSteady()
            } else {
                val options = availableEncounterChoices()
                val choice = if (refined) GamblerPersonalityV2.chooseEncounter(options,
                    awareness, remainingSteps, steadyTravelChosen, pendingPushHazardBonus)
                else GamblerPersonalityV1.chooseEncounter(options,
                    awareness, remainingSteps, steadyTravelChosen, pendingPushHazardBonus)
                chooseEncounter(choice)
            }
        } finally {
            insideBotCommand = false
        }
        record(MatchAction(MatchActionKind.BOT))
    }

    private fun guardianBotStep() {
        require(players[currentIndex].isBot && phase != Phase.FINISHED)
        if (!playtestRules || phase == Phase.TARGET) {
            adaptiveTargetBotStep()
            return
        }
        val awareness = BotRaceAssessment.assess(currentIndex,
            players.mapIndexed { seat, player ->
                BotRacerView(seat, distanceToFinish(player.position), player.shield)
            }, distanceToFinish(board.start), finalRoundPending)
        insideBotCommand = true
        try {
            when (phase) {
                Phase.ROLL -> if (GuardianPersonalityV1.shouldPush(awareness))
                    rollBold() else rollSteady()
                Phase.ENCOUNTER -> chooseEncounter(GuardianPersonalityV1.chooseEncounter(
                    availableEncounterChoices(), awareness, remainingSteps, steadyTravelChosen))
                Phase.ROUTE -> {
                    val fork = board.forkAt(players[currentIndex].position)
                    val chosen = availableRoutes().minWith(compareBy<BoardNode> { route ->
                        val risk = fork?.choices?.firstOrNull { it.to == route.id }?.risk
                        val base = AdaptiveBotRouteR2.score(distanceToFinish(route.id), risk,
                            BotRouteLookahead.inspect(board, route.id, remainingSteps),
                            awareness, routeEncounterAvailable, steadyTravelChosen &&
                                DecisionLab.supportsSteadyShelterBonus(board))
                        GuardianPersonalityV1.routeScore(base, risk, awareness, routeEncounterAvailable)
                    }.thenBy { it.id })
                    chooseRoute(chosen.id)
                }
                else -> error("Unexpected Guardian phase")
            }
        } finally {
            insideBotCommand = false
        }
        record(MatchAction(MatchActionKind.BOT))
    }

    private fun strategistBotStep() {
        require(players[currentIndex].isBot && phase != Phase.FINISHED)
        if (!playtestRules || phase == Phase.ENCOUNTER) {
            adaptiveTargetBotStep()
            return
        }
        val awareness = BotRaceAssessment.assess(currentIndex,
            players.mapIndexed { seat, player ->
                BotRacerView(seat, distanceToFinish(player.position), player.shield)
            }, distanceToFinish(board.start), finalRoundPending)
        insideBotCommand = true
        try {
            when (phase) {
                Phase.ROLL -> if (StrategistPersonalityV1.shouldPush(awareness))
                    rollBold() else rollSteady()
                Phase.ROUTE -> {
                    val fork = board.forkAt(players[currentIndex].position)
                    val chosen = availableRoutes().minWith(compareBy<BoardNode> { route ->
                        val risk = fork?.choices?.firstOrNull { it.to == route.id }?.risk
                        val horizon = BotRouteLookahead.inspect(board, route.id, remainingSteps)
                        val base = AdaptiveBotRouteR2.score(distanceToFinish(route.id), risk,
                            horizon, awareness, routeEncounterAvailable, steadyTravelChosen &&
                                DecisionLab.supportsSteadyShelterBonus(board))
                        StrategistPersonalityV1.routeScore(base, horizon, awareness, routeEncounterAvailable)
                    }.thenBy { it.id })
                    chooseRoute(chosen.id)
                }
                Phase.TARGET -> {
                    val candidates = availableTargets().map { seat ->
                        val player = players[seat]
                        val retreatIndex = (player.traversed.lastIndex - pendingPrank).coerceAtLeast(0)
                        val distance = distanceToFinish(player.position)
                        val after = if (player.shield) distance else
                            distanceToFinish(player.traversed[retreatIndex])
                        BotTargetView(seat, distance, player.shield, after)
                    }
                    val target = StrategistPersonalityV1.chooseTarget(candidates)
                    if (target == null) { finishTurn(); revision++ }
                    else chooseTarget(target)
                }
                else -> error("Unexpected Strategist phase")
            }
        } finally {
            insideBotCommand = false
        }
        record(MatchAction(MatchActionKind.BOT))
    }

    private fun adaptiveTargetBotStep() {
        require(players[currentIndex].isBot && phase != Phase.FINISHED)
        if (phase != Phase.TARGET) {
            adaptiveEncounterBotStep()
            return
        }
        val candidates = availableTargets().map { seat ->
            val player = players[seat]
            val retreatIndex = (player.traversed.lastIndex - pendingPrank).coerceAtLeast(0)
            val distance = distanceToFinish(player.position)
            val after = if (player.shield) distance else
                distanceToFinish(player.traversed[retreatIndex])
            BotTargetView(seat, distance, player.shield, after)
        }
        val target = AdaptiveBotTargetR4.choose(candidates)
        insideBotCommand = true
        try {
            if (target == null) { finishTurn(); revision++ }
            else chooseTarget(target)
        } finally {
            insideBotCommand = false
        }
        record(MatchAction(MatchActionKind.BOT))
    }

    private fun adaptiveEncounterBotStep() {
        require(players[currentIndex].isBot && phase != Phase.FINISHED)
        if (phase != Phase.ENCOUNTER) {
            adaptiveRouteBotStep()
            return
        }
        val awareness = BotRaceAssessment.assess(currentIndex,
            players.mapIndexed { seat, player ->
                BotRacerView(seat, distanceToFinish(player.position), player.shield)
            }, distanceToFinish(board.start), finalRoundPending)
        val choice = AdaptiveBotEncounterR3.choose(availableEncounterChoices(),
            awareness, remainingSteps, steadyTravelChosen, pendingPushHazardBonus)
        insideBotCommand = true
        try {
            chooseEncounter(choice)
        } finally {
            insideBotCommand = false
        }
        record(MatchAction(MatchActionKind.BOT))
    }

    // First difficulty experiment: Scout route choices only. All other
    // phases retain the exact existing R2 behavior and random draws.
    private fun scoutDifficultyBotStep(difficulty: BotDifficultyV1) {
        require(players[currentIndex].isBot && phase != Phase.FINISHED)
        if (phase != Phase.ROUTE || !playtestRules) {
            adaptiveRouteBotStep()
            return
        }
        val awareness = BotRaceAssessment.assess(currentIndex,
            players.mapIndexed { seat, player ->
                BotRacerView(seat, distanceToFinish(player.position), player.shield)
            }, distanceToFinish(board.start), finalRoundPending)
        val fork = board.forkAt(players[currentIndex].position)
        val chosen = availableRoutes().minWith(compareBy<BoardNode> { route ->
            val distance = distanceToFinish(route.id)
            val horizon = BotRouteLookahead.inspect(board, route.id, remainingSteps)
            val base = AdaptiveBotRouteR2.score(distance,
                fork?.choices?.firstOrNull { it.to == route.id }?.risk,
                horizon, awareness, routeEncounterAvailable, steadyTravelChosen &&
                    DecisionLab.supportsSteadyShelterBonus(board))
            BotDifficultyRouteV1.score(difficulty, BotPersonalityV1.SCOUT,
                base, distance, horizon, routeEncounterAvailable)
        }.thenBy { it.id })
        insideBotCommand = true
        try {
            chooseRoute(chosen.id)
        } finally {
            insideBotCommand = false
        }
        record(MatchAction(MatchActionKind.BOT))
    }

    private fun adaptiveRouteBotStep() {
        require(players[currentIndex].isBot && phase != Phase.FINISHED)
        if (phase != Phase.ROUTE || !playtestRules) {
            adaptiveBotStep()
            return
        }
        val awareness = BotRaceAssessment.assess(currentIndex,
            players.mapIndexed { seat, player ->
                BotRacerView(seat, distanceToFinish(player.position), player.shield)
            }, distanceToFinish(board.start), finalRoundPending)
        val fork = board.forkAt(players[currentIndex].position)
        val chosen = availableRoutes().minWith(compareBy<BoardNode> { route ->
            AdaptiveBotRouteR2.score(
                distanceToFinish(route.id),
                fork?.choices?.firstOrNull { it.to == route.id }?.risk,
                BotRouteLookahead.inspect(board, route.id, remainingSteps),
                awareness, routeEncounterAvailable, steadyTravelChosen &&
                    DecisionLab.supportsSteadyShelterBonus(board)
            )
        }.thenBy { it.id })
        insideBotCommand = true
        try {
            chooseRoute(chosen.id)
        } finally {
            insideBotCommand = false
        }
        record(MatchAction(MatchActionKind.BOT))
    }

    private fun legacyBotStep() {
        require(players[currentIndex].isBot && phase != Phase.FINISHED)
        insideBotCommand = true
        try {
        when (phase) {
            Phase.ROLL -> if (playtestRules) {
                val own = distanceToFinish(players[currentIndex].position)
                val nearest = players.indices.filter { it != currentIndex }
                    .minOf { distanceToFinish(players[it].position) }
                if (own <= nearest) rollSteady() else rollBold()
            } else roll()
            Phase.ROUTE -> {
                val routes = availableRoutes()
                val fork = board.forkAt(players[currentIndex].position)
                val myDistance = distanceToFinish(players[currentIndex].position)
                val closestOpponent = players.indices.filter { it != currentIndex }
                    .minOfOrNull { distanceToFinish(players[it].position) } ?: myDistance
                val leading = myDistance < closestOpponent
                // Bots use visible route lengths and advertised risk, never hidden event outcomes.
                // A leader values shelter; a trailing bot takes more calculated gambles.
                val chosen = routes.minBy { route ->
                    val risk = fork?.choices?.firstOrNull { it.to == route.id }?.risk
                    val riskCost = when (risk) {
                        "high" -> if (leading) 2.1 else 0.55
                        "medium" -> if (leading) 0.8 else 0.35
                        "low" -> if (leading) -1.6 else -0.35
                        else -> 0.0
                    }
                    distanceToFinish(route.id).toDouble() + riskCost + random.nextDouble(0.0, 1.3)
                }
                chooseRoute(chosen.id)
            }
            Phase.ENCOUNTER -> {
                val options = availableEncounterChoices()
                val own = distanceToFinish(players[currentIndex].position)
                val closest = players.indices.filter { it != currentIndex }
                    .minOf { distanceToFinish(players[it].position) }
                val leading = own < closest
                val choice = when {
                    "supplies" in options -> if (leading && !players[currentIndex].shield)
                        "cover" else "supplies"
                    "gamble" in options -> if (leading && !players[currentIndex].shield)
                        "brace" else "gamble"
                    else -> error("No valid encounter choice")
                }
                chooseEncounter(choice)
            }
            Phase.TARGET -> {
                val targets = availableTargets()
                if (targets.isEmpty()) { finishTurn(); revision++ }
                else chooseTarget(targets.minBy { distanceToFinish(players[it].position) })
            }
            Phase.FINISHED -> Unit
        }
        } finally {
            insideBotCommand = false
        }
        record(MatchAction(MatchActionKind.BOT))
    }

    private fun progress() {
        while (remainingSteps > 0 && phase != Phase.FINISHED) {
            val node = board.node(players[currentIndex].position)
            if (node.next.size > 1) {
                phase = Phase.ROUTE
                return
            }
            if (node.next.isEmpty()) { declareWinner(); return }
            stepForward(node.next.single())
            remainingSteps--
            if (players[currentIndex].position == board.finish) { declareWinner(); return }
        }
        if (phase != Phase.FINISHED) resolveLanding()
    }

    private fun stepForward(next: String) {
        val player = players[currentIndex]
        require(next in board.node(player.position).next) { "Nonadjacent move" }
        player.position = next
        player.traversed.add(next)
    }

    /**
     * Commit to a route = encounter its advertised risk. On legacy boards,
     * chance events still trigger only on landing as before. Lab routes
     * instead always draw one eligible event on entry (at most once per
     * turn), fixing the issue where long dice throws skipped every hazard.
     */
    private fun resolveRouteCommitment(choice: ForkChoice, entered: BoardNode) {
        val poolName = when (choice.risk) {
            "high" -> "hazard"
            "medium" -> "mischief"
            "low" -> "shelter"
            else -> { progress(); return }
        }
        val options = catalogue.events.filter { event ->
            entered.region in event.regions && poolName in event.pools && event.weight > 0
        }
        if (options.isEmpty()) {
            // A missing regional pool must never stall or manufacture an event.
            progress()
            return
        }
        eventTriggeredThisTurn = true
        // v2+ sheltered paths are longer (often two spaces on Long).
        // A player who commits to Steady AND Shelter keeps a one-space
        // momentum advantage, in addition to the single existing shelter draw.
        // This is intentionally not applied on v1, other risks, or LAN.
        val steadyShelterBonus =
            if (DecisionLab.supportsSteadyShelterBonus(board) &&
                choice.risk == "low" && steadyFooting) 1
            else 0
        remainingSteps += steadyShelterBonus
        val bonusMessage = if (steadyShelterBonus > 0) " · Steady trail +1" else ""
        if (DecisionLab.hasInteractiveEncounter(board) && poolName in listOf("shelter", "hazard")) {
            pendingEncounterPool = poolName
            pendingEncounterRegion = entered.region
            pendingEncounterRoute = choice.label
            phase = Phase.ENCOUNTER
            lastMessage = "${players[currentIndex].name} entered ${choice.label}. Choose how to respond."
            return
        }
        val total = options.sumOf { it.weight }
        var ticket = random.nextInt(total)
        val event = options.first { candidate ->
            ticket -= candidate.weight
            ticket < 0
        }
        lastEventId = event.id
        val title = event.description.ifBlank { event.id.replace('_', ' ') }
        when (event.effect.type) {
            "advance" -> {
                remainingSteps += event.effect.spaces
                lastMessage = "${players[currentIndex].name}: $title! +${event.effect.spaces} steps$bonusMessage."
                recordEncounter(title, "Route benefit: +${event.effect.spaces} steps$bonusMessage", "benefit")
                progress()
            }
            "retreat" -> {
                if (steadyFooting) {
                    steadyFooting = false
                    lastMessage = "${players[currentIndex].name} held steady through $title!"
                    recordEncounter(title, "Steady footing blocked the setback", "benefit")
                    progress()
                } else {
                    moveBack(players[currentIndex], event.effect.spaces)
                    lastMessage = "${players[currentIndex].name}: $title! Back ${event.effect.spaces}."
                    recordEncounter(title, "Route setback: back ${spacesText(event.effect.spaces)}", "setback")
                    finishTurn()
                }
            }
            "shield" -> {
                players[currentIndex].shield = true
                lastMessage = "${players[currentIndex].name} found protection on $title$bonusMessage."
                recordEncounter(title, "Gained a shield$bonusMessage", "benefit")
                progress()
            }
            "prank" -> {
                val targets = availablePrankTargets()
                if (targets.isEmpty() || event.effect.spaces <= 0) {
                    progress()
                } else {
                    pendingPrank = event.effect.spaces
                    pendingPrankName = title
                    phase = Phase.TARGET
                    lastMessage = "Mischief! Choose a rival to send back ${spacesText(pendingPrank)}."
                }
            }
            else -> error("Unsupported route event ${event.effect.type}")
        }
    }

    private fun availablePrankTargets(): List<Int> =
        players.indices.filter { it != currentIndex && players[it].position != board.finish }

    private fun resolveLanding() {
        if (eventTriggeredThisTurn || board.node(players[currentIndex].position).kind != "event") {
            finishTurn()
            return
        }
        eventTriggeredThisTurn = true
        val space = board.node(players[currentIndex].position)
        val region = space.region
        val pool = catalogue.events.filter { event ->
            region in event.regions && space.eventPool in event.pools && event.weight > 0
        }
        if (pool.isEmpty()) {
            lastMessage = "Quiet stretch of road."
            finishTurn()
            return
        }
        val total = pool.sumOf { it.weight }
        var ticket = random.nextInt(total)
        val event = pool.first { e -> ticket -= e.weight; ticket < 0 }
        lastEventId = event.id
        val encounterTitle = event.description.ifBlank { event.id.replace('_', ' ') }
        val effect = event.effect
        when (effect.type) {
            "advance" -> {
                lastMessage = "${players[currentIndex].name}: $encounterTitle! Move +${effect.spaces}."
                recordEncounter(encounterTitle, "Moved ahead ${spacesText(effect.spaces)}", "benefit")
                remainingSteps = effect.spaces
                if (remainingSteps > 0) progress() else finishTurn()
            }
            "retreat" -> {
                if (playtestRules && steadyFooting) {
                    steadyFooting = false
                    lastMessage = "${players[currentIndex].name} steadied against $encounterTitle."
                    recordEncounter(encounterTitle, "Steady footing blocked the setback", "benefit")
                } else {
                    moveBack(players[currentIndex], effect.spaces)
                    lastMessage = "${players[currentIndex].name}: $encounterTitle! Back ${effect.spaces}."
                    recordEncounter(encounterTitle, "Moved back ${spacesText(effect.spaces)}", "setback")
                }
                finishTurn()
            }
            "shield" -> {
                players[currentIndex].shield = true
                lastMessage = "${players[currentIndex].name} found a shield!"
                recordEncounter(encounterTitle, "Gained protection from one prank", "benefit")
                finishTurn()
            }
            "prank" -> {
                val targets = players.indices.filter { it != currentIndex }
                if (targets.isEmpty() || effect.spaces <= 0) {
                    finishTurn()
                } else {
                    pendingPrank = effect.spaces
                    pendingPrankName = encounterTitle
                    phase = Phase.TARGET
                    lastMessage = "Mischief! Choose someone to send back ${spacesText(effect.spaces)}."
                }
            }
            else -> error("Unsupported event effect ${effect.type}")
        }
    }

    private fun moveBack(player: Player, spaces: Int) {
        repeat(spaces) {
            if (player.traversed.size > 1) player.traversed.removeAt(player.traversed.lastIndex)
        }
        player.position = player.traversed.last()
    }

    private fun declareWinner() {
        if (!DecisionLab.finishRoundEqually(board)) {
            winnerIndex = currentIndex
            phase = Phase.FINISHED
            remainingSteps = 0
            lastMessage = "${players[currentIndex].name} reached the finish and won!"
            return
        }

        val finishedName = players[currentIndex].name
        val excess = remainingSteps.coerceAtLeast(0)
        if (equalRoundFirstFinisher == null) equalRoundFirstFinisher = currentIndex
        // Higher unused movement wins if multiple reach FINISH this round.
        // Equal excess preserves earliest of those tied for the highest score.
        if (equalRoundLeader == null || excess > equalRoundLeadExcess) {
            equalRoundLeader = currentIndex
            equalRoundLeadExcess = excess
            equalRoundBestExcessTies = 1
        } else if (excess == equalRoundLeadExcess) {
            equalRoundBestExcessTies++
        }
        equalRoundFinishers++
        if (currentIndex == players.lastIndex) {
            finishEqualRound()
        } else {
            finishTurn()
            lastMessage = "$finishedName reached FINISH! Final round: remaining racers get one last turn."
        }
    }

    private fun finishEqualRound() {
        val leader = requireNotNull(equalRoundLeader)
        check(DecisionLab.finishRoundEqually(board))
        winnerIndex = leader
        currentIndex = leader
        phase = Phase.FINISHED
        remainingSteps = 0
        lastMessage = if (equalRoundFinishers > 1)
            "${players[leader].name} wins the final round! More unused steps wins; equal steps favor the first arrival."
        else "${players[leader].name} reached FINISH and won after everyone had a turn!"
    }

    private fun finishTurn() {
        // A first finish never denies the last seat its current-round action.
        // Resolve only AFTER the last seat completes a legal turn or reaches FINISH.
        if (DecisionLab.finishRoundEqually(board) &&
            equalRoundLeader != null && currentIndex == players.lastIndex) {
            finishEqualRound()
            return
        }
        currentIndex = (currentIndex + 1) % players.size
        phase = Phase.ROLL
        remainingSteps = 0
        die = null
        turn++
        eventTriggeredThisTurn = false
        pendingPrank = 0
        pendingPrankName = ""
        pendingEncounterPool = ""
        pendingEncounterRegion = ""
        pendingEncounterRoute = ""
        steadyFooting = false
        pushedThisTurn = false
    }

    private val botDistances by lazy { BotRouteDistances(board) }

    private fun distanceToFinish(id: String): Int = botDistances.distance(id)
}
