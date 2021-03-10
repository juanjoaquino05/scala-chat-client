import java.io._
import scala.io._
import java.net._

val socket = new Socket(InetAddress.getByName("localhost"), 5000)
val in = new BufferedSource(socket.getInputStream()).getLines()
val out = new PrintStream(socket.getOutputStream())

out.println("Hola")
out.flush()

val message = in.next()