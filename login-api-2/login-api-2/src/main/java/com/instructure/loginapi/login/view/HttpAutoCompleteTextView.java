package com.instructure.loginapi.login.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * A custom AutoCompleteTextView that will ignore "http://" and "https://"
 * and give suggestions that ignore these strings.
 */
public class HttpAutoCompleteTextView extends AutoCompleteTextView {

	public HttpAutoCompleteTextView(Context context, AttributeSet attrs,
                                    int defStyle) {
		super(context, attrs, defStyle);
	}

	public HttpAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HttpAutoCompleteTextView(Context context) {
		super(context);
	}
	
	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		if (text.length() < 7) {
			super.performFiltering(text, keyCode);	
			return;
		}
		//if correct length, check to see if the string starts with a protocol
		//if so, ignore it and give a suggestion based off of the remainder of the string
		String value = text.toString();
		if (value.startsWith("http://")) {
			String protocolRemoved = value.substring(7);
			super.performFiltering(protocolRemoved, keyCode);
			return;
		}
		if (value.startsWith("https://")) {
			String protocolRemoved = value.substring(8);
			super.performFiltering(protocolRemoved, keyCode);
			return;
		}
	}
}
