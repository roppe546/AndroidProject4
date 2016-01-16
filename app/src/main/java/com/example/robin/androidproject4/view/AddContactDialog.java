package com.example.robin.androidproject4.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.example.robin.androidproject4.R;
import com.example.robin.androidproject4.model.Communicator;

/**
 * Created by robin on 8/1/16.
 */
public class AddContactDialog extends DialogFragment {
    /**
     * This interface can be used by an activity in order to get information whether
     * this DialogFragment succeeded with its purpose or note.
     */
    public interface AddContactDialogListener {
        void onFinishAddContactDialog(boolean success);
    }

    private String loggedInUser;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        loggedInUser = getArguments().getString("loggedInUserEmail", null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Set the title of the dialog
        builder.setTitle(R.string.contact_dialog_title);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_add_contact, null))
                // Add action buttons
                .setPositiveButton(R.string.contact_dialog_add_contact, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("ContactDialog", "Add button clicked");

                        // Get field that holds entered email
                        EditText email = (EditText) AddContactDialog.this.getDialog().findViewById(R.id.dialog_contact_email);

                        if (email.getText().length() > 0) {
                            Log.i("ContactDialog", "Email string not empty, contacting server");

                            // Add to back end
                            Communicator.addNewContactRequest(loggedInUser, email.getText().toString());

                            // Return success to activity
                            AddContactDialogListener listener = (AddContactDialogListener) getActivity();
                            listener.onFinishAddContactDialog(true);
                        }
                    }
                })
                .setNegativeButton(R.string.contact_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("ContactDialog", "Cancel button clicked");
                        dialog.cancel();
                    }
                });

        return builder.create();
    }
}
