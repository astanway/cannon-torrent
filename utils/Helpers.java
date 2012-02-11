package utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class Helpers 
{
  //converts a byte buffer to a string
  public static String bufferToString(ByteBuffer buffer)
  {
    byte[] bufferBytes = new byte[buffer.capacity()];
    buffer.get(bufferBytes, 0, bufferBytes.length);
    String value = new String(bufferBytes);
    return value;
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
