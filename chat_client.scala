import java.io._
import scala.io._
import java.net._
import scala.io.StdIn.readLine
import scala.concurrent.Future

val socket = new Socket(InetAddress.getByName("localhost"), 5000)
val in = new BufferedSource(socket.getInputStream()).getLines()
val out = new PrintStream(socket.getOutputStream())

out.println("Hola")
out.flush()

val message = in.next()

var connected = false
var user =  ""
val commands = Map(
    "login" -> "/ID $username",
    "getUsers" -> "/USERLIST",
    "chat" -> "CHAT -u $username -m \"$message\"",
    "logout" -> "/CLOSE"
)


while(!connected){
    val input = readLine("Introduzca su usuario: ")
    connected = connect(input) == "Ok"
    if(connected){
        user = input
        println("Conectado Exitosamente")
        getUsers()
    }   
    
    else{
        println("Intentelo nuevamente")
    }
    
}

val future = Future {
    val result = getInput()
    result
}

def getInput() = {
    while(true){
        val next = in.next
        if(next.slice(0,5) == "/CHAT"){
            println("Mensaje de usuario: " + next)
        }
        else if (last == null)
        else if (last == "getUsers"){
            if(next != "Empty"){
                println("Lista de usuario: " + next)
            }
        }
        else if (last == "logout"){
            if(next == "Ok"){
                System.exit(0)
            }
        }
        last = null
    }
}


def getValue(x: Option[String]) = x match{case Some(s) => s case None => ""}

def connect(user: String) : String = {
    val connectCommand = getValue(commands.get("login")).replace("$username", user)
    out.println(connectCommand)
    out.flush()

    val response = in.next

    return response
}

def getUsers()={
    val getUsersCommand = getValue(commands.get("getUsers"))
    out.println(getUsersCommand)
    out.flush()
    last = "getUsers"
}

def logout()={
    val getUsersCommand = getValue(commands.get("logout"))
    out.println(getUsersCommand)
    out.flush()
    last = "logout"
}


