package com.varramie.spots.server;


/**
 * This is a container for a package which is to be
 * sent to all users except the one specified by the id
 * value.
 * 
 * If id equals "" then all connected clients will
 * receive this package.
 * */
public class PacketContainer {

	public final byte[] packetData;
	public final String id;
	
	/**
	 * The Constructor of this class, creates a package using
	 * an array of bytes and a id string.
	 * */
	public PacketContainer(final byte[] _packetData, final String _id) {
		packetData = _packetData;
		id = _id;
	}
	
}
