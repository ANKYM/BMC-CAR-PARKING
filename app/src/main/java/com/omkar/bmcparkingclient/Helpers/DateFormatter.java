package com.omkar.bmcparkingclient.Helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

    public static String returnDate(String inputString) {
        Date date = null;
        DateFormat inFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
        DateFormat outFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
        try {
            date = inFormat.parse(inputString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if( date != null ) {
            String myDate = outFormat.format(date);
            return myDate;
        }else
        {
            return inputString;
        }
    }
}
