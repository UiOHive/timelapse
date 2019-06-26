package com.example.lazylapse;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.util.ArrayList;

import static android.telephony.PhoneNumberUtils.formatNumber;

public class SMSManager {
    private static SMSManager smsManager;
    private String phoneNumber;
    private Logger logger;
    private String formattedNumber;

    private SMSManager() {
        logger = Logger.getLogger();

        phoneNumber = "none";
        formattedNumber = "none";

        boolean ready = false;


        TelephonyManager telMgr = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                logger.addToLog("misssing the SIM card");
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                logger.addToLog("unlock the SIM card plz");
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                logger.addToLog("unlock the SIM card plz");
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                logger.addToLog("unlock the SIM card plz");
                break;
            case TelephonyManager.SIM_STATE_READY:
                logger.addToLog("SIM card ready");
                ready = true;
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                logger.addToLog("SIM state unknown, try again later");
                break;
        }
        if(ready) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());

            String countryCodeValue = telMgr.getNetworkCountryIso();

            phoneNumber = preferences.getString("phoneNumber", "none");
            //formattedNumber = formatNumber(phoneNumber,countryCodeValue); // return either
            // formatted telephone number with country code (based on SIM network country) or null
            formattedNumber = phoneNumber;
        }
    }

    public static SMSManager getSMSManager(){
        if(smsManager==null){
            smsManager = new SMSManager();
        }

        return smsManager;
    }
    public void sendMessage(String message){
        if(formattedNumber!="none") {
            Toast.makeText(App.getContext(), "message send to " + formattedNumber, Toast.LENGTH_LONG).show();

            SmsManager smgr = SmsManager.getDefault();
            ArrayList<String> messages = smgr.divideMessage(message);
            smgr.sendMultipartTextMessage(formattedNumber, null, messages, null, null);
        }
        else{
            Toast.makeText(App.getContext(),"no phone number registered, message couldn't be sent!", Toast.LENGTH_LONG).show();
        }
    }

    public void checkMessagesFromAdmin(){
        String filter = "date>="+App.getLastSMSCheck().getTime()+" AND address=?"; //we select only messages that interest us
        Cursor cursor = App.getContext().getContentResolver().query(Uri.parse("content://sms/inbox"), new String[]{"_id", "address", "date", "body"}, filter, new String[] {formattedNumber.replace(" ","")}, null);
        App.setLastSMSCheck();
        int n = 0;
        while (cursor.moveToNext()) {
            n++;
        }
        Toast.makeText(App.getContext(),n+" new messages from admin!",Toast.LENGTH_LONG).show();
    }
}
