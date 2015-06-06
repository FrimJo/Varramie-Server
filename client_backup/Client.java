package com.spots.varramie;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import android.net.ParseException;


/**
 * The main class of ChattApp 2.0 Client side. It uses the
 * Singleton design pattern. Controls all the connection
 * from the server to different connected clients.
 * @author Fredrik Johansson
 * @author Mattias Edin 
 */
public enum Client {
	INSTANCE;
	
	private IGUI				gui;
	private ToServer		connectedServer;
	//private ToDNS			dns_server;
	
	/**
	 * The constructor for class Client. Being a singleton implies that
	 * the constructor is empty because object wont and can't be
	 * generated. This class is initialized from main in the
	 * init method and used through Client.INSTANCE.[method].
	 */
	private Client(){ }
	
	/**
	 * This is the initialize method, it uses a GUI and
	 * setup the connection towards the name server (DNS).
	 * @param gui The GUI to be used with the client, needs to implement GUI_Interface_Client.java.
	 */
	public void init(final IGUI gui){
		this.gui = gui;
		println("Initiating the client . . .");
		
		boolean end = false;
		println("Pleas enter IP and port of the server (xxx.xxx.xxx.xxx:yyyy): ");
		while(!end){
			String in = gui.getInput();
			//in = "10.0.2.2:8000";
			in = "130.239.237.19:8000";
			String[] server_ip_port_array = in.split(":");
			String server_ip = server_ip_port_array[0];
			try{
				int server_port = Integer.parseInt(server_ip_port_array[1]);
				addToServer(server_ip, server_port);
				end = true;
			}catch(IOException e){
				e.printStackTrace();
				println("Wrong IP and/or port, please try again.");
			}catch(ParseException e3){
				e3.printStackTrace();
				println("Wrong IP and/or port, please try again.");
			}catch(Exception e2){
				e2.printStackTrace();
				println("Got something else wrong.");
			}
		}
		println("Connecting . . .");
		try {
			this.connectedServer.connect();
			println("Conncetion is live, waiting for data.");
		} catch (IOException e) {
			println("Couldn't connect.");
		}
		
		this.connectedServer.disconnect();
		
	}
	
	public void receiveTouch(int x, int y){
		this.gui.receiveTouch(x, y);
	}
	
	/**
	 * This method creates a new ToServer object. 
	 * @param server_ip IP of server.
	 * @param server_port Port of server.
	 * @param topic The topic of the server.
	 * @param nr_clients Number of currently connected clients.
	 * @return A new ready to connect server connection, defined by the values received.
	 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void addToServer(final String server_ip, final int server_port) throws UnknownHostException, IOException{
		this.connectedServer = new ToServer(server_ip, server_port);
	}
	
	/**
	 * Make the connection to a server.
	 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void connectToServer() throws UnknownHostException, IOException{
		connectedServer.connect();
	}
	
	/**
	 * Disconnects the client from a specific server.
	 * @param server Server to disconnect from.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void disconnectFromServer(final ToServer server) throws IOException{
		server.disconnect();
	}
	
	/**
	 * Signals the client to shut down and disconnect from all servers.
	 * @param servers A list of servers to disconnect from.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void shutDown() throws IOException{

		
	}

	/**
	 * Tells the GUI to print a string to the user of the interface. 
	 * This method invokes the runLater method of the GUI, and sends
	 * a new thread as a parameter. The GUI then runs the run
	 * method when possible.
	 * @param str The string to print.
	 */
	public synchronized void println(final String str){
		this.gui.println(str);
	}
	
	/**
	 * Sends a message to a connected server.
	 * @param server The server in question.
	 * @param type The type of the message. (MsgTypes.TEXT, MsgTypes.COMP,
	 * MsgTypes.CRYPT, MsgTypes.COMPCRYPT)
	 * @param text The text to send.
	 * @throws SocketException Thrown to indicate that there is an error creating or accessing a Socket.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 * @throws WrongCryptTypeException Thrown to indicate that it has been used a unknown MsgType. The known Msg.Types is as follows: MsgType.TEXT MsgType.COMP MsgType.CRYPT MsgType.COMPCRYPT
	 */
	public void sendMessage(final ToServer server, final int type, final String text) throws SocketException, IOException, WrongCryptTypeException {
		server.sendMessage(type, text);
	}

	public void sendTouch(final float x, final float y) throws SocketException, IOException {
		this.connectedServer.sendTouch(x, y);	
	}
}
