import java.net._
import java.io._
import scala.io._
import scala.io.StdIn.readLine
import scala.concurrent.Future



val s = new Socket(InetAddress.getByName("localhost"), 6000)
lazy val in = new BufferedSource(s.getInputStream()).getLines()
val out = new PrintStream(s.getOutputStream())

var last = ""
last= null
var connected = false
var user = ""
val commands = Map(
    "login" -> "/ID $username",
    "getUsers" -> "/USERLIST",
    "getAllUserRooms" -> "/ROOMLIST",
    "getAllUserInvites" -> "/INVITELIST",
    "chat" -> "/CHAT -u $username -m \"$message\"",
    "createRoom" -> "/ROOM $roomName",
    "logout" -> "/CLOSE"
    "getAllRequestsForRoom" -> "/REQUESTLIST $roomName",
)

while(!connected){
    val input = readLine("Introduzca su usuario: ")
    val response = connect(input)
    connected = response == "Ok"
    if(connected){
        user = input
        println("Connectado exitosamente")
        getUsers()
        last = "getUsers"
    }else if(response == "NotValid"){
        println("Nombre de usuario invalido. Intente nuevamente.")
    }else if(response == "Full"){
        println("El chat esta lleno. Intente nuevamente.")
    }else{
        println("Intente nuevamente")
    }
}

implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
val future = Future {
    val result = getInput()
    result
}

while(true){
    val input = readLine("Mensaje a Enviar (username:message) o (command): ")
    processCommand(input)
}


def getInput() : Boolean = {
    while(true){
        val next = in.next()
        if(next.slice(0,5) == "/CHAT"){
            val userStart = next.indexOf(" -u ")
            val messageStart = next.indexOf(" -m ")
            val sender = next.slice(userStart + 3,messageStart)
            val message = next.substring(messageStart + 3)
            println(s"\nMensaje de usuario: $sender => $message")
        }else if(last == "getUsers") {
            if (next != "Empty"){
                println("\nConnected users: " + next)
            }else if(next == "Empty"){
                println("No hay usuarios registrados.")
            }
        }else if(last == "logout"){
            if(next == "Ok"){
                System.exit(0)
            }else if(next == "Error"){
                println("\nOcurrio un error en el proceso.")
            }
        }else if(last == "chat"){
            if(next == "Ok"){

            }else if(next == "NotFound"){
                println("\nUsuario no encontrado.")
            }else if(next == "Error"){
                println("\nOcurrio un error en el proceso.")
            }
        }else if(last == "createRoom"){
            if(next == "Ok"){
                println("\nEl Room fue creado correctamente.")
            }else if(next == "Taken"){
                println("\nEl nombre del Room ya existe.")
            }else if(next == "Error"){
                println("\nOcurrio un error en el proceso.")
            }
        }
        last = null
    }
    return true
}

def connect(user: String) : String = {
    val connectCommand = commands("login").replace("$username", user)
    out.println(connectCommand)
    out.flush()

    val response = in.next()

    return response
}

def createRoom(roomName: String) = {
    val createRoomCommand = commands("createRoom").replace("$roomName", roomName)
    out.println(createRoomCommand)
    out.flush()

    last = "createRoom"
}

def getUsers() = {
    val getUsersCommand = commands("getUsers")
    out.println(getUsersCommand)
    out.flush()

    last = "getUsers"
}

def getAllUserRooms() = {
    val getAllUserRoomsCommand = commands("getAllUserRooms")
    out.println(getAllUserRoomsCommand)
    out.flush()

    last = "getAllUserRooms"
}

def getAllUserInvites() = {
    val getAllUserInvitesCommand = commands("getAllUserInvites")
    out.println(getAllUserInvitesCommand)
    out.flush()

    last = "getAllUserInvites"
}

def getAllRequestsForRoom(roomName : String) = {
    val getAllRequestsForRoomCommand = commands("getAllRequestsForRoom").replace("$roomName", roomName)
    out.println(getAllRequestsForRoomCommand)
    out.flush()

    last = "getAllRequestsForRoom"
}

def logout() = {
    val getUsersCommand = commands("logout")
    out.println(getUsersCommand)
    out.flush()
    last = "logout"

}

def sendMessage(message: String, username: String) = {
    val chatCommand = commands("chat").replace("$username", username).replace("$message", message)
    out.println(chatCommand)
    out.flush()
    last = "chat"
}

def processCommand(inputData: String) = {
    val data = inputData.split(":")
    if(data.size > 1 ){
        val username = data(0).trim()
        val message = data(1).trim()

        sendMessage(message, username)
    }
    else{ //comandos
        val command = data(0).split("/")
        if(command.size > 1){
            if(command(0) == "createroom"){
                var roomName = command(1)
                createRoom(roomName)
                last = "createRoom"
            } else if(command(0) == "getallrequestsforroom"){
                var chatRoomName = command(1)
                getAllRequestsForRoom(chatRoomName)
                last = "getAllRequestsForRoom"
            } else {
                last = ""
                println("Invalid command")
            }
        }else{

            if(command(0) == "userlist"){
                getUsers()
                last = "getUsers"
            }

            if(command(0) == "getalluserrooms"){
                getAllUserRooms()
                last = "getAllUserRooms"
            }
            
            if(command(0) == "getalluserinvites"){
                getAllUserInvites()
                last = "getAllUserInvites"
            }
            else if(command(0) == "quit"){
                logout()
                last = "logout"
            } else{
                last = ""
                println("Invalid command")
            }
        }
    }
}

def validateResponse() = {
    val getUsersCommand = commands("getUsers")
    out.println(getUsersCommand)
    out.flush()
    last = "getUsers"

}
s.close()
