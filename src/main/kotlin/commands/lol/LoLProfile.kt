package me.igorunderplayer.kono.commands.lol

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.embed
import me.igorunderplayer.kono.Kono
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.utils.formatNumber
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard

class LoLProfile : BaseCommand(
    "lolprofile",
    "Mostra perfil do lol de alguem",
    category = CommandCategory.LoL
) {

    private val champions = Kono.riot.dDragonAPI.champions
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val query = args.joinToString(" ")
        val summoner = Kono.riot.loLAPI.summonerAPI.getSummonerByName(LeagueShard.BR1, query) ?: return
        val summonerIcon = Kono.riot.dDragonAPI.profileIcons[summoner.profileIconId.toLong()]!!
        val champ = champions[summoner.championMasteries.first().championId]

        println(summonerIcon.image.full)

        event.message.reply {
            embed {
                author {
                    name = "${summoner.name} - ${summoner.platform}"
                    icon = "http://ddragon.leagueoflegends.com/cdn/${Kono.riot.dDragonAPI.versions[0]}/img/profileicon/${summonerIcon.image.full}"
                }

                field {
                    name = "Melhores campeões"
                    value = summoner.championMasteries.slice(IntRange(0, 3)).joinToString("\n") {
                        val champion = champions[it.championId]
                        "${champion!!.name} - ${formatNumber(it.championPoints)}"
                    }
                }
            }
        }
    }
}