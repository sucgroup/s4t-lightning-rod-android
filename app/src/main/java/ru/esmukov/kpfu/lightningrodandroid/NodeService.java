package ru.esmukov.kpfu.lightningrodandroid;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;


public class NodeService extends Service implements Runnable {

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;

    private final static String NODE_THREAD_NAME = "LightningRodNodeThread";
    private final static String NODE_STDOUT_THREAD_NAME = "LightningRodNodeStdoutThread";
    private final static String NODE_STDERR_THREAD_NAME = "LightningRodNodeStderrThread";

    private Thread mThread = null;
    private final NodeAssetsManager mNodeAssetsManager = new NodeAssetsManager(this);
    private final LogKeeper mLogKeeper = new LogKeeper();
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    public NodeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        makeForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mThread == null || !mThread.isAlive()) {
            mNodeAssetsManager.extractAll();

            mLogKeeper.reset();
            mThread = new Thread(this, NODE_THREAD_NAME);
            mThread.start();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
            // todo ?? join?
        }
        mThread = null;
        super.onDestroy();
    }

    private void makeForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setContentIntent(pendingIntent)
                //.setTicker(getText(R.string.ticker_text))
                .build();

        startForeground(1, notification);
    }

    @Override
    public void run() {
        Process p;
        try {
            String[] envp = {
                    "LD_LIBRARY_PATH=" + mNodeAssetsManager.getTermuxPath() + "/usr/lib" + ":$LD_LIBRARY_PATH",
                    "PATH=" + mNodeAssetsManager.getTermuxPath() + "/usr/bin:$PATH"
            };

            p = Runtime.getRuntime().exec(new String[]{
                    mNodeAssetsManager.getTermuxPath() + "/usr/bin/node",
                    mNodeAssetsManager.getJsPath() + "/index.js"
            }, envp);
        } catch (IOException e) {
            throw new RuntimeException(e); // todo
        }

        Thread stdoutThread = new Thread(
                new ScannerThread(p.getInputStream(), mLogKeeper, LogKeeper.TYPE_STDOUT),
                NODE_STDOUT_THREAD_NAME);
        Thread stderrThread = new Thread(
                new ScannerThread(p.getErrorStream(), mLogKeeper, LogKeeper.TYPE_STDERR),
                NODE_STDERR_THREAD_NAME);

        stderrThread.start();
        stdoutThread.start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            p.destroy();
        } finally {
            stderrThread.interrupt();
            stdoutThread.interrupt();
        }
    }

    private static class ScannerThread implements Runnable {
        private final Scanner scanner;
        private final LogKeeper logKeeper;
        private final byte logKeeperType;

        public ScannerThread(InputStream is, LogKeeper logKeeper, byte logKeeperType) {
            scanner = new Scanner(is);
            this.logKeeper = logKeeper;
            this.logKeeperType = logKeeperType;
        }

        @Override
        public void run() {
            try {
                while (scanner.hasNextLine()) {
                    logKeeper.addLine(scanner.nextLine(), logKeeperType);
                    if (Thread.interrupted())
                        return;
                }
            } finally {
                logKeeper.closePipe(logKeeperType);
            }
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mLogKeeper.subscribe(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mLogKeeper.unsubscribe(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
