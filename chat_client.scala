import java.net._
import java.io._
import scala.io._
import scala.io.StdIn.readLine
import scala.concurrent.Future



val s = new Socket(InetAddress.getByName("localhost"), 5000)
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
    "rejectRoom" -> "/REJECT $roomName",
    "joinRoom" -> "/JOIN $roomName",
    "addUsers" -> "/ADD $f $roomName",
    "quitRoom" -> "/QUIT $roomName",
    "logout" -> "/CLOSE",
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
    val input = readLine()
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
        }else if(next.slice(0,11) == "/ROOMREJECT"){
            val salida = next.substring(11)
            println(s"\nRechazo de usuario: $salida")
        } else if(next.slice(0,9) == "/ROOMJOIN"){
            val salida = next.substring(9)
            println(s"\nEntrada de usuario: $salida")
        } else if(next.slice(0,9) == "/ROOMQUIT"){
            val salida = next.substring(9)
            println(s"\nSalida de usuario: $salida")
        }
        else if(last == "getUsers") {
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
        }else if(last == "quitRoom"){
            if(next == "Ok"){

            }else if(next == "NotInRoom"){
                println("\nNo esta en esa sala.")
            }else if(next == "Error"){
                println("\nOcurrio un error en el proceso.")
            }
        }
        else if(last == "createRoom"){
            if(next == "Ok"){
                println("\nEl Room fue creado correctamente.")
            }else if(next == "Taken"){
                println("\nEl nombre del Room ya existe.")
            }else if(next == "Error"){
                println("\nOcurrio un error en el proceso.")
            }
        } else if(last == "rejectRoom"){
            if(next == "Ok"){
                println("\nRechazado correctamente.")
            }else if(next == "Error"){
                println("\nOcurrio un error en el proceso.")
            }
        } 
        else if(last == "addUsers"){
            if(next == "Ok"){
                println("\nAñadido correctamente.")
            } else if(next == "NotFound"){
                println("\nNombre de usuario no fue encontrado.")
            }
            else if(next == "Error"){
                println("\nOcurrio un error en el proceso.")
            }
        }
        else if(last == "joinRoom"){
            if(next == "Ok"){
                println("\nEl Room fue creado correctamente.")
            }else if(next == "Already"){
                println("\nYa esta unido al Room.")
            }else if(next == "NotFound"){
                println("\nEl nombre del Room no fue encontrado.")
            }
            else if(next == "Error"){
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

def addUsers(forced: Boolean, roomName: String, names: String) = {
    val addUsersCommand = commands("createRoom").replace("$roomName", roomName)
    if(forced){
        addUsersCommand.replace("$f","-f")
    }else{
        addUsersCommand.replace("$f","")
    }
    addUsersCommand + " " + names.mkString(" ")
    out.println(addUsersCommand)
    out.flush()

    last = "addUsers"
}

def createRoom(roomName: String) = {
    val createRoomCommand = commands("createRoom").replace("$roomName", roomName)
    out.println(createRoomCommand)
    out.flush()

    last = "createRoom"
}

def quitRoom(roomName: String) = {
    val quitRoomCommand = commands("quitRoom").replace("$roomName", roomName)
    out.println(quitRoomCommand)
    out.flush()

    last = "createRoom"
}

def rejectRoom(roomName: String) = {
    val rejectRoomCommand = commands("rejectRoom").replace("$roomName", roomName)
    out.println(rejectRoomCommand)
    out.flush()

    last = "rejectRoom"
}

def joinRoom(roomName: String) = {
    val joinRoomCommand = commands("joinRoom").replace("$roomName", roomName)
    out.println(joinRoomCommand)
    out.flush()

    last = "joinRoom"
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
            }else if(command(0) == "rejectroom"){
                var chatRoomName = command(1)
                rejectRoom(chatRoomName)
            }else if(command(0) == "quit"){
                var chatRoomName = command(1)
                quitRoom(chatRoomName)
            }
             else if(command(0) == "joinroom"){
                var chatRoomName = command(1)
                joinRoom(chatRoomName)
            } else if(command(0) == "adduser"){
                val forced = command(1) == "f"
                val chatRoomName = command(2)
                val names = command(3)
                joinRoom(chatRoomName)
            }
            else {
                last = null
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
            else if(command(0) == "close"){
                logout()
                last = "logout"
            } else{
                last = null
                println("Invalid command")
            }
        }
    }
}

s.close()
