package com.instructure.loginapi.login.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.instructure.canvasapi2.models.AccountDomain;
import com.instructure.loginapi.login.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountAdapter extends BaseAdapter implements Filterable {

    private LayoutInflater layoutInflater;
    private Activity activity;
    private List<AccountDomain> locationAccounts = new ArrayList<>();
    private List<AccountDomain> originalAccounts = new ArrayList<>();
    private List<AccountDomain> displayAccounts = new ArrayList<>();
    private Filter filter;

    public AccountAdapter(Activity activity, List<AccountDomain> originalAccounts, ArrayList<AccountDomain> locationAccounts) {
        this.activity = activity;
        this.layoutInflater = activity.getLayoutInflater();
        this.originalAccounts = originalAccounts;
        this.locationAccounts = locationAccounts;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public int getCount() {
        if(displayAccounts == null) {
            return 0;
        }
        return displayAccounts.size();
    }

    @Override
    public Object getItem(int position) {
        return displayAccounts.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.accounts_adapter_item, null);

            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.distance = (TextView) convertView.findViewById(R.id.distance);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        final AccountDomain account = displayAccounts.get(position);
        final Double distance = account.getDistance();

        viewHolder.name.setText(account.getName());

        if(distance == null) {
            viewHolder.distance.setText("");
            viewHolder.distance.setVisibility(View.GONE);
        } else {
            //distance is in meters
            //We calculate this in miles because the USA is dumb and still uses miles
            //At the time of this most of our customers are in the USA
            double distanceInMiles = (distance* 0.000621371192237334);
            double distanceInKm = distance / 1000;
            String distanceText = "";
            if(distanceInMiles < 1) {
                distanceText = activity.getString(R.string.lessThanOne);
            } else if(distanceInMiles - 1 < .1) {
                //we're 1 mile away
                distanceText = activity.getString(R.string.oneMile);
            } else {
                distanceText = String.format("%.1f", distanceInMiles) + " " + activity.getString(R.string.miles) + ", " + String.format("%.1f", distanceInKm) + " " + activity.getString(R.string.kilometers);
            }
            viewHolder.distance.setText(distanceText);
            viewHolder.distance.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if(filter == null) {
            filter =  new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    final Locale locale = Locale.getDefault();
                    final String toMatch = constraint.toString().toLowerCase(locale);
                    final FilterResults filterResults = new FilterResults();

                    if(!TextUtils.isEmpty(toMatch)) {
                        ArrayList<AccountDomain> accountContains = new ArrayList<>();
                        for(AccountDomain account : originalAccounts) {

                            if(account.getName().toLowerCase(locale).contains(toMatch) ||
                                    toMatch.contains(account.getName().toLowerCase(locale)) ||
                                    account.getDomain().toLowerCase(locale).contains(toMatch) ||
                                    toMatch.contains(account.getDomain().toLowerCase(locale))) {
                                accountContains.add(account);
                            }
                        }

                        filterResults.count = accountContains.size();
                        filterResults.values = accountContains;

                    } else {
                        filterResults.count = locationAccounts.size();
                        filterResults.values = locationAccounts;
                    }

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    displayAccounts = (ArrayList<AccountDomain>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
        return filter;
    }

    public static class ViewHolder {
        TextView name;
        TextView distance;
    }
}
