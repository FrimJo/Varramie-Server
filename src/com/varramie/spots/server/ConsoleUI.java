package com.varramie.spots.server;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.util.Scanner;

public class ConsoleUI extends Thread implements IGUI{
	
	private final Scanner	systemIn = new Scanner(System.in);
	private static boolean	exit = false;
	private final Time		time = new Time(System.currentTimeMillis());
	
	/**
	 * Constructor
	 */
	public ConsoleUI(){
		super("GUI main thread");
	}
	
	/**
	 * Displays the string of text in the UI
	 * @param str
	 */
	@Override
	public void print(String str) {
		System.out.print(str);
	}
	
	/**
	 * Displays the string of text as a line in the UI.
	 * @param str 
	 */
	@Override
	public void println(String str) {
		this.time.setTime(System.currentTimeMillis());
		System.out.println("(" + this.time.toString()+") " + str + "["+ToClient.Manager.packageQueueSize()+"]");
	}
	
	/**
	 * Gets the typed text from the user .
	 * @return The string of text typed by the user.
	 */
	@Override
	public String getInput() {
		return this.systemIn.nextLine();
	}
	
	/**
	 * Starts and runs the UI and after this method the UI can expect inputs from the keyboard.
	 */
	@Override
	public void run(){
		String[] input;
		while(!exit){
			input = getInput().split(" ");
			if( input[0].equals("quit") ){
				Server.INSTANCE.shutDown();
				break;
				
			}else if( input[0].equals("list") ){
				Object[] connectedClients = ToClient.Manager.getAllClientsAsArray();
				println("Connected clients ("+connectedClients.length+"):");
				for(int i = 0; i < connectedClients.length; i++){
					ToClient c = (ToClient) connectedClients[i];
					println("["+i+"] IP: " + c.getAddress().getHostAddress() + ":" + c.getPort());
				}
			}else if( input[0].equals("form") ){
				
				try {
					if(input.length != 2)
						throw new UnsupportedEncodingException();
					Server.INSTANCE.sendForm(input[1]);
				} catch (UnsupportedEncodingException e) {
					println("Comand 'form' not used correctly, example: 'form http://www.example.com'");
				} catch (InterruptedException e) {}
				
			}else{
				println("Command '"+ input[0] +"' not recognized.");
			}
		}
		Server.INSTANCE.shutDown();
		println("GUI Closed");
	}
}