// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.backend;

import ocsf.client.*;

import java.io.*;

import edu.seg2105.client.common.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 */
public class ChatClient extends AbstractClient
{
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF clientUI; 
  
  String loginID;
 
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
  
  public ChatClient(String loginID, String host, int port, ChatIF clientUI) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
    this.loginID = loginID;
    openConnection();
    sendToServer("#login " + loginID);
  }

  
  //Instance methods ************************************************
    
  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
  public void handleMessageFromServer(Object msg) 
  {
    clientUI.display(msg.toString());
    
    
  }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */

  public void handleMessageFromClientUI(String message) {
      if (message.startsWith("#")) {
          handleCommand(message);
      } else {
          try {
              sendToServer(message);
          } catch (IOException e) {
              clientUI.display("Could not send message to server. Terminating client.");
              quit();
          }
      }
  }

  // Handle commands starting with #
  private void handleCommand(String command) {
      String[] parts = command.split(" ");
      String cmd = parts[0];

      switch (cmd) {
          case "#quit":
              quit();
              break;
          case "#logoff":
              logoff(); 
              break;
          case "#sethost":
              if (isLoggedOff()) { 
                  if (parts.length > 1) {
                      setHost(parts[1]); 
                      clientUI.display("Host set to: " + parts[1]);
                  } else {
                      clientUI.display("Usage: #sethost <host>");
                  }
              } else {
                  clientUI.display("Error: You must be logged off to change the host.");
              }
              break;
          case "#setport":
              if (isLoggedOff()) { 
                  if (parts.length > 1) {
                      try {
                          int port = Integer.parseInt(parts[1]);
                          setPort(port); 
                          clientUI.display("Port set to: " + port);
                      } catch (NumberFormatException e) {
                          clientUI.display("Error: Invalid port number.");
                      }
                  } else {
                      clientUI.display("Usage: #setport <port>");
                  }
              } else {
                  clientUI.display("Error: You must be logged off to change the port.");
              }
              break;
          case "#login":
              if (!isConnected()) { 
                  try {
                      openConnection(); 
                      clientUI.display("Logged in to server.");
                  } catch (IOException e) {
                      clientUI.display("Failed to connect to server.");
                  }
              } else {
                  clientUI.display("Error: Already connected.");
              }
              break;
          case "#gethost":
              clientUI.display("Current host: " + getHost()); 
              break;
          case "#getport":
              clientUI.display("Current port: " + getPort()); 
              break;
          default:
              clientUI.display("Error: Unknown command.");
      }
  }

  private void logoff() {
      if (isConnected()) {
          try {
              closeConnection(); 
              clientUI.display("Logged off from the server.");
          } catch (IOException e) {
              clientUI.display("Error while logging off: " + e.getMessage());
          }
      } else {
          clientUI.display("Error: Not currently connected to the server.");
      }
  }
  
  private boolean isLoggedOff() {
      return !isConnected(); 
  }
  
  @Override
  protected void connectionClosed() {
      clientUI.display("Connection closed.");
  }

  @Override
  protected void connectionException(Exception exception) {
      clientUI.display("The server has shut down due to an exception.");
      quit();
  }

  
  /**
   * This method terminates the client.
   */
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {
    	System.err.println("Error while closing connection: " + e.getMessage());
    }
    System.exit(0);
  }
}
//End of ChatClient class
