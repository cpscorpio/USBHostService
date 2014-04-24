package com.uboxol.USBHostService;


enum SerialComPortStatus {
    NOT_CONNECT(0),
    CONNECTED(1),
    BE_USAGE(2),
    CONNECT_ERROR(3) ;

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



    private String appAction = "";
    public long ts = System.currentTimeMillis();

    public int maxSendLength = 0;

    public MessageBuffer sendBuffer = new MessageBuffer(0x500000);  //16M

    public MessageBuffer readBuffer = new MessageBuffer(0x500000);  //16M


    SerialComPort(int com)
    {
        this.portId = com;
        this.stopBits = 0;
        this.baudRate = 9600;
        this.dataBits = 8;
        this.parity = 0;
        this.appAction = "";
    }

    SerialComPort(int com, int baudRate, int stopBits, int dataBits, int parity)
    {
        this.portId = com;
        this.stopBits = stopBits;
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.parity = parity;
        this.appAction = "";
    }


    public void setMaxSendLength(byte [] b)
    {

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

    public byte[] toBytes()
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
