package com.application.fitgym.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.application.fitgym.R;
import com.application.fitgym.helpers.dashboardItems.CustomerDashMenuItems;

import java.util.List;

public class CustomersAdapter extends BaseAdapter {

    private Context context;
    private List<CustomerDashMenuItems> list;
    private LayoutInflater inflater;

    public CustomersAdapter(Context c,List<CustomerDashMenuItems> l){
        this.context=c;
        this.list=l;
    }


    @Override
    public int getCount() {
        return list.size();
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
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (view == null) {
            view = inflater.inflate(R.layout.dashboard_menu_cards, null);
        }
        ImageView imageView = view.findViewById(R.id.dashboard_menu_card_image);
        TextView menuTitleText = view.findViewById(R.id.dashboard_menu_card_action_text);

        imageView.setImageResource(list.get(i).getDrawable_resource_id());
        menuTitleText.setText(list.get(i).getMenu_name());

        return view;
    }

}
