package com.example.lazylapse.Interface;

import android.content.Context;
import android.widget.Toast;

import com.example.lazylapse.App;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Logger {

    private static Logger logger;
    private static String log;
    private static ArrayList<ILogVisitor> visitors;
    private FileOutputStream outputStream;
    private final String logFileName = "lazyLog.txt";
    private final String focusFileName = "focus.txt";

    private Logger(){
        log = "";
        visitors = new ArrayList<ILogVisitor>();
        load();
    }

    public static Logger getLogger(){
        if(logger == null) {
            logger = new Logger();
        }
        return logger;
    }

    public void accept(ILogVisitor visitor){
        visitors.add(visitor);
    }

    public void addToLog(String msg){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        String date = dateFormat.format(new Date());
        log += date+" :: "+msg+" \n";

        updateVisitors();

        try {

            outputStream = (FileOutputStream) App.getContext().openFileOutput(logFileName, Context.MODE_PRIVATE);
            outputStream.write(log.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void load(){
        try{
            Context ctx = App.getContext();
            FileInputStream fileInputStream = ctx.openFileInput(logFileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            inputStreamReader.close();
            log = bufferedReader.readLine();
        }catch(Exception e){
            Toast.makeText(App.getContext(),e.getMessage(),Toast.LENGTH_LONG);
        }
    }

    public String getLog(){
        return log;
    }
    public void updateVisitors(){
        for(ILogVisitor visitor: visitors){
            visitor.visit(log);
        }
    }
}
