package org.kys.mars.models.notifications

import monix.eval.Task
import net.katsstuff.ackcord.data.{EmbedField, OutgoingEmbed, OutgoingEmbedFooter, OutgoingEmbedThumbnail}
import org.kys.mars.util.{DiscordUtils, EsiUtils}

object ParsedNotification {
  private def generateDamagePercentField(fieldName: String, percentage: Double): EmbedField = {
    EmbedField(fieldName, percentage.formatted("%,.2f") + "%")
  }
  case class StructureUnderAttackText(shieldPercentage: Double,
                                      armorPercentage: Double,
                                      hullPercentage: Double,
                                      allianceID: Option[Long],
                                      allianceName: Option[String],
                                      corpID: Option[Long],
                                      corpName: Option[String],
                                      charID: Option[Long],
                                      structureTypeID: Long,
                                      structureID: Long,
                                      solarsystemID: Long) extends ParsedNotificationLike {

    override def prettyPrintEmbed: Task[OutgoingEmbed] = {
      val idsToNames = List(Some(structureTypeID), Some(solarsystemID), charID).flatten
      EsiUtils.resolveNamesTask(idsToNames).map { n =>
        val structureName = n.find(_.id == structureTypeID).map(_.name)
        val systemName = n.find(_.id == solarsystemID).map(_.name)

        val title = s"${structureName.getOrElse("Unknown")} in ${systemName.getOrElse("Unknown")} is under attack"
        val attacker = List(charID.flatMap(i => n.find(_.id == i).map(_.name)), corpName, allianceName)
          .flatten
          .mkString(" | ")
        val attackerField =
          if(attacker != "") Some(DiscordUtils.generateField("Attacker", attacker, inline = false))
          else None
        val structureField = structureName.map(DiscordUtils.generateField("Type", _))
        val systemField = systemName.map(DiscordUtils.generateField("System", _))
        val shieldField = Some(generateDamagePercentField("Shield", shieldPercentage))
        val armorField = Some(generateDamagePercentField("Armor", armorPercentage))
        val hullField = Some(generateDamagePercentField("Hull", hullPercentage))

        val thumbnail = OutgoingEmbedThumbnail(EsiUtils.getUrlByItemId(structureTypeID))

        val fieldSeq = Seq(
          attackerField,
          structureField,
          systemField,
          shieldField,
          armorField,
          hullField).flatten

        OutgoingEmbed(
          title = Some(title),
          thumbnail = Some(thumbnail),
          color = Some(DiscordUtils.orangeColor),
          fields = fieldSeq)
      }
    }
  }

  case class StructureLostShieldsText(structureTypeID: Long,
                                structureID: Long,
                                solarsystemID: Long,
                                timeLeft: Long,
                                timestamp: Long,
                                vulnerableTime: Long) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = {
      Task(OutgoingEmbed())
    }
  }

  case class StructureLostArmorText(structureTypeID: Long,
                                      structureID: Long,
                                      solarsystemID: Long,
                                      timeLeft: Long,
                                      timestamp: Long,
                                      vulnerableTime: Long) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = {
      Task(OutgoingEmbed())
    }
  }

  case class TowerAlertMsgText(aggressorID: Option[Long],
                               aggressorCorpID: Option[Long],
                               aggressorAllianceID: Option[Long],
                               shieldValue: Long,
                               armorValue: Long,
                               hullValue: Long,
                               solarSystemID: Long,
                               moonID: Long,
                               typeID: Long) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = {
      Task(OutgoingEmbed())
    }
  }

  case class TowerResourceAlertMsgTextWants(quantity: Int, typeID: Long)
  case class TowerResourceAlertMsgText(corpID: Long,
                                       allianceID: Long,
                                       solarSystemID: Long,
                                       typeID: Long,
                                       wants: List[TowerResourceAlertMsgTextWants]) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = {
      Task(OutgoingEmbed())
    }
  }

  case class StructureFuelAlertText(solarSystemID: Long,
                                    structureID: Long,
                                    structureTypeID: Long,
                                    listOfTypesAndQty: List[Long]) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = {
      Task(OutgoingEmbed())
    }
  }

  case class StructureServicesOfflineText(solarSystemID: Long,
                                          structureID: Long,
                                          structureTypeID: Long,
                                          listOfServiceModulesIDs: List[Long]) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = {
      Task(OutgoingEmbed())
    }
  }

  case class StructureWentHighPowerText(solarSystemID: Long,
                                        structureID: Long,
                                        structureTypeID: Long) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = {
      Task(OutgoingEmbed())
    }
  }

  case class OwnershipTransferredText(characterName: String,
                                      fromCorporationName: String,
                                      toCorporationName: String,
                                      solarSystemName: String) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = Task(OutgoingEmbed())
  }

  case class CorpAllBillMsgText(amount: Double,
                                billTypeID: Long,
                                creditorID: Long,
                                currentDate: Long,
                                debtorID: Long,
                                dueDate: Long,
                                externalID: Long,
                                externalID2: Long) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = Task(OutgoingEmbed())
  }

  case class CorpAppNewMsgText(charID: Long, corpID: Long, applicationText: String) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = Task(OutgoingEmbed())
  }

  case class CorpTaxChangeMsgText(corpID: Long, newTaxRate: Double, oldTaxRate: Double) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = Task(OutgoingEmbed())
  }

  case class CharLeftCorpMsgText(charID: Long, corpID: Long) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = Task(OutgoingEmbed())
  }

  case class CharAppAcceptMsgText(charID: Long, corpID: Long, applicationText: String) extends ParsedNotificationLike {
    override def prettyPrintEmbed: Task[OutgoingEmbed] = Task(OutgoingEmbed())
  }
}
