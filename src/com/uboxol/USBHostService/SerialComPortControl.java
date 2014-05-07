package com.uboxol.USBHostService;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.*;
import android.os.Handler;
import android.os.Message;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


public class SerialComPortControl {

    private static final int SEND_CTR_TIMEOUT = 100;
    private static final int SEND_CTR_REQ_Type = 0x21;
    private static final int SEND_CTR_REQ = 0x20;

    private static final int RECV_CTR_LEN_REQ = 0x24;

    DataTransfer readDateTransfer = null;

    public final static int TimeOut = 100;

    private final int deviceVendorId = 1155;
    private final int deviceProductId = 22336;

    private UsbManager usbManager = null;
    private UsbDevice usbDevice = null;
    private UsbInterface[] usbInterface = null;
    private UsbEndpoint[][] usbEndPoint = new UsbEndpoint[5][5];
    private UsbDeviceConnection usbDeviceConnection = null;

    private Context context = null;


    private List<SerialComPort> portList ;

    public DeviceStatus status = DeviceStatus.NOT_CONNECT;

    Timer timerSend = null;
    Timer timerRead = null;      //接受定时器


    //接收消息
    ReadMessageHandler readMessageHandler = new ReadMessageHandler();
    ReadUsbMessageThread readThread = null;

    SendMessageHandler sendMessageCOM1Handler = new SendMessageHandler(0);
    SendMessageHandler sendMessageCOM2Handler = new SendMessageHandler(1);
    SendMessageHandler sendMessageCOM3Handler = new SendMessageHandler(2);
    SendMessageHandler sendMessageCOM4Handler = new SendMessageHandler(3);
    SendMessageHandler sendMessageCOM5Handler = new SendMessageHandler(4);




    SerialComPortControl(int portCounts,Context context) throws Exception
    {
        portList = new ArrayList<SerialComPort>();
        for (int i = 0; i < portCounts; i++) {
            portList.add( new SerialComPort(i + 1));
        }
        this.context = context;
        this.usbManager = (UsbManager) this.context.getSystemService(Context.USB_SERVICE);
    }

    TimerTask readTimerTask = null;
    TimerTask sendTimerTask =  null;

    class ReadTimerTask extends TimerTask
    {
        @Override
        public void run() {
            // 需要做的事:发送消息
            readMessageHandler.obtainMessage(1).sendToTarget();
        }
    }
    class SendTimerTask extends TimerTask
    {
        @Override
        public void run() {
            // 需要做的事:发送消息
            sendMessageCOM1Handler.obtainMessage(1).sendToTarget();
            sendMessageCOM2Handler.obtainMessage(1).sendToTarget();
            sendMessageCOM3Handler.obtainMessage(1).sendToTarget();
            sendMessageCOM4Handler.obtainMessage(1).sendToTarget();
            sendMessageCOM5Handler.obtainMessage(1).sendToTarget();

        }
    }

    public void disconnectAllPort() throws Exception
    {
        for ( SerialComPort com : portList)
        {
            com.removeAction();
        }
    }
    public void disconnectPort(String appAction) throws Exception
    {
        for ( SerialComPort com : portList)
        {
            com.close();
            com.removeAction(appAction);
        }
    }


    private SerialComPort getPort( int index)
    {
        return portList.get(index);
    }
    public void sendMessage(int portIndex, byte[] b, int len ) throws Exception
    {
        getPort(portIndex).send(b, 0, len);
    }

    /**
     * 判断是设备是否正确
     * @param device is the Device
     * @return is'nt true
     */
    private boolean isTheDevice(UsbDevice device)
    {
        if (device != null){
            return device.getVendorId() == deviceVendorId && device.getProductId() == deviceProductId;
        }
        return false;
    }

