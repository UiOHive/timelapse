package com.example.lazylapse.Photo;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.lazylapse.App;
import com.example.lazylapse.Interface.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class LogPictures {

    private static final String pathToLog = "pic_to_upload.txt";

    protected static void addPictureToBeUploaded(String path){
        try {
            ArrayList<String> other = getFilesToUpload();
            if(other == null){
                other = new ArrayList<>();
            }
            other.add(path+"\n");

            String files = "";
            for(String file: other){
                files+=file;
            }
            File logPicFile = new File(getDir(),pathToLog);
            if(!logPicFile.exists()){
                try{
                    logPicFile.createNewFile();
                }catch(Exception e){
                    Logger.getLogger().addToLog(e.getMessage());
                }
            }
            FileOutputStream outputStream = (FileOutputStream)
                    new FileOutputStream(logPicFile);

            OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
            outputWriter.write(files);
            Logger.getLogger().addToLog("wrote "+files+" to logpic");

        } catch (Exception e) {
            Toast.makeText(App.getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    public static ArrayList<String> getFilesToUpload(){
        try {
            Context ctx = App.getContext();
            File logPicFile = new File(getDir(), pathToLog);
            if (!logPicFile.exists()) {
                return null;
            }
            FileReader fin = null;
            BufferedReader bin = null;
            ArrayList<String> log = new ArrayList<>();
            try {
                fin = new FileReader(logPicFile);
                bin = new BufferedReader(fin);

                String line;
                while ((line = bin.readLine()) != null) {
                    log.add(line);
                }
            } finally {
                fin.close();
                bin.close();
            }
            Toast.makeText(App.getContext(), log.toString(), Toast.LENGTH_LONG).show();

            return log;
        }catch(Exception e){
            Toast.makeText(App.getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public static void appendLog(String text)
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

    public static void clear() {
        File file = new File(new File(getDir(),pathToLog).getPath());
        boolean deleted = file.delete();
    }
    private static File getDir() {
        File sdDir = App.getContext().getFilesDir();
        if(!sdDir.exists()){
            sdDir.mkdirs();
        }
        return sdDir;
    }
}
