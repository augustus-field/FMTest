package com.sgfm.api.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeTest {
    public static void main(String... args) throws ParseException {  
        
        Calendar calInitial = Calendar.getInstance();  
        int offsetInitial = calInitial.get(Calendar.ZONE_OFFSET)  
                + calInitial.get(Calendar.DST_OFFSET);  
  
        long current = System.currentTimeMillis();  
          
        // Check right time  
        Date utcTimeNow = new Date(current - offsetInitial);  
  
        System.out.println("UTC current " + current);  
        System.out.println("UTC offsetInitial " + offsetInitial);  
          
        System.out.println("UTC now " + utcTimeNow);  
        System.out.println("Date System.currentTimeMillis() " + new Date(current));  
        System.out.println("Date current " + new Date());  
        
        System.out.println(new Date(System.currentTimeMillis()));
        System.out.println(new Date().toGMTString());
        System.out.println(new Date().toLocaleString());
        System.out.println(new Date().toString());
        
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    	System.out.println("Date output from long"+sdf.format(current));
    	Date dateFromCurrent = new Date(current);
    	System.out.println("Date output from new Date(long)"+sdf.format(dateFromCurrent.getTime()));
    	Date dateFromSdf = sdf.parse(sdf.format(current));
    	System.out.println("Date output from sdf parse: "+dateFromSdf);

    }  
}