    /**
     * 检测USB设备
     * @return is'nt findDevice
     */
    private boolean findUsbDevice()
    {
        if( usbDevice != null && usbDevice.getVendorId() == deviceVendorId
                && usbDevice.getProductId() == deviceProductId)
        {
            return true;
        }
        this.usbManager = (UsbManager) this.context.getSystemService(Context.USB_SERVICE);
        HashMap< String, UsbDevice> deviceList = usbManager.getDeviceList();
        if(deviceList != null)
        {
            for (UsbDevice device : deviceList.values()) {
                if (isTheDevice(device)) {
                    Tools.debug("找到设备:" + device.getVendorId() +
                            "\t pid: " + device.getProductId());

                    usbDevice = device;
                    usbInterface = new UsbInterface[usbDevice.getInterfaceCount()];

                    for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
                        usbInterface[i] = usbDevice.getInterface(i);
                        for (int j = 0; j < usbInterface[i].getEndpointCount(); j++) {

                            usbEndPoint[i][j] = usbInterface[i].getEndpoint(j);
                            if (usbEndPoint[i][j].getDirection() == 0) {
                                printDebugLog("接口 " + i + "端点" + j + "的数据方向为输出");
                            } else {
                                printDebugLog("接口 " + i + "端点" + j + "的数据方向为输入");
                            }
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public void printDebugLog(String message)
    {
        Intent intent = new Intent(UboxAction.SERVICE_DEBUG_MESSAGE);
        intent.putExtra("msg",message);
        context.sendBroadcast(intent);
    }
    public DeviceStatus getDeviceStatus() throws Exception
    {
        try {
            if( status == DeviceStatus.CONNECTED) return status;

            if( findUsbDevice())
            {
                if(connectDevice())
                {
                    status = DeviceStatus.CONNECTED;
                }
                else
                {
                    //请求连接中
                    PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(UboxAction.ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(usbDevice, pi);
                    status = DeviceStatus.NO_PERMISSION;
                }

            }
            else
            {
                printDebugLog("not Find Device");
                status = DeviceStatus.NOT_CONNECT;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            Tools.error(e.getMessage());

        }
        return  status;
    }

    public boolean isConnected()
    {
        return status == DeviceStatus.CONNECTED;
    }

    /**
     * 打开USB设备
     * @return is'nt Open?
     */
    public boolean connectDevice()
    {
        this.usbManager = (UsbManager) this.context.getSystemService(Context.USB_SERVICE);
        if( findUsbDevice() && usbManager.hasPermission(usbDevice)){

            if( usbDeviceConnection != null){
                usbDeviceConnection.close();
            }
//            usbDeviceConnection.
            try {
                usbDeviceConnection = usbManager.openDevice( usbDevice);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }

            if (usbDeviceConnection == null)
            {
                printDebugLog("not open device");
                status = DeviceStatus.NOT_CONNECT;
                return false;
            }

            usbDeviceConnection.claimInterface(usbInterface[1], true);

            if(readThread != null)
            {
                readThread.stopThread();
            }
            readThread= new ReadUsbMessageThread();
            readThread.start();

            timerSend = new Timer();
            sendTimerTask = new SendTimerTask();
            timerSend.schedule(sendTimerTask, 0, TimeOut);

            status = DeviceStatus.CONNECTED;
            printDebugLog(DeviceStatus.CONNECTED.toString());
            return true;
        }
        else
        {
            printDebugLog("not find Device or no Permission");
            status = DeviceStatus.NOT_CONNECT;
            return false;
        }
    }
    public void checkCacheLength()
    {
        byte [] datas = new byte[10];
        usbDeviceConnection.controlTransfer(SEND_CTR_REQ_Type | UsbConstants.USB_DIR_IN, RECV_CTR_LEN_REQ, 0,0,datas,10,SEND_CTR_TIMEOUT);
        for (SerialComPort port: portList)
        {
            int l = datas[port.getPortId() * 2 - 1] & 0xff;
            int h = datas[port.getPortId() * 2 - 2] & 0xff;
            port.maxSendLength = ( l << 8) | h;
            printDebugLog("" + port.maxSendLength);
        }

        printDebugLog(String.format("%02x %02x %02x %02x %02x %02x %02x %02x %02x %02x ",datas[0],datas[1],datas[2],datas[3],datas[4],datas[5],datas[6],datas[7],datas[8],datas[9]));
    }
    public SerialComPortStatus open( int com_id, int baud_rate, int stop_bits, int data_bits, int parity, String appAction) throws Exception
    {
        SerialComPort port = getPort( com_id - 1);

        switch (getDeviceStatus())
        {
            case NOT_CONNECT:
                port.close();
                break;
            case CONNECTED:{
                if ( usbDeviceConnection != null)
                {
                    disconnectPort(appAction);
                    if ( port.getAppAction().equals("") || port.getAppAction().equals(appAction))
                    {
                        port.open(baud_rate, stop_bits, data_bits, parity);
                        port.setAppAction( appAction);
                        int len = 7;
                        byte[] data = port.getSerialBytes();
                        int value = usbDeviceConnection.controlTransfer( SEND_CTR_REQ_Type | UsbConstants.USB_DIR_OUT, SEND_CTR_REQ, 0, 0, data, len, SEND_CTR_TIMEOUT);

                        while (value < len && value != 0)
                        {
                            len = len  - value;
                            byte[] sendByte = new byte[len];
                            System.arraycopy( data,value,sendByte,0,len);

                            value = usbDeviceConnection.controlTransfer( SEND_CTR_REQ_Type | UsbConstants.USB_DIR_OUT , SEND_CTR_REQ, 0, 0, sendByte, len, SEND_CTR_TIMEOUT);
                            data = sendByte;
                        }
                        return SerialComPortStatus.CONNECTED; // success
                    }
                    else
                    {
                        return SerialComPortStatus.BE_USAGE;
                    }
                }
                else
                {
                    return SerialComPortStatus.DEVICE_NOT_CONNECT;
                }
            }
            case NO_PERMISSION:
                return SerialComPortStatus.DEVICE_NO_PERMISSION;
        }
        return port.getStatus();

    }


    public void sendMessageFromCom(String action, int com, byte[] datas, int length)
    {
        if(action!= null)
        {
            Intent intent = new Intent(action);
            intent.putExtra(UboxAction.EXTRA_MESSAGE_COM_ID,com);
            intent.putExtra(UboxAction.EXTRA_MESSAGE_DATA,datas);
            intent.putExtra(UboxAction.EXTRA_MESSAGE_LEN,length);
            context.sendBroadcast(intent);
        }
    }

    /**
     * 接收消息Handler
     */
    class ReadMessageHandler extends Handler {


        @Override
        public void handleMessage(Message msg) {

            for (SerialComPort port : portList)
            {
                if (port.getAppAction().equals("") || port.getReadSize() == 0)
                {
                    continue;
                }
                byte[] data = new byte[1024];
                int len = port.read(data,0,1024);
                while (len > 0)
                {
                    sendMessageFromCom(port.getAppAction(), port.getPortId(), data,  len);
                    len = port.read(data,0,1024);
                }

            }
        }
    }

    /**
     * 发送消息Handler
     */
    class SendMessageHandler extends Handler {

        private int index = 0;
        SendMessageHandler(int index)
        {
            this.index = index;
        }

        @Override
        public void handleMessage(Message msg) {



            SerialComPort port = portList.get(index);

            if (port.getSendSize() == 0)
            {
                return;
            }
            checkCacheLength();

            int maxLen = port.maxSendLength - 63;
            int length = 0;

            while (length < maxLen - 63)
            {
                byte [] data = new byte[64];
                int len = port.getSendData(data, 0, 60);
                if (len <=0)
                {
                    break;
                }
                byte [] datas = DataTransfer.getDataBuffer( port.getPortId(), data, len);
                int dataLength = len + 3;
                int value = usbDeviceConnection.bulkTransfer(usbEndPoint[1][0],datas, len + 3,100);
                while (value != dataLength && value != 0)
                {
                    dataLength = dataLength  - value;
                    byte[] sendByte = new byte[dataLength];
                    System.arraycopy( datas,value,sendByte,0,dataLength);

                    value = usbDeviceConnection.bulkTransfer(usbEndPoint[1][0],sendByte, len + 3,100);
                    datas = sendByte;
                }
                length += len;
            }
        }
    }

    /**
     * 接收消息线程
     */
    class ReadUsbMessageThread extends Thread{

        private final int RECV_LENGTH = 64 * 1024;
        private boolean mThreadControl;
        public ReadUsbMessageThread(){
            readDateTransfer = new DataTransfer();
            this.mThreadControl = true;
        }

        public void stopThread()
        {
            this.mThreadControl = false;
        }
        @Override
        public void run() {

            while( this.mThreadControl && usbDeviceConnection != null)
            {
                try {

                    int dataLength;
                    byte[] myBuffer=new byte[RECV_LENGTH];


                    dataLength = usbDeviceConnection.bulkTransfer( usbEndPoint[1][1], myBuffer, RECV_LENGTH, 100);

                    for (int i = 0; i < dataLength; i++) {
                        if( readDateTransfer.AddData(myBuffer[i]))
                        {
                            SerialComPort port = portList.get( readDateTransfer.getWho()  -1);
                            if( port != null && !port.getAppAction().equals(""))
                            {
                                byte [] datas = readDateTransfer.getDatas();

                                sendMessageFromCom(port.getAppAction(),port.getPortId(),datas,readDateTransfer.getLen());
                                Tools.WriteData(port.getPortId(), datas);
                            }
                            readDateTransfer.reset();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void close() throws Exception
    {
        disconnectAllPort();
        status = DeviceStatus.NOT_CONNECT;

        if ( readTimerTask != null)
        {
            readTimerTask.cancel();
            readTimerTask = null;
        }

        if ( sendTimerTask != null)
        {
            sendTimerTask.cancel();
            sendTimerTask = null;
        }


        if(timerRead!= null)
        {
            timerRead.cancel();
            timerRead = null;
        }
        if (timerSend != null)
        {
            timerSend.cancel();
            timerSend = null;
        }



        if (readThread != null)
        {
            readThread.stopThread();
        }
        if (usbDeviceConnection != null)
        {

            for (int i = 0; i < usbInterface.length; i++) {
                if (usbInterface[i] != null)
                {
                    usbDeviceConnection.releaseInterface(usbInterface[i]);
                }
            }

            usbInterface = null;
            usbDeviceConnection.close();
            usbDeviceConnection = null;
            usbDevice = null;
        }
    }

    /**
     * 将int转为低字节在前，高字节在后的byte数组
     */
    public static byte[] toLH(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }
}
