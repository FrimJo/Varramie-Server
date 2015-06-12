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
//				ArrayList<ToClient> connectedClients = Server.INSTANCE.getConnectedClients();
//				println("Connected clients ("+connectedClients.size()+"):");
//				for(int i = 0; i < connectedClients.size(); i++){
//					println("["+i+"] " + connectedClients.get(i));
//				}
				println("Not yet implemented.");
			}else{
				println("Command '"+ input[0] +"' not recognized.");
			}
		}
		Server.INSTANCE.shutDown();
		println("GUI Closed");
	}
}