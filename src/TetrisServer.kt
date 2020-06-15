package xyz.joonhyung.tetris

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * For future use...
 */
class TetrisServer(coroutineScope: CoroutineScope) {
    /**
     * Atomic counter used to get unique user-names based on the maxiumum users the server had.
     */
    val usersCounter = AtomicInteger()

    /**
     * A concurrent map associating session IDs to user names.
     */
    val memberNames = ConcurrentHashMap<String, String>()

    /**
     * Associates a session-id to a websocket.
     */
    val members = ConcurrentHashMap<String, WebSocketSession>()

    /**
     * This will later become a hash map.
     */
    private val tetrisBattle = TetrisBattleController(coroutineScope, this)

    suspend fun send(recipient: String, frame: Frame) {
        members[recipient]?.send(frame)
    }

    suspend fun broadcast(recipients: Enumeration<String>, frame: Frame) {
        for (recipient in recipients) {
            val socket = members[recipient]
            try {
                socket?.send(frame)
            } catch (t: Throwable) {
                try {
                    socket?.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                } catch (ignore: ClosedSendChannelException) {
                    // at some point it will get closed
                }
            }
        }
    }

    suspend fun receiveFromMember(member: String, message: String) {
        tetrisBattle.receiveFromMember(member, message)
    }

    /**
     * Handles that a member identified with a session id and a socket joined.
     */
    suspend fun memberJoin(member: String, socket: WebSocketSession) {
        // Checks if this user is already registered in the server and gives him/her a temporal name if required.
        memberNames.computeIfAbsent(member) { "user${usersCounter.incrementAndGet()}" }

        // Associates this socket to the member id.
        val oldSocket = members.put(member, socket)
        oldSocket?.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "duplicate session"))
        tetrisBattle.memberJoined(member)
    }

    fun getMemberName(member: String?): String? {
        return if (member == null) null else memberNames[member]
    }

    /**
     * Handles that a [member] with a specific [socket] left the server.
     */
    suspend fun memberLeft(member: String, socket: WebSocketSession) {
        // Removes the socket connection for this member
        val connection = members[member]
        if (connection == socket) {
            members.remove(member)
            memberNames.remove(member)
            tetrisBattle.memberLeft(member)
        }
    }
}