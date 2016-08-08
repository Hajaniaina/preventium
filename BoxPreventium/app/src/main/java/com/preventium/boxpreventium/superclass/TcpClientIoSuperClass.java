package com.preventium.boxpreventium.superclass;

import android.util.Log;

import com.preventium.boxpreventium.utils.Chrono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by Franck on 08/08/2016.
 */

public class TcpClientIoSuperClass {

    private final static String TAG = "TcpClientIoSuperClass";

    public interface MessageListener {
        public void messageReceived(byte[] data, boolean success, int requestCode);
        public void messageSend(byte[] data, boolean success, int requestCode);
    }
    public interface NotifyListener {
        public void onConnectionStateChanged(boolean connected);
        public void onConnected();
        public void onDisconnected();
        public void onNotify(byte[] data);
    }

    protected Socket mSocket = null;
    protected InputStream mIs = null;
    protected OutputStream mOs = null;
    private NotifyListener mNotify = null;

    public TcpClientIoSuperClass( NotifyListener listener ) { mNotify = listener; }

    public boolean connectTo( String host, int port, int timeout ) {
        boolean ret = false;
        SocketAddress socketAddress = new InetSocketAddress( host, port );
        mSocket = new Socket();

        try {
            mSocket.connect( socketAddress, timeout );
            if( mSocket.isConnected() ) {
                if( initializeInputOutputStream() ) {
                    if( mNotify != null ) {
                        mNotify.onConnectionStateChanged( true );
                        mNotify.onConnected();
                    }
                    ret = true;
                }
                else{
                    disconnect();
                }
            }
        } catch (IOException e) {
//            Log.d(TAG,"IOException when trying to connect socket");
//            e.printStackTrace();
        }
        return ret;
    }

    public void disconnect() {
        if( mSocket != null ) {

            if(( mOs != null && !mSocket.isOutputShutdown() ) )
                try { mOs.close(); } catch (IOException e) { e.printStackTrace(); }

            if(( mIs != null && !mSocket.isInputShutdown() ) )
                try { mIs.close(); } catch (IOException e) { e.printStackTrace(); }

            if(( mSocket.isConnected() ) )
                try {
                    mSocket.close();
                    if( mNotify != null ) {
                        mNotify.onConnectionStateChanged( false );
                        mNotify.onDisconnected();
                    }
                } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public boolean isConnected(int timeout) {
        boolean ret = false;
        try {
            ret = ( mSocket != null && mSocket.isConnected()
                    && mSocket.getInetAddress().isReachable(timeout) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public InputStream getInputStream(){ return mIs; }
    public OutputStream getOutputStream(){ return mOs; }

    public boolean write( byte[] bytes ) {
        boolean ret = false;
        try {
            if ( mSocket.isConnected() ) {
                mOs.write( bytes );
                ret = true;
            } else {
                Log.i(TAG, "Cannot send message. Socket is closed");
            }
        } catch (Exception e) {
            Log.i(TAG, "Message send failed. Caught an exception");
        }
        return ret;
    }

    public boolean write( byte[] bytes, MessageListener message) {
        boolean ret = write(bytes);
        if( message != null ) message.messageSend(bytes,ret,-1);
        return ret;
    }

    public boolean write(byte[] bytes, int requestCode, MessageListener message) {
        boolean ret = write(bytes);
        if( message != null ) message.messageSend(bytes,ret,requestCode);
        return ret;
    }

    public byte[] read(int length, int timeout_ms) {
        byte[] buffer;
        int available = 0;
        Chrono elapsed = Chrono.newInstance();
        elapsed.start();
        long ms = 0;
        while( mSocket.isConnected() && ms < (long)timeout_ms && available < length ) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ms = elapsed.getMilliseconds();
            try {
                available = mIs.available();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if( available >= length )
        {
            buffer = new byte[length];
            try {
                mIs.read(buffer,0,length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if( available > 0 ) {
            buffer = new byte[available];
            try {
                mIs.read(buffer,0,available);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            buffer = new byte[0];
        }
        return buffer;
    }

    public byte[] read(int length, int timeout_ms, MessageListener message) {
        byte[] buffer = read(length,timeout_ms);
        if( message != null ) message.messageReceived(buffer, (buffer.length == length), -1 );
        return buffer;
    }

    public byte[] read(int length, int timeout_ms, int requestCode, MessageListener message) {
        byte[] buffer = read(length,timeout_ms);
        if( message != null ) message.messageReceived(buffer, (buffer.length == length), requestCode );
        return buffer;
    }

    public byte[] writeAndRead( byte[] bytes, int length, int timeout_ms ) {
        write(bytes);
        return read(length,timeout_ms);
    }

    public byte[] writeAndRead( byte[] bytes, int length, int timeout_ms, MessageListener message ) {
        write(bytes,message);
        return read(length,timeout_ms,message);
    }

    public byte[] writeAndRead( byte[] bytes, int length, int timeout_ms, int requestCode, MessageListener message ) {
        write(bytes,requestCode,message);
        return read(length,timeout_ms,requestCode,message);
    }

    public void flush() {
        if ( mSocket.isConnected() ) {
            if (mOs != null) try { mOs.flush(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private boolean initializeInputOutputStream(){
        return ( initializeInputStream() && initializeOutputStream() );
    }

    private boolean initializeInputStream(){
        try {
            mIs = mSocket.getInputStream();
        } catch (IOException e) {
            Log.i(TAG, "IOException when trying to get socket input stream");
            e.printStackTrace();
        }
        return ( mIs != null );
    }

    private boolean initializeOutputStream(){
        try {
            mOs= mSocket.getOutputStream();
        } catch (IOException e) {
            Log.i(TAG, "IOException when trying to get socket output stream");
            e.printStackTrace();
        }
        return( mOs != null );
    }
}
