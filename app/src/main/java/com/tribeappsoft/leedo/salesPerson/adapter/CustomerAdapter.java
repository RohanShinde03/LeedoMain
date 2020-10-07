package com.tribeappsoft.leedo.salesPerson.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.fontAwesome.FontAwesomeManager;
import com.tribeappsoft.leedo.models.leads.LeadStagesModel;

import java.util.ArrayList;

import androidx.appcompat.widget.AppCompatTextView;


public class CustomerAdapter extends ArrayAdapter<LeadStagesModel> {
    ArrayList<LeadStagesModel> customers, tempCustomer, suggestions;
    Context context;
    public CustomerAdapter(Context context, ArrayList<LeadStagesModel> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
        this.customers = objects;
        this.context = context;
        this.tempCustomer = new ArrayList<LeadStagesModel>(objects);
        this.suggestions = new ArrayList<LeadStagesModel>(objects);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LeadStagesModel stagesModel = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_spinner_city, parent, false);
        }
        AppCompatTextView tv_itemServices_dot = convertView.findViewById(R.id.tv_itemServices_dot);
        AppCompatTextView tv_spinner_item = convertView.findViewById(R.id.tv_spinner_item);
        if (tv_itemServices_dot != null) tv_itemServices_dot.setTypeface(FontAwesomeManager.getTypeface(context, FontAwesomeManager.FONTAWESOME));
        if (tv_spinner_item != null) tv_spinner_item.setText(stagesModel.getLead_stage_name());
        /*if (ivCustomerImage != null && stagesModel.getProfilePic() != -1)
            ivCustomerImage.setImageResource(stagesModel.getProfilePic());
        // Now assign alternate color for rows
        */
        if (position== 0) tv_itemServices_dot.setTextColor(context.getResources().getColor(R.color.colorhot));
        else  if (position== 1) tv_itemServices_dot.setTextColor(context.getResources().getColor(R.color.colorwarm));
        else  if (position== 2) tv_itemServices_dot.setTextColor(context.getResources().getColor(R.color.colorcold));
        else  if (position== 3)tv_itemServices_dot.setTextColor(context.getResources().getColor(R.color.colorni));
        else  if (position== 4)tv_itemServices_dot.setTextColor(context.getResources().getColor(R.color.color_lead_mismatch));
        else tv_itemServices_dot.setTextColor(context.getResources().getColor(R.color.BlackLight));

        return convertView;
    }


    @Override
    public Filter getFilter() {
        return myFilter;
    }

    Filter myFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            LeadStagesModel customer = (LeadStagesModel) resultValue;
            return customer.getLead_stage_name();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (LeadStagesModel people : tempCustomer) {
                    if (people.getLead_stage_name().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        suggestions.add(people);
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<LeadStagesModel> c = (ArrayList<LeadStagesModel>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (LeadStagesModel cust : c) {
                    add(cust);
                    notifyDataSetChanged();
                }
            }
        }
    };
}
