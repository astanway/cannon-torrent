package utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class Helpers 
{
  public static String bufferToString(ByteBuffer buffer)
  {
    byte[] bufferBytes = new byte[buffer.capacity()];
    buffer.get(bufferBytes, 0, bufferBytes.length);
    String value = new String(bufferBytes);
    return value;
  }
  
  public static int bufferToInt(ByteBuffer buffer)
  {
    byte[] bufferBytes = new byte[buffer.capacity()];
    buffer.get(bufferBytes, 0, bufferBytes.length);
    int value = 0;
    for (int i = 0; i < 4; i++) {
       int shift = (4 - 1 - i) * 8;
       value += (bufferBytes[i] & 0x000000FF) << shift;
    }
    return value;
  }
  
  public static void printBytes(byte[] bytes){
    String value = new String(bytes);
    System.out.print(value);
  }
  
  public static final char[] HEX_CHARS = 
   {'0','1','2','3','4','5','6','7',
    '8','9','A','B','C','D','E','F'};

  public static String toURLHex(byte[] bytes){
     StringBuffer sb = new StringBuffer();
     for(int i = 0; i < bytes.length; ++i){
      sb.append('%')
       .append(HEX_CHARS[(bytes[i]>>4&0x0F)])
       .append(HEX_CHARS[(bytes[i]&0x0F)]);
     }
     return sb.toString();
  }  
}
