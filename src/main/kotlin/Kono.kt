package me.igorunderplayer.kono

import dev.kord.core.Kord
import dev.kord.core.entity.Emoji
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.igorunderplayer.kono.commands.CommandManager
import me.igorunderplayer.kono.events.EventManager
import no.stelar7.api.r4j.basic.APICredentials
import no.stelar7.api.r4j.impl.R4J
import org.slf4j.LoggerFactory

class Kono {
    companion object {
        lateinit var kord: Kord
        lateinit var events: EventManager
        lateinit var commands: CommandManager
        lateinit var config: Config
        lateinit var db: Database
        lateinit var cache: Cache

        lateinit var riot: R4J
        lateinit var emojis: List<Emoji>
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    @OptIn(PrivilegedIntent::class)
    suspend fun start() {
        config = Config().load()

        db = Database()
        db.start()

        kord = Kord(config.token)

        cache = Cache()
        cache.start()

        riot = R4J(APICredentials(config.riotApiKey))

        logger.info(
            """
             
            [0;31m _  __                        
            [0;32m| |/ /   ___    _ __     ___  
            [0;33m| ' /   / _ \  | '_ \   / _ \ 
            [0;34m| . \  | (_) | | | | | | (_) |
            [0;35m|_|\_\  \___/  |_| |_|  \___/ 
            [0m             
            """.trimIndent()
        )

        logger.info("Starting up!")

        events = EventManager(kord)
        events.start()

        commands = CommandManager(kord)
        commands.start()

        kord.login {
            intents = Intents.all
        }
    }
}