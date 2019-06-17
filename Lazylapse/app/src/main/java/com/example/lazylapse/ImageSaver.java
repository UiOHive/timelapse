package com.example.lazylapse;

import android.media.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageSaver implements Runnable {
    /**
    * The file we save the image into.
    */

    private final File file;
    private final Image image;

    ImageSaver(Image image, File file) {
        this.image = image;
        this.file = file;
    }

    @Override
    public void run() {
        ByteBuffer buffer = this.image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(this.file);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.image.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}