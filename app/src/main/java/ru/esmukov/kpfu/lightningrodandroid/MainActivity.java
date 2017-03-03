package ru.esmukov.kpfu.lightningrodandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NodeAssetsManager nodeAssetsManager = new NodeAssetsManager(this);
        nodeAssetsManager.extractAll();

        try {
            String[] envp = {
                "LD_LIBRARY_PATH=" + nodeAssetsManager.getTermuxPath() + "/usr/lib" + ":$LD_LIBRARY_PATH",
                "PATH=" + nodeAssetsManager.getTermuxPath() + "/usr/bin:$PATH"
            };

            Process p = Runtime.getRuntime().exec(new String[]{
                    nodeAssetsManager.getTermuxPath() + "/usr/bin/node",
                    nodeAssetsManager.getJsPath() + "/index.js"
            }, envp);

            Scanner s = new Scanner(p.getInputStream());

            while (s.hasNextLine()) {
                Log.i("hihihi", s.nextLine());
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e); // todo
        }
    }

}
