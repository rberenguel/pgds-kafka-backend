package minteressa.producer

import java.util.regex.Pattern
import java.nio.charset.Charset
import java.io._
import javax.net.SocketFactory
import java.net.InetSocketAddress

// The data is sent over a plain socket

class GraphiteClient(address: InetSocketAddress, topics: Array[String] ) extends Closeable {

  private final val WHITESPACE = Pattern.compile("[\\s]+")
  private final val charset: Charset = Charset.forName("UTF-8")

  private lazy val socket = {
    val s = SocketFactory.getDefault.createSocket(address.getAddress, address.getPort)
    s.setKeepAlive(true)
    s
  }

  private lazy val writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream, charset))

  private val counts = collection.mutable.Map[String, Int]()
  private val timestamps = collection.mutable.Map[String, Long]()

  def init() {
    val timestamp = System.currentTimeMillis / 1000
    for(topic <- topics ){
      counts += topic -> 0
      timestamps += topic -> timestamp
    }
  }

  def send(name: String, value: String, timestamp: Long) {
    val sb = new StringBuilder()
      .append(sanitize(name)).append(' ')
      .append(sanitize(value)).append(' ')
      .append(timestamp.toString).append('\n')

    // The write calls below handle the string in-one-go (locking);
    // Whereas the metrics' implementation of the graphite client uses multiple `write` calls,
    // which could become interwoven, thus producing a wrong metric-line, when called by multiple threads.
    writer.write(sb.toString())
    writer.flush()
  }

  private var updated:Long = System.currentTimeMillis / 1000

  // This batching operation is probably not thread safe if called
  // from more than one thread

  def batch(topic: String, value: String, timestamp: Long) {
    val timestamp = System.currentTimeMillis / 1000
    if(timestamps(topic) == timestamp){
      counts(topic) = counts(topic) + 1
    }else{
      for (topic <- topics){
        updated = timestamp
        println(topic + " " + timestamp.toString + " " + counts(topic).toString)
        send(topic, counts(topic).toString, timestamps(topic))
        timestamps(topic) = timestamp
        counts(topic) = 0
      }
    }
  }

  /** Closes underlying connection. */
  def close() {
    try socket.close() finally writer.close()
  }

  protected def sanitize(s: String): String = {
    WHITESPACE.matcher(s).replaceAll("-")
  }
}
