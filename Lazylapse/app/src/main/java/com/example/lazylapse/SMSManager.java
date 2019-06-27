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
import java.util.HashMap;
import java.util.Map;

import static android.telephony.PhoneNumberUtils.formatNumber;

public class SMSManager {
    private static SMSManager smsManager;
    private String phoneNumbers;
    private Logger logger;
    private String[] formattedNumbers;

    private SMSManager() {
        logger = Logger.getLogger();

        phoneNumbers = "none";

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

            phoneNumbers = preferences.getString("phoneNumber", "none");

            formattedNumbers = phoneNumbers.split(";");
        }
    }

    public static SMSManager getSMSManager(){
        if(smsManager==null){
            smsManager = new SMSManager();
        }

        return smsManager;
    }
    public void sendMessage(String message, String address){
        if(address!="none") {
            Toast.makeText(App.getContext(), "message send to " + address, Toast.LENGTH_LONG).show();

            SmsManager smgr = SmsManager.getDefault();
            ArrayList<String> messages = smgr.divideMessage(message);
            smgr.sendMultipartTextMessage(address, null, messages, null, null);
        }
        else{
            Toast.makeText(App.getContext(),"no phone number registered, message couldn't be sent!"
                    , Toast.LENGTH_LONG).show();
        }
    }
    public void sendMessage(String message){
        for(String number : formattedNumbers){
            sendMessage(message,number);
        }
    }

    /**
     * Functions that retrive all new messages from the admin since the last call to this function
     * (or since the app started if not called yet)
     * @return
     */
    public ArrayList<Map<String, String>> checkMessagesFromAdmin(){
        String filter = "date>="+App.getLastSMSCheck().getTime()+" AND "; //we select only
        // messages that interest us
        String condAddress = "(";
        for(String address: formattedNumbers){
            condAddress+= "address= ? OR ";
            address = address.replaceAll(" ","");
        }
        condAddress+="1=2)";
        Cursor cursor = App.getContext().getContentResolver().query(Uri.parse("content://sms/inbox"),
                new String[]{"_id", "address", "date", "body"}, filter+condAddress,
                        formattedNumbers, null);
        App.setLastSMSCheck();
        ArrayList<Map<String,String>> messages = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String,String> message = new HashMap<>();
            message.put("id",cursor.getString(0));
            message.put("address",cursor.getString(1));
            message.put("date",cursor.getString(2));
            message.put("body",cursor.getString(3));

            messages.add(message);
        }
        Toast.makeText(App.getContext(),messages.size()+" new messages from admin!",
                Toast.LENGTH_LONG).show();
        return messages;
    }
}
