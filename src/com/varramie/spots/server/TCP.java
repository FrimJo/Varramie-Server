package com.varramie.spots.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public abstract class TCP extends Thread{
	
	protected boolean				stop = false;
	protected String				server_ip;
	protected int					server_port;
	protected Socket				socket;
	protected DataOutputStream		outStream;
	protected InputStream			inStream;
	protected Thread				waitThread;
	protected int 					id;
		
	/**
	 * This constructor will be run by the server.
	 * @param thread_name
	 * @param socket
	 */
	public TCP(String thread_name, Socket socket, int count){
		super(thread_name + "" + count);
		this.id = count;
		this.socket = socket;
		this.server_ip = socket.getInetAddress().getHostAddress();
		this.server_port = socket.getPort();
	}
	
	/**
	 * This will be run by the server and client. 
	 * @throws UnknownHostException if the IP address of the host could not be determined. 
	 * @throws IOException if an I/O error occurs when creating the socket. 
	 */
	public void connect() throws UnknownHostException, IOException{
		
		/* If Client runs this method, it will add socket.
		 * If Server runs this method, it will not add socket. */
		if(this.socket == null || this.socket.isClosed()){
			this.socket = new Socket(this.server_ip, this.server_port);	
		}
		/* Both client and server need input-, output-streams. */
		this.outStream = new DataOutputStream(this.socket.getOutputStream());
		this.inStream = this.socket.getInputStream();
		
		start();
	}
	
	/**
	 * After this method is called the class will start reading from the socket..
	 */
	@Override
	public void run(){	
		byte[] streamBuffer = new byte[65535];
		int bytesRead;
		while(!stop){
			try {
				if( (bytesRead = this.inStream.read(streamBuffer) ) != -1 ){
					receive(streamBuffer, bytesRead);	
				}else{
					disconnectTCP();
				}
			} catch (IOException e) {
				disconnectTCP();
			}
		}
	}
	
	/**
	 * This method will be called only by this class run() method and should not be called anywhere . 
	 * @param bytes The bytes read from the socket.
	 * @param bytesRead Number of bytes read.
	 * @throws IOException If the bytes can somehow not be used, this will disconnect the extended class.
	 * @throws Thrown to indicate that an array has been accessed with an illegal index. The index is either negative or greater than or equal to the size of the array.
	 */
	protected abstract void receive(byte[] bytes, int bytesRead) throws IOException, ArrayIndexOutOfBoundsException;
	
	/**
	 * This method will be called if the connection should be closed.
	 */
	public abstract void disconnect();
	
	/**
	 * This method will be called if the server-connection is lost.
	 */
	protected abstract void disconnectTCP();
	
	/**
	 * Writes bytes to the socket.
	 * @param bytes
	 * @throws IOException If there was a problem sending the bytes.
	 */
	public void send(byte[] bytes) throws IOException{
		this.outStream.write(bytes); 
	}
	
	/**
	 * Gets the IP of the socket.
	 * @return IP
	 */
	protected InetAddress getInetAddress(){
		return this.socket.getInetAddress();
	}
	
	/**
	 * Gets the Port of the socket
	 * @return Port
	 */
	protected int getPort(){
		return this.socket.getPort();
	}
}
