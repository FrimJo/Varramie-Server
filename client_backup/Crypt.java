package com.spots.varramie;
/* Crypt.java
 * Given fil till laboration 2 - Distribuerad Chat p? kursen Datakommunikation
 * och Datorn?t C, 5p vid Ume? Universitet ht 2001 och vt 2002.
 * Av Per Nordlinder (per@cs.umu.se) och Jon Hollstr?m (jon@cs.umu.se)
 */

public class Crypt {

   /* Namn:       {en, de}crypt 
    * Purpose:    Krypterar eller dekrypterar data 
    * Argument:   src - Buffert med datan som ska behandlas 
    *             srclen - L?ngden i bytes p? src 
    *             key - Krypteringsnyckel som skall anv?ndas
    *             keylen - L?ngden i bytes p? krypteringsnyckeln
    * Returnerar: Ingenting 
    */ 
    
   public static void encrypt(byte[] src, int srclen, byte[] key, int keylen) {
      
      for(int i=0; i<srclen; i++) 
         src[i] ^= key[i%keylen]; 
   }
   
  	public static void decrypt(byte[] src, int srclen, byte[] key, int keylen) {
      encrypt(src, srclen, key, keylen);
  	}

}
