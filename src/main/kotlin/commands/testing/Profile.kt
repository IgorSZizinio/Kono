package me.igorunderplayer.kono.commands.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.Image
import io.ktor.client.request.forms.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.Config
import me.igorunderplayer.kono.Kono
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.utils.getMentionedUser
import me.igorunderplayer.kono.utils.getOrCreateDBUser
import no.stelar7.api.r4j.basic.APICredentials
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard
import no.stelar7.api.r4j.impl.R4J
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

class Profile : BaseCommand(
    "profile",
    "exibe o perfil de alguem",
    category = CommandCategory.Misc,
    aliases = listOf("perfil")
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val width = 800
        val height = 600

        if (event.guildId == null) return

        val user = getMentionedUser(event.message, args)

        if (user == null) {
            event.message.reply {
                content = "Usuario não encontrado"
            }

            return
        }

        val xpUser = Kono.cache.getXPInfoFromUser(event.guildId!!, user.id)
        val xpToLevelUp = Kono.cache.getXPToLevelUP(xpUser)

        val dbUser = getOrCreateDBUser(
            Kono.db.usersCollection,
            user.id.toString()
        )

        if (dbUser == null) {
            event.message.reply {
                content = "Usuario não encontrado"
            }

            return
        }

        val profileImageBuffer = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = profileImageBuffer.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = Color(49, 51, 56)
        g2.fillRect(0, 0, width, height)


        // Draw info
        val nameFont = Font("Arial", Font.ITALIC, 32)
        val subInfoFont = Font("Arial", Font.ITALIC, 24)
        g2.font = nameFont
        g2.paint = Color.WHITE
        g2.drawString(user.username, 152, 46)
        val fontMetrics = g2.fontMetrics
        val stringWidth = fontMetrics.stringWidth(user.username)
        g2.font = subInfoFont
        g2.paint = Color.LIGHT_GRAY
        g2.drawString('#' + user.discriminator, 152 + stringWidth + 4, 46)
        g2.drawString("${dbUser.koins} Koins", 152, 78)
        g2.drawString("Level: ${xpUser.level}", 152, 106)
        g2.drawString("XP: ${xpUser.actualXp}/$xpToLevelUp", 152, 134)

        // Draw avatar
        val avatar = URL((user.avatar ?: user.defaultAvatar).cdnUrl.toUrl {
            format = Image.Format.PNG
            size = Image.Size.Size128
        })

        val avatarImage = withContext(Dispatchers.IO) {
            ImageIO.read(avatar)
        }

        // Border
        val center = 76f
        val borderRadius = 70f
        g2.color = Color(82, 201, 224)
        g2.fill(Ellipse2D.Float(center - borderRadius, center - borderRadius, 2f * borderRadius, 2f * borderRadius))

        // Avatar image
        val imageRadius = 64
        g2.clip = Ellipse2D.Float(12f, 12f, 128f, 128f)
        g2.drawImage(
            avatarImage,
            center.toInt() - imageRadius,
            center.toInt() - imageRadius,
            (2 * imageRadius),
            (2 * imageRadius),
            null
        )


        ByteArrayOutputStream().use {
            ImageIO.write(profileImageBuffer, "png", it)
            val imageByteArray = it.toByteArray()
            event.message.reply {
                addFile("profile.png", ChannelProvider { ByteReadChannel(imageByteArray) })
            }

            g2.dispose()
            profileImageBuffer.flush()
            avatarImage.flush()
            it.flush()
        }
    }
}