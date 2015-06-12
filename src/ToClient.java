import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;


public class ToClient{

	
	private final InetAddress		_address;
	private final int				_port;
	private final int				_id;
	private boolean					_alive = true;
	
	public ToClient(final InetAddress address, final int port, final int id){
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
	
	public int getId(){
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

		private static final int MAX_CLIENTS = 255;
		
		private static final ToClient[] connectedClients = new ToClient[MAX_CLIENTS];
		private static int connectedClientsSize = 0;
		private static int _pointer = 0;
		
		//private static final HashMap<Key, ToClient>	connectedClients = new HashMap<Key,ToClient>();
		private static final Queue<ByteBuffer> packageQueue = new LinkedList<ByteBuffer>();
		private static boolean isUpdated = false;
		private static boolean end = false;
		private static final CleanerThread cleanerThread = new CleanerThread();

		public static ToClient createClient(final InetAddress client_address, final int client_port){
			ToClient client = new ToClient(client_address, client_port, _pointer);
			boolean success = addClient(client);
			return success ? client : null;
		}
		
		public static boolean addClient(ToClient client){
			int index = _pointer;
			synchronized (connectedClients) {
				
				do{
					try  {
						if(connectedClients[_pointer] == null){
							connectedClients[_pointer] = client;
							connectedClientsSize++;
							_pointer++;
							isUpdated = true;
							return true;
						}
						_pointer++;

					}catch(IndexOutOfBoundsException e){
						_pointer %= MAX_CLIENTS;
					}
				}while(_pointer != index);
			}
			return false;
		}
		
		public static ToClient hasClient(final int id){
			synchronized (connectedClients) {
				return connectedClients[id];
			}
		}
		
		public static ToClient hasClient(final InetAddress address, final int port){
			ToClient c;
			synchronized (connectedClients) {
				for(int i = 0; i < MAX_CLIENTS; i++){
					c = connectedClients[i];
					if(c != null && c.getPort() == port && c.getAddress().equals(address))
						return c;	
				}
			}
			return null;
		}
		
		public static ToClient getClient(final byte id){
			synchronized (connectedClients) {
				return connectedClients[id];	
			}
		}
		
		public static ToClient[] getAllClientsAsArray(){
			isUpdated = false;
			
			ToClient[] clients = new ToClient[connectedClientsSize];
			
			int index = 0;
			for(int i = 0; i < MAX_CLIENTS; i++){
				synchronized (connectedClients) {
					if(connectedClients[i] != null){
						clients[index] = connectedClients[i];
						index++;
					}
				}
				
			}
			 
			return clients;
		}
		
		public static boolean removeClientFromList(final int id){
			isUpdated = true;
			ToClient client = null;
			synchronized (connectedClients) {
				client = connectedClients[id];
			}
			if(client != null){
				connectedClients[id] = null;
				connectedClientsSize--;
				Server.INSTANCE.println("Removed client with id: " + id + " from the list with connected clients.");
				client.quit();
				return true;
			}
			return false;
		}

		public static void removeAllClientsFromList(){
			isUpdated = true;
			ToClient c = null;
			
			for(int i = 0; i < MAX_CLIENTS; i++){
				synchronized (connectedClients) {
					c = connectedClients[i];
					if(c != null){
						connectedClients[i] = null;
						c.quit();
					}
				}
			}
		}
		
		public static void addPackage(ByteBuffer bb){
			Server.INSTANCE.println("Added package to queue.");
			synchronized (packageQueue) {
				packageQueue.add(bb);	
			}
		}
		
		public static ByteBuffer pollPackage(){
			synchronized (packageQueue) {
				return packageQueue.poll();	
			}
		}
		
		public static int packageQueueSize(){
			synchronized (packageQueue) {
				return packageQueue.size();
			}
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
				while(!end){
					try {
						sleep(30000);
					} catch (InterruptedException e) {
						break;
					}
					synchronized (connectedClients) {

						for(int i = 0; i < MAX_CLIENTS; i++){
							ToClient c = connectedClients[i];
							if(c != null){
								if(c.isAlive())
									c.setAlive(false);
								else
									removeClientFromList(c.getId());
							}
						}
					}
				}
			}
		}
	}
}
