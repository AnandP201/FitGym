package com.application.fitgym.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.application.fitgym.R;
import com.application.fitgym.helpers.dashboardItems.AdminDashMenuItems;

import java.util.List;

public class AdminMenuAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context context;
    private List<AdminDashMenuItems> menuItems;

    public AdminMenuAdapter(Context c,List<AdminDashMenuItems> l){
        this.menuItems=l;
        this.context=c;
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(inflater==null){
            inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if(view==null){
            view=inflater.inflate(R.layout.dashboard_menu_cards,null);
        }

        ImageView imageView=view.findViewById(R.id.dashboard_menu_card_image);
        TextView menuTitleText=view.findViewById(R.id.dashboard_menu_card_action_text);

        imageView.setImageResource(menuItems.get(i).getDrawable_resource_id());
        menuTitleText.setText(menuItems.get(i).getMenu_name());

        return view;
    }


}
