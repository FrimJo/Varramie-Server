
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Represents the connection between the client and the chat server.
 * Extends class TCP.
 * @author Fredrik Johansson
 * @author Mattias Edin
 */
public class ClientReceiver extends UDP{
	
	private static int					threadCounter = 0;
	protected int 						id;
	
	/**
	 * Constructor for class ToClient, extends TCP. Calls the super class
	 * with received parameters.
	 * @param socket The socket to witch the connection has been established.
	 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public ClientReceiver(String server_ip, int server_port) throws UnknownHostException, IOException{
		super("Client thread ", server_ip, server_port, threadCounter);
		this.id = threadCounter; 
		threadCounter++;
	}
	
	/**
	 * Overrides the abstract method receive of the class TCP. When the
	 * TCP connection receives a package it sends it's content to
	 * this this method. Depending on the OpCode contained in the package
	 * this method executes different actions. 
	 * @param bytes The received bytes.
	 * @param bytesRead Number of bytes read.
	 */
	@Override
	public void receive(final byte[] bytes){
		final ClientReceiver clientReceiver = this;
		new Thread("Runner")
		{
		    public void run() {
				PDU pdu = new PDU(bytes, bytes.length);
				int client_id;
				switch (pdu.getByte(0)) {
				case OpCodes.DOWN: case OpCodes.MOVE: case OpCodes.UP:
					int id = (int) pdu.getInt(1);
					ToClient.Manager.giveClientPackage(id, pdu);
					Server.INSTANCE.println("Received a touch from " + id + " with action: " + pdu.getByte(0));
					
					break;
				case OpCodes.JOIN:
					byte[] address = new byte[4];
					pdu.setSubrange(1, address);
					try {
						InetAddress client_ip = InetAddress.getByAddress(address);
						int client_port = (int) pdu.getInt(5);
						ToClient c = new ToClient(client_ip, client_port, clientReceiver);
						
						
						pdu = new PDU(5);
						pdu.setByte(0, (byte) OpCodes.JOIN);
						pdu.setInt(1, c.getId());
						sendPDU(pdu, client_ip, client_port);
					} catch (IOException e) {
						
					}
					
					break;
				
				case OpCodes.ALIVE:
					
					break;
				case OpCodes.QUIT:
					client_id = (int) pdu.getInt(1);
					ToClient.Manager.removeClientFromList(client_id);
					break;
				default:
					break;
				}
		    }
		}.start();
	}
	
	/**
	 * Returns A string representation of this thread, including the thread's name, priority, and thread group.
	 * @return A string representation of this thread.
	 */
	@Override
	public String toString(){
		return this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort();
	}	
	
	@Override
	public void disconnect(){
		super.stop = true;
		if(this.socket != null)
			super.socket.close();
	}
	
	public synchronized void sendPDU(PDU pdu, InetAddress client_ip, int client_port){
		try {
			super.sendPDU(pdu, client_ip, client_port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
