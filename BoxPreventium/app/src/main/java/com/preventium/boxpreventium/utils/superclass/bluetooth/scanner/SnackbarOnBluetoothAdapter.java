package com.preventium.boxpreventium.utils.superclass.bluetooth.scanner;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Franck on 08/08/2016.
 */

public class SnackbarOnBluetoothAdapter {

    private final ViewGroup rootView;
    private final String text;
    private final String buttonText;
    private final View.OnClickListener onButtonClickListener;
    private final Snackbar.Callback snackbarCallback;

    /**
     * @param rootView Parent view to show the snackbar
     * @param text Message displayed in the snackbar
     * @param buttonText Message displayed in the snackbar button
     * @param onButtonClickListener Action performed when the user clicks the snackbar button
     */
    public SnackbarOnBluetoothAdapter(ViewGroup rootView, String text, String buttonText, View.OnClickListener onButtonClickListener, Snackbar.Callback snackbarCallback) {
        this.rootView = rootView;
        this.text = text;
        this.buttonText = buttonText;
        this.onButtonClickListener = onButtonClickListener;
        this.snackbarCallback = snackbarCallback;
    }

    public void showSnackbar() {
        Snackbar snackbar = Snackbar.make(rootView, text, Snackbar.LENGTH_LONG);
        if (buttonText != null && onButtonClickListener != null) {
            snackbar.setAction(buttonText, onButtonClickListener);
        }
        if (snackbarCallback != null) {
            snackbar.setCallback(snackbarCallback);
        }
        snackbar.show();
    }

    /**
     * Builder class to configure the displayed snackbar
     * Non set fields will not be shown
     */
    public static class Builder {
        private final ViewGroup rootView;
        private final String text;
        private String buttonText;
        private View.OnClickListener onClickListener;
        private Snackbar.Callback snackbarCallback;

        private Builder(ViewGroup rootView, String text) {
            this.rootView = rootView;
            this.text = text;
        }

        public static Builder with(ViewGroup rootView, String text) {
            return new Builder(rootView, text);
        }

        public static Builder with(ViewGroup rootView, @StringRes int textResourceId) {
            return Builder.with(rootView, rootView.getContext().getString(textResourceId));
        }

        /**
         * Adds a text button with the provided click listener
         */
        public Builder withButton(String buttonText, View.OnClickListener onClickListener) {
            this.buttonText = buttonText;
            this.onClickListener = onClickListener;
            return this;
        }

        /**
         * Adds a text button with the provided click listener
         */
        public Builder withButton(@StringRes int buttonTextResourceId,
                                  View.OnClickListener onClickListener) {
            return withButton(rootView.getContext().getString(buttonTextResourceId), onClickListener);
        }

        /**
         * Adds a button that opens the application settings when clicked
         */
        public Builder withEnableBluetoothButton(String buttonText) {
            this.buttonText = buttonText;
            this.onClickListener = new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Context context = rootView.getContext();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    context.startActivity(enableBtIntent);
                }
            };
            return this;
        }

        /**
         * Adds a button that opens the application settings when clicked
         */
        public Builder withEnableBluetoothButton(@StringRes int buttonTextResourceId) {
            return withEnableBluetoothButton(rootView.getContext().getString(buttonTextResourceId));
        }

        /**
         * Adds a button that opens the application settings when clicked
         */
        public Builder withEnableLocationButton(String buttonText) {
            this.buttonText = buttonText;
            this.onClickListener = new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Context context = rootView.getContext();
                    Intent enableBtIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(enableBtIntent);
                }
            };
            return this;
        }

        /**
         * Adds a button that opens the application settings when clicked
         */
        public Builder withEnableLocationButton(@StringRes int buttonTextResourceId) {
            return withEnableLocationButton(rootView.getContext().getString(buttonTextResourceId));
        }

        /**
         * Adds a callback to handle the snackbar {@code onDismissed} and {@code onShown} events.
         */
        public Builder withCallback(Snackbar.Callback callback) {
            this.snackbarCallback = callback;
            return this;
        }

        /**
         * Builds a new instance of {@link SnackbarOnBluetoothAdapter}
         */
        public SnackbarOnBluetoothAdapter build() {
            return new SnackbarOnBluetoothAdapter(rootView, text, buttonText, onClickListener,
                    snackbarCallback);
        }
    }
}
