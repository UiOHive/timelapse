package com.example.lazylapse.Photo;

import android.content.Context;
import android.widget.Toast;

import com.example.lazylapse.App;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class LogPictures {

    private static final String pathToLog = "pic_to_upload.txt";

    protected static void addPictureToBeUploaded(String path){
        try {
            ArrayList<String> other = getFilesToUpload();
            other.add(path+"\n");

            String files = "";
            for(String file: other){
                files+=file;
            }
            FileOutputStream outputStream = (FileOutputStream) App.getContext().openFileOutput(pathToLog, Context.MODE_PRIVATE);
            outputStream.write(files.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static ArrayList<String> getFilesToUpload(){
        try{
            Context ctx = App.getContext();
            FileInputStream fileInputStream = ctx.openFileInput(pathToLog);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            inputStreamReader.close();

            ArrayList<String> log = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.add(line);
            }

            return log;
        }catch(Exception e){
            Toast.makeText(App.getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            return null;

        }

    }
}
