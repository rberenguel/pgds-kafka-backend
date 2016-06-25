package minteressa.producer

import kafka.producer.{ProducerConfig, Producer}
import org.apache.kafka.clients.producer.{Callback, RecordMetadata}
import twitter4j.TwitterStreamFactory
import kafka.producer.KeyedMessage
import twitter4j._

class TwitterStreamListener(twitterStream: TwitterStream) extends StatusListener  {
  this: StringKafkaProducer =>

  val LOG = Logger.getLogger(classOf[TwitterStreamListener])

  def onStallWarning(stallWarning: StallWarning): Unit = {
    // print("Stall warning\n")
    // print(stallWarning)
  }

  def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {
    // print("Deletion notice\n")
    // print(statusDeletionNotice)
  }

  def onScrubGeo(l: Long, l1: Long): Unit = {}

  def onStatus(status: Status): Unit = {
    val msg = new KeyedMessage[String, String](TwitterProducerConfig.KAFKA_TOPIC, TwitterObjectFactory.getRawJSON(status))
    print(msg + "\n")
    producer.send(msg)
  }

  def onTrackLimitationNotice(i: Int): Unit = {}

  def onException(e: Exception): Unit = {

    LOG.info("Shutting down Twitter sample stream...")
    print("Shutting down!\n")
    print(e+"\n")
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
