package com.omkar.bmcparkingclient.Activity;

import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.omkar.bmcparkingclient.Model.ParkingAttendant;
import com.omkar.bmcparkingclient.R;

public class DashBoardActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener,AHBottomNavigation.OnNavigationPositionListener {
    String UserId;
    int UserPermission;
    int UserRole;
    String userLotId;
    AHBottomNavigation bottomNavigation;
    AHBottomNavigationViewPager view_pager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        view_pager = (AHBottomNavigationViewPager) findViewById(R.id.view_pager);



        UserId = ParkingAttendant.getUserID();
        UserPermission = ParkingAttendant.getUserPermission();
        UserRole = ParkingAttendant.getUserRole();
        userLotId = ParkingAttendant.getUserLotId();


        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.title_tab_home, R.drawable.ic_home_black_24dp, R.color.colorAccent);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.title_tab_Read, R.drawable.ic_menu_read, R.color.colorAccent);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.title_tab_Write, R.drawable.ic_menu_write, R.color.colorAccent);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.title_tab_Reset, R.drawable.ic_menu_reset, R.color.colorAccent);

        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);
        bottomNavigation.setCurrentItem(1);




    }

    @Override
    public void onPositionChange(int y) {

    }

    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {
        return false;
    }
}
