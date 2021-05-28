import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString

import java.io.File
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.SECONDS
import java.time.{LocalDateTime, ZoneId}

object FileService {

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("LLL d H:m:s yyyy")
  def isBetweenDateAndContainPhrase(from: LocalDateTime)(until: LocalDateTime)(phrase: String)(it: (LocalDateTime, String))
  = SECONDS.between(from, it._1) >= 0 && SECONDS.between(it._1, until) >= 0 && it._2.contains(phrase)
  implicit val system: ActorSystem = ActorSystem("fileParser")

  def getFileSize(path: String) = {
    val file = new File(path)
    file.length
  }

  def getData(path: String, dataRequest: DataRequest) = {
    readFile(path, dataRequest)
      .map(it => findHighlightedTest(it, dataRequest.phrase))
      .runWith(Sink.seq)
  }

  def getHistogram(path: String, dataRequest: DataRequest) = {
    readFile(path, dataRequest)
      .map(it => it._1.toString)
      .groupBy(Int.MaxValue, identity)
      .map(it => it -> 1L)
      .reduce((left, right) => (left._1, left._2 + right._2))
      .mergeSubstreams
      .map(it => Histogram(it._1, it._2))
      .runWith(Sink.seq)
  }

  def readFile(path: String, dataRequest: DataRequest) = {
    val datetimeFrom = LocalDateTime.parse(dataRequest.datetimeFrom, dateTimeFormatter)
    val datetimeUntil = LocalDateTime.parse(dataRequest.datetimeUntil, dateTimeFormatter)
    val filter = isBetweenDateAndContainPhrase(datetimeFrom)(datetimeUntil)(dataRequest.phrase) _

    FileIO.fromPath(Paths.get(path))
      .via(Framing.delimiter(ByteString("\n"), 512, true).map(_.utf8String))
      .map(line => (line.split(" "), line))
      .filter(it => it._1.length > 3)
      .map(parseDate)
      .filter(filter)
  }

  def parseDate(it: (Array[String], String)) = {
    val lines = it._1
    val dateStr = s"${lines(0)} ${lines(1)} ${lines(2)} ${LocalDateTime.now(ZoneId.systemDefault).getYear}"
    val date = LocalDateTime.parse(dateStr, dateTimeFormatter)
    (date, it._2)
  }

  def findHighlightedTest(it: (LocalDateTime, String), phrase: String) = {
    val list = phrase.toLowerCase.r
      .findAllMatchIn(it._2.toLowerCase)
      .map(m => HighlightText(m.start, m.end))
      .toList
    Data(it._1.toString, it._2, list)
  }

}
