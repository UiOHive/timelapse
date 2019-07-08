package com.example.lazylapse.SMS;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SMSReader extends Service {
    public SMSReader() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent i, int flags, int startId){
        try{
        SMSManager smsManager = SMSManager.getSMSManager();
        ArrayList<Map<String, String>> messages = smsManager.checkMessagesFromAdmin();

        Map<String,Intent> intentMap = getIntentMap();

        for(Map<String,String> message: messages){
            String body = message.get("body");
            if(body.substring(0,1).equals("#")){
                Intent clonedIntent = new Intent();
                if(body.contains(":")){
                    String commandName = body.substring(1,body.indexOf(":"));

                    Toast.makeText(this,commandName,Toast.LENGTH_LONG).show();

                    String[] attributes = body.substring(body.indexOf(":")+1,body.length()).split(";");
                    if(intentMap.containsKey(commandName)){
                        clonedIntent = (Intent) intentMap.get(commandName).clone();
                        int n = 0;
                        for(String extra: attributes) {
                            clonedIntent.putExtra("" + n, extra);
                            n++;
                        }
                    }
                }else{
                    String commandName = body.substring(1,body.length());
                    Toast.makeText(this,commandName,Toast.LENGTH_LONG).show();
                    if(intentMap.containsKey(commandName)){
                        clonedIntent = (Intent) intentMap.get(commandName).clone();
                    }

                }
                clonedIntent.putExtra("address", message.get("address"));
                startService(clonedIntent);

            }else{
                Toast.makeText(this,"not a command",Toast.LENGTH_LONG).show();
            }
        }
        }
        catch(Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return flags;
    }
    private Map<String,Intent> getIntentMap(){
        Map<String,Intent> intentMap = new HashMap<String,Intent>();
        Intent iStatus = new Intent(SMSReader.this, PhoneStatus.class);
        intentMap.put("Status",iStatus);
        return intentMap;
    }
}
