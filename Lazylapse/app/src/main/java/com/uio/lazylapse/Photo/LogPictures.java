package com.uio.lazylapse.Photo;

import com.uio.lazylapse.LogFile;


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
        String[] filesToUpload = null;
        if(rawLog!=null){
            filesToUpload = rawLog.split("\n");
            clear();
        }
        return filesToUpload;
    }

}
