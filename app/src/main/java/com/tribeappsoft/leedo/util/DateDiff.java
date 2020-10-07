package com.tribeappsoft.leedo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
 * Created by ${ROHAN} on 11/2/19.
 */
public class DateDiff
{

    public static void compareDates(String d1,String d2)
    {
        try{
            // If you already have date objects then skip 1

            //1
            // Create 2 dates starts
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date1 = sdf.parse(d1);
            Date date2 = sdf.parse(d2);

            System.out.println("Date1"+sdf.format(date1));
            System.out.println("Date2"+sdf.format(date2));System.out.println();

            // Create 2 dates ends
            //1

            // Date object is having 3 methods namely after,before and equals for comparing

            // after() will return true if and only if date1 is after date 2
            if(date1.after(date2)){
                System.out.println("Date1 is after Date2");
            }

            // before() will return true if and only if date1 is before date2
            if(date1.before(date2)){
                System.out.println("Date1 is before Date2");
            }

            //equals() returns true if both the dates are equal
            if(date1.equals(date2)){
                System.out.println("Date1 is equal Date2");
            }

            System.out.println();
        }
        catch(ParseException ex){
            ex.printStackTrace();
        }
    }

    public static void compareDates(Date date1, Date date2)
    {
        // if you already have date objects then skip 1
        //1

        //1

        //date object is having 3 methods namely after,before and equals for comparing

        //after() will return true if and only if date1 is after date 2
        if(date1.after(date2)){
            System.out.println("Date1 is after Date2");
        }

        //before() will return true if and only if date1 is before date2
        if(date1.before(date2)){
            System.out.println("Date1 is before Date2");
        }

        //equals() returns true if both the dates are equal
        if(date1.equals(date2)){
            System.out.println("Date1 is equal Date2");
        }

        System.out.println();
    }

}
