package ru.esmukov.kpfu.lrandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createExternalStoragePrivateFile();

        getResources().getIdentifier("index.js", "raw", getPackageName());
        try {
            // http://stackoverflow.com/questions/15093663/packaging-linux-binary-in-android-apk
            // http://stackoverflow.com/questions/17383552/how-to-package-native-commandline-application-in-apk
            // http://stackoverflow.com/questions/6998419/package-android-apk-with-additional-executables

            // https://nodejs.org/dist/latest-v6.x/

            Process p = Runtime.getRuntime().exec(new String[]{
                    getApplicationInfo().nativeLibraryDir + "/libnode.so",
                    getFilesDir().getPath() + "/node/index.js"
            });

            Scanner s = new Scanner(p.getInputStream());

            while (s.hasNextLine()) {
                Log.i("hihihi", s.nextLine());
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e); // todo
        }
    }

    void createExternalStoragePrivateFile() {
        // http://stackoverflow.com/questions/4447477/how-to-copy-files-from-assets-folder-to-sdcard
        // http://stackoverflow.com/questions/19218775/android-copy-assets-to-internal-storage
        // http://stackoverflow.com/questions/22903540/android-copy-files-from-assets-to-data-data-folder

        // https://developer.android.com/guide/topics/resources/providing-resources.html#ResourceTypes

        // mkdir -p
        new File(getFilesDir(), "node").mkdirs();

        File file = new File(getFilesDir(), "node/index.js");

        try {
            InputStream is = getAssets().open("node/index.js");
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e); // todo
        }
    }
}
