
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * A basic class for handeling UDP connections.
 * @author Fredrik Johansson
 *
 */
public class UDP extends Thread{
	
	private final int 				BUFFER_SIZE = 6;
	
	protected Boolean				stop = false;
	
	private final String			IP = "224.0.0.3";
	
	protected DatagramSocket		socket;
	private InetAddress				group_address;
	private final int				group_port = 4446;
	private Sender					senderThread = new Sender();

	
	
	/**
	 * Constructor of UDP class.
	 * @param thread_name The name of the thread.
	 * @param server_ip Server IP.
	 * @param server_port Server port.
	 * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
	 * @throws SocketException Thrown to indicate that there is an error creating or accessing a Socket.
	 */
	public UDP(String thread_name, String server_ip, int server_port) throws UnknownHostException, SocketException{
		super(thread_name);
		System.setProperty("java.net.preferIPv4Stack" , "true");
		try {
			this.group_address = InetAddress.getByName(IP);
			this.socket = new DatagramSocket(server_port);
			this.socket.setBroadcast(true);
			this.start();
			this.senderThread.start();
			ToClient.Manager.startCleaner();
		} catch (IOException e) {
			e.printStackTrace();
			Server.INSTANCE.println("Couldn't start the server, IOException in UDP");
		}
	}
	
