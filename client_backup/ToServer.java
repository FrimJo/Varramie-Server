package com.spots.varramie;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Time;

/**
 * This class handles all the connections of the clients towards the server.
 * @author Fredrik Johansson
 * @author Matias Edin
 *
 */
public class ToServer extends TCP{
	private static int			threadCounter = 0;
	
	/**
	 * This is the constructor of class ToServer witch sets up the ToServer
	 * for future connection.
	 * @param server_ip Represents the servers IP in string format.
	 * @param server_port The server port on the server in string format.
	 * @param topic A string with the server topic.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public ToServer(String server_ip, int server_port) throws UnknownHostException, IOException{
		super("Client thread " + threadCounter++, server_ip, server_port);
	}
	
	/**
	 * Connects the client to the serve with the preferred nickname.
	 * @param nick The preferred nickname.
	 * @throws UnknownHostException Throws when the super class TCP can't find the
	 * server.
	 * @throws IOException Is thrown when trying to connect or send to a server
	 * witch dosn't have an accept-socket open for incoming connection or the connection
	 * is down for sending packages. 
	 */
	public void connect() throws UnknownHostException, IOException{
		super.connect();
		//TODO Send join message  super.send(PDU_Factory.join(nick).getBytes());
	}

	/**
	 * Is called by the super class TCP when it receives a message from the server.
	 * After receiving the bytes this method checks the OpCode of the message and depending
	 * on the code it uses different cases to interpret the message act there after.
	 * @param bytes The bytes received from the connected server.
	 * @param bytesRead Number of bytes read.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations. 
	 */
	@Override
	public void receive(byte[] bytes, int bytesRead) throws IOException{
		try{
			PDU pdu = new PDU(bytes, bytesRead);

			/* Retrieves the OpCode from the received PDU. */
			switch(pdu.getByte(0)){
				case OpCodes.TOUCH:
					try {
						InetAddress client_ip = InetAddress.getByAddress(pdu.getSubrange(1, 4));
						int client_port = (int) pdu.getInt(5);
						int client_touchX = (int) pdu.getInt(9);
						int client_touchY = (int) pdu.getInt(13);
						Client.INSTANCE.println("Received a touch from " + client_ip.getHostAddress() + ":" + client_port + " at x: " + client_touchX + ", y: " + client_touchY);
						Client.INSTANCE.receiveTouch(client_touchX, client_touchY);
					} catch (UnknownHostException e) {
						Client.INSTANCE.println("UnknownHostException in ToClient");
						e.printStackTrace();
					}
					
					break;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
//			
//			/* OpCodes.NICKS: Is received when the client joins the chat-server.
//			 * It contains all nicknames of the current connected clients. */
//			case OpCodes.NICKS:
//				String nicks = new String ( pdu.getSubrange(4, pdu.getShort(2) ), "UTF-8" );
//				String[] nickList = PDU_Factory.removeZeros(nicks);
//				
//				Client.INSTANCE.setNick(nickList[0]);
//				this.nick = nickList[0];
//				
//				for(int i = 0; i < nickList.length; i++){
//					Client.INSTANCE.addUser(new User(nickList[i]));
//				}
//				
//				break;
//				
//			/*
//			 * OpCode.MESSAGE: Is received from the chat-server when a message has been, sent
//			 * from a client to the chat server. The message can have four different types.
//			 * 1. MsgType.TEXT: Just plain text.
//			 * 2. MsgType.COMP: Text compiled.
//			 * 3. MsgType.CRYPT: Encrypt text.
//			 * 4. MsgType.COMPCRYPT: Text witch is compiled then encrypted.
//			 * */
//			case OpCodes.MESSAGE:
//				int timeStamp = (int) pdu.getInt(8);
//				
//				/* Checks to see that the checksum is correct. */
//				if (PDU_Factory.checksum(pdu, 3)) {
//					String text = "";
//					
//					int msgType = pdu.getByte(1);
//					int nickLength = pdu.getByte(2);
//					int cryptCompByteLength = pdu.getShort(4);
//					
//					byte[] messageByteWithZeroes = pdu.getSubrange(12, cryptCompByteLength);
//					byte[] messageByte = PDU_Factory.removeZeros(messageByteWithZeroes);
//					byte[] nickname = pdu.getSubrange(12 + cryptCompByteLength, nickLength);
//					
//					/* MsgType.TEXT */
//					switch (msgType) {
//					case MsgTypes.TEXT:
//						text = new String(pdu.getSubrange(12, (int) pdu.getShort(4)), "UTF-8");
//						break;
//	
//					/* MsgType.COMP */
//					case MsgTypes.COMP:
//						byte[] compBytePDU = pdu.getSubrange(12, (int) pdu.getShort(4) );
//						PDU compPDU = new PDU( compBytePDU, compBytePDU.length );
//						
//						if(PDU_Factory.checksum(compPDU, 1)){
//							int compTextLength = compPDU.getShort(2);
//							byte[] compBytes = compPDU.getSubrange(8, compTextLength);
//							text = PDU_Factory.deCompress(compBytes);
//						} else {
//							Client.INSTANCE.println(timeStamp, "Message received but corupped from another user.");
//						}
//						break;
//						
//					/* MsgType.CRYPT */
//					case MsgTypes.CRYPT:
//						
//						byte[] cryptBytePDU = pdu.getSubrange(12, (int) pdu.getShort(4) );
//						PDU cryptPDU = new PDU( cryptBytePDU, cryptBytePDU.length );
//						
//						if(PDU_Factory.checksum(cryptPDU, 1)){
//							byte[] cryptKey = this.cryptKey;
//							if(this.cryptKey.length == 0){
//								cryptKey = new byte[]{' '};
//							}
//							int cryptTextLength = cryptPDU.getShort(2);
//							byte[] cryptBytes = cryptPDU.getSubrange(8, cryptTextLength);
//							Crypt.decrypt(cryptBytes, cryptBytes.length, cryptKey, cryptKey.length);
//							text = new String( cryptBytes, "UTF-8");
//						}
//						break;
//					
//					/* MsgType.COMPCRYPT */
//					case MsgTypes.COMPCRYPT:
//						
//						PDU cryptCompPDU = new PDU(messageByte, messageByte.length);
//						
//						int algoritm = cryptCompPDU.getByte(0);
//						
//						/* Makes sure that the checksum is correct and a known algorithm for encryption was used. */
//						if (PDU_Factory.checksum(cryptCompPDU, 1) && (algoritm == 0)) {
//					
//							cryptCompByteLength = cryptCompPDU.getShort(2);		
//							byte[] cryptCompByte = cryptCompPDU.getSubrange(8, cryptCompByteLength);
//							
//							byte[] cryptKey2 = this.cryptKey;
//							if(this.cryptKey.length == 0){
//								cryptKey2 = new byte[]{' '};
//							}
//							
//							Crypt.decrypt(cryptCompByte, cryptCompByte.length, cryptKey2, cryptKey2.length);
//							
//							PDU compPDU2 = new PDU(cryptCompByte, cryptCompByte.length);
//							int algoritm2 = compPDU2.getByte(0);
//							
//							/* Makes sure that the checksum is correct and a known algorithm for encryption was used. */
//							if (PDU_Factory.checksum(compPDU2, 1) && (algoritm2 == 0)) {
//								int compByteLength2 = compPDU2.getShort(2);
//								byte[] compBytes2 = compPDU2.getSubrange(8, compByteLength2);
//								
//								text = PDU_Factory.deCompress(compBytes2);
//							} else if(algoritm != 0){
//								Client.INSTANCE.println(timeStamp, "Can't decompress message, unknown algoritm!");
//							} else {
//								Client.INSTANCE.println(timeStamp, "Encrypted message, wrong key.");
//							}
//							
//						} else if(algoritm != 0) {
//							Client.INSTANCE.println("Can't decrypt message, unknown algoritm!");
//						} else {
//							System.err.println("Checksum mismatch, or crypt algoritm isn't zero");
//							Client.INSTANCE.println(timeStamp, "Message received but coruppt from another user.");
//						}			
//						break;
//						
//					default:
//						Client.INSTANCE.println("Cant interpret message from server, wrong message type.");
//						break;
//					}
//					
//					if (text.length() != 0) {
//						Client.INSTANCE.println(timeStamp , new String(nickname, "UTF-8") + ": " + text);
//					}
//					
//				}else {
//					Client.INSTANCE.println(timeStamp, "Message received from server but coruppt. Disconnect sugested.");
//				}
//	
//				break;
//				
//			/*
//			 * OpCode.UNIFO: Is received when a server send information about a
//			 * specific user.
//			 */
//			case OpCodes.UINFO:
//				String ip = InetAddress.getByAddress( pdu.getSubrange(4,4) ).getHostAddress();
//				Time t = new Time( (int) pdu.getInt(8) );
//				Client.INSTANCE.println( ip + " connected at " + t.toString()  );
//				break;
//				
//			/* OpCode.UJOIN: Is received when a user has joined the server. */
//			case OpCodes.UJOIN:
//				nick = new String( pdu.getSubrange(8, pdu.getByte(1)), "UTF-8");
//				
//				if( !nick.equals(this.nick) ) {
//					Client.INSTANCE.addUser(new User(nick) );
//					this.nr_clients++;
//				}
//				
//				Client.INSTANCE.println( (int) pdu.getInt(4),  nick + " has joined the chat.");
//				break;
//			
//			/* OpCode.ULEAV: Is received when a user leaves the chatroom. */
//			case OpCodes.ULEAVE:
//				nick = new String(pdu.getSubrange(8, pdu.getByte(1)), "UTF-8");
//				
//				Client.INSTANCE.removeUser(new User( nick ));
//				Client.INSTANCE.println((int) pdu.getInt(4) ,nick  + " left the server.");
//				this.nr_clients--;
//				break;
//				
//			/* OpCode.UCNICK: Is received when a user changes nickname. */
//			case OpCodes.UCNICK:
//				String nickOld = PDU_Factory.removeZeros(new String( pdu.getSubrange(8, pdu.getByte(1)), "UTF-8" ) )[0];
//				String nickNew = PDU_Factory.removeZeros(new String( pdu.getSubrange(8 + pdu.getByte(1), pdu.getByte(2)), "UTF-8" ) )[0];
//				
//				if(nickOld.equals(this.nick)){
//					this.nick = nickNew;
//					Client.INSTANCE.setNick(nickNew);
//				}
//				
//				Client.INSTANCE.updateUser(new User(nickNew), new User(nickOld));
//				Client.INSTANCE.println( (int) pdu.getInt(4) , nickOld + " changed name to " + nickNew);
//				break;
//			
//			/* OpCode.CHTOPIC: Is received when the topic has bin changed. */
//			case OpCodes.CHTOPIC:
//				this.topic = new String( pdu.getSubrange(4, pdu.getByte(1)), "UTF-8" );
//				Client.INSTANCE.setTopic(this.topic);
//				break;
//			}
//		} catch (ArrayIndexOutOfBoundsException e){
//			Client.INSTANCE.println("Messag ereceived is corup.");
//		}
	}
	
	/**
	 * Receives a PDU and sends it through the super class TCP to the server.
	 * @param pdu The PDU to send.
	 * @throws SocketException Thrown to indicate that there is an error creating or accessing a Socket.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void sendPDU(PDU pdu) throws SocketException, IOException {
		if(this.socket != null){
			super.send(pdu.getBytes());
		}else{
			throw new SocketException();
		}
	}
	
	
	/**
	 * Sends a message to the server.
	 * @param type Type of message.
	 * @param text The text being sent.
	 * @throws SocketException Thrown to indicate that there is an error creating or accessing a Socket.
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 * @throws WrongCryptTypeException Thrown when a cryptype is received with is not recognized. 
	 */
	public void sendMessage(int type, String text) throws SocketException, IOException, WrongCryptTypeException{
		sendPDU(PDU_Factory.message(type, text) );
	}
	
	public void sendTouch(final float x, final float y) throws SocketException, IOException {
		sendPDU(PDU_Factory.touch(this.socket.getInetAddress(), this.socket.getPort(), x, y));
	}
	
	/**
	 * Indicates whether some other object is "equal to" this one.
	 */
	@Override
	public boolean equals(Object o){
		if(o == null)
			return false;
		
		if(o.getClass() != this.getClass())
			return false;
		
		ToServer s = (ToServer) o;
		
		if(!s.toString().equals(this.toString()))
			return false;
		
		return true;
	}
	
	/**
	 * Returns a string representation of this thread, including the thread's name, priority, and thread group
	 */
	@Override
	public String toString(){
		return "(" + this.getName() + ") ";
	}
	
	/**
	 * Disconnects the client from this server-connection.
	 */
	@Override
	public void disconnect(){
		if(this.socket != null){
			try {
				//super.send(PDU_Factory.quit().getBytes());
				super.socket.close();
			} catch (IOException e) {
				disconnectTCP();
			}
			
		}		
	}
	
	/**
	 * Called from the super class TCP when the server-connection is lost.
	 */
	@Override
	public void disconnectTCP() {
		Client.INSTANCE.println("Lost connection with server.");
		super.stop = true;
		try {
			super.socket.close();
		} catch (IOException e) {
			//Catches the exception but does nothing.
		}
	}
}

