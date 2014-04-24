package com.uboxol.USBHostService;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */


    private Button button ;
    private TextView textView;
    private int comId = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textView = (TextView)findViewById(R.id.textView);
        button = (Button)findViewById(R.id.button);
        resetButtonView();
        Intent i = new Intent(this, MainService.class);
        startService(i);


        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UboxAction.SERVICE_OPEN_COM);
                intent.putExtra(UboxAction.CONFIG_COM,comId);
                intent.putExtra(UboxAction.CONFIG_BIT_RATE,115200);
                intent.putExtra(UboxAction.APP_ACTION,UboxAction.SERVICE_DEBUG_MESSAGE);
                sendBroadcast(intent);
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Tools.showDialog(MainActivity.this,"我是对话框!");
                Intent intent = new Intent(UboxAction.COMS[comId]);
                String message ="hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello" +
                        " world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!world!hello world!hello world!hello world!hello world!hello worl" +
                        "d!hello world!hello world!hello world!world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!hello world!";
                byte[] datas = message.getBytes();
                intent.putExtra(UboxAction.EXTRA_MESSAGE_LEN,datas.length);
                intent.putExtra(UboxAction.EXTRA_MESSAGE_DATA,datas);
                intent.putExtra(UboxAction.EXTRA_MESSAGE_COM_ID,comId);
                intent.putExtra(UboxAction.APP_ACTION,UboxAction.SERVICE_DEBUG_MESSAGE);
                sendBroadcast(intent);
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(UboxAction.SERVICE_INIT_MESSAGE);
                    i.putExtra(UboxAction.APP_ACTION,UboxAction.SERVICE_DEBUG_MESSAGE);
                    sendBroadcast(i);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(UboxAction.CACHE_LENGTH_MESSAGE);
                    i.putExtra(UboxAction.APP_ACTION,UboxAction.SERVICE_DEBUG_MESSAGE);
                    sendBroadcast(i);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });
    }

    private void resetButtonView()
    {
        if(MainService.isServiceRunning(MainActivity.this))
        {
            button.setText("运行中...");
            button.setEnabled(false);
            textView.setText("服务正在运行");
        }
        else
        {
            button.setEnabled(true);
            button.setText("启动服务");
            textView.setText("服务没有运行");
        }

    }
    DataReceiver dataReceiver;//BroadcastReceiver对象
    private class DataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

//            textView.setText(textView.getText().toString() + " \n action :" + intent.getAction());
            try {
                if(intent.getAction().equals(UboxAction.SERVICE_START_MESSAGE))
                {
                    resetButtonView();
                }
                if(intent.getAction().equals(UboxAction.SERVICE_DEBUG_MESSAGE))
                {
                    if(intent.getIntExtra(UboxAction.EXTRA_INIT_STATUS,10) != 10)
                    {
                        textView.setText(textView.getText().toString() + ", " + intent.getIntExtra(UboxAction.EXTRA_INIT_STATUS,10) + "\n");
                    }

                    if (intent.getIntExtra(UboxAction.EXTRA_COM_STATUS,10) != 10)
                    {
                        textView.setText(textView.getText().toString() + ", " + intent.getIntExtra(UboxAction.EXTRA_COM_STATUS,10) + "\n");
                    }
                    if(intent.getByteArrayExtra(UboxAction.EXTRA_MESSAGE_DATA) != null)
                    {
                        textView.setText(textView.getText().toString() + ",\n Message :" + new String(intent.getByteArrayExtra(UboxAction.EXTRA_MESSAGE_DATA)));
                    }
                    if(intent.getStringExtra("msg") != null) {
                        textView.setText(textView.getText().toString() + "\n debug" + intent.getStringExtra("msg"));
                    }
                    if(intent.getIntExtra("len",0) > 0)
                    {
                        textView.setText(textView.getText().toString() + "\n from usb" + intent.getByteArrayExtra("byte").toString());
                    }

                    return;
                }
                if(intent.getAction().equals(UboxAction.SERVICE_INIT_MESSAGE))
                {
                    textView.setText(textView.getText().toString() + "\ninit:" + (intent.getBooleanExtra("init",false) ? "true":"false"));
                    return;
                }
                Tools.info("action " + intent.getAction());
                Tools.getTopActivityCN(context);
            }
            catch (Exception e)
            {
                e.printStackTrace();
               Tools.error(e.toString());
            }

        }
    }
    @Override
    public void onStart()
    {
        dataReceiver = new DataReceiver();
        IntentFilter filter = new IntentFilter();//创建IntentFilter对象
        filter.addAction(UboxAction.SERVICE_START_MESSAGE);
        filter.addAction(UboxAction.SERVICE_DEBUG_MESSAGE);
        registerReceiver(dataReceiver, filter);//注册Broadcast Receiver
        super.onStart();
    }

    @Override
    public void onStop()
    {
        Intent intent = new Intent(UboxAction.SERVICE_DISCONNECT);
        intent.putExtra(UboxAction.APP_ACTION,UboxAction.SERVICE_DEBUG_MESSAGE);
        sendBroadcast(intent);
        unregisterReceiver(dataReceiver);//取消注册Broadcast Receiver
        super.onStop();
    }
}
