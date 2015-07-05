package com.varramie.spots.server;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.varramie.spots.dal.SQLConnector;


/**
 * The main class of Varraimire_Server Server side. It uses the
 * Singleton design pattern. Controls all the connection
 * from the client to different servers.
 * @author Fredrik Johansson
 */
public enum Server {
	INSTANCE;
	
	
	
	/** Private attributes. */
	private String				server_ip;
	private int					server_port;

	private IGUI				gui;
	private UDP					udp;
	
	private SQLConnector		sqlConnector = new SQLConnector();
	
	/**
	 * The constructor for class Server. Being a singleton implies that
	 * the constructor is empty because object wont and can't be
	 * generated. This class is initialized from main in the
	 * init method and used through Server.INSTANCE.[method].
	 */
	private Server(){ }
	
	public void init(IGUI gu, String server_ip, String server_port){
		this.gui = gu;
		
		/* Start up the userinterface. */
		this.gui.start();
		
		try{
			this.server_port = Integer.parseInt(server_port);	
		}catch(NumberFormatException e){
			this.gui.println("Wrong type of arguments, shutting down.");
			shutDown();
		}
		
		this.server_ip = server_ip;
		
		println("Initializing server at "+server_ip+":"+server_port+".");
		
		try {
			this.udp = new UDP("UDP Thread", this.server_ip, this.server_port);
			println("Done initializing server, waiting for packages . . .");
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
			println("Could not initializing server.");
			shutDown();
		}
		
	}
	
	/**
	 * Sends the data to the SQL Data Access Layer through SQLConnector pipeline.
	 */
	public void writeToDB(final float x, final float y, final float pressure, final byte action, final String id, final float vel_x, final float vel_y){
		sqlConnector.addEntry(x, y, pressure, action, id, vel_x, vel_y);
	}
	
	/**
	 * Sends the data to the SQL Data Access Layer through SQLConnector pipeline.
	 */
	public void writeToDB(final float x, final float y, final String collision_idA, final String collision_idB, final byte action){
		sqlConnector.addEntry(x, y, collision_idA, collision_idB, action);
	}
	
	
	/**
	 * Shutdown the server and close the welcome socket.
	 */
	public void shutDown(){
		println("System shutting down . . .");
		udp.disconnect();
		sqlConnector.close();
	}
	
	/**
	 * Getter of the server IP.
	 * @return Server IP.
	 */
	public synchronized String getServer_ip(){
		return this.server_ip;
	}
	
	/**
	 * Gets input from user through the GUI.
	 * @return User entered string.
	 */
	public synchronized String getInput(){
		return this.gui.getInput();
	}
	
	/**
	 * Tells the GUI to print a string and a enter.
	 * @param str The string to print.
	 */
	public synchronized void println(String str){
		this.gui.println(str);
	}
	
	/**
	 * Tells the GUI to print a string and a enter.
	 * @param str The string to print.
	 */
	public synchronized void print(String str){
		this.gui.print(str);
	}
	
	public void sendForm(String url) throws UnsupportedEncodingException{
		byte[] packet = PDU_Factory.form(url);
		ToClient.Manager.addPackage(new PacketContainer(packet, ""));
	}
	
	/**
	 * The main method to kick the program off.
	 * */
	public static void main(
			String[] args) {
		try{
			Server.INSTANCE.init(new ConsoleUI(), args[0], args[1]);
		}catch(NumberFormatException e){
			
		}
	}
}
