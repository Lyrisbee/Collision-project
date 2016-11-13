package com.example.lyrisbee.google_service;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Lyrisbee on 2016/11/9.
 */

public class ConnectMessageDialog extends DialogFragment {
    boolean answer;
    @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.connect_message)
                    .setPositiveButton(R.string.reconnect, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            answer = true;
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ConnectMessageDialog.this.getDialog().cancel();
                            answer = false;
                        }
                    });

            // Create the AlertDialog object and return it
            return builder.create();
        }
    public boolean getAnswer(){
        return answer;
    }
}

