package edu.seg2105.edu.server.backend;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.IOException;

import edu.seg2105.client.ui.ServerConsole;

import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 */
public class EchoServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  
  final private String loginIDKey = "loginID";

  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) 
  {
    super(port);
  }

  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient
    (Object msg, ConnectionToClient client)
  {
    String message = msg.toString();
    try {
        String loginID = (String) client.getInfo(loginIDKey); 
        if (loginID == null && message.startsWith("#login ")) {
            handleLoginCommand(message, client);  
        } else if (loginID != null) {
            System.out.println("SERVER MSG> Message received from " + loginID + ": " + message);  
            sendToAllClients(loginID + "> " + message); 
        } else {
            client.sendToClient("SERVER MSG> ERROR: You must log in first using #login <loginID>");
            client.close();
        }
    } catch (IOException e) {
        System.out.println("SERVER MSG> Error handling message from client: " + e.getMessage());
    }
  }
  
  private void handleLoginCommand(String message, ConnectionToClient client) throws IOException {
	    if (client.getInfo(loginIDKey) != null) {
	        client.sendToClient("ERROR: You are already logged in. Connection will be closed.");
	        client.close();
	        return;
	    }

	    String loginID = message.substring(7).trim(); 
	    if (loginID.isEmpty()) {
	        client.sendToClient("ERROR: Login ID cannot be empty. Connection will be closed.");
	        client.close();
	    } else {
	        client.setInfo(loginIDKey, loginID); 
	        client.sendToClient("#login " + loginID);
	        System.out.println("SERVER MSG> Client logged in with ID: " + loginID);
	    }
	}
    
  public void handleMessageFromServerConsole(String message) {
	  if (message.startsWith("#")) {
		  String[] parameters = message.split(" ");
		  String command = parameters[0];
		  switch (command) {
		  	case "#quit":
		  		try {
		  			this.close();
		  		} catch (IOException e) {
		  			System.exit(1);
		  		}
		  		System.exit(0);
		  		break;
		  	case "#stop":
		  		this.stopListening();
		  		break;
		  	case "#close":
		  		try {
		  			this.close();
		  		} catch (IOException e) {
		  	        System.out.println("SERVER MSG> Error closing connection: " + e.getMessage());
		  		}
		  		break;
		  	case "#setport":
		  		if (!this.isListening() && this.getNumberOfClients() < 1) {
		  			super.setPort(Integer.parseInt(parameters[1]));
		  			System.out.println("SERVER MSG> Port set to " +
		  					Integer.parseInt(parameters[1]));
		  		} else {
		  			System.out.println("SERVER MSG> Can't do that now. Server is connected.");
		  		}
		  		break;
          		  	case "#start":
		  		if (!this.isListening()) {
		  			try {
		  				this.listen();
		  			} catch (IOException e) {
		  			    System.out.println("SERVER MSG> Error listening for incoming connections: " + e.getMessage());

		  			}
		  		} else {
		  			System.out.println("SERVER MSG> Can't do that now. Server is connected.");
		  		}
		  		break;
		  	case "#getport":
		  		System.out.println("Current port is " + this.getPort());
		  		break;
		  	default:
		  		System.out.println("Invalid command: '" + command+ "'");
		  		break;
		  }
	  } else {
		  this.sendToAllClients(message);
	  	}
  }
  
  public void handleMessageFromServer(Object msg) {
	    System.out.println("SERVER MSG> " + msg);
	    this.sendToAllClients("SERVER MSG> " + msg); 
	}


  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
      ("SERVER MSG> Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
      ("SERVER MSG> Server has stopped listening for connections.");
  }
  
  protected void clientConnected(Object client) {
      System.out.println("SERVER MSG> Client connected: " + client.toString()); 
  }

  synchronized protected void clientDisconnected(Object client) {
	  String loginID = (String) ((ConnectionToClient) client).getInfo(loginIDKey);
      System.out.println(" SERVER MSG> Client disconnected: " + (loginID != null ? loginID : "Unknown"));  }
  
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  public static void main(String[] args) 
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    EchoServer sv = new EchoServer(port);
    
    try 
    {
      sv.listen(); //Start listening for connections
    } 
    catch (Exception ex) 
    {
      System.out.println("SERVER MSG> ERROR - Could not listen for clients!");
    }
    
    ServerConsole console = new ServerConsole(port, sv);
    console.accept(); 
  }
}
//End of EchoServer class
