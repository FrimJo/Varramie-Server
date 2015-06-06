package com.spots.varramie;
/* Checksum.java
 * Given fil till laboration 2 - Distribuerad Chat p? kursen Datakommunikation
 * och Datorn?t C, 5p vid Ume? Universitet ht 2001 och vt 2002
 * Av Per Nordlinder (per@cs.umu.se) och Jon Hollstr?m (jon@cs.umu.se)
 */

public class Checksum {

  /* Namn: calc
   * Syfte: Ber?knar checksumma p? en byte-array.
   * Argument: buf   - Datat som checksumman skall ber?knas p?.
   *           count - Det antal bytes som checksumman skall ber?knas p?.
   * Returnerar: checksumman som en byte.
   */
   public static byte calc(byte[] buf, int count) {
      int sum = 0;
      int i = 0;

      while((count--) != 0) {
         sum += (buf[i] & 0x000000FF);
         i++;
         if((sum & 0x00000100) != 0) {
            sum &= 0x000000FF;
            sum++;
         }     
      }

      return (byte)~(sum & 0xFF);
   }
}

