package com.uboxol.USBHostService;


enum SerialComPortStatus {
    NOT_CONNECT(-1),
    CONNECTED(0),
    DEVICE_NOT_CONNECT(1),
    DEVICE_NO_PERMISSION(2),
    BE_USAGE(3);
    private int value;

    SerialComPortStatus(int v)
    {
        this.value = v;
    }

    public  int getValue()
    {
        return this.value;
    }
}


public class SerialComPort {

    private int baudRate = 0;   // 110 / 300 / 600 / 9600/ 115200 /256000

    private int stopBits = 0;   //0=1stop bit, 1=1.5 stop bit, 2=2 stop bit;

    private int parity = 0;     //0=none, 1=Odd(奇校验), 2=Even(偶校验)

    private int portId = 0;

    private int dataBits = 0;   //7 or 8

    private SerialComPortStatus serialComPortStatus;

    private String appAction = "";

    public int maxSendLength = 0;

    private MessageBuffer sendBuffer = null;  //5M

    private MessageBuffer readBuffer = null;  //5M


    SerialComPort(int com)
    {
        this.portId = com;
        this.stopBits = 0;
        this.baudRate = 9600;
        this.dataBits = 8;
        this.parity = 0;
        this.appAction = "";
        serialComPortStatus = SerialComPortStatus.NOT_CONNECT;
    }

    public void open(int baudRate, int stopBits, int dataBits, int parity)
    {
        this.stopBits = stopBits;
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.parity = parity;
        this.readBuffer = new MessageBuffer(5 * this.baudRate);
        this.sendBuffer = new MessageBuffer(0x200000);
        this.serialComPortStatus = SerialComPortStatus.CONNECTED;
    }
    public void close()
    {
        this.readBuffer = null;
        this.sendBuffer = null;
        this.serialComPortStatus = SerialComPortStatus.NOT_CONNECT;
    }

    SerialComPort(int com, int baudRate, int stopBits, int dataBits, int parity)
    {
        this.portId = com;
        this.stopBits = stopBits;
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.parity = parity;
        this.appAction = "";
        serialComPortStatus = SerialComPortStatus.NOT_CONNECT;
    }
    public SerialComPortStatus getStatus() {
        return serialComPortStatus;
    }

    public void setStatus(SerialComPortStatus status) {
        this.serialComPortStatus = status;
    }
    public String getString()
    {
        return String.format("COM%d baudRate:%d, stopBits %d, parity %d, dataBits %d", this.portId, this.baudRate, this.stopBits, this.parity, this.dataBits);
    }
    public String getAppAction() {
        return appAction;
    }

    public void removeAction( String appAction)
    {
        if(appAction.equals(this.appAction))
        {
            this.appAction = "";
        }
    }
    public void removeAction ()
    {
        this.appAction = "";
    }
    public void setAppAction(String appAction) {
        this.appAction = appAction;
    }

    public void send( byte[] b, int offset, int len) {
        if (sendBuffer!= null)
        {
            sendBuffer.put(b, offset, len);
        }
    }

    public int getSendData( byte[] b, int offset, int len)
    {
        if (sendBuffer!= null)
        {
            return sendBuffer.read(b, offset, len);
        }
        return 0;
    }

    public void putReadData(byte[] b, int offset, int len)
    {
        if (readBuffer!= null)
        {
            readBuffer.put(b, offset, len);
        }
    }
    public int read( byte[] b, int offset, int len) {
        if (readBuffer != null)
        {
            return readBuffer.read(b,offset,len);
        }
        return 0;
    }

    public int getReadSize()
    {
        if (readBuffer != null)
        {
            return readBuffer.getSize();
        }
        else
        {
            return 0;
        }
    }

    public int getSendSize()
    {
        if (sendBuffer != null)
        {
            return sendBuffer.getSize();
        }
        else
        {
            return 0;
        }
    }
    public byte[] getSerialBytes()
    {
        /**
         * uint32_t bitrate; //110 / 300 / 9600/ 115200 ...
         * uint8_t stop_bits; //0=1stop bit, 1=1.5 stop bit, 2=2 stop bit;
         * uint8_t paritytype; //0=none, 1=Odd(奇校验), 2=Even(偶校验)
         * uint8_t datatype; //7 or 8 (高3位代表串口编号:1~5 代表串口1到串口5)
         */

        byte[] dataBuffer = new byte[7];

        byte[] temp = SerialComPortControl.toLH(this.baudRate);
        System.arraycopy(temp, 0, dataBuffer, 0, temp.length);

        temp = new byte[1];
        temp[0] = (byte)( this.stopBits & 0xff);

        System.arraycopy(temp, 0, dataBuffer, 4, temp.length);


        temp[0] = (byte)( this.parity & 0xff);
        System.arraycopy(temp, 0, dataBuffer, 5, temp.length);


        temp[0] = (byte)( ( ( this.portId << 5 ) | this.dataBits ) & 0xff);
        System.arraycopy(temp, 0, dataBuffer, 6, temp.length);

        return dataBuffer;

    }
    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public int getStopBits() {
        return stopBits;
    }

    /**
     * 从start开始数组arr中截取长度为length的子数组
     * @return 新数组
     */
    private byte[] subArray(byte[] arr, int start, int length)
    {
        byte[] newArr = new byte[length];
        System.arraycopy(arr,start,newArr,0,length);
        return newArr;
    }
    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public int getPortId() {
        return portId;
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }
}
