package com.example.lazylapse.SMS;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.example.lazylapse.App;
import com.example.lazylapse.Interface.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static android.telephony.PhoneNumberUtils.formatNumber;

public class SMSManager {
    private static SMSManager smsManager;
    private String phoneNumbers;
    private Logger logger;
    private String[] formattedNumbers;
    boolean ready = false;

    private SMSManager() {
        logger = Logger.getLogger();

        phoneNumbers = "none";

        TelephonyManager telMgr = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();

        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                logger.appendLog("misssing the SIM card");
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                logger.appendLog("unlock the SIM card plz");
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                logger.appendLog("unlock the SIM card plz");
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                logger.appendLog("unlock the SIM card plz");
                break;
            case TelephonyManager.SIM_STATE_READY:
                logger.appendLog("SIM card ready");
                ready = true;
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                logger.appendLog("SIM state unknown, try again later");
                break;
        }
        if(ready) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());

            String countryCodeValue = telMgr.getNetworkCountryIso();

            phoneNumbers = preferences.getString("phoneNumber", null);
            if(phoneNumbers!=null) {
                formattedNumbers = phoneNumbers.split(";");
            }
        }
    }

    public static SMSManager getSMSManager(){
        if(smsManager==null){
            smsManager = new SMSManager();
        }

        return smsManager;
    }

    /**
     * send a message to the given phone number
     * @param message
     * @param address
     */
    public void sendMessage(String message, String address){
        if(ready){
        try {
                Toast.makeText(App.getContext(), "message send to " + address, Toast.LENGTH_LONG).show();

                SmsManager smgr = SmsManager.getDefault();
                ArrayList<String> messages = smgr.divideMessage(message);
                smgr.sendMultipartTextMessage(address, null, messages, null, null);
        }catch(Exception e){
            logger.appendLog("Couldn't send message to "+address+" : "+e.getMessage());
        }
        }
    }

    /**
     * send the same message to every admin
     * @param message
     */
    public void sendMessage(String message){
        if(ready){
            if(formattedNumbers!=null){
            for(String number : formattedNumbers){
                if (number != null) { //if no number are registered in the preference by the user we will get "none" as value for formated number
                    try {
                        sendMessage(message, number);
                    }catch(Exception e){
                        Toast.makeText(App.getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(App.getContext(), "no phone number registered, message couldn't be sent!"
                            , Toast.LENGTH_LONG).show();
                    }
                }
            }
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
        condAddress+="1=2)";//"1=2" is always false, it is used to cancel the last "OR" of the sql
                            // request

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
