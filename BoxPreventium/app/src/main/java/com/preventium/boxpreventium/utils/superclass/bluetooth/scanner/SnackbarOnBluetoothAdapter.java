package com.preventium.boxpreventium.utils.superclass.bluetooth.scanner;

import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar.Callback;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class SnackbarOnBluetoothAdapter {
    private final String buttonText;
    private final OnClickListener onButtonClickListener;
    private final ViewGroup rootView;
    private final Callback snackbarCallback;
    private final String text;

    public static class Builder {
        private String buttonText;
        private OnClickListener onClickListener;
        private final ViewGroup rootView;
        private Callback snackbarCallback;
        private final String text;

        class C01421 implements OnClickListener {
            C01421() {
            }

            public void onClick(View v) {
                Builder.this.rootView.getContext().startActivity(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"));
            }
        }

        class C01432 implements OnClickListener {
            C01432() {
            }

            public void onClick(View v) {
                Builder.this.rootView.getContext().startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
            }
        }

        private Builder(ViewGroup rootView, String text) {
            this.rootView = rootView;
            this.text = text;
        }

        public static Builder with(ViewGroup rootView, String text) {
            return new Builder(rootView, text);
        }

        public static Builder with(ViewGroup rootView, @StringRes int textResourceId) {
            return with(rootView, rootView.getContext().getString(textResourceId));
        }

        public Builder withButton(String buttonText, OnClickListener onClickListener) {
            this.buttonText = buttonText;
            this.onClickListener = onClickListener;
            return this;
        }

        public Builder withButton(@StringRes int buttonTextResourceId, OnClickListener onClickListener) {
            return withButton(this.rootView.getContext().getString(buttonTextResourceId), onClickListener);
        }

        public Builder withEnableBluetoothButton(String buttonText) {
            this.buttonText = buttonText;
            this.onClickListener = new C01421();
            return this;
        }

        public Builder withEnableBluetoothButton(@StringRes int buttonTextResourceId) {
            return withEnableBluetoothButton(this.rootView.getContext().getString(buttonTextResourceId));
        }

        public Builder withEnableLocationButton(String buttonText) {
            this.buttonText = buttonText;
            this.onClickListener = new C01432();
            return this;
        }

        public Builder withEnableLocationButton(@StringRes int buttonTextResourceId) {
            return withEnableLocationButton(this.rootView.getContext().getString(buttonTextResourceId));
        }

        public Builder withCallback(Callback callback) {
            this.snackbarCallback = callback;
            return this;
        }

        public SnackbarOnBluetoothAdapter build() {
            return new SnackbarOnBluetoothAdapter(this.rootView, this.text, this.buttonText, this.onClickListener, this.snackbarCallback);
        }
    }

    public SnackbarOnBluetoothAdapter(ViewGroup rootView, String text, String buttonText, OnClickListener onButtonClickListener, Callback snackbarCallback) {
        this.rootView = rootView;
        this.text = text;
        this.buttonText = buttonText;
        this.onButtonClickListener = onButtonClickListener;
        this.snackbarCallback = snackbarCallback;
    }

    public void showSnackbar() {
        Snackbar snackbar = Snackbar.make(this.rootView, this.text, 0);
        if (!(this.buttonText == null || this.onButtonClickListener == null)) {
            snackbar.setAction(this.buttonText, this.onButtonClickListener);
        }
        if (this.snackbarCallback != null) {
            snackbar.setCallback(this.snackbarCallback);
        }
        snackbar.show();
    }
}
