package com.eightyeightysix.shourya.almondclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.data.BroadCast;
import com.google.firebase.database.DatabaseReference;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

/*
 * Created by shourya on 20/6/17.
 */

public class NewBroadCastDialog extends DialogFragment{
    private static final String DEBUG_TAG = "AlmondLog:: " + NewBroadCastDialog.class.getSimpleName();
    private static final String REQUIRED = "Required";
    //EditText fTitle;
    TextView cCircle;
    EditText fBody;
    Button submit;
    DatabaseReference userPosts, allPosts;
    private String currentCircle;
    public NewBroadCastDialog() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View inf2 = inflater.inflate(R.layout.new_broadcast_dialog, null);
        //fTitle = (EditText) inf2.findViewById(R.id.field_title);
        cCircle = (TextView) inf2.findViewById(R.id.current_circle);
        currentCircle = getCircleText();
        cCircle.setText(currentCircle);
        fBody = (EditText) inf2.findViewById(R.id.field_body);
        submit = (Button) inf2.findViewById(R.id.broadcast_send);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pushBroadCast();
            }
        });

        builder.setView(inf2);
        return builder.create();
    }

    private String getCircleText() {
        if(BaseActivity.currCircle == 0) {
             return BaseActivity.locationDetails.getCountryName();
        }
        else if(BaseActivity.currCircle == 1){
            return BaseActivity.locationDetails.getAdminAreaName();
        }
        else{
            //temp
            return ("Zone name");
        }
    }

    private String formReference(int circle) {
        Map<String, String> params = new HashMap<>();
        String reference;
        switch(circle) {
            case 0: {
                //country users
                params.put("countryID", BaseActivity.locationDetails.getCountryID());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_country), params);
                break;
            }
            case 1:{
                //city users
                params.put("cityID", BaseActivity.locationDetails.getCityID());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_city), params);
                break;
            }
            default:{
                //zone
                params.put("zoneID", BaseActivity.locationDetails.zonesList.get(circle - 2));
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_zone), params);
                break;
            }
        }
        return reference;
    }

    private void pushBroadCast() {
        //final String title = fTitle.getText().toString();
        final String body = fBody.getText().toString();

        // Body is required
        if (TextUtils.isEmpty(body)) {
            fBody.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(getActivity().getApplicationContext(), "Posting...", Toast.LENGTH_SHORT).show();

        //for all broadcasts
        final String bReference = formReference(BaseActivity.currCircle);

        //for all current user broadcasts
        Map<String, String> params = new HashMap<String, String>();
        params.put("userID", BaseActivity.mUser.getUserId());
        final String bUserReference = BaseActivity.substituteString(getResources().getString(R.string.user_broadcasts), params);
        //userPosts = BaseActivity.mDatabase.getReference(bUserReference);

        //create Reference and get key for later use
        allPosts = BaseActivity.mDatabase.getReference(bReference);
        String key = allPosts.push().getKey();

        BroadCast new_bc = new BroadCast(params.get("userID"), BaseActivity.mUser.getDisplayName(), currentCircle, body);
        Map<String, Object> postValues = new_bc.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(bReference + "/" + key, postValues);
        childUpdates.put(bUserReference + "/" + key, new_bc.circle);

        BaseActivity.mDatabase.getReference().updateChildren(childUpdates);
        setEditingEnabled(true);
        dismiss();
    }

    private void setEditingEnabled(boolean enabled) {
        //fTitle.setEnabled(enabled);
        fBody.setEnabled(enabled);
        if (enabled) {
            submit.setVisibility(View.VISIBLE);
        } else {
            submit.setVisibility(View.GONE);
        }
    }
}
