package com.omkar.bmcparkingclient.Model;

import android.app.Application;
import android.content.Context;

import java.io.Serializable;

/**
 * Created by omkar on 09-Mar-18.
 */

public class ParkingAttendant extends  Application {

    private static String userID;
    private static int userRole;
    private static int userPermission;

    public static String getUserID() {
        return userID;
    }

    public static void setUserID(String userID) {
        ParkingAttendant.userID = userID;
    }

    public static int getUserRole() {
        return userRole;
    }

    public static void setUserRole(int userRole) {
        ParkingAttendant.userRole = userRole;
    }

    public static int getUserPermission() {
        return userPermission;
    }

    public static void setUserPermission(int userPermission) {
        ParkingAttendant.userPermission = userPermission;
    }
}
