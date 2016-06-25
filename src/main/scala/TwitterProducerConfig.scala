package minteressa.producer

import java.util.Properties
import javax.naming.directory.{InitialDirContext, Attribute}
import javax.naming.NamingException
import scala.collection.JavaConversions._

import twitter4j.conf.ConfigurationBuilder

object TwitterProducerConfig {
  val props = new Properties()
  val props_s = getClass.getResourceAsStream("../../producer.properties")
  props.load(props_s)
  val environmentVars = System.getenv
  // Useful for debug: for ((k,v) <- environmentVars) println(s"key: $k, value: $v")
  try {
    val broker = sys.env("KAFKA")
    props.setProperty("metadata.broker.list", broker)
    props.setProperty("consumerKey", sys.env("TWITTERCONSUMERKEY"))
    props.setProperty("consumerSecret", sys.env("TWITTERCONSUMERSECRET"))
    props.setProperty("accessToken", sys.env("TWITTERACCESSTOKEN"))
    props.setProperty("accessTokenSecret", sys.env("TWITTERACCESSTOKENSECRET"))
  } catch {
    case e: Exception => {
      print("Problem: environment variable not correctly set: " + e)
      System.exit(255)
    }
  }
  print(props)

  val KAFKA_TOPIC = props.getProperty("producer.topic")

  val producerProps = new Properties()
  List(
    "serializer.class",
    "metadata.broker.list",
    "request.required.acks"
  ) foreach(s => producerProps.put(s, props.get(s)))


  val cb = new ConfigurationBuilder()
  cb.setOAuthConsumerKey(props.getProperty("consumerKey"))
  cb.setOAuthConsumerSecret(props.getProperty("consumerSecret"))
  cb.setOAuthAccessToken(props.getProperty("accessToken"))
  cb.setOAuthAccessTokenSecret(props.getProperty("accessTokenSecret"))
  cb.setJSONStoreEnabled(true)
  cb.setIncludeEntitiesEnabled(true)

  val twitterStreamingConf = cb.build()

}
