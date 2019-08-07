package com.example.lazylapse.Interface;

import android.content.Context;
import android.widget.Toast;

import com.example.lazylapse.App;
import com.example.lazylapse.LogFile;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Holds information on actions taken by the app, make them accessible for display and save it to a
 * file. Follow the singleton pattern to prevent aving to value of the log in different instance.
 * Accept visitors from {@link ILogVisitor} in order to update them whenether new actions are added
 * to the log
 */
public class Logger extends LogFile {

    private static Logger logger;
    private static String log;
    private static ArrayList<ILogVisitor> visitors;
    private FileOutputStream outputStream;

    /**
     * private constructor (follow singleton design pattern)used by {@link #getLogger()} when first
     * creating it. Read the file when called to retrieve previous saved actions
     */
    private Logger(){
        log = "";
        visitors = new ArrayList<ILogVisitor>();
        load();
    }

    /**
     * Singleton getter
     * @return singleton instance
     */
    public static Logger getLogger(){
        if(logger == null) {
            logger = new Logger();
        }
        return logger;
    }

    /**
     * accept visitor in order to update them whenever data the content of {@link Logger#log}
     * @param visitor ILogVisitor
     */
    public void accept(ILogVisitor visitor){
        visitors.add(visitor);
    }

    /**
     * add given String to the log, add timestamp based on the time of execution.
     * @param text String to add to the log
     */
    @Override
    public void appendLog(String text){
        pathToLog = getPathToLog();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        String date = dateFormat.format(new Date());
        String line = date+" :: "+text+" \n";
        logger.log += line;

        logger.updateVisitors();
        super.appendLog(line);

    }

    /**
     * Read the file and if the string returned is not null, save it in log.
     */
    public void load(){

        pathToLog = getPathToLog();
        String rawLog = getLogContent();
        if(rawLog!=null){
            log = rawLog;
        }
    }

    /**
     * update the log using {@link #load} to be sure to have the latest and then return it
     * @return
     */
    public String getLog(){
        load();
        return log;
    }

    /**
     * Get path to log, change it each month, in order to upload it
     * @return the path
     */
    public String getPathToLog(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        String date = dateFormat.format(new Date());
        pathToLog = "LazyLog_" + date + ".txt";
        return pathToLog;
    }
    /**
     * Get path to last month log, change it each month, in order to upload it
     * @return the path
     */
    public String getPreviousPathToLog(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH,-1);

        String date = dateFormat.format(calendar);
        pathToLog = "LazyLog_" + date + ".txt";

        return pathToLog;
    }


    /**
     *  call the visit method of all visitors giving it {@link #log} as parameter. Used mainly with
     *  {@link Controller#visit(String)}.
     */
    public void updateVisitors(){
        for(ILogVisitor visitor: visitors){
            visitor.visit(log);
        }
    }
}
