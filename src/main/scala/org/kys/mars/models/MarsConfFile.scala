package org.kys.mars.models

case class MarsConfFile(discordBotToken: String,
                        discordStatus: Option[String],
                        killmailDestinations: List[RedisqDestination],
                        notificationDestinations: List[NotificationDestination])