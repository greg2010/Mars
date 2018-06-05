package org.kys.mars.models

import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}

object NotificationSchema {
  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
  @ConfiguredJsonCodec case class Notification(notificationId: Long,
                                               senderId: Long,
                                               senderType: String,
                                               text: String,
                                               `type`: String)
}