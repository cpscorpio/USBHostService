package com.uboxol.USBHostService;

import java.util.SimpleTimeZone;

/**
 * Created by chenpeng on 14-4-23.
 */
public class MessageBuffer {
    private byte buf[];
    private int limit;
    private int readPos;
    private int writePos;
    private int size;

    MessageBuffer(int limit)
    {
        this.readPos = 0;
        this.writePos = 0;
        this.size = 0;
        this.limit = limit;
        buf = new byte[limit];
    }

    public int getSize()
    {
        return this.size;
    }
    public synchronized int read() {

        if( this.size > 0 )
        {
            size --;
            return  buf[ readPos ++ % limit];
        }
        else
        {
            return -1;
        }
    }

    public synchronized int read(byte b[], int off, int len) {

        if (size == 0) {
            return -1;
        }


        if (len > size) {
            len = size;
        }
        if (len <= 0) {
            return 0;
        }
        readPos %= limit;
        if (readPos + len < limit)
        {
            System.arraycopy(buf, readPos, b, off, len);

        }
        else
        {
            int length = limit - readPos;
            System.arraycopy( buf, readPos, b, off, length);
            System.arraycopy( buf, 0, b, off + length, len - length);

        }
        readPos = (readPos + len)% limit;
        size -= len;
        return len;
    }

    public synchronized void put(byte b)
    {
        if(size < limit)
        {
            size++;
            buf[writePos++%limit] = b;
        }
    }

    public synchronized int put(byte[] b,int off, int len)
    {
        if (size + len > limit)
        {
            len = limit - size;
        }

        writePos %= limit;
        if ( writePos + len <= limit)
        {
            System.arraycopy( b,0,buf, writePos,len);

        }
        else
        {
            int length = limit - writePos;
            System.arraycopy( b, off, buf, writePos,length);
            System.arraycopy( b, off + length, buf, 0, len - length);

        }
        writePos = (writePos + len) % limit;
        size += len;
        return len;
    }
}
