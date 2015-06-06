
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * A basic class for handeling UDP connections.
 * @author Fredrik Johansson
 *
 */
public abstract class UDP extends Thread{
	
	protected Boolean				stop = new Boolean(false);
	protected DatagramSocket		socket;
	
	private final int 				BUFFER_SIZE = 65535;
	
	private String					server_ip;
	private int						server_port;
	
	/**
	 * Constructor of UDP class.
	 * @param thread_name The name of the thread.
	 * @param server_ip Server IP.
	 * @param server_port Server port.
	 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
	 * @throws SocketException Thrown to indicate that there is an error creating or accessing a Socket.
	 */
	public UDP(String thread_name, String server_ip, int server_port, int count) throws UnknownHostException, SocketException{
		super(thread_name + "" + count);
		this.server_ip = server_ip;
		this.server_port = server_port;
		
		
		start();	
	}
	
	/**
	 * This run method overrides the run method in class Thread. It is invoked form
	 * the constructor of this class. It listens on the socket and pouches received
	 * data up to the ToServer and ToClient classes through the method receive.
	 */
	@Override
	public void run(){
		byte[] buffer = new byte[BUFFER_SIZE];
		try {
			this.socket = new DatagramSocket();
			DatagramPacket packetContainer = new DatagramPacket(buffer, buffer.length);
			while(!stop){
			
				this.socket.receive(packetContainer);
				try{
					receive(buffer);
				} catch (IOException e) {
					disconnect();
				}
			}		
		} catch (SocketException e) {
			disconnect();
		} catch (IOException e) {
			//Catches the IOException but keeps the thread running.
		}
	}
	
	/**
	 * An abstract class with needs to be implemented by any class witch want to
	 * extend this class. 
	 * @param bytes The data containing bytes.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public abstract void receive(byte[] bytes) throws IOException;
	
	/**
	 * Sends a PDU through the UDP protocol using datagram packets.
	 * @param pdu PDU to send.
	 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public synchronized void sendPDU(PDU pdu, InetAddress client_ip, int client_port) throws UnknownHostException, IOException{
		this.socket.send(new DatagramPacket(pdu.getBytes(), pdu.getBytes().length, client_ip, client_port));
	}
	
	/**
	 * Makes the run thread of UDP to end and close the sockets.
	 */
	public void disconnect(){
		this.stop = true;
		this.socket.close();
	}
}
