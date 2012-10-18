package org.hqtp.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.google.inject.Inject;

public final class Alerter {
    @Inject
    Context context;

    void toastLong(String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    void toastShort(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    void alert(String title, String message) {
        alert(title, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
    }

    void alert(String title, String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", listener).show();
    }
}
