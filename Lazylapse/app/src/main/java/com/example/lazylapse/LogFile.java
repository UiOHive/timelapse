package com.example.lazylapse;

import android.widget.Toast;

import com.example.lazylapse.Interface.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public abstract class LogFile {
    protected static String pathToLog;

    /**
     * Add a line to the file located at pathToLog
     * @param text
     */
    public void appendLog(String text)
    {
        File logFile = new File(getDir(),pathToLog);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Open the file and get all of its content.
     * @return String with the raw content with lines separated by "\n"
     */
    public String getLogContent(){
        try {
            File logPicFile = new File(getDir(), pathToLog);
            if (!logPicFile.exists()) {
                return null;
            }
            FileReader fin = null;
            BufferedReader bin = null;

            String txt = "";

            try {
                fin = new FileReader(logPicFile);
                bin = new BufferedReader(fin);


                String line;
                while ((line = bin.readLine()) != null) {
                    txt+=line+"\n";
                }
            } finally {
                fin.close();
                bin.close();
            }

            return txt;
        }catch(Exception e){
            Toast.makeText(App.getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * delete the fill. A new one will be created when {@link #appendLog(String)} is called
     */
    public static void clear() {
        File file = new File(new File(getDir(),pathToLog).getPath());
        file.delete();
    }

    /**
     * Get the directory that will contain the log file, if it doesn't exist, it creates it.
     * @return File file that will contain log files
     */
    public static File getDir() {
        File sdDir = App.getContext().getFilesDir();
        if(!sdDir.exists()){
            sdDir.mkdirs();
        }
        return sdDir;
    }
}
