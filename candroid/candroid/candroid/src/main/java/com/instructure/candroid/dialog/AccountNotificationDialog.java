/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.candroid.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.candroid.R;
import com.instructure.candroid.activity.InternalWebViewActivity;
import com.instructure.canvasapi.api.AccountNotificationAPI;
import com.instructure.canvasapi.model.AccountNotification;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import retrofit.client.Response;


public class AccountNotificationDialog extends DialogFragment implements APIStatusDelegate {

    public static final String TAG = "accountNotificationDialog";
    public static final String ACCOUNT_NOTIFICATIONS = "accountNotifications";

    private ListView listView;
    private ArrayList<AccountNotification> accountNotifications;
    private AccountNotificationAdapter adapter;
    private CanvasCallback<AccountNotification> accountNotificationCanvasCallback;
    private OnAnnouncementCountInvalidated onAnnouncementCountInvalidated;

    public interface OnAnnouncementCountInvalidated {
        public void invalidateAnnouncementCount();
    }

    public static AccountNotificationDialog show(FragmentActivity activity, ArrayList<AccountNotification> notifications) {
        AccountNotificationDialog accountNotificationDialog = new AccountNotificationDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ACCOUNT_NOTIFICATIONS, notifications);
        accountNotificationDialog.setArguments(args);
        accountNotificationDialog.show(activity.getSupportFragmentManager(), TAG);
        return accountNotificationDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.account_notification_dialog, null);

        listView = (ListView) view.findViewById(R.id.listView);
        adapter = new AccountNotificationAdapter();
        return new MaterialDialog.Builder(getActivity())
                .customView(view, false)
                .show();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        accountNotifications = getArguments().getParcelableArrayList(ACCOUNT_NOTIFICATIONS);

        listView.setAdapter(adapter);

        setupCallback();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnAnnouncementCountInvalidated) {
            onAnnouncementCountInvalidated = (OnAnnouncementCountInvalidated) activity;
        }
    }

    private void setupCallback() {
        accountNotificationCanvasCallback = new CanvasCallback<AccountNotification>(this) {
            @Override
            public void cache(AccountNotification accountNotification) {

            }

            @Override
            public void firstPage(AccountNotification accountNotification, LinkHeaders linkHeaders, Response response) {
                if(onAnnouncementCountInvalidated != null) {
                    onAnnouncementCountInvalidated.invalidateAnnouncementCount();
                }
                adapter.removeItem(accountNotification);
                if(accountNotifications.size() == 0) {
                    //if we don't have any more notifications just close the dialog
                    dismiss();
                }
            }
        };
    }

    private class AccountNotificationAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return accountNotifications.size();
        }

        @Override
        public Object getItem(int position) {
            return accountNotifications.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        private void removeItem(AccountNotification notification) {
            accountNotifications.remove(notification);

            if(accountNotifications.size() > 0) {
                notifyDataSetChanged();
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);

                convertView = li.inflate(R.layout.account_notification_row, parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.subject = (TextView) convertView.findViewById(R.id.subject);
                holder.message = (TextView) convertView.findViewById(R.id.message);
                holder.close = (ImageView) convertView.findViewById(R.id.dismiss);
                holder.htmlImage = (ImageView) convertView.findViewById(R.id.htmlImage);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final AccountNotification notification = accountNotifications.get(position);

            holder.subject.setText(notification.getSubject());
            holder.close.setEnabled(true);

            if(notification.getMessage().contains("<img")) {
                Spanned spanned = Html.fromHtml(notification.getMessage(),
                        new Html.ImageGetter() {
                            Drawable d;
                            @Override
                            public Drawable getDrawable(String source) {


                                //math images don't start with a protocol, so if it doesn't assume it's the user's domain
                                if(!source.startsWith("http") && !source.startsWith(APIHelpers.getFullDomain(getActivity()))) {
                                    source = APIHelpers.getFullDomain(getActivity()) + source;
                                }
                                Picasso.with(getActivity()).load(source).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        if (!isAdded()) {
                                            return;
                                        }
                                        d = new BitmapDrawable(getResources(), bitmap);
                                        holder.htmlImage.setImageDrawable(d);
                                        holder.htmlImage.setVisibility(View.VISIBLE);
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });

                                return d;
                            }
                        }, null);

                setTextViewHTML(holder.message, notification.getMessage().replaceAll("<img.+?>", ""));
            }
            else {
                setTextViewHTML(holder.message, trimTrailingWhitespace(notification.getMessage()).toString());

                holder.htmlImage.setVisibility(View.GONE);
            }

            holder.message.setMovementMethod(LinkMovementMethod.getInstance());

            holder.close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //remove the notification from the user's dashboard
                    holder.close.setEnabled(false); // debounce the close button to avoid unnecessary api calls. remove item is called in the deletion callback.

                    AccountNotificationAPI.deleteAccountNotification(notification.getId(), accountNotificationCanvasCallback);
                }
            });

            String icon = notification.getIcon();
            if(icon.equals(AccountNotification.ACCOUNT_NOTIFICATION_ERROR)) {
                Drawable d = CanvasContextColor.getColoredDrawable(getActivity(), R.drawable.alerts_and_states_warning, getResources().getColor(R.color.canvasRed));
                holder.icon.setImageDrawable(d);
            } else if(icon.equals(AccountNotification.ACCOUNT_NOTIFICATION_CALENDAR)) {
                holder.icon.setImageResource(R.drawable.ic_cv_calendar);
            } else if(icon.equals(AccountNotification.ACCOUNT_NOTIFICATION_QUESTION)) {
                holder.icon.setImageResource(R.drawable.ic_cv_help);
            } else if(icon.equals(AccountNotification.ACCOUNT_NOTIFICATION_INFORMATION)) {
                holder.icon.setImageResource(R.drawable.ic_cv_information);
            } else if(icon.equals(AccountNotification.ACCOUNT_NOTIFICATION_WARNING)) {
                Drawable d = CanvasContextColor.getColoredDrawable(getActivity(), R.drawable.alerts_and_states_warning, getResources().getColor(R.color.courseYellow));
                holder.icon.setImageDrawable(d);
            }

            return convertView;
        }
    }

    private void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                // Do something with span.getURL() to handle the link click...
                getActivity().startActivity(InternalWebViewActivity.createIntent(getActivity(), span.getURL(), "", false));
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }


    private void setTextViewHTML(TextView text, String html) {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        text.setText(strBuilder);
    }
    private static class ViewHolder {

        TextView subject;
        TextView message;
        ImageView icon;
        ImageView close;
        ImageView htmlImage;
    }

    /** Trims trailing whitespace. Removes any of these characters:
     * 0009, HORIZONTAL TABULATION
     * 000A, LINE FEED
     * 000B, VERTICAL TABULATION
     * 000C, FORM FEED
     * 000D, CARRIAGE RETURN
     * 001C, FILE SEPARATOR
     * 001D, GROUP SEPARATOR
     * 001E, RECORD SEPARATOR
     * 001F, UNIT SEPARATOR
     * @return "" if source is null, otherwise string with all trailing whitespace removed
     */
    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // For APIStatusDelegate
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCallbackStarted() {}

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {}

    @Override
    public void onNoNetwork() {}

    @Override
    public Context getContext() {
        return getActivity();
    }
}
