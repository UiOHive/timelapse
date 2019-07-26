package com.example.lazylapse.Photo;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.lazylapse.App;
import com.example.lazylapse.Interface.Logger;
import com.example.lazylapse.LogFile;

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


public class LogPictures extends LogFile {
    /**
     * Constructor only setting the path to the log file for {@link LogFile}'s saving and reading
     * methods.
     */
    public void LogPictures(){
        pathToLog = "pic_to_upload.txt";
    }

    /**
     * instead of using {@link LogFile#getLogContent()} directly to get paths to the pictures that
     * need uploading, this method allow to get a String[] which is easier to iterate on.
     *
     * @return String[] containing the path to pictures taken by the app and that haven't been
     * uploaded yet.
     */
    public String[] getFilesToUpload(){
        String rawLog = getLogContent();
        String[] filesToUpload = rawLog.split("\n");
        clear();
        return filesToUpload;
    }

}
