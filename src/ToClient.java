

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ToClient{
	
	private static int				idCounter = 0;

	private final int				id;
	private final InetAddress		ip;
	private final int				port;
	private final Queue<PDU> 		packageList = new LinkedList<PDU>();
	private final Worker			worker = new Worker();
	private final ClientReceiver	sender;
	
	private boolean 				stop = false; 
	
	
	
	public ToClient(final InetAddress ip, final int port, final ClientReceiver sender){
		this.id = idCounter++; 
		this.ip = ip;
		this.port = port;
		this.worker.start();
		this.sender = sender;
		Manager.putClientToList(this);
		
	}
	
	private synchronized void receivePackage(final PDU pdu){
		this.packageList.add(pdu);
	}
	
	public int getId(){
		return this.id;
	}
	
	
	public void quit(){
		this.stop = true;
	}
	/**
	 * The workers job is to get packages from the packageList, and
	 * send it.
	 * */
	private class Worker extends Thread{
		
		@Override
		public void run(){
			while(!stop){
				PDU pdu = packageList.poll();
				if(pdu != null)
					sender.sendPDU(pdu, ip, port);
			}
		}
		
	}
	
	/**
	 * The manager keeps tracks of all the clients and can
	 * give Clients packages.
	 * */
	public static class Manager{

		private static final ArrayList<ToClient>	connectedClients = new ArrayList<ToClient>();
		
		private static void putClientToList(final ToClient client){
			synchronized (connectedClients) {
				connectedClients.add(client);
			}
		}
		
		public static void removeClientFromList(final int client_id){
			ToClient[] array;
			synchronized (connectedClients) {
				array = (ToClient[]) connectedClients.toArray();
			}
			for(int i = 0; i < array.length; i++){
				if(array[i].getId() == client_id){
					connectedClients.remove(i);
					array[i].quit();
					break;
				}
			}
		}
		
		public static void removeAllClientsFromList(){
			synchronized (connectedClients) {
				connectedClients.clear();
			}
		}
		
		public static void giveClientPackage(final int client_id, final PDU pdu){
			ToClient[] array;
			synchronized (connectedClients) {
				array = (ToClient[]) connectedClients.toArray();
			}
			int i;
			for(i = 0; i < array.length; i++){
				if(array[i].getId() == client_id)
					break;
				array[i].receivePackage(pdu);
			}
			for(i++; i < array.length; i++){
				array[i].receivePackage(pdu);
			}
		}
	}
}
