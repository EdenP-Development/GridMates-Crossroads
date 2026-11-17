package com.playereden.crossroads.core

import kotlin.random.Random

/**
 * Small deterministic local rules engine. No sockets, UI, account or Android dependency.
 * Dice/events are sourced from one seeded PRNG owned by the session.
 */
class GameSession(
    val board: BoardDefinition,
    private val catalogue: EventCatalogue,
    seats: Int = 2,
    seed: Int = 1234,
    humanIndices: Set<Int> = setOf(0)
) {
    private val random = Random(seed)
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
    private val encounterLog = mutableListOf<EncounterNote>()
    private fun recordEncounter(title: String, outcome: String, kind: String) {
        encounterLog.add(EncounterNote(turn, players[currentIndex].name, title, outcome, kind))
        if (encounterLog.size > 16) encounterLog.removeAt(0)
    }

    init {
        require(seats in 2..4)
        require(board.start != board.finish)
        require(board.nodes.map { it.id }.toSet().size == board.nodes.size)
        require(board.nodes.any { it.id == board.start } && board.nodes.any { it.id == board.finish })
        players = (0 until seats).map {
            Player(if (it in humanIndices) "You" else "AI $it", it !in humanIndices, board.start, mutableListOf(board.start))
        }
    }

    fun snapshot(): Snapshot = Snapshot(
        revision, phase, currentIndex,
        players.map { it.copy(traversed = it.traversed.toMutableList()) },
        remainingSteps, die, lastMessage, winnerIndex, turn, lastEventId,
        encounterLog.toList()
    )

    fun roll() {
        require(phase == Phase.ROLL) { "Not waiting for a roll" }
        die = random.nextInt(1, 7)
        remainingSteps = requireNotNull(die)
        lastEventId = null
        lastMessage = "${players[currentIndex].name} rolled $die."
        progress()
        revision++
    }

    fun availableRoutes(): List<BoardNode> =
        if (phase == Phase.ROUTE) board.node(players[currentIndex].position).next.map(board::node)
        else emptyList()

    fun chooseRoute(id: String) {
        require(phase == Phase.ROUTE) { "Not at a crossroads" }
        require(id in board.node(players[currentIndex].position).next) { "Invalid route" }
        stepForward(id)
        remainingSteps--
        val label = board.forkAt(players[currentIndex].traversed[players[currentIndex].traversed.lastIndex - 1])
            ?.choices?.firstOrNull { it.to == id }?.label ?: board.node(id).region
        lastMessage = "${players[currentIndex].name} chose $label."
        progress()
        revision++
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
    }

    /** Bot submits exactly one regular legal action. UI may animate between actions. */
    fun botStep() {
        require(players[currentIndex].isBot && phase != Phase.FINISHED)
        when (phase) {
            Phase.ROLL -> roll()
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
            Phase.TARGET -> {
                val targets = availableTargets()
                if (targets.isEmpty()) { finishTurn(); revision++ }
                else chooseTarget(targets.minBy { distanceToFinish(players[it].position) })
            }
            Phase.FINISHED -> Unit
        }
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
                recordEncounter(encounterTitle, "Moved ahead ${effect.spaces} spaces", "benefit")
                remainingSteps = effect.spaces
                if (remainingSteps > 0) progress() else finishTurn()
            }
            "retreat" -> {
                moveBack(players[currentIndex], effect.spaces)
                lastMessage = "${players[currentIndex].name}: $encounterTitle! Back ${effect.spaces}."
                recordEncounter(encounterTitle, "Moved back ${effect.spaces} spaces", "setback")
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
                    lastMessage = "Mischief! Choose someone to send back ${effect.spaces}."
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
        winnerIndex = currentIndex
        phase = Phase.FINISHED
        remainingSteps = 0
        lastMessage = "${players[currentIndex].name} reached the finish and won!"
    }

    private fun finishTurn() {
        currentIndex = (currentIndex + 1) % players.size
        phase = Phase.ROLL
        remainingSteps = 0
        die = null
        turn++
        eventTriggeredThisTurn = false
        pendingPrank = 0
        pendingPrankName = ""
    }

    private fun distanceToFinish(id: String, seen: Set<String> = emptySet()): Int {
        if (id == board.finish) return 0
        if (id in seen) return 100_000 // Guard against an invalid cycle.
        val next = board.node(id).next
        return if (next.isEmpty()) 100_000 else 1 + next.minOf { distanceToFinish(it, seen + id) }
    }
}
