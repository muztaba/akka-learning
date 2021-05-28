
case class StatusOk(status: String)

case class FileSiz(size: Long)

case class HighlightText(fromPosition: Int, toPosition: Int)

case class Data(datetime: String, message: String, highlightText: List[HighlightText])

case class DataResp(data: List[Data], datetimeFrom: String, datetimeUntil: String, phrase: String)

case class Histogram(datetime: String, counts: Long)

case class HistogramResp(histogram: List[Histogram], datetimeFrom: String, datetimeUntil: String, phrase: String)
