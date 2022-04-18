package com.application.fitgym.helpers.dashboardItems;

import com.application.fitgym.R;

public class AdminDashMenuItems {
    private String menu_name;
    private int drawable_resource_id;
    private String action;

    public AdminDashMenuItems(String name,int id,String action){
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

    public static final AdminDashMenuItems []dashBoardItems={
            new AdminDashMenuItems("New registrations", R.drawable.ic_chat_add,"customers"),
            new AdminDashMenuItems("Plans Management",R.drawable.ic_stationery,"plans"),
            new AdminDashMenuItems("Manage customers",R.drawable.ic_staff,"manage"),
            new AdminDashMenuItems("Inventory Management",R.drawable.ic_calculator_money,"money")
    };
}
