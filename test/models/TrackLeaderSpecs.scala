/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Association du Paris Java User Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package models

import play.api.test.{WithApplication, FakeApplication, PlaySpecification}
import org.apache.commons.lang3.RandomStringUtils

import scala.collection.JavaConverters._

/**
 * Simple Spec2 test for TrackLeader
 * Created by nicolas on 15/05/2014.
 */
class TrackLeaderSpecs extends PlaySpecification {

  val testRedis = Map("redis.host" -> "localhost", "redis.port" -> "6363", "redis.activeDatabase" -> 1)
  val testTracks = Map(
    "cfp.tracks" -> Seq(
      Map(
        "id" -> "java"
        , "icon" -> ""
      ).asJava
      , Map(
        "id" -> "web mobile"
        , "icon" -> ""
      ).asJava
      , Map(
        "id" -> "archisec"
        , "icon" -> ""
      ).asJava
      , Map(
        "id" -> "agility tests"
        , "icon" -> ""
      ).asJava
    ).asJava
  )

  // To avoid Play Cache Exception during tests, check this
  // https://groups.google.com/forum/#!topic/play-framework/PBIfeiwl5rU
  val appWithTestRedis = () => FakeApplication(additionalConfiguration = testRedis ++ testTracks)


  "TrackLeader" should {

    "associate a user to a Track if the user belongs to CFP group" in new WithApplication(app = appWithTestRedis()) {

      // Given
      val email = RandomStringUtils.randomAlphabetic(9)
      val testWebuser = Webuser.createSpeaker(email, RandomStringUtils.randomAlphabetic(4), RandomStringUtils.randomAlphabetic(2))
      Webuser.saveAndValidateWebuser(testWebuser)

      Webuser.addToCFPAdmin(testWebuser.uuid)

      // When
      TrackLeader.assign(Track.parse("web mobile").id, testWebuser.uuid)

      // Then the webuser is assigned to the Track
      TrackLeader.isTrackLeader(Track.parse("web mobile").id, testWebuser.uuid) must beTrue
      TrackLeader.isTrackLeader(Track.parse("java").id, testWebuser.uuid) must beFalse


      Webuser.delete(testWebuser)
      // Check that we did a cleanup
      TrackLeader.isTrackLeader(Track.parse("web mobile").id, testWebuser.uuid) must beFalse
    }

    "not associate a user to a Track if the user does not belong to CFP group" in new WithApplication(app = appWithTestRedis()) {

      // Given
      val email = RandomStringUtils.randomAlphabetic(9)
      val testWebuser = Webuser.createSpeaker(email, RandomStringUtils.randomAlphabetic(2), RandomStringUtils.randomAlphabetic(4))
      Webuser.saveAndValidateWebuser(testWebuser)


      // When
      TrackLeader.assign(Track.parse("web mobile").id, testWebuser.uuid)

      // Then the webuser is NOT assigned to the Track
      TrackLeader.isTrackLeader(Track.parse("java").id, testWebuser.uuid) must beFalse

      Webuser.delete(testWebuser)
    }

    "unassign a CFP webuser from a track" in new WithApplication(app = appWithTestRedis()) {

      // Given
      val email = RandomStringUtils.randomAlphabetic(9)
      val testWebuser = Webuser.createSpeaker(email, RandomStringUtils.randomAlphabetic(2), RandomStringUtils.randomAlphabetic(4))
      Webuser.saveAndValidateWebuser(testWebuser)

      // When
      Webuser.addToCFPAdmin(testWebuser.uuid)
      TrackLeader.assign(Track.parse("archisec").id, testWebuser.uuid)
      TrackLeader.unassign(Track.parse("archisec").id, testWebuser.uuid)

      // Then
      TrackLeader.isTrackLeader(Track.parse("archisec").id, testWebuser.uuid) must beFalse

      Webuser.delete(testWebuser)
    }

    "unassign a non-CFP webuser from a track" in new WithApplication(app = appWithTestRedis()) {

      // Given
      val email = RandomStringUtils.randomAlphabetic(9)
      val testWebuser = Webuser.createSpeaker(email, RandomStringUtils.randomAlphabetic(2), RandomStringUtils.randomAlphabetic(4))
      Webuser.saveAndValidateWebuser(testWebuser)

      // When
      TrackLeader.assign(Track.parse("archisec").id, testWebuser.uuid)
      TrackLeader.unassign(Track.parse("archisec").id, testWebuser.uuid)


      // Then
      TrackLeader.isTrackLeader(Track.parse("archisec").id, testWebuser.uuid) must beFalse

      Webuser.delete(testWebuser)
    }

    "assigns more than one Track to a Webuser" in new WithApplication(app = appWithTestRedis()) {
      // Given
      val email = RandomStringUtils.randomAlphabetic(9)
      val testWebuser = Webuser.createSpeaker(email, RandomStringUtils.randomAlphabetic(2), RandomStringUtils.randomAlphabetic(4))
      Webuser.saveAndValidateWebuser(testWebuser)

      // When
      Webuser.addToCFPAdmin(testWebuser.uuid)
      TrackLeader.assign(Track.parse("archisec").id, testWebuser.uuid)
      TrackLeader.assign(Track.parse("java").id, testWebuser.uuid)


      // Then
      TrackLeader.isTrackLeader(Track.parse("archisec").id, testWebuser.uuid) must beTrue
      TrackLeader.isTrackLeader(Track.parse("agility tests").id, testWebuser.uuid) must beFalse
      TrackLeader.isTrackLeader(Track.parse("java").id, testWebuser.uuid) must beTrue

      Webuser.delete(testWebuser)
    }

    "correctly update all tracks" in new WithApplication(app = appWithTestRedis()) {
      // Given
      val email = RandomStringUtils.randomAlphabetic(9)
      val testWebuser = Webuser.createSpeaker(email, RandomStringUtils.randomAlphabetic(2), RandomStringUtils.randomAlphabetic(4))
      Webuser.saveAndValidateWebuser(testWebuser)
      Webuser.addToCFPAdmin(testWebuser.uuid)
      val mapsByTrack:Map[String,Seq[String]] = Map(Track.parse("archisec").id -> List(testWebuser.uuid).toSeq)

      // When
      TrackLeader.isTrackLeader(Track.parse("archisec").id, testWebuser.uuid) must beFalse
      TrackLeader.updateAllTracks(mapsByTrack)

      // Then
      TrackLeader.isTrackLeader(Track.parse("archisec").id, testWebuser.uuid) must beTrue
      TrackLeader.isTrackLeader(Track.parse("archisec").id,  RandomStringUtils.randomAlphabetic(9)) must beFalse
      TrackLeader.isTrackLeader(Track.parse("agility tests").id, testWebuser.uuid) must beFalse

      Webuser.delete(testWebuser)
    }

    "correctly assign then unassign a track" in new WithApplication(app = appWithTestRedis()) {
      // Given
      val email = RandomStringUtils.randomAlphabetic(9)
      val testWebuser = Webuser.createSpeaker(email, RandomStringUtils.randomAlphabetic(2), RandomStringUtils.randomAlphabetic(4))
      Webuser.saveAndValidateWebuser(testWebuser)
      Webuser.addToCFPAdmin(testWebuser.uuid)
      val mapsByTrack:Map[String,Seq[String]] = Map(Track.parse("archisec").id -> List(testWebuser.uuid).toSeq)

      // When
      TrackLeader.updateAllTracks(mapsByTrack)

      // Then
      TrackLeader.isTrackLeader(Track.parse("archisec").id, testWebuser.uuid) must beTrue

      val mapsByTrack02:Map[String,Seq[String]] = Map(Track.parse("archisec").id -> List(RandomStringUtils.randomAlphabetic(3)).toSeq)
      TrackLeader.updateAllTracks(mapsByTrack02)

      TrackLeader.isTrackLeader(Track.parse("archisec").id,  testWebuser.uuid) must beFalse

      Webuser.delete(testWebuser)
    }


     "correctly assign then unassign a track" in new WithApplication(app = appWithTestRedis()) {
      // Given
      val uuid01 = RandomStringUtils.randomAlphabetic(9)
      val uuid02 = RandomStringUtils.randomAlphabetic(11)
      val uuid03 = RandomStringUtils.randomAlphabetic(10)

      Webuser.addToCFPAdmin(uuid01)
      Webuser.addToCFPAdmin(uuid02)
      Webuser.addToCFPAdmin(uuid03)

      val mapsByTrack:Map[String,Seq[String]] = Map(
        Track.parse("web mobile").id -> List(uuid01).toSeq,
        Track.parse("java").id -> List(uuid02).toSeq,
        Track.parse("archisec").id -> List(uuid01).toSeq
      )

      // When
      TrackLeader.updateAllTracks(mapsByTrack)

      // Then
      TrackLeader.isTrackLeader(Track.parse("web mobile").id, uuid01) must beTrue
      TrackLeader.isTrackLeader(Track.parse("java").id, uuid01) must beFalse
      TrackLeader.isTrackLeader(Track.parse("archisec").id, uuid01) must beTrue

      TrackLeader.isTrackLeader(Track.parse("web mobile").id, uuid02) must beFalse
      TrackLeader.isTrackLeader(Track.parse("java").id, uuid02) must beTrue
      TrackLeader.isTrackLeader(Track.parse("archisec").id, uuid02) must beFalse

      TrackLeader.isTrackLeader(Track.parse("web mobile").id, uuid03) must beFalse
      TrackLeader.isTrackLeader(Track.parse("java").id, uuid03) must beFalse
      TrackLeader.isTrackLeader(Track.parse("archisec").id, uuid03) must beFalse

       val mapsByTrack2:Map[String,Seq[String]] = Map(
         Track.parse("web mobile").id -> List(uuid03).toSeq,
         Track.parse("java").id -> List(uuid03).toSeq
      )
      TrackLeader.updateAllTracks(mapsByTrack2)

      TrackLeader.isTrackLeader(Track.parse("web mobile").id, uuid01) must beFalse
      TrackLeader.isTrackLeader(Track.parse("java").id, uuid01) must beFalse
      TrackLeader.isTrackLeader(Track.parse("archisec").id, uuid01) must beFalse

      TrackLeader.isTrackLeader(Track.parse("web mobile").id, uuid02) must beFalse
      TrackLeader.isTrackLeader(Track.parse("java").id, uuid02) must beFalse
      TrackLeader.isTrackLeader(Track.parse("archisec").id, uuid02) must beFalse

      TrackLeader.isTrackLeader(Track.parse("web mobile").id, uuid03) must beTrue
      TrackLeader.isTrackLeader(Track.parse("java").id, uuid03) must beTrue
      TrackLeader.isTrackLeader(Track.parse("archisec").id, uuid01) must beFalse
      TrackLeader.isTrackLeader(Track.parse("archisec").id, uuid03) must beFalse

      Webuser.removeFromCFPAdmin(uuid01)
      Webuser.removeFromCFPAdmin(uuid02)
      Webuser.removeFromCFPAdmin(uuid03)
    }

  }
}
