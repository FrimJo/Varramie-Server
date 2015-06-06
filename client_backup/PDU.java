package com.spots.varramie;

/**
 * A wrapper around a byte array which supports unsigned integers.
 * 
 * Since Java lacks support for true unsigned integers some conversion has
 * to be made before sending them to a system that expects unsigned values.
 * Non-integer data types are not directly supported but can be used by
 * calling getSubrange() and setSubrange() with the byte representation.
 *
 * There is one important thing to know when using this class. When you set
 * or get a value you need to store that in a variable that is at least one
 * step larger than the data type you use when you store it.
 *
 * See the following example where we create a two byte PDU and store a
 * value. We then retrieve the value again and prints it to the console.
 * Note that your PDU may probably be larger than two bytes and you may
 * want to store more than just one value. Use the offset argument when
 * setting values in your PDU in order to put them where you want them.
 * 
 * int myValue = 32768;
 * PDU pdu = new PDU(2);
 * pdu.setShort(0, (short) myValue);
 * int myOtherValue = pdu.getShort(0);
 * System.out.println(myOtherValue);
 *
 * 32768 is too large for Java to keep in a short, so you need an int in
 * order to use it. However, ints in Java are 4 bytes and we want to store
 * it as an unsigned short using only two bytes. That's what the PDU class
 * can do for us.
 *
 * Once we want the data back from the PDU we need to store it as an int 
 * again. This is important to know because the PDU class won't give you
 * unsigned types, but the representation of the data stored in the PDU
 * is unsigned so it can be extracted and sent as it is to something that
 * expects unsigned values.
 * 
 * Changelog:
 * 
 * 2010-10-01: Marcus Karlsson (marcusk@cs.umu.se)
 *             Original version.
 */

public class PDU
{
    private byte[] rawData;
    
    /**
     * Creates a new PDU with the specified length
     * @param length size of PDU measured in bytes
     */
    public PDU(int length)
    {
        this.rawData = new byte[length];
    }

    /**
     * Creates and initializes a new PDU with the specified length and
     * initial data content. 
     * @param rawData initial data used by PDU
     * @param length size of PDU measured in bytes
     */
    public PDU(byte[] rawData, int length)
    {
        this(length);
        System.arraycopy(rawData, 0, this.rawData, 0, length);
    }
    
    /**
     * Reads a byte at the given offset
     * @param offset a non-negative number of bytes that specifies where
     *    the byte begins
     * @return the byte at the given offset encapsulated in a short
     */
    public short getByte(int offset)
    {
        return (short) (this.rawData[offset] & 0xFF);
    }
    
    /**
     * Writes a byte at the given offset
     * @param offset a non-negative number of bytes that specifies where
     *    to write
     * @param theByte the byte that should be written
     */
    public void setByte(int offset, byte theByte)
    {
        this.rawData[offset] = theByte;
    }
    
    /**
     * Reads a short at the given offset
     * @param offset a non-negative number of bytes that specifies where
     *    the short begins
     * @return the short at the given offset encapsulated in an int
     */
    public int getShort(int offset)
    {
        int b0 = this.rawData[offset]   & 0xFF;
        int b1 = this.rawData[offset+1] & 0xFF;
        
        return b0 << 010 | b1;
    }
    
    /**
     * Writes a short at the given offset
     * @param offset a non-negative number of bytes that specifies where 
     *    to write
     * @param theShort the short that should be written
     */
    public void setShort(int offset, short theShort)
    {
        this.rawData[offset]   = (byte) ((theShort & 0xFF00) >> 010);
        this.rawData[offset+1] = (byte)  (theShort & 0x00FF);
    }
    
    /**
     * Reads an int at the given offset
     * @param offset a non-negative number of bytes that specifies where
     *      the int begins
     * @return the int at the given offset encapsulated in a long
     */
    public long getInt(int offset)
    {
        long b0 = this.rawData[offset]   & 0xFF;
        long b1 = this.rawData[offset+1] & 0xFF;
        long b2 = this.rawData[offset+2] & 0xFF;
        long b3 = this.rawData[offset+3] & 0xFF;
        
        return b0 << 030 | b1 << 020 | b2 << 010 | b3;
    }
    
    /**
     * Writes an int at the given offset
     * @param offset a non-negative number of bytes that specifies where 
     *    to write
     * @param theInt the int that should be written
     */
    public void setInt(int offset, int theInt)
    {
        this.rawData[offset]   = (byte) ((theInt & 0xFF000000) >> 030);
        this.rawData[offset+1] = (byte) ((theInt & 0x00FF0000) >> 020);
        this.rawData[offset+2] = (byte) ((theInt & 0x0000FF00) >> 010);
        this.rawData[offset+3] = (byte)  (theInt & 0x000000FF);
    }
    
    /**
     * Reads a byte array at the given subrange
     * @param offset a non-negative number of bytes that specifies where
     *    the subrange begins
     * @param length a non-negative number of bytes to read
     * @return the subrange at the given offset
     */
    public byte[] getSubrange(int offset, int length)
    {
        byte[] subrange = new byte[length];
        System.arraycopy(this.rawData, offset, subrange, 0, length);
        
        return subrange;
    }

    /**
     * Writes a byte array at the given subrange
     * @param offset a non-negative number of bytes that specifies where 
     *    to write
     * @param subrange the byte array that should be written
     */
    public void setSubrange(int offset, byte[] subrange)
    {
        System.arraycopy(subrange, 0, this.rawData, offset,
            subrange.length);
    }
    
    /**
     * Returns the size of the PDU in bytes
     * @return size of PDU in bytes
     */
    public int length()
    {
        return this.rawData.length;
    }

    /**
     * Extends the size of the PDU
     * @param length a non-negative new size of the PDU in bytes
     */
    public void extendTo(int length)
    {
        byte[] newArray = new byte[length];
        length = (length < this.rawData.length) ? length :
            this.rawData.length;
        
        System.arraycopy(this.rawData, 0, newArray, 0, length);
        this.rawData = newArray;
    }
    
    /**
     * Returns the actual bytes stored by the entire PDU
     * @return all bytes stored by the PDU
     */
    public byte[] getBytes()
    {
        byte[] newArray = new byte[this.rawData.length];
        System.arraycopy(this.rawData, 0, newArray, 0, this.rawData.length);
        
        return newArray;
    }
}
