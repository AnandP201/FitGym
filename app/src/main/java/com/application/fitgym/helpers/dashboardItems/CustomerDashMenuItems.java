package com.application.fitgym.helpers.dashboardItems;

import com.application.fitgym.R;

public class CustomerDashMenuItems {
    private String menu_name;
    private int drawable_resource_id;
    private String action;

    public CustomerDashMenuItems(String name, int id, String action){
        this.menu_name=name;
        this.drawable_resource_id=id;
        this.action=action;
    }

    public int getDrawable_resource_id() {
        return drawable_resource_id;
    }

    public String getMenu_name() {
        return menu_name;
    }

    public String getAction() {
        return action;
    }

    public static final CustomerDashMenuItems[]dashBoardItems={
            new CustomerDashMenuItems("View Peers",R.drawable.ic_baseline_people_24,"people"),
            new CustomerDashMenuItems("Daily Tasks",R.drawable.ic_baseline_note_add_24,"tasks"),
            new CustomerDashMenuItems("Gym Plans",R.drawable.ic_baseline_sticky_note_2_24,"plans"),
            new CustomerDashMenuItems("Payments&Bills",R.drawable.ic_baseline_attach_money_24,"bills")
    };
}
