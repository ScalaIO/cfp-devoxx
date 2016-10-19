package models

import java.util.Locale

import org.joda.time.{DateTime, DateTimeZone, Period}
import play.api.Play
import play.api.libs.json._

import scala.collection.JavaConversions._

/**
  * ConferenceDescriptor.
  * This might be the first file to look at, and to customize.
  * Idea behind this file is to try to collect all configurable parameters for a conference.
  *
  * For labels, please do customize messages and messages.fr
  *
  * Note from Nicolas : the first version of the CFP was much more "static" but hardly configurable.
  *
  * @author Frederic Camblor, BDX.IO 2014
  */

case class ConferenceUrls(faq: String, registration: String, confWebsite: String, cfpHostname: String) {
  def cfpURL: String = {
    if (Play.current.configuration.getBoolean("cfp.activateHTTPS").getOrElse(false)) {
      s"https://$cfpHostname"
    } else {
      s"http://$cfpHostname"
    }
  }

}

case class ConferenceTiming(
                             datesI18nKey: String,
                             speakersPassDuration: Integer,
                             preferredDayEnabled: Boolean,
                             firstDayFr: String,
                             firstDayEn: String,
                             datesFr: String,
                             datesEn: String,
                             cfpOpenedOn: DateTime,
                             cfpClosedOn: DateTime,
                             scheduleAnnouncedOn: DateTime,
                             days: Iterator[DateTime]
                           )

case class ConferenceSponsor(showSponsorProposalCheckbox: Boolean, sponsorProposalType: ProposalType = ProposalType.UNKNOWN)

case class TrackDesc(id: String, imgSrc: String, i18nTitleProp: String, i18nDescProp: String)

case class ProposalConfiguration(id: String, slotsCount: Int,
                                 givesSpeakerFreeEntrance: Boolean,
                                 freeEntranceDisplayed: Boolean,
                                 htmlClass: String,
                                 hiddenInCombo: Boolean = false,
                                 chosablePreferredDay: Boolean = false,
                                 impliedSelectedTrack: Option[Track] = None)

object ProposalConfiguration {

  val UNKNOWN = ProposalConfiguration(id = "unknown", slotsCount = 0, givesSpeakerFreeEntrance = false, freeEntranceDisplayed = false,
    htmlClass = "", hiddenInCombo = true, chosablePreferredDay = false)

  def parse(propConf: String): ProposalConfiguration = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.find(p => p.id == propConf).getOrElse(ProposalConfiguration.UNKNOWN)
  }

  def totalSlotsCount = ConferenceDescriptor.ConferenceProposalConfigurations.ALL.map(_.slotsCount).sum

  def isDisplayedFreeEntranceProposals(pt: ProposalType): Boolean = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.filter(p => p.id == pt.id).map(_.freeEntranceDisplayed).headOption.getOrElse(false)
  }

  def getProposalsImplyingATrackSelection = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.filter(p => p.impliedSelectedTrack.nonEmpty)
  }

  def getHTMLClassFor(pt: ProposalType): String = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.filter(p => p.id == pt.id).map(_.htmlClass).headOption.getOrElse("unknown")
  }

  def isChosablePreferredDaysProposals(pt: ProposalType): Boolean = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.filter(p => p.id == pt.id).map(_.chosablePreferredDay).headOption.getOrElse(false)
  }

  def doesProposalTypeGiveSpeakerFreeEntrance(pt: ProposalType): Boolean = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.filter(p => p.id == pt.id).map(_.givesSpeakerFreeEntrance).headOption.getOrElse(false)
  }
}

case class ConferenceDescriptor(eventCode: String,
                                confUrlCode: String,
                                frLangEnabled: Boolean,
                                fromEmail: String,
                                committeeEmail: String,
                                bccEmail: Option[String],
                                bugReportRecipient: String,
                                conferenceUrls: ConferenceUrls,
                                timing: ConferenceTiming,
                                hosterName: String,
                                hosterWebsite: String,
                                hashTag: String,
                                conferenceSponsor: ConferenceSponsor,
                                locale: List[Locale],
                                localisation: String,
                                notifyProposalSubmitted: Boolean,
                                maxProposalSummaryCharacters: Int = 1200)

