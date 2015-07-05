package com.varramie.spots.server;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;


public class ToClient{

	private InetAddress				_address;
	private int						_port;
	private final String			_id;
	private boolean					_alive = true;
	
	public ToClient(final InetAddress address, final int port, final String id){
		_address = address;
		_port = port;
		_id = id;
	}
	
	
	public InetAddress getAddress(){
		return _address;
	}
	
	public int getPort(){
		return _port;
	}
	
	public String getId(){
		return _id;
	}
	
	public void quit(){
		//TODO Implement this method
	}
	
	public void setAlive(boolean val){
		_alive = val;
	}
	
	public boolean isAlive(){
		return _alive;
	}
	
	/**
	 * The manager keeps tracks of all the clients.
	 * */
	public static class Manager{
		
		private static final 	ConcurrentHashMap<String,ToClient>	connectedClients	=	new ConcurrentHashMap<String, ToClient>();
		private static final	LinkedBlockingDeque<PacketContainer> 	packageQueue		=	new LinkedBlockingDeque<>();
		
		
		private static 			boolean				isUpdated 			= 		false;
		private static 			boolean				end 				= 		false;
		private static final 	CleanerThread		cleanerThread 		= 		new CleanerThread();
	

		public static ToClient createClient(final String id, final InetAddress client_address, final int client_port){
			ToClient client = new ToClient(client_address, client_port, id);
			addClient(client);
			return client;
		}
		
		public static void addClient(ToClient client){
			isUpdated = true;
			connectedClients.put(client.getId(), client);
		}
		
		public static boolean hasClient(final String id){
			return connectedClients.containsKey(id);
		}
		/*
		public static ToClient hasClient(final String id){
			Object[] array;
			synchronized (connectedClients) {
				array = connectedClients.values().toArray();
			}
			String remote_address;
			if(array.length > 0){
				remote_address = address.getHostAddress();
				String local_address;
				for(Object o : array){
					ToClient c = (ToClient) o;
					local_address = c.getAddress().getHostAddress();
					if(c.getPort() == port && remote_address.equals(local_address))
						return c;
				}
			}
			return null;
		}*/
		
		public static ToClient getClient(final String id){
			return connectedClients.get(id);
		}
		
		public static ToClient getClient(final String id, final InetAddress client_address, final int client_port){
			ToClient c = connectedClients.get(id);
			c._address = client_address;
			c._port = client_port;
			return 	c;
		}
		
		public static Object[] getAllClientsAsArray(){
			isUpdated = false;
			return connectedClients.values().toArray();
		}
		
		public static boolean removeClientFromList(final String id){
			isUpdated = true;
			ToClient client = connectedClients.remove(id);
			Server.INSTANCE.println("Removed client with id: " + id + " from the list with connected clients.");
			if(client != null)
				client.quit();
			return client != null;
		}

		public static void removeAllClientsFromList(){
			isUpdated = true;
			connectedClients.clear();
		}
		
		public static void addPackage(PacketContainer packet){
			packageQueue.add(packet);	
		}
		
		public static PacketContainer takePackage() throws InterruptedException{
			return packageQueue.take();	
		}
		
		public static int packageQueueSize(){
			return packageQueue.size();
		}
		
		public synchronized static boolean isUpdated(){
			return isUpdated;
		}
		
		public static void destroy(){
			end = true;
			cleanerThread.interrupt();
		}
		
		public static void startCleaner(){
			cleanerThread.start();
		}
		
		private static class CleanerThread extends Thread{
			
			public CleanerThread(){
				super("Cleaner Thread");
			}
			
			@Override
			public void run(){
				Object[] array;
				try {
					while(!end){
						sleep(30000);
						array = connectedClients.values().toArray();
							
						for(Object o : array){
							ToClient c = (ToClient) o;
							if(c.isAlive())
								c.setAlive(false);
							else
								removeClientFromList(c.getId());
						}
					}
				} catch (InterruptedException e) {
					
				}
				Server.INSTANCE.println("Cleaner Thread stoped.");
			}
		}
	}
}
