package utils;

import java.util.Comparator;

public class StringComparator implements Comparator{

    public int compare(Object o1, Object o2)
    {
        String s1 = (String)o1;
        String s2 = (String)o2;
        
        s1 = s1.replaceAll("\\s","");
        s2 = s2.replaceAll("\\s","");
        
        int i1 = Integer.parseInt(s1);
        int i2 = Integer.parseInt(s2);
        int res = 0;
        
        if(i1 > i2){
          res = -1;
        }
        
        if(i1 == i2){
          res = 0;
        }
        
        if(i1 > i2){
          res = 1;
        }
        
        return res;
    }
}
