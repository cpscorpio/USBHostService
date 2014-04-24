package com.uboxol.USBHostService;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.content.pm.ProviderInfo;
import android.hardware.usb.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.util.*;

/**
 * Created by chenpeng on 14-4-11.
 */
public class MainService extends Service {

    private List<String> listeningActions;

    private List<String> toOpenDeviceApps = new ArrayList<String>();

    SerialComPortControl serialComPortControl = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public  void onCreate()
    {
        Tools.info("start create service.");
        listeningActions = new ArrayList<String>();
        listeningActions.add(UboxAction.SERVICE_OPEN_COM);
        listeningActions.add(UboxAction.SERVICE_COM1_MESSAGE);
        listeningActions.add(UboxAction.SERVICE_COM2_MESSAGE);
        listeningActions.add(UboxAction.SERVICE_COM3_MESSAGE);
        listeningActions.add(UboxAction.SERVICE_COM4_MESSAGE);
        listeningActions.add(UboxAction.SERVICE_COM5_MESSAGE);
        listeningActions.add(UboxAction.SERVICE_INIT_MESSAGE);
        listeningActions.add(UboxAction.SERVICE_DISCONNECT);

        listeningActions.add(UboxAction.CACHE_LENGTH_MESSAGE);

        listeningActions.add(UboxAction.ACTION_USB_PERMISSION);

        listeningActions.add(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);

        listeningActions.add(UsbManager.ACTION_USB_ACCESSORY_DETACHED);

        listeningActions.add(UsbManager.ACTION_USB_DEVICE_ATTACHED);

        listeningActions.add(UsbManager.ACTION_USB_DEVICE_DETACHED);
        Tools.info("1");
        try {
            serialComPortControl = new SerialComPortControl(5,this);
            Intent intent = new Intent(UboxAction.SERVICE_START_MESSAGE);
            intent.putExtra("msg","start succeed");
            sendBroadcast(intent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Tools.info("start create service end.");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Tools.info("flags " + flags + ", startId " + startId);

        this.registerMyReceiver();
        flags = START_STICKY;

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 判读服务是否启动
     */
    public static boolean isServiceRunning(Context context)
    {
        ActivityManager activityManagermanager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManagermanager.getRunningServices(Integer.MAX_VALUE)) {
            if (MainService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 接受广播
     */
    private BroadcastReceiver broadCastReceiver = new BroadcastReceiver(){


        @Override
        public void onReceive(Context context, Intent intent) {
            Tools.info("action " + intent.getAction());
            Tools.getTopActivityCN(context);

            try {

                if( intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED))    //设备被拔出
                {
                    serialComPortControl.close();
                }
                else if(intent.getAction().equals(UboxAction.CACHE_LENGTH_MESSAGE))
                {
                    serialComPortControl.checkCacheLength();
                }
                else if(intent.getAction().equals(UboxAction.ACTION_USB_PERMISSION))    //获取USB权限返回值
                {
                    boolean needSend = serialComPortControl.status == DeviceStatus.CONNECTING;


                    if( intent.getBooleanExtra( UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        //获取权限成功
                        serialComPortControl.connectDevice();
                    }
                    else
                    {
                        serialComPortControl.status = DeviceStatus.NOT_CONNECT;
                    }
                    if ( needSend)
                    {
                        printDebugLog("need" + ", size " + toOpenDeviceApps.size());
                        for (String appAction : toOpenDeviceApps)
                        {
                            Intent i = new Intent( appAction);
                            i.putExtra(UboxAction.EXTRA_INIT_STATUS,serialComPortControl.status.getValue());
                            sendBroadcast(i);
                        }
                        toOpenDeviceApps.clear();
                    }
                    else
                    {
                        printDebugLog("not need");
                    }
                }
                else if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
                {
                    //插入设备监听 TODO 无效
                }
                else if(intent.getAction().equals(UboxAction.SERVICE_DISCONNECT))
                {
                    //APP主动发起关闭
                    String appAction = intent.getStringExtra(UboxAction.APP_ACTION);
                    if(appAction != null){
                        serialComPortControl.disconnectPort(appAction);
                    }
                }
                else if (intent.getAction().equals(UboxAction.SERVICE_INIT_MESSAGE))    //App请求打开设备，询问初始化与否
                {
                    try {
                        DeviceStatus status = serialComPortControl.getDeviceStatus();

                        if(status.equals(DeviceStatus.CONNECTING))
                        {
                            printDebugLog("add " + intent.getStringExtra(UboxAction.APP_ACTION) + " to list");
                            toOpenDeviceApps.add(intent.getStringExtra(UboxAction.APP_ACTION));
                        }

                        Intent i = new Intent( intent.getStringExtra(UboxAction.APP_ACTION));
                        i.putExtra(UboxAction.EXTRA_INIT_STATUS,status.getValue());
                        sendBroadcast(i);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }


                }
                else if (intent.getAction().equals(UboxAction.SERVICE_OPEN_COM))
                {
                    String appAction = intent.getStringExtra(UboxAction.APP_ACTION);
                    if(appAction != null)
                    {
                        SerialComPortStatus connected = SerialComPortStatus.CONNECTED;

                        if( serialComPortControl.isConnected())
                        {
                            SerialComPort port = new SerialComPort(
                                    intent.getIntExtra(UboxAction.CONFIG_COM,0),
                                    intent.getIntExtra(UboxAction.CONFIG_BIT_RATE,9600),
                                    intent.getIntExtra(UboxAction.CONFIG_STOP_BITS,0),
                                    intent.getIntExtra(UboxAction.CONFIG_DATA_TYPE,8),
                                    intent.getIntExtra(UboxAction.CONFIG_PARITY_TYPE,0) );
                            connected = serialComPortControl.open( port, appAction);
                        }
                        else
                        {
                            connected = SerialComPortStatus.NOT_CONNECT;
                        }

                        //return to APP
                        Intent i = new Intent(appAction);
                        i.putExtra(UboxAction.EXTRA_COM_STATUS,connected.getValue());
                        sendBroadcast(i);
                    }
                }
                else if(UboxAction.isExist( UboxAction.COMS, intent.getAction())) //是否是发给串口消息
                {
                    String appAction = intent.getStringExtra(UboxAction.APP_ACTION);
                    int comID = intent.getIntExtra(UboxAction.EXTRA_MESSAGE_COM_ID,0);
                    byte[] datas = intent.getByteArrayExtra( UboxAction.EXTRA_MESSAGE_DATA);
                    int len = intent.getIntExtra( UboxAction.EXTRA_MESSAGE_LEN,0);
                    serialComPortControl.sendMessage( comID - 1, datas, len);

                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    };
    public void printDebugLog(String message)
    {
        Intent intent = new Intent(UboxAction.SERVICE_DEBUG_MESSAGE);
        intent.putExtra("msg",message);
        this.sendBroadcast(intent);
    }
    /**
     * 注册接受广播
     * @return
     */
    private boolean registerMyReceiver()
    {
        try {
            IntentFilter myFilter = new IntentFilter();
            for (String action : listeningActions)
            {
                myFilter.addAction(action);
            }

            this.registerReceiver(broadCastReceiver, myFilter);
            Tools.info("registerReceiver OK");
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 取消接受广播
     * @return
     */
    private boolean unregisterMyReceiver()
    {
        try {
            this.unregisterReceiver(broadCastReceiver);
            Tools.info( "OK");
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 启动服务
     * @param context
     * @return
     */
    public static boolean startService(Context context)
    {
        try {
            Intent i = new Intent(context, MainService.class);
            ComponentName cn = context.startService(i);
            if( cn != null) Tools.info(cn.toString());
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 启动服务
     * @return
     */
    private boolean startService()
    {
        return startService(this);
    }

    @Override
    public void onDestroy() {
        try {
            serialComPortControl.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this.unregisterMyReceiver();
        startService();
    }



}
