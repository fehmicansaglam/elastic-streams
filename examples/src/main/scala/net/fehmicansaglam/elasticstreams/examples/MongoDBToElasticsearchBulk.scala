package net.fehmicansaglam.elasticstreams.examples

import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.elasticstreams.BulkIndexSubscriber
import net.fehmicansaglam.tepkin.MongoClient
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import scala.collection.JavaConverters._

import scala.concurrent.duration._

object MongoDBToElasticsearchBulk extends App {

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
    val sink = Sink(new BulkIndexSubscriber(elasticClient, "users", "user", 20))
    source.map(_.map(doc => BsonDocument(doc.elements.filterNot(_.name == "_id")).toJson()).asJava).runWith(sink)
  }

}
