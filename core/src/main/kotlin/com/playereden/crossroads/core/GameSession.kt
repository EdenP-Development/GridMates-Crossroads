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
        remainingSteps, die, lastMessage, winnerIndex, turn, lastEventId
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
        lastMessage = "${players[currentIndex].name} chose the ${board.node(id).region} path."
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
        pendingPrank = 0
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
                val chosen = if (routes.size > 1 && random.nextInt(100) < 25) {
                    routes[random.nextInt(routes.size)]
                } else {
                    routes.minBy { distanceToFinish(it.id) + if (it.region == "forest") 1 else 0 }
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
        val region = board.node(players[currentIndex].position).region
        val pool = catalogue.events.filter { region in it.regions && it.weight > 0 }
        if (pool.isEmpty()) {
            lastMessage = "Quiet stretch of road."
            finishTurn()
            return
        }
        val total = pool.sumOf { it.weight }
        var ticket = random.nextInt(total)
        val event = pool.first { e -> ticket -= e.weight; ticket < 0 }
        lastEventId = event.id
        val effect = event.effect
        when (effect.type) {
            "advance" -> {
                lastMessage = "${players[currentIndex].name}: ${event.id.replace('_', ' ')}! Move +${effect.spaces}."
                remainingSteps = effect.spaces
                if (remainingSteps > 0) progress() else finishTurn()
            }
            "retreat" -> {
                moveBack(players[currentIndex], effect.spaces)
                lastMessage = "${players[currentIndex].name}: ${event.id.replace('_', ' ')}! Back ${effect.spaces}."
                finishTurn()
            }
            "shield" -> {
                players[currentIndex].shield = true
                lastMessage = "${players[currentIndex].name} found a shield!"
                finishTurn()
            }
            "prank" -> {
                val targets = players.indices.filter { it != currentIndex }
                if (targets.isEmpty() || effect.spaces <= 0) {
                    finishTurn()
                } else {
                    pendingPrank = effect.spaces
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
    }

    private fun distanceToFinish(id: String, seen: Set<String> = emptySet()): Int {
        if (id == board.finish) return 0
        if (id in seen) return 100_000 // Guard against an invalid cycle.
        val next = board.node(id).next
        return if (next.isEmpty()) 100_000 else 1 + next.minOf { distanceToFinish(it, seen + id) }
    }
}
