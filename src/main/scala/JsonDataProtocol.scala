import spray.json.DefaultJsonProtocol

trait JsonDataProtocol extends DefaultJsonProtocol {
  implicit val okFormat = jsonFormat1(StatusOk)
  implicit val fileSizeFormat = jsonFormat1(FileSiz)
  implicit val dataRequestFormat = jsonFormat3(DataRequest)
  implicit val highlightTextFormat = jsonFormat2(HighlightText)
  implicit val dataFormat = jsonFormat3(Data)
  implicit val dataResponseFormat = jsonFormat4(DataResp)
  implicit val histogramFormat = jsonFormat2(Histogram)
  implicit val histogramResponseFormat = jsonFormat4(HistogramResp)
}
