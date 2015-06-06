import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
	private boolean				stop = false;
	private ClientReceiver		welcomeClient;
	
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
		
		println("Initializing server.");
		
		
		/* Set up welcome socket for clients to connect to. */
		while(!stop){
			println("Setup up data socket at " + this.server_ip + ":" + this.server_port);
			try {
				this.welcomeClient = new ClientReceiver(this.server_ip, this.server_port);
				break;
			} catch (BindException e){
				println("Port " + server_port + " is occupied.");
			} catch (IOException e) {
				
			}
		}
		println("Shuting down.");
		shutDown();
		
		
		
		//this.dns_server.disconnect();
		println("System stoped.");
	}
	
//	public synchronized void receive(PDU pdu, int receiveingClientId){
//		
//		synchronized (this.connectedClients) {
//			int i = 0;
//			for(; i < this.connectedClients.size(); i++){
//				ToClient c = this.connectedClients.get(i);
//				if(c.id == receiveingClientId){
//					i++;
//					break;
//				}else{
//					c.sendPDU(pdu);
//				}
//			}
//			
//			for(; i < this.connectedClients.size(); i++){
//				this.connectedClients.get(i).sendPDU(pdu);
//			}
//
//		}
//	}
	
	/**
	 * Shutdown the server and close the welcome socket.
	 */
	public void shutDown(){
		this.stop = true;
		this.welcomeClient.disconnect();
		ToClient.Manager.removeAllClientsFromList();
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
	
//	/**
//	 * Appends the specified element to the end of the list
//	 * of connected clients.
//	 * @param e Element to be appended to this list.
//	 * @return A boolean representing whether the client was successfully added.
//	 */
//	public synchronized boolean connectedClientsAdd(ToClient e){		
//		println("Added a client to connectedClients.");
//		return this.connectedClients.add(e);	
//	}
	
//	/**
//	 * Removes the first occurrence of the specified element from this list,
//	 * if it is present. If the list does not contain the element, it is unchanged.
//	 * More formally, removes the element with the lowest index i such that
//	 * (o==null ? get(i)==null : o.equals(get(i))) (if such an element exists).
//	 * Returns true if this list contained the specified element
//	 * (or equivalently, if this list changed as a result of the call).
//	 * @param e Element to be removed from this list, if present.
//	 */
//	public synchronized void connectedClientsRemove(ToClient o){
//		this.connectedClients.remove(o);
//	}
//	
//	/**
//	 * Getter for the list with connected clients.
//	 * @return The whole list with connected clients.
//	 */
//	public synchronized ArrayList<ToClient> getconnectedClients(){
//		return this.connectedClients;
//	}
	
//	/**
//	 * This class is a nestled class inside the class Server.
//	 * It extends UDP to communicate with a DNS.
//	 * @param alive A thread used to send alive messages to the DNS.
//	 * @author Fredrik Johansson
//	 */
//	private static class ToDNS extends UDP{
//		
//		private final int ALIVE_TIME = 3000;
//		
//		private Thread alive;
//		
//		/**
//		 * Constructor for ToDNS. Register with a name server (DNS) and starts
//		 * a thread with sends alive messages to the DNS.
//		 * @param dns_ip IP of DNS.
//		 * @param dns_port Port of DNS.
//		 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
//		 * @throws SocketException Thrown to indicate that there is an error creating or accessing a Socket.
//		 */
//		public ToDNS(String dns_ip, int dns_port) throws UnknownHostException, SocketException{
//			super("DNS Thread", dns_ip, dns_port);
//			try{
//				Server.INSTANCE.println("Connecting to DNS server: " + dns_ip + " at port: " + dns_port);
//				reg();
//				alive();
//			} catch (IOException e) {
//				Server.INSTANCE.println("Couldn't send register message to DNS server.");
//				disconnect();
//			}
//		}
//		
//		/**
//		 * This method overrides a abstract method in the UDP class. It is called
//		 * when the UDP class receives a package. The receive method then uses the
//		 * received data to do different action depending of the OpCode of the package.
//		 */
//		@Override
//		public void receive(byte[] bytes) throws IOException {
//			//TODO Receive and use the received byte array
//		}
//		
//		/**
//		 * Register method, sends a register package to the DNS.
//		 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
//		 */
//		public void reg() throws IOException{
//			//TODO Send Reg message to the DNS
//			
//		}
//		
//		/**
//		 * Set an alive thread in motion, witch will send alive messages
//		 * to the DNS.
//		 */
//		public void alive(){
//			this.alive = new Thread("Alive Thread"){
//				@Override
//				public void run(){
//					try {
//						sleep(ALIVE_TIME);
//						while(!ToDNS.this.stop){
//							//TODO Send Alive message to the DNS
//							sleep(ALIVE_TIME);
//						}
//					} catch (InterruptedException e) {
//						//Catches the InterruptedException but keeps the thread running.
//					}
//					Server.INSTANCE.println("Alive Closed");
//				}
//			};
//			this.alive.start();
//		}
//		
//		/**
//		 * Overrides the abstract disconnect method of class UDP.
//		 * Disconnects the UDP and interrupts the alive thread.
//		 */
//		@Override
//		public void disconnect(){
//			super.disconnect();
//			this.alive.interrupt();
//		}
//	}
	
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
