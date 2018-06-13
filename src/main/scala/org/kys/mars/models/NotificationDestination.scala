package org.kys.mars.models

case class NotificationDestination(esiRefreshToken: String,
                                   characterId: Long,
                                   discordChannelIds: List[Long],
                                   adminDiscordChannelId: Option[Long])