	/**
	 * This run method overrides the run method in class Thread. It is invoked form
	 * the constructor of this class. It listens on the socket and pouches received
	 * data up to the ToServer and ToClient classes through the method receive.
	 */
	@Override
	public void run(){
		
		byte[] buffer = new byte[256];
		
			DatagramPacket packetContainer = new DatagramPacket(buffer, buffer.length, group_address, group_port);
			Server.INSTANCE.println("Waiting for conections . . .");
			while(!stop){
				try {
					socket.receive(packetContainer);
					
					InetAddress client_address = packetContainer.getAddress();
					int			client_port = packetContainer.getPort();
					
					byte[] bytes = packetContainer.getData();
					
					ByteBuffer bb;
					byte action = buffer[0];
					
					ToClient client = null;
					switch (action) {
					case OpCodes.JOIN:
						Server.INSTANCE.println("Received packet JOIN from: " + client_address.getHostAddress() + ":" + client_port);
						
						if(!Checksum.isCorrect(new byte[]{bytes[0], bytes[1]}))
							throw new ChecksumException("The checksum of the package is not correct.");
						
						client = ToClient.Manager.hasClient(client_address, client_port);
						if(client == null)
							client = ToClient.Manager.createClient(client_address, client_port);
						if(client == null)
							throw new Exception("Server full!");
						Server.INSTANCE.println("Client width id: "+client.getId()+" connected from: " + client_address.getHostAddress() + ":" + client_port);
						byte[] joinBytes = PDU_Factory.join(client.getId());
						
						socket.send(new DatagramPacket(joinBytes, joinBytes.length, client_address, client_port));
						Server.INSTANCE.println("Sent a returning JOIN message to client with id: " + client.getId());

						break;
					case OpCodes.ALIVE:
						
						bb = ByteBuffer.wrap(new byte[]{bytes[0], bytes[1],  bytes[2]}, 0, 3);
						if(!Checksum.isCorrect(bb.array()))
							throw new ChecksumException("The checksum of the package is not correct.");
						int alive_id = bb.get(2) & 0xff;
						Server.INSTANCE.println("Received ALIVE message from client with id: "+alive_id);
						client = ToClient.Manager.hasClient(alive_id);
						if(client == null){
							byte[] notregBytes = PDU_Factory.notreg();
							socket.send(new DatagramPacket(notregBytes, notregBytes.length, client_address, client_port));
							Server.INSTANCE.println("Sent a returning NOTREG message to client with id: " + alive_id);
							break;
						}
						
						client.setAlive(true);
						
						byte[] aliveBytes = PDU_Factory.alive(client.getId());
						socket.send(new DatagramPacket(aliveBytes, aliveBytes.length, client_address, client_port));
						Server.INSTANCE.println("Sent a returning ALIVE message to client with id: " + client.getId());
						break;
						
					case OpCodes.QUIT:
						Server.INSTANCE.println("Received packet QUIT.");
						bb = ByteBuffer.wrap(new byte[]{bytes[0], bytes[1], bytes[2]}, 0, 3);
						if(!Checksum.isCorrect(bb.array()))
							throw new ChecksumException("The checksum of the package is not correct.");
						int quit_id = bb.get(2) & 0xff;
						boolean success = ToClient.Manager.removeClientFromList(quit_id);
						if(success){
							Server.INSTANCE.println("Client with id: "+quit_id+" disconnected");
							byte[] quitBytes = PDU_Factory.quit(quit_id);
							socket.send(new DatagramPacket(quitBytes, quitBytes.length, client_address, client_port));
							Server.INSTANCE.println("Sent a returning QUIT message to client with id: " + quit_id);
							break;
						}
						Server.INSTANCE.println("Couldn't find client, allready disconnected.");
						break;
					default:
						bb = ByteBuffer.wrap(new byte[]{ bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5],
								bytes[6], bytes[7], bytes[8], bytes[9], bytes[10] }, 0, 11);
						if(!Checksum.isCorrect(bb.array()))
							throw new ChecksumException("The checksum of the package is not correct.");
						int default_id = bb.get(2) & 0xff;
						client = ToClient.Manager.hasClient(default_id);
						if(client == null){
							Server.INSTANCE.println("Received WRONG packet from client: "+client_address.getHostAddress() + ":"+client_port);
							byte[] notregBytes = PDU_Factory.notreg();
							socket.send(new DatagramPacket(notregBytes, notregBytes.length, client_address, client_port));
							Server.INSTANCE.println("Sent a returning NOTREG message to client with id: " + default_id);
							break;
						}
						
						String str = "NONE";
						switch (action) {
						case OpCodes.ACTION_DOWN:
							str = "ACTION_DOWN";
							break;
						case OpCodes.ACTION_UP:
							str = "ACTION_UP";
							break;
						case OpCodes.ACTION_MOVE:
							str = "ACTION_MOVE";
							break;
						default:
							str = "ACTION_DEFAULT";
							break;
						}
						
						int local_id = client.getId();
						bb.put(2, (byte) local_id);
						byte[] touchBytes = Checksum.recalcChecksum(bb.array());
						Server.INSTANCE.println("Received packet with action: " + str + " from client with id: " + local_id);
						ToClient.Manager.addPackage(ByteBuffer.wrap(touchBytes, 0, 11));
						//byte[] defaultBytes = Checksum.recalcChecksum(bb.array());
						//socket.send(new DatagramPacket(defaultBytes, defaultBytes.length, group_address, group_port));
						break;
					}
					
				} catch (SocketException e) {
					Server.INSTANCE.println("SocketException: " + e.getMessage());
					disconnect();
				} catch (IOException e) {
					Server.INSTANCE.println("IOException: " + e.getMessage());
				} catch (ChecksumException e) {
					e.printStackTrace();
					Server.INSTANCE.println("ChecksumException: " + e.getMessage());
				} catch (Exception e) {
					Server.INSTANCE.println("Exception: " + e.getMessage());
					e.printStackTrace();
				}
			}	

	}
	
	public class Sender extends Thread{
		
		public Sender(){
			super("Sender Thread");
		}
		
		
		@Override
		public void run(){
			ToClient[] clients = ToClient.Manager.getAllClientsAsArray();
			while(!stop){
				if(ToClient.Manager.isUpdated())
					clients = ToClient.Manager.getAllClientsAsArray();				
				ByteBuffer bb = null;				
				if(clients.length != 0 && (bb = ToClient.Manager.pollPackage()) != null ){
					Server.INSTANCE.println("Removed package from queue.");
					int sendingClient_id = bb.get(2) & 0xff;
					
					byte[] bytes = bb.array();
					
					int i = 0;
					for(; i < clients.length; i++){
						ToClient c = (ToClient) clients[i];
						if(sendingClient_id == c.getId())
							break;
						try {
							socket.send(new DatagramPacket(bytes, bytes.length, c.getAddress(), c.getPort()));
							Server.INSTANCE.println("Sent returning package to: " + c.getAddress().getHostAddress() + ":" + c.getPort());
						} catch (IOException e) {
							Server.INSTANCE.println("Couldn't send package!");
							e.printStackTrace();
						}
					}
					
					for(i++; i < clients.length; i++){
						ToClient c = (ToClient) clients[i];
						try {
							socket.send(new DatagramPacket(bytes, bytes.length, c.getAddress(), c.getPort()));
							Server.INSTANCE.println("Sent returning package to: " + c.getAddress().getHostAddress() + ":" + c.getPort());
						} catch (IOException e) {
							Server.INSTANCE.println("Couldn't send package!");
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
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
		this.interrupt();
		try {
			this.join();
		} catch (InterruptedException e) {
		}
		ToClient.Manager.destroy();
		this.socket.close();
	}
	
	private class ChecksumException extends Exception {
		
		private static final long serialVersionUID = -3123036363636946612L;

		public ChecksumException(String str){
			super(str);
		}
	}
}


