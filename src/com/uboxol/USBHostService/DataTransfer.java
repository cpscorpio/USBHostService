package com.uboxol.USBHostService;


public class DataTransfer {
    private byte[] DataBuffer = new byte[64];
    private int DataBufferLen = 0;

    public DataTransfer(){
        reset();
    }

    public void reset()
    {
        DataBuffer = new byte[64];
        DataBufferLen = 0;
    }


    public static byte[] getDataBuffer(int who,byte[] datas, int len)
    {
        byte[] dataBuffer = new byte[len + 3];

        dataBuffer[0] = (byte)0xA5;
        dataBuffer[1] = (byte)(who & 0xff);
        dataBuffer[2] = (byte)(len & 0xff);
        int WritePos = 3;
        for (int i = 0; i < len ; i++)
        {
            dataBuffer[ WritePos ++] = datas[i];
        }
        return dataBuffer;
    }

    private boolean checkData()
    {
        return !(DataBufferLen > 0 && (DataBuffer[0] & 0xff) != 0xA5) &&
               !(DataBufferLen > 1 && (DataBuffer[1] & 0xff) > 0x05) &&
               !(DataBufferLen > 2 && (DataBuffer[2] & 0xff) > 0x3C);
    }

    public int getWho() {
        return (int)DataBuffer[1];
    }


    public int getLen() {
        return (int)DataBuffer[2];
    }

    public byte[] getDatas() {

        int length = (int)DataBuffer[2];
        byte[] data = new byte[length];
        System.arraycopy( DataBuffer, 3, data, 0, length);
        return data;
    }

    public boolean AddData(byte mbyte) {
        if(!checkData())
        {
            DataBufferLen = 0;
        }

        if( DataBufferLen < 63){
            DataBuffer[ DataBufferLen++]=mbyte;
        }

        return DataBufferLen >= getLen() + 3;
    }


    /**
     * 浮点转换为字节
     */
    public byte[] float2byte(float f) {

        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        // 翻转数组
        int len = b.length;
        // 建立一个与源数组元素类型相同的数组
        byte[] dest = new byte[len];
        // 为了防止修改源数组，将源数组拷贝一份副本
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        // 将顺位第i个与倒数第i个交换
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }

        return dest;

    }

    /**
     * 字节转换为浮点
     *
     * @param b 字节（至少4个字节）
     * @param index 开始位置
     */
    public float byte2float(byte[] b, int index) {
        int l;
        l = b[index];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }
}