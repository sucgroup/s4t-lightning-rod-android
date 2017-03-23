package ru.esmukov.kpfu.lightningrodandroid;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final int MSG_FULL_LOG = 1;
    public static final int MSG_PARTIAL_LOG = 2;
    public static final int MSG_PROCESS_STOPPED = 3;

    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mService = null;

    private EditText logText = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            sendRegisterMessage();
            renderButtons();
        }

        public void onServiceDisconnected(ComponentName className) {
            // todo notify activity that a service has been destroyed
            mService = null;
            renderButtons();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logText = (EditText) findViewById(R.id.logText);

        hideButtons();
        startNodeService();

        findViewById(R.id.button_start)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mService == null) {
                            MainActivity.this.startNodeService();
                        }
                    }
                });

        findViewById(R.id.button_stop)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mService != null) {
                            MainActivity.this.stopNodeService();
                        }
                    }
                });
    }

    private void startNodeService() {
        startService(new Intent(this, NodeService.class));
        bindService(new Intent(this, NodeService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private void stopNodeService() {
        unbindNodeService();
        stopService(new Intent(this, NodeService.class));
    }

    private void unbindNodeService() {
        sendUnregisterMessage();
        unbindService(mConnection);
    }

    private void renderButtons() {
        findViewById(R.id.button_start).setVisibility(View.GONE);
        findViewById(R.id.button_stop).setVisibility(View.GONE);
        if (mService == null) {
            findViewById(R.id.button_start).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.button_stop).setVisibility(View.VISIBLE);
        }
    }

    private void hideButtons() {
        findViewById(R.id.button_start).setVisibility(View.GONE);
        findViewById(R.id.button_stop).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        unbindNodeService();
        super.onDestroy();
    }

    class IncomingHandler extends Handler {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FULL_LOG:
                    List<LogKeeper.LogRecord> logRecords = (List<LogKeeper.LogRecord>) msg.obj;
                    logText.setText("");
                    for (LogKeeper.LogRecord logRecord : logRecords) {
                        logText.append(logRecord.getLine() + "\n");
                    }
                    break;
                case MSG_PARTIAL_LOG:
                    LogKeeper.LogRecord logRecord = (LogKeeper.LogRecord) msg.obj;
                    logText.append(logRecord.getLine() + "\n");
                    break;
                case MSG_PROCESS_STOPPED:
                    unbindNodeService();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void sendRegisterMessage() {
        try {
            Message msg = Message.obtain(null, NodeService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mService.send(msg);
        } catch (RemoteException e) {
            mService = null;
            renderButtons();
        }
    }

    public void sendUnregisterMessage() {
        if (mService == null)
            return;

        try {
            Message msg = Message.obtain(null, NodeService.MSG_UNREGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mService.send(msg);
        } catch (RemoteException e) {
            // RemoteException -> service has been already destroyed -> no need to unregister
        } finally {
            mService = null;
            renderButtons();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