object ConferenceDescriptor {

  /**
    * TODO configure here the kind of talks you will propose
    */
  object ConferenceProposalTypes {
    val CONF = ProposalType(id = "conf", label = "conf.label")

    val UNI = ProposalType(id = "uni", label = "uni.label")

    val TIA = ProposalType(id = "tia", label = "tia.label")

    val LAB = ProposalType(id = "lab", label = "lab.label")

    val QUICK = ProposalType(id = "quick", label = "quick.label")

    val BOF = ProposalType(id = "bof", label = "bof.label")

    val KEY = ProposalType(id = "key", label = "key.label")

    val IGNITE = ProposalType(id = "ignite", label = "ignite.label")

    val OTHER = ProposalType(id = "other", label = "other.label")

    val ALL: List[ProposalType] = List(CONF, TIA, LAB, QUICK, KEY, OTHER)

    def valueOf(id: String): ProposalType = id match {
      case "conf" => CONF
      case "tia" => TIA
      case "lab" => LAB
      case "quick" => QUICK
      case "key" => KEY
      case "other" => OTHER
    }

  }

  // TODO Configure here the slot, with the number of slots available, if it gives a free ticket to the speaker, some CSS icons
  object ConferenceProposalConfigurations {
    val CONF = ProposalConfiguration(id = "conf", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.CONF.id)), givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-microphone",
      chosablePreferredDay = true)
    val UNI = ProposalConfiguration(id = "uni", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.UNI.id)), givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-laptop",
      chosablePreferredDay = true)
    val TIA = ProposalConfiguration(id = "tia", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.TIA.id)), givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-legal",
      chosablePreferredDay = true)
    val LAB = ProposalConfiguration(id = "lab", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.LAB.id)), givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-beaker",
      chosablePreferredDay = true)
    val QUICK = ProposalConfiguration(id = "quick", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.QUICK.id)), givesSpeakerFreeEntrance = false, freeEntranceDisplayed = false, htmlClass = "icon-fast-forward",
      chosablePreferredDay = true)
    val BOF = ProposalConfiguration(id = "bof", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.BOF.id)), givesSpeakerFreeEntrance = false, freeEntranceDisplayed = false, htmlClass = "icon-group",
      chosablePreferredDay = false)
    val KEY = ProposalConfiguration(id = "key", slotsCount = 7, givesSpeakerFreeEntrance = true, freeEntranceDisplayed = false, htmlClass = "icon-microphone",
      chosablePreferredDay = true)
    val IGNITE = ProposalConfiguration(id = "ignite", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.IGNITE.id)), givesSpeakerFreeEntrance = false, freeEntranceDisplayed = false, htmlClass = "icon-microphone",
      chosablePreferredDay = false)
    val OTHER = ProposalConfiguration(id = "other", slotsCount = 5, givesSpeakerFreeEntrance = false, freeEntranceDisplayed = false, htmlClass = "icon-microphone",
      hiddenInCombo = true, chosablePreferredDay = false)

    val ALL = List(CONF, TIA, LAB, QUICK, KEY, OTHER)

    def doesItGivesSpeakerFreeEntrance(proposalType: ProposalType): Boolean = {
      ALL.filter(_.id == proposalType.id).exists(_.givesSpeakerFreeEntrance)
    }
  }

  // TODO Configure here your Conference's tracks.

  def getConferenceTracksFromConfiguration = {
    Play.current.configuration.getConfigList("cfp.tracks").map(tracks => {
      tracks.map(
        track => track.getString("id") :: track.getString("icon") :: Nil)
        .collect({
          case Some(id) :: Some(icon) :: Nil => (Track(id, id + ".label"), TrackDesc(id, icon, "track." + id + ".title", "track." + id + ".desc"))
        }).toList
    })
      .getOrElse(List())
  }

  object ConferenceTracks {

    val ALL = getConferenceTracksFromConfiguration.map(_._1)

  }

  // TODO configure the description for each Track
  object ConferenceTracksDescription {
    val ALL = getConferenceTracksFromConfiguration.map(_._2)

    def findTrackDescFor(t: Track): TrackDesc = {
      ALL.find(_.id == t.id).head
    }

    implicit val conferenceTracksDescriptionFormat = Json.format[TrackDesc]

  }

  // TODO If you want to use the Devoxx Scheduler, you can describe here the list of rooms, with capacity for seats
  object ConferenceRooms {

    // Tip : I use the ID to sort-by on the view per day... So if the exhibition floor id is "aaa" it will be
    // the first column on the HTML Table

    // Do not change the ID's once the program is published
    val HALL_EXPO = Room("a_hall", "Exhibition floor", 2300, "special", "")


    // scalaIO
    val ADA_LOVELACE = Room("ada_lovelace", "Ada Lovelace", 500, "theatre", "camera")
    val GRACE_HOPPER = Room("grace_hopper", "Grace Hopper", 120, "theatre", "camera")
    val BARBARA_LISKOV = Room("barbara_liskov", "Barbara Liskov", 80, "classroom", "camera")
    val LYNN_CONNWAY = Room("LYNN_CONNWAY", "Lynn Connway", 80, "theater", "camera")
    val WORKSHOP = Room("workshop", "Workshop", 80, "classroom", "camera")

    val allRooms = List(ADA_LOVELACE, GRACE_HOPPER, BARBARA_LISKOV, LYNN_CONNWAY, WORKSHOP)


    val allRoomsShortConfThu = List(ADA_LOVELACE, GRACE_HOPPER, BARBARA_LISKOV, LYNN_CONNWAY)
    val allRoomsShortConfFri = List(GRACE_HOPPER, LYNN_CONNWAY)

    val allRoomsLabThursday = List(WORKSHOP, LYNN_CONNWAY)
    val allRoomsLabFriday = List(WORKSHOP, LYNN_CONNWAY)


    val keynoteRoom = List(ADA_LOVELACE)

    val allRoomsConf = List(ADA_LOVELACE, GRACE_HOPPER, BARBARA_LISKOV, LYNN_CONNWAY)


    val allRoomsQuickiesThu = List(ADA_LOVELACE, GRACE_HOPPER)
    val allRoomsQuickiesFriday = List(ADA_LOVELACE, GRACE_HOPPER, BARBARA_LISKOV, LYNN_CONNWAY)
  }

  // TODO if you want to use the Scheduler, you can configure the breaks
  object ConferenceSlotBreaks {
    val registration = SlotBreak("reg", "Registration, Welcome and Breakfast", "Accueil", ConferenceRooms.HALL_EXPO)
    val petitDej = SlotBreak("dej", "Breakfast", "Accueil et petit-déjeuner", ConferenceRooms.HALL_EXPO)
    val coffee = SlotBreak("coffee", "Coffee Break", "Pause café", ConferenceRooms.HALL_EXPO)
    val lunch = SlotBreak("lunch", "Lunch", "Pause déjeuner", ConferenceRooms.HALL_EXPO)
    val shortBreak = SlotBreak("chgt", "Break", "Pause courte", ConferenceRooms.HALL_EXPO)
  }

  // TODO The idea here is to describe in term of Agenda, for each rooms, the slots. This is required only for the Scheduler
  object ConferenceSlots {


    //shortConf

    val shortConfSlotsThursday: List[Slot] = {
      val toolsThursdayAfternoonSlot1 = ConferenceRooms.allRoomsShortConfThu.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.TIA.id, "thursday",
            new DateTime("2016-10-27T12:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T12:20:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)
      }
      val toolsThursdayAfternoonSlot2 = ConferenceRooms.allRoomsShortConfThu.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.TIA.id, "thursday",
            new DateTime("2016-10-27T13:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T13:50:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)
      }
      toolsThursdayAfternoonSlot1 ++ toolsThursdayAfternoonSlot2
    }


    val shortConfSlotsFriday: List[Slot] = {


      val toolsFridayAfternoonSlot1 = ConferenceRooms.allRoomsShortConfFri.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.TIA.id, "friday",
            new DateTime("2016-10-28T12:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T12:50:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)
      }


      val toolsFridayAfternoonSlot2 = ConferenceRooms.allRoomsShortConfFri.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.TIA.id, "friday",
            new DateTime("2016-10-28T13:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T13:20:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r2)
      }

      val toolsFridayAfternoonSlot3 = List(ConferenceRooms.BARBARA_LISKOV).map {
        r3 =>
          SlotBuilder(ConferenceProposalTypes.TIA.id, "friday",
            new DateTime("2016-10-28T16:45:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T17:05:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r3)
      }
      toolsFridayAfternoonSlot1 ++ toolsFridayAfternoonSlot2 ++toolsFridayAfternoonSlot3
    }


    // HANDS ON LABS

    val labsSlotsThursday: List[Slot] = {

      val labsThursdayMorning = ConferenceRooms.allRoomsLabThursday.filter(_ != ConferenceRooms.LYNN_CONNWAY).map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.LAB.id, "thursday",
            new DateTime("2016-10-27T10:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T13:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)
      }
      val labsThursdayAfternoon = ConferenceRooms.allRoomsLabThursday.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.LAB.id, "thursday",
            new DateTime("2016-10-27T15:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T18:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r2)
      }
      labsThursdayMorning ++ labsThursdayAfternoon
    }

    val labsSlotsFriday: List[Slot] = {

      val labsFridayMorning1 = ConferenceRooms.allRoomsLabFriday.filter(_ != ConferenceRooms.LYNN_CONNWAY).map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.LAB.id, "friday",
            new DateTime("2016-10-28T10:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T13:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)

      }

      val labsFridayAF = ConferenceRooms.allRoomsLabFriday.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.LAB.id, "friday",
            new DateTime("2016-10-28T14:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T17:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r2)
      }
      labsFridayMorning1 ++ labsFridayAF
    }


    // QUICKIES
    val quickiesSlotsThursday: List[Slot] = {
      val quickiesThursdayLunch1 = ConferenceRooms.allRoomsQuickiesThu.filter(_ != ConferenceRooms.GRACE_HOPPER).map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.QUICK.id, "thursday",
            new DateTime("2016-10-27T13:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T13:10:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)
      }
      val quickiesThursdayLunch2 = ConferenceRooms.allRoomsQuickiesThu.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.QUICK.id, "thursday",
            new DateTime("2016-10-27T13:15:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T13:25:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r2)
      }
      quickiesThursdayLunch1 ++ quickiesThursdayLunch2
    }


    // CONFERENCE KEYNOTES
    val keynoteSlotsThursday: List[Slot] = {
      val keynoteThursdayWelcome = ConferenceRooms.keynoteRoom.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "thursday",
            new DateTime("2016-10-27T08:50:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T09:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)
      }
      val keynoteThursdaySlot1 = ConferenceRooms.keynoteRoom.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "thursday",
            new DateTime("2016-10-27T09:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T09:45:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)
      }

      keynoteThursdayWelcome ++ keynoteThursdaySlot1
    }

    val keynoteSlotsFriday: List[Slot] = {
      val platineWordFridaySlot1 = ConferenceRooms.keynoteRoom.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "friday",
            new DateTime("2016-10-28T08:50:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T09:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)
      }
      val keynoteFridaySlot1 = ConferenceRooms.keynoteRoom.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "friday",
            new DateTime("2016-10-28T09:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T09:40:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)
      }
      val keynoteFridaySlot2 = ConferenceRooms.keynoteRoom.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "friday",
            new DateTime("2016-10-28T09:45:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T10:25:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r2)
      }


      platineWordFridaySlot1 ++ keynoteFridaySlot1 ++ keynoteFridaySlot2

    }

    // CONFERENCE SLOTS
    val conferenceSlotsThursday: List[Slot] = {

      val conferenceThursdaySlot1 = ConferenceRooms.allRoomsConf.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "thursday",
            new DateTime("2016-10-27T10:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T10:45:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)
      }
      val conferenceThursdaySlot2 = ConferenceRooms.allRoomsConf.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "thursday",
            new DateTime("2016-10-27T11:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T11:45:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r2)
      }
      val conferenceThursdaySlot3 = ConferenceRooms.allRoomsConf.map {
        r3 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "thursday",
            new DateTime("2016-10-27T14:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T14:45:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r3)
      }
      val conferenceThursdaySlot4 = ConferenceRooms.allRoomsConf.filter(_ != ConferenceRooms.LYNN_CONNWAY).map {
        r4 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "thursday",
            new DateTime("2016-10-27T15:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T15:45:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r4)
      }
      val conferenceThursdaySlot5 = ConferenceRooms.allRoomsConf.filter(_ != ConferenceRooms.LYNN_CONNWAY).map {
        r5 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "thursday",
            new DateTime("2016-10-27T16:15:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-27T17:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r5)
      }

      conferenceThursdaySlot1 ++ conferenceThursdaySlot2 ++ conferenceThursdaySlot3 ++ conferenceThursdaySlot4 ++ conferenceThursdaySlot5
    }

    val conferenceSlotsFriday: List[Slot] = {

      val conferenceFridaySlot1 = ConferenceRooms.allRoomsConf.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-10-28T10:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T11:15:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r1)
      }
      val conferenceFridaySlot2 = ConferenceRooms.allRoomsConf.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-10-28T11:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T12:15:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r2)
      }

      val conferenceFridaySlot3 = ConferenceRooms.allRoomsConf.filter(r => r != ConferenceRooms.GRACE_HOPPER && r != ConferenceRooms.LYNN_CONNWAY).map {
        r3 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-10-28T12:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T13:15:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r3)
      }

      val conferenceFridaySlot4 = ConferenceRooms.allRoomsConf.filter(_ != ConferenceRooms.LYNN_CONNWAY).map {
        r4 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-10-28T14:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T15:15:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r4)
      }
      val conferenceFridaySlot5 = ConferenceRooms.allRoomsConf.filter(_ != ConferenceRooms.LYNN_CONNWAY).map {
        r5 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-10-28T15:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T16:15:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r5)
      }
      val conferenceFridaySlot6 = ConferenceRooms.allRoomsConf.filter(r=> r!= ConferenceRooms.LYNN_CONNWAY && r!= ConferenceRooms.BARBARA_LISKOV).map {
        r6 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-10-28T16:45:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
            new DateTime("2016-10-28T17:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")), r6)
      }

      conferenceFridaySlot1 ++ conferenceFridaySlot2 ++ conferenceFridaySlot3 ++ conferenceFridaySlot4 ++ conferenceFridaySlot5 ++ conferenceFridaySlot6
    }

    // Registration, coffee break, lunch etc


    val thursdayBreaks: List[Slot] = List(
      SlotBuilder(ConferenceSlotBreaks.registration, "thursday",
        new DateTime("2016-10-27T08:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
        new DateTime("2016-10-27T08:50:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")))
      , SlotBuilder(ConferenceSlotBreaks.lunch, "thursday",
        new DateTime("2016-10-27T12:20:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
        new DateTime("2016-10-27T13:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")))
      , SlotBuilder(ConferenceSlotBreaks.coffee, "thursday",
        new DateTime("2016-10-27T15:45:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
        new DateTime("2016-10-27T16:15:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")))
    )

    val fridayBreaks = List(
      SlotBuilder(ConferenceSlotBreaks.petitDej, "friday",
        new DateTime("2016-10-28T08:00:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
        new DateTime("2016-10-28T08:50:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")))
      , SlotBuilder(ConferenceSlotBreaks.lunch, "friday",
        new DateTime("2016-10-28T13:10:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
        new DateTime("2016-10-28T14:30:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")))
      , SlotBuilder(ConferenceSlotBreaks.coffee, "friday",
        new DateTime("2016-10-27T16:15:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")),
        new DateTime("2016-10-27T16:45:00.000+02:00").toDateTime(DateTimeZone.forID("Europe/Paris")))
    )


    val thursday: List[Slot] = {
      thursdayBreaks ++ keynoteSlotsThursday ++ conferenceSlotsThursday ++ quickiesSlotsThursday ++ labsSlotsThursday ++ shortConfSlotsThursday
    }

    val friday: List[Slot] = {
      fridayBreaks ++ keynoteSlotsFriday ++ conferenceSlotsFriday ++ labsSlotsFriday ++ shortConfSlotsFriday
    }

    // COMPLETE DEVOXX
    def all: List[Slot] = {
      thursday ++ friday
    }
  }

  def dateRange(from: DateTime, to: DateTime, step: Period): Iterator[DateTime] = Iterator.iterate(from)(_.plus(step)).takeWhile(!_.isAfter(to))

  val fromDay = new DateTime().withYear(2016).withMonthOfYear(10).withDayOfMonth(27)
  val toDay = new DateTime().withYear(2016).withMonthOfYear(10).withDayOfMonth(28)

  // TODO You might want to start here and configure first, your various Conference Elements
  def current() = ConferenceDescriptor(
    eventCode = "ScalaIOFR2016",
    // You will need to update conf/routes files with this code if modified
    confUrlCode = "scalaio2016",
    frLangEnabled = true,
    fromEmail = Play.current.configuration.getString("mail.from").getOrElse("cfp@scala.io"),
    committeeEmail = Play.current.configuration.getString("mail.committee.email").getOrElse("cfp@scala.io"),
    bccEmail = Play.current.configuration.getString("mail.bcc"),
    bugReportRecipient = Play.current.configuration.getString("mail.bugreport.recipient").getOrElse("jhelou@scala.io"),
    conferenceUrls = ConferenceUrls(
      faq = "http://scala.io/faq.html",
      registration = "http://scala.io",
      confWebsite = "http://scala.io",
      cfpHostname = Play.current.configuration.getString("cfp.hostname").getOrElse("cfp.scala.io")
    ),
    timing = ConferenceTiming(
      datesI18nKey = "27 et 28 Octobre 2016",
      speakersPassDuration = 5,
      preferredDayEnabled = true,
      firstDayFr = "27 Octobre",
      firstDayEn = "October 27th",
      datesFr = "du 27 au 28 octobre 2016",
      datesEn = "from 27th to 28th of October, 2016",
      cfpOpenedOn = DateTime.parse("2016-05-19T00:00:00+02:00"),
      cfpClosedOn = DateTime.parse("2016-09-05T09:00:00+02:00"),
      scheduleAnnouncedOn = DateTime.parse("2016-09-15T00:00:00+02:00"),
      days = dateRange(fromDay, toDay, new Period().withDays(1))
    ),
    hosterName = "Clever-cloud", hosterWebsite = "http://www.clever-cloud.com/#ScalaIO",
    hashTag = "#Scalaio16",
    conferenceSponsor = ConferenceSponsor(showSponsorProposalCheckbox = true, sponsorProposalType = ConferenceProposalTypes.CONF)
    , List(Locale.FRENCH, Locale.ENGLISH)
    , "CPE, Lyon"
    , notifyProposalSubmitted = true // Do not send an email for each talk submitted for France
    , 1200 // French developers tends to be a bit verbose... we need extra space :-)
  )

  def isConferenceOpen() = {
    val tim = current().timing
    val now = DateTime.now
    now.isAfter(tim.cfpOpenedOn) && now.isBefore(tim.cfpClosedOn)
  }

  // It has to be a def, not a val, else it is not re-evaluated
  def isCFPOpen: Boolean = {
    Play.current.configuration.getBoolean("cfp.isOpen").getOrElse(false)
  }

  def isGoldenTicketActive: Boolean = Play.current.configuration.getBoolean("goldenTicket.active").getOrElse(false)

}
