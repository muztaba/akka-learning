import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory


object Application extends JsonDataProtocol with SprayJsonSupport with App {
  implicit val system = ActorSystem(Behaviors.empty, "AkkaHttpJson")
  val config = ConfigFactory.load()

  val route: Route = {
    (path("api" / "get_status") & get) {
      complete(StatusOk("ok"))
    } ~
      (path("api" / "get_size") & get) {
        complete(FileSiz(FileService.getFileSize(config.getString("file.path"))))
      } ~
      (path("api" / "data") & post) {
        entity(as[DataRequest]) { dataRequest: DataRequest =>
          onSuccess(FileService.getData(config.getString("file.path"), dataRequest)) { result =>
            complete(DataResp(result.toList, dataRequest.datetimeFrom, dataRequest.datetimeUntil, dataRequest.phrase))
          }
        }
      } ~
      (path("api" / "histogram") & post) {
        entity(as[DataRequest]) { dataRequest: DataRequest =>
          onSuccess(FileService.getHistogram(config.getString("file.path"), dataRequest)) { result =>
            complete(HistogramResp(result.toList, dataRequest.datetimeFrom, dataRequest.datetimeUntil, dataRequest.phrase))
          }
        }
      }
  }

  Http().newServerAt("localhost", config.getInt("server.port")).bind(route)
}
