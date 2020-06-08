package xyz.joonhyung.tetris

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.*
import java.time.*

fun Application.main() {
    TetrisApplication(this).apply { main() }
}

class TetrisApplication(coroutineScope: CoroutineScope) {
    /**
     * This will later become a hash map.
     */
    private var tetrisBattle = TetrisBattleController(coroutineScope)

    fun Application.main() {
        // This adds automatically Date and Server headers to each response, and would allow you to configure
        // additional headers served to each response.
        install(DefaultHeaders)
        // Freemarker template engine
        install(FreeMarker) {
            templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        }
        // This uses use the logger to log every call (request/response)
        install(CallLogging)
        // This installs the websockets feature to be able to establish a bidirectional configuration
        // between the server and the client
        install(WebSockets) {
            pingPeriod = Duration.ofMinutes(1)
        }
        // This enables the use of sessions to keep information between requests/refreshes of the browser.
        install(Sessions) {
            cookie<TetrisSession>("SESSION")
        }

        // This adds an interceptor that will create a specific session in each request if no session is available already.
        intercept(ApplicationCallPipeline.Features) {
            if (call.sessions.get<TetrisSession>() == null) {
                call.sessions.set(TetrisSession(generateNonce()))
            }
        }

        /**
         * Now we are going to define routes to handle specific methods + URLs for this application.
         */
        routing {

            get("/") {
                call.respond(FreeMarkerContent("tetris.ftl", null, ""))
            }

            // This defines a websocket `/ws` route that allows a protocol upgrade to convert a HTTP request/response request
            // into a bidirectional packetized connection.
            webSocket("/tetris") { // this: WebSocketSession ->

                // First of all we get the session.
                val session = call.sessions.get<TetrisSession>()

                // We check that we actually have a session. We should always have one,
                // since we have defined an interceptor before to set one.
                if (session == null) {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                    return@webSocket
                }

                // We notify that a member joined by calling the server handler [memberJoin]
                // This allows to associate the session id to a specific WebSocket connection.
                tetrisBattle.clientJoined(this)

                try {
                    // We starts receiving messages (frames).
                    // Since this is a coroutine. This coroutine is suspended until receiving frames.
                    // Once the connection is closed, this consumeEach will finish and the code will continue.
                    incoming.consumeEach { frame ->
                        // Frames can be [Text], [Binary], [Ping], [Pong], [Close].
                        // We are only interested in textual messages, so we filter it.
                        if (frame is Frame.Text) {
                            // Now it is time to process the text sent from the user.
                            // At this point we have context about this connection, the session, the text and the server.
                            // So we have everything we need.
                            tetrisBattle.receiveFromClient(this, frame.readText())
                        }
                    }
                } finally {
                    // Either if there was an error, of it the connection was closed gracefully.
                    // We notify the server that the member left.
                    tetrisBattle.clientLeft(this)
                }
            }

            // This defines a block of static resources for the '/' path (since no path is specified and we start at '/')
            static("static") {
                // This marks index.html from the 'web' folder in resources as the default file to serve.
                defaultResource("index.html", "static")
                // This serves files from the 'web' folder in the application resources.
                resources("static")
            }

        }
    }

    /**
     * A chat session is identified by a unique nonce ID. This nonce comes from a secure random source.
     */
    data class TetrisSession(val id: String)
}
