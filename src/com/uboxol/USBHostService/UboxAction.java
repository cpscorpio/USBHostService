package com.uboxol.USBHostService;


/**
 * Created by chenpeng on 14-4-11.
 */


enum DeviceStatus {
    NOT_CONNECT(0),
    NO_PERMISSION(1),
    CONNECTED(2);

    private int value;
    DeviceStatus(int v)
    {
        value = v;
    }
    int getValue()
    {
        return this.value;
    }
}



public class UboxAction {


    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";


    public final static String SERVICE_INIT_MESSAGE = "ubox.service.init";
    public final static String EXTRA_INIT_STATUS = "device_status";


    public final static String CACHE_LENGTH_MESSAGE = "ubox.cache.length";


    //打开COM
    public final static String SERVICE_OPEN_COM = "ubox.service.open";
    public final static String EXTRA_COM_STATUS = "usbconfig_status";

    //提示消息
    public final static String EXTRA_MESSAGE = "message";

    //测试消息
    public final static String SERVICE_START_MESSAGE = "ubox.service.start";
    public final static String SERVICE_DEBUG_MESSAGE = "ubox.service.debug";

    //关闭COM
    public final static String SERVICE_DISCONNECT = "ubox.service.disconnect";

    public final static String SERVICE_COM1_MESSAGE = "ubox.message.com1";
    public final static String SERVICE_COM2_MESSAGE = "ubox.message.com2";
    public final static String SERVICE_COM3_MESSAGE = "ubox.message.com3";
    public final static String SERVICE_COM4_MESSAGE = "ubox.message.com4";
    public final static String SERVICE_COM5_MESSAGE = "ubox.message.com5";



    public final static String[] COMS = {"",
            SERVICE_COM1_MESSAGE,
            SERVICE_COM2_MESSAGE,
            SERVICE_COM3_MESSAGE,
            SERVICE_COM4_MESSAGE,
            SERVICE_COM5_MESSAGE
    };


    public final static String CONFIG_BIT_RATE = "bitrate";
    public final static String CONFIG_STOP_BITS = "stop_bits";
    public final static String CONFIG_PARITY_TYPE = "paritytype";
    public final static String CONFIG_DATA_TYPE = "datatype";
    public final static String CONFIG_COM = "com";

    public final static String APP_ACTION = "action";


    /**
     * EXTRA for message
     */
    public final static String EXTRA_MESSAGE_COM_ID = "com";
    public final static String EXTRA_MESSAGE_LEN = "len";
    public final static String EXTRA_MESSAGE_DATA = "datas";
    public final static String EXTRA_MESSAGE_STRING = "message";


    public final static boolean isExist(String[] arr, String value)
    {
        for (int i = 0; i < arr.length; i++)
        {
            if (value.equals(arr[i]))
            {
                return true;
            }
        }
        return false;
    }


}
