package com.spots.varramie;

/**
 * Thrown to indicate that it has ben used a unknown MsgType.
 * The known Msg.Types is as follows:
 * MsgType.TEXT
 * MsgType.COMP
 * MsgType.CRYPT
 * MsgType.COMPCRYPT
 * @author Fredrik Johansson
 * @author Mattias Edin
 *
 */
public class WrongCryptTypeException extends Exception{
	
	private static final long serialVersionUID = 3572705553528089291L;
	
	/**
	 * Constructs a new exception with null as its detail message.
	 * The cause is not initialized, and may subsequently be initialized by a call
	 * to initCause.
	 */
	public WrongCryptTypeException(){
		super();
	}
	
	/**
	 * Constructs a new exception with null as its detail message.
	 * The cause is not initialized, and may subsequently be initialized by a call
	 * to initCause.
	 * @param message message the detail message. The detail message is saved for later retrieval by the getMessage() method.
	 */
	public WrongCryptTypeException(String message){
		super(message);
	}
}
