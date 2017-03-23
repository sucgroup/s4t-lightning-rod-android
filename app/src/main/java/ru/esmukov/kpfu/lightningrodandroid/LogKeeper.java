package ru.esmukov.kpfu.lightningrodandroid;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kostya on 04/03/2017.
 */

// thread-safe
public class LogKeeper {

    static final byte TYPE_STDOUT = 1;
    static final byte TYPE_STDERR = 2;
    static final int MAX_LOG_LINES = 500;

    private final Object mLock = new Object();
    private final List<Messenger> mClients = new ArrayList<>();
    private final LinkedList<LogRecord> mLogrecords = new LinkedList<>();
    private boolean[] mOpenPipes = new boolean[] {true, true};

    public void reset() {
        synchronized (mLock) {
            mClients.clear();
            mLogrecords.clear();
            mOpenPipes = new boolean[] {true, true};
        }
    }

    public void addLine(String line, byte type) {
        synchronized (mLock) {
            LogRecord logRecord = new LogRecord(line, type);
            mLogrecords.add(logRecord);
            while (mLogrecords.size() > MAX_LOG_LINES) {
                mLogrecords.removeFirst();
            }

            sendPartial(logRecord);
        }
    }

    public void closePipe(byte type) {
        addLine("<pipe closed>", type);

        mOpenPipes[type - 1] = false;
        if (!mOpenPipes[0] && !mOpenPipes[1]) {
            sendMessage(Message.obtain(null, MainActivity.MSG_PROCESS_STOPPED));
        }
    }

    public void subscribe(Messenger client) {
        synchronized (mLock) {
            try {
                client.send(Message.obtain(null, MainActivity.MSG_FULL_LOG, mLogrecords.clone()));
                mClients.add(client);
            } catch (RemoteException e) {
            }
        }
    }

    public void unsubscribe(Messenger client) {
        synchronized (mLock) {
            mClients.remove(client);
        }
    }

    private void sendPartial(LogRecord logRecord) {
        sendMessage(Message.obtain(null, MainActivity.MSG_PARTIAL_LOG, logRecord));
    }

    private void sendMessage(Message msg) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Message m = Message.obtain();
                m.copyFrom(msg);
                mClients.get(i).send(m);
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    public static class LogRecord {
        private String line;
        private byte type;

        public LogRecord(String line, byte type) {
            this.line = line;
            this.type = type;
        }

        public String getLine() {
            return line;
        }

        public byte getType() {
            return type;
        }
    }

}
