package me.igorunderplayer.kono.commands.slash.lol

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.Kono
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.utils.formatNumber
import me.igorunderplayer.kono.utils.regionFromLeagueShard
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType

class Profile: KonoSlashSubCommand {
    override val name = "profile"
    override val description = "mostra perfil de alguem"

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val response = event.interaction.deferPublicResponse()
        val queryAccount = event.interaction.command.strings["riot-id"]
        val queryRegion = event.interaction.command.strings["region"] ?: "NA1"

        if (queryAccount.isNullOrBlank()) {
            response.respond {
                content = "Insira o Riot ID do invocador desejado"
            }

            return
        }

        val blank = "<:transparent:1142620050952556616>"
        val champions = Kono.riot.dDragonAPI.champions

        val queryName = queryAccount.split('#').first()
        var queryTag = queryAccount.split('#').getOrNull(1)

        if (queryTag.isNullOrBlank()) {
            queryTag = queryRegion
        }

        val account = Kono.riot.accountAPI.getAccountByTag(regionFromLeagueShard(LeagueShard.fromString(queryRegion).get()), queryName, queryTag)

        val summoner = Kono.riot.loLAPI.summonerAPI.getSummonerByPUUID(LeagueShard.fromString(queryRegion).get(), account.puuid)

        if (summoner == null) {
            response.respond {
                content = "Invocador não encontrado!"
            }

            return
        }

        val summonerIcon = Kono.riot.dDragonAPI.profileIcons[summoner.profileIconId.toLong()]!!

        response.respond {
            embed {
                author {
                    name = "${summoner.name} - ${summoner.platform}"
                    icon = "http://ddragon.leagueoflegends.com/cdn/${Kono.riot.dDragonAPI.versions[0]}/img/profileicon/${summonerIcon.image.full}"
                }

                field {
                    name = "Ranqueado"
                    inline = true
                    value = summoner.leagueEntry
                        .filter { it.queueType != GameQueueType.CHERRY }
                        .joinToString("\n") { leagueEntry ->
                            val type = leagueEntry.queueType.prettyName().replace("5v5", "")
                            val rank = leagueEntry.rank
                            val rankTier = leagueEntry.tier
                            val pdl = leagueEntry.leaguePoints

                            "${type}: $rankTier $rank ($pdl PDL) $blank"
                        }
                }

                field {
                    name = "Melhores campeões"
                    inline = true
                    value = summoner.championMasteries.slice(IntRange(0, 2)).joinToString("\n") { championMastery ->
                        val champion = champions[championMastery.championId]!!
                        val emoji = Kono.emojis.firstOrNull { it.name == "lolchampion_${champion.key}" }
                        val iconText = emoji?.mention ?: ""
                        "$iconText ${champion.name} - ${formatNumber(championMastery.championPoints)}"
                    }
                }
            }
        }
    }
}