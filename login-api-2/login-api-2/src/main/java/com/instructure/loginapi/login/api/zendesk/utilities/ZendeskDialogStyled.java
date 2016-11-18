package com.instructure.loginapi.login.api.zendesk.utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.ErrorReportManager;
import com.instructure.canvasapi2.models.ErrorReportResult;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.loginapi.login.BuildConfig;
import com.instructure.loginapi.login.R;
import com.instructure.loginapi.login.materialdialogs.CustomDialog;
import com.instructure.loginapi.login.util.Const;
import com.instructure.loginapi.login.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * NOTE: this no longer uses Zendesk. There is an api from canvas that we can post to and the endpoint
 * will take care of the help desk client that customer support is currently using.
 */
public class ZendeskDialogStyled extends DialogFragment {

    public interface ZendeskDialogResultListener {
        void onTicketPost();
        void onTicketError();
    }

    private static String DEFAULT_DOMAIN = "canvas.instructure.com";
    public static final String TAG = "zendeskDialog";
    private static final int customFieldTag = 20470321;
    private EditText descriptionEditText;
    private EditText subjectEditText;
    private EditText emailAddressEditText;
    private TextView emailAddress;
    private Spinner severitySpinner;

    private int titleColor;
    private int positiveColor;
    private int negativeColor;
    private boolean fromLogin;
    private boolean mUseDefaultDomain;

    private ZendeskDialogResultListener resultListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            resultListener = (ZendeskDialogResultListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ZendeskDialogResultListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity(), getActivity().getString(R.string.zendesk_reportAProblem), getString(R.string.zendesk_send));
        builder.darkTheme(false);

        //set the default colors for the title and positive and negative buttons
        titleColor = getResources().getColor(R.color.courseBlueDark);
        positiveColor = getResources().getColor(R.color.courseGreen);
        negativeColor = getResources().getColor(R.color.gray);

        //check to see if there are custom colors we need to set
        handleBundle();

        builder.titleColor(titleColor);
        builder.positiveColor(positiveColor);
        builder.negativeText(getString(R.string.cancel));
        builder.negativeColor(negativeColor);

        final CustomDialog dialog = builder.build();

        // Create View
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_zendesk_ticket, null);
        subjectEditText = (EditText) view.findViewById(R.id.subjectEditText);
        descriptionEditText = (EditText) view.findViewById(R.id.descriptionEditText);
        emailAddressEditText = (EditText) view.findViewById(R.id.emailAddressEditText);
        emailAddress = (TextView) view.findViewById(R.id.emailAddress);

        if (fromLogin) {
            emailAddressEditText.setVisibility(View.VISIBLE);
            emailAddress.setVisibility(View.VISIBLE);
        }

        initSpinner(view);

        dialog.setClickListener(new CustomDialog.ClickListener() {
            @Override
            public void onConfirmClick() {
                saveZendeskTicket();
            }

            @Override
            public void onCancelClick() {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.setCustomView(view);

        return dialog;
    }

    /////////////////////////////////////////////////////////////////
    //              Helpers
    /////////////////////////////////////////////////////////////////

    public void initSpinner(View view) {
        List<String> severityList = Arrays.asList(
                getString(R.string.zendesk_casualQuestion),
                getString(R.string.zendesk_needHelp),
                getString(R.string.zendesk_somethingsBroken),
                getString(R.string.zendesk_cantGetThingsDone),
                getString(R.string.zendesk_extremelyCritical));

        severitySpinner = (Spinner) view.findViewById(R.id.severitySpinner);
        ZenDeskAdapter adapter = new ZenDeskAdapter(getActivity(), R.layout.zendesk_spinner_item, severityList);
        severitySpinner.setAdapter(adapter);
    }

    private class ZenDeskAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;

        public ZenDeskAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getViewForText(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getViewForText(position, convertView, parent);
        }

        private View getViewForText(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.zendesk_spinner_item, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(getItem(position));
            holder.text.post(new Runnable() {
                @Override
                public void run() {
                    holder.text.setSingleLine(false);
                }
            });
            com.instructure.pandautils.utils.Utils.testSafeContentDescription(holder.text,
                    getContext().getString(R.string.severity_text_content_desc, position),
                    holder.text.getText().toString(),
                    com.instructure.canvasapi2.BuildConfig.IS_TESTING);

            return convertView;
        }
    }

    private static class ViewHolder {
        TextView text;
    }

    public void saveZendeskTicket() {
        String comment = descriptionEditText.getText().toString();
        String subject = subjectEditText.getText().toString();

        // if we're on the login page we need to set the cache user's email address so that support can
        // contact the user
        if (fromLogin) {
            if (emailAddressEditText.getText() != null && emailAddressEditText.getText().toString() != null) {
                User user = new User();
                user.setPrimaryEmail(emailAddressEditText.getText().toString());
                APIHelper.setCacheUser(getActivity(), user);
            }

        }


        final String email = APIHelper.getCacheUser(getActivity()).getPrimaryEmail();
        String domain = APIHelper.getDomain(getActivity());
        if (domain == null) {
            domain = DEFAULT_DOMAIN;
        }

        //add device info to comment
        //try to get the version number and version code
        PackageInfo pInfo = null;
        String versionName = "";
        int versionCode = 0;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Utils.d(e.getMessage());
        }
        String deviceInfo = "";
        deviceInfo += getString(R.string.device) + " " + Build.MANUFACTURER + " " + Build.MODEL + "\n" +
                getString(R.string.osVersion) + " " + Build.VERSION.RELEASE + "\n" +
                getString(R.string.versionNum) + ": " + versionName + " " + versionCode + "\n" +
                getString(R.string.zendesk_severityText) + " " + getUserSeveritySelectionTag() + "\n" +
                getString(R.string.installDate) + " " + getInstallDateString() + "\n\n";

        comment = deviceInfo + comment;
        if (mUseDefaultDomain) {
            ErrorReportManager.postGenericErrorReport(subject, domain, email, comment, getUserSeveritySelectionTag(), new StatusCallback<ErrorReportResult>(mStatusDelegate) {

                @Override
                public void onResponse(Response<ErrorReportResult> response, LinkHeaders linkHeaders, ApiType type) {
                    super.onResponse(response, linkHeaders, type);
                    resetCachedUser();
                    if(type.isAPI()) {
                        resultListener.onTicketPost();
                    }
                }

                @Override
                public void onFail(Call<ErrorReportResult> response, Throwable error) {
                    super.onFail(response, error);
                    resetCachedUser();
                    resultListener.onTicketError();
                }
            });
        } else {
            ErrorReportManager.postErrorReport(subject, domain, email, comment, getUserSeveritySelectionTag(), new StatusCallback<ErrorReportResult>(mStatusDelegate) {

                @Override
                public void onResponse(Response<ErrorReportResult> response, LinkHeaders linkHeaders, ApiType type) {
                    super.onResponse(response, linkHeaders, type);
                    resetCachedUser();
                    if(type.isAPI()) {
                        resultListener.onTicketPost();
                    }
                }

                @Override
                public void onFail(Call<ErrorReportResult> response, Throwable error) {
                    super.onFail(response, error);
                    resetCachedUser();
                    resultListener.onTicketError();
                }
            });
        }
    }

    private void resetCachedUser() {
        if (fromLogin) {
            //reset the cached user so we don't have any weird data hanging around
            APIHelper.setCacheUser(getActivity(), null);
        }
    }

    private String getInstallDateString() {
        try {
            long installed = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0)
                    .firstInstallTime;
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
            return format.format(new Date(installed));
        } catch (Exception e) {
            return "";
        }
    }


    private String getUserSeveritySelectionTag() {
        if (severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_extremelyCritical))) {
            return getString(R.string.zendesk_extremelyCritical_tag);
        } else if (severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_casualQuestion))) {
            return getString(R.string.zendesk_casualQuestion_tag);
        } else if (severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_somethingsBroken))) {
            return getString(R.string.zendesk_somethingsBroken_tag);
        } else if (severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_cantGetThingsDone))) {
            return getString(R.string.zendesk_cantGetThingsDone_tag);
        } else if (severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_needHelp))) {
            return getString(R.string.zendesk_needHelp_tag);
        } else {
            return "";
        }
    }

    /**
     * A way to customize the colors that display in the dialog. Use 0 if you don't want to change that color
     *
     * @param titleColor
     * @param positiveColor
     * @param negativeColor
     * @return
     */
    public static Bundle createBundle(int titleColor, int positiveColor, int negativeColor) {
        return createBundle(titleColor, positiveColor, negativeColor, false);
    }

    /**
     * if we're coming from the login screen there won't be any user information (because the user hasn't
     * logged in)
     *
     * @param titleColor
     * @param positiveColor
     * @param negativeColor
     * @param fromLogin
     * @return
     */
    public static Bundle createBundle(int titleColor, int positiveColor, int negativeColor, boolean fromLogin) {
        Bundle bundle = new Bundle();
        if (titleColor != 0) {
            bundle.putInt(Const.TITLE_COLOR, titleColor);
        }
        if (positiveColor != 0) {
            bundle.putInt(Const.POSITIVE_COLOR, positiveColor);
        }
        if (negativeColor != 0) {
            bundle.putInt(Const.NEGATIVE_COLOR, negativeColor);
        }

        bundle.putBoolean(Const.FROM_LOGIN, fromLogin);
        return bundle;
    }

    /**
     * if we're coming from the parent app we want to use the default domain
     *
     * @param titleColor
     * @param positiveColor
     * @param negativeColor
     * @param fromLogin
     * @param useDefaultDomain
     * @return
     */
    public static Bundle createBundle(int titleColor, int positiveColor, int negativeColor, boolean fromLogin, boolean useDefaultDomain) {
        Bundle bundle = new Bundle();
        if (titleColor != 0) {
            bundle.putInt(Const.TITLE_COLOR, titleColor);
        }
        if (positiveColor != 0) {
            bundle.putInt(Const.POSITIVE_COLOR, positiveColor);
        }
        if (negativeColor != 0) {
            bundle.putInt(Const.NEGATIVE_COLOR, negativeColor);
        }

        bundle.putBoolean(Const.FROM_LOGIN, fromLogin);
        bundle.putBoolean(Const.USE_DEFAULT_DOMAIN, useDefaultDomain);
        return bundle;
    }

    /**
     * Set the colors of the dialog based on the arguments passed in the bundle. If there isn't a color
     * we just use what is already set
     */
    public void handleBundle() {
        if (getArguments() == null) {
            return;
        }

        titleColor = getArguments().getInt(Const.TITLE_COLOR, titleColor);
        positiveColor = getArguments().getInt(Const.POSITIVE_COLOR, positiveColor);
        negativeColor = getArguments().getInt(Const.NEGATIVE_COLOR, negativeColor);
        fromLogin = getArguments().getBoolean(Const.FROM_LOGIN, false);
        mUseDefaultDomain = getArguments().getBoolean(Const.USE_DEFAULT_DOMAIN, false);
    }

    public StatusCallback.StatusDelegate mStatusDelegate = new StatusCallback.StatusDelegate() {
        @Override
        public boolean hasNetworkConnection() {
            return AppManager.hasNetworkConnection(getContext());
        }
    };
}