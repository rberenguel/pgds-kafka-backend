package minteressa.producer

import kafka.producer.{ProducerConfig, Producer}
import org.apache.kafka.clients.producer.{Callback, RecordMetadata}
import twitter4j.TwitterStreamFactory
import kafka.producer.KeyedMessage
import twitter4j._
import java.net.InetSocketAddress


class TwitterStreamListener(twitterStream: TwitterStream) extends StatusListener  {
  this: StringKafkaProducer =>

  val LOG = Logger.getLogger(classOf[TwitterStreamListener])

  val GraphiteEndpoint = new InetSocketAddress("graphite", 2003)
  val Graphite = new GraphiteClient(GraphiteEndpoint, Array("kafka.twitter.stall", "kafka.twitter.deletion", "kafka.raw_tweets"))
  Graphite.init()

  def onStallWarning(stallWarning: StallWarning): Unit = {
    val timestamp: Long = System.currentTimeMillis / 1000
    Graphite.batch("kafka.twitter.stall", "1", timestamp)
  }

  def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {
    val timestamp: Long = System.currentTimeMillis / 1000
    Graphite.batch("kafka.twitter.deletion", "1", timestamp)
  }

  def onScrubGeo(l: Long, l1: Long): Unit = {}

  def onStatus(status: Status): Unit = {
    val timestamp: Long = System.currentTimeMillis / 1000
    val msg = new KeyedMessage[String, String](TwitterProducerConfig.KAFKA_TOPIC, TwitterObjectFactory.getRawJSON(status))
    Graphite.batch("kafka.raw_tweets", "1", timestamp)
    producer.send(msg)
  }

  def onTrackLimitationNotice(i: Int): Unit = {}

  def onException(e: Exception): Unit = {
    LOG.info("Shutting down Twitter sample stream...")
    print(e)
    print("Shutting down!\n")
    Graphite.close()
    twitterStream.shutdown()
  }
}

trait StringKafkaProducer {
  val props = TwitterProducerConfig.producerProps
  val config = new ProducerConfig(props)
  val producer = new Producer[String, String](config)
}

class TwitterProducer {
  this: StringKafkaProducer =>

  def start() = {
    val twitterStream = new TwitterStreamFactory(TwitterProducerConfig.twitterStreamingConf).getInstance()
    val listener = new TwitterStreamListener(twitterStream) with StringKafkaProducer
    twitterStream.addListener(listener)
    twitterStream.sample()
  }
}
