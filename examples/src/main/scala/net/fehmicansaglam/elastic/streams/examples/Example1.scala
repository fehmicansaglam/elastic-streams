package net.fehmicansaglam.elastic.streams.examples

import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.elastic.streams.IndexSubscriber
import net.fehmicansaglam.tepkin.MongoClient
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

import scala.concurrent.duration._

object Example1 extends App {

  val mongoClient = MongoClient("mongodb://localhost")

  import mongoClient.{context, ec}

  implicit val timeout: Timeout = 5.seconds
  implicit val mat = ActorFlowMaterializer()

  val db = mongoClient("tepkin")

  val collection1 = db("collection1")

  val elasticClient = new TransportClient()
    .addTransportAddress(new InetSocketTransportAddress("localhost", 9300))

  for {
    source <- collection1.find(BsonDocument.empty)
  } {
    val sink = Sink(new IndexSubscriber(elasticClient, "users", "user", 20))
    source.mapConcat(_.map(doc => BsonDocument(doc.elements.filterNot(_.name == "_id")).toJson())).runWith(sink)
  }

}
