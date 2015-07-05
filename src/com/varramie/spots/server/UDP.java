package com.varramie.spots.server;

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
		
	protected Boolean				stop = false;
	
	private final String			IP = "224.0.0.3";
	
	protected DatagramSocket		socket;
	private InetAddress				group_address;
	private final int				group_port = 4446;
	private final Sender			senderThread = new Sender();

	
	
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
					byte[] byteArray;
					byte action = buffer[0];
					
					ToClient client = null;
					switch (action) {
					case OpCodes.JOIN:
						Server.INSTANCE.println("Received packet JOIN from: " + client_address.getHostAddress() + ":" + client_port);
						
						int join_id_length = bytes[2] & 0xff;
						
						bb = ByteBuffer.allocateDirect(3 + join_id_length);
						bb.put(bytes, 0, 3 + join_id_length);
						byteArray = new byte[3 + join_id_length];
						bb.position(0);
						bb.get(byteArray);
						
						if(!Checksum.isCorrect(byteArray))
							throw new ChecksumException("The checksum of the package is not correct.");
						
						byte[] join_id_array = new byte[join_id_length];
						bb.position(3);
						bb.get(join_id_array);
						String join_id = new String(join_id_array, "UTF-8");
						
						
						if(!ToClient.Manager.hasClient(join_id)){
							client = ToClient.Manager.createClient(join_id, client_address, client_port);
							Server.INSTANCE.println("Client width id: "+client.getId()+" connected from: " + client_address.getHostAddress() + ":" + client_port);
						}else
							client = ToClient.Manager.getClient(join_id, client_address, client_port);
						
						
						
						socket.send(new DatagramPacket(byteArray, byteArray.length, client_address, client_port));
						Server.INSTANCE.println("Sent a returning JOIN message to client with id: " + client.getId());

						break;
					case OpCodes.ALIVE:
						
						int alive_id_length = bytes[2] & 0xff;
						
						bb = ByteBuffer.allocateDirect(3 + alive_id_length);
						bb.put(bytes, 0, 3 + alive_id_length);
						byteArray = new byte[3 + alive_id_length];
						bb.position(0);
						bb.get(byteArray);
						
						if(!Checksum.isCorrect(byteArray))
							throw new ChecksumException("The checksum of the package is not correct.");
						
						byte[] alive_id_array = new byte[alive_id_length];
						bb.position(3);
						bb.get(alive_id_array);
						String alive_id = new String(alive_id_array, "UTF-8");
						
						// Server.INSTANCE.println("Received ALIVE message from client with id: "+alive_id);
						if(!ToClient.Manager.hasClient(alive_id)){
							byte[] notregBytes = PDU_Factory.notreg();
							socket.send(new DatagramPacket(notregBytes, notregBytes.length, client_address, client_port));
							// Server.INSTANCE.println("Sent a returning NOTREG message to client with id: " + alive_id);
							break;
						}
						client = ToClient.Manager.getClient(alive_id, client_address, client_port);
						client.setAlive(true);
						
						byte[] aliveBytes = PDU_Factory.alive(client.getId());
						socket.send(new DatagramPacket(aliveBytes, aliveBytes.length, client_address, client_port));
						Server.INSTANCE.println("Sent a returning ALIVE message to client with id: " + client.getId());
						break;
						
					case OpCodes.QUIT:
						Server.INSTANCE.println("Received packet QUIT.");
						int quit_id_length = bytes[2] & 0xff;
						
						bb = ByteBuffer.allocateDirect(3 + quit_id_length);
						bb.put(bytes, 0, 3 + quit_id_length);
						byteArray = new byte[3 + quit_id_length];
						bb.position(0);
						bb.get(byteArray);
						
						if(!Checksum.isCorrect(byteArray))
							throw new ChecksumException("The checksum of the package is not correct.");
						
						byte[] quit_id_array = new byte[quit_id_length];
						bb.position(3);
						bb.get(quit_id_array);
						String quit_id = new String(quit_id_array, "UTF-8");
						ToClient.Manager.removeClientFromList(quit_id);
						Server.INSTANCE.println("Client with id: "+quit_id+" disconnected");
						break;
					case OpCodes.COLLISION:
						Server.INSTANCE.println("Received packet COLISSION.");
						int collission_idA_length = bytes[2] & 0xff;
						int collission_idB_length = bytes[3] & 0xff;
						
						
						int totalLength = 12 + collission_idA_length + collission_idB_length;
						bb = ByteBuffer.allocateDirect(totalLength);
						bb.put(bytes, 0, totalLength);
						byteArray = new byte[totalLength];
						bb.position(0);
						bb.get(byteArray);
						
						if(!Checksum.isCorrect(byteArray))
							throw new ChecksumException("The checksum of the package is not correct.");
						
						byte[] collission_idA_array = new byte[collission_idA_length];
						byte[] collission_idB_array = new byte[collission_idB_length];
						
						bb.position(4);
						bb.get(collission_idA_array);
						bb.get(collission_idB_array);
						
						float pos_x = bb.getFloat();
						float pos_y = bb.getFloat();
						
						String collision_idA = new String(collission_idA_array, "UTF-8");
						String collision_idB = new String(collission_idB_array, "UTF-8");
						Server.INSTANCE.writeToDB(pos_x, pos_y, collision_idA, collision_idB, action);
						Server.INSTANCE.println("Clients with id: "+collision_idA+" and " + collision_idB + " collided at: ");
						break;
					default:
						
						int default_id_length = bytes[2] & 0xff;
						bb = ByteBuffer.allocateDirect(23 + default_id_length);
						bb.put(bytes, 0, 23 + default_id_length);
						byteArray = new byte[23 + default_id_length];
						bb.position(0);
						bb.get(byteArray);
						
						if(!Checksum.isCorrect(byteArray))
							throw new ChecksumException("The checksum of the package is not correct.");
						
						byte[] default_id_array = new byte[default_id_length];
						bb.position(3);
						bb.get(default_id_array);
						String default_id = new String(default_id_array, "UTF-8");
						
						if(!ToClient.Manager.hasClient(default_id)){
							byte[] notregBytes = PDU_Factory.notreg();
							socket.send(new DatagramPacket(notregBytes, notregBytes.length, client_address, client_port));
							break;
						}
						client = ToClient.Manager.getClient(default_id, client_address, client_port);
						//ToClient.Manager.addPackage(ByteBuffer.wrap(byteArray));
						ToClient.Manager.addPackage(new PacketContainer(byteArray, default_id));
						bb.position(3 + default_id_length);
						float x = bb.getFloat();
						float y = bb.getFloat();
						float pressure = bb.getFloat();
						float vel_x = bb.getFloat();
						float vel_y = bb.getFloat();
						
						Server.INSTANCE.writeToDB(x, y, pressure, action, default_id, vel_x, vel_y);
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
				}
			}	

	}
	
	public class Sender extends Thread{
		
		public Sender(){
			super("Sender Thread");
		}
		
		
		@Override
		public void run(){		
			Object[] clients;
			PacketContainer packet;
			try {
				while(!stop){
					clients = ToClient.Manager.getAllClientsAsArray();
					packet = ToClient.Manager.takePackage();
					int i = 0;
				
					ToClient c;
					for(; i < clients.length; i++){
						c = (ToClient) clients[i];
						if(packet.id.equals(c.getId()))
							break;
						try {
							socket.send(new DatagramPacket(packet.packetData, packet.packetData.length, c.getAddress(), c.getPort()));
							Server.INSTANCE.println("Sent message to " + c.getId());
						} catch (IOException e) {
							Server.INSTANCE.println("Couldn't send package!");
							e.printStackTrace();
						}
							
					}
					
					for(i++; i < clients.length; i++){
						c = (ToClient) clients[i];
						try {
							socket.send(new DatagramPacket(packet.packetData, packet.packetData.length, c.getAddress(), c.getPort()));
							Server.INSTANCE.println("Sent message to " + c.getId());
						} catch (IOException e) {
							Server.INSTANCE.println("Couldn't send package!");
							e.printStackTrace();
						}
					}
						
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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


