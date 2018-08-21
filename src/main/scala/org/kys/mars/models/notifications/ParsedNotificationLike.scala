package org.kys.mars.models.notifications

import monix.eval.Task
import net.katsstuff.ackcord.data.OutgoingEmbed

trait ParsedNotificationLike {
  def prettyPrintEmbed(): Task[Either[Throwable, OutgoingEmbed]]
}