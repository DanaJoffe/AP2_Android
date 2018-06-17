package com.dana.maayan.imageserviceandroid;

import android.util.Log;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * responsible for network connection to ImageServer.
 */
public class SingletonClient {

    private static final String SERVER_IP = "10.0.2.2"; //server IP address
    private static final int SERVER_PORT = 8500;
    private Socket socket;

    /**
     * static variable single_instance of type Singleton
     */
    private static SingletonClient single_instance = null;

    /**
     * private constructor restricted to this class itself
     */
    private SingletonClient() { }

    /**
     * static method to create instance of Singleton class
     * @return the single instance of Singleton class
     */
    public static SingletonClient getInstance()
    {
        if (single_instance == null)
            single_instance = new SingletonClient();
        return single_instance;
    }

    /**
     * open connection socket with ImageServer
     * @return true if the connection established well, o.w false
     */
    public boolean ConnectToServer() {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try  {
                        //here you must put your computer's IP address. -- localhost
                        InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                        // create a socket to make the connection with the server
                        socket = new Socket(serverAddr, SERVER_PORT);
                    } catch (Exception e) {
                        Log.e("TCP", "ConnectToServer thread: Error", e);
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (Exception e) {
            Log.e("TCP", "ConnectToServer: Error", e);
            return false;
        }
        return true;
    }

    /**
     * close connection socket with ImageServer
     */
    public void CloseConnection() {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try  {
                        OutputStream output = socket.getOutputStream();
                        DataOutputStream dot = new DataOutputStream(output);
                        dot.writeInt(0);
                        dot.flush();
                        socket.close();
                    } catch (Exception e) {
                        Log.e("TCP", "CloseConnection-Sending String: Error", e);
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (Exception e) {
            Log.e("TCP", "CloseConnection: Error", e);
        }
    }

    /**
     * send a byte array to ImageServer.
     * Communication Protocol: 1st argument - integer, represents the length
     * of the upcoming byte array. 2st argument  - the byte array.
     * @param bytes byte array
     */
    public void SendBytesToServer(final byte[] bytes) {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try  {
                        // sends the message to the server
                        OutputStream output = socket.getOutputStream();
                        DataOutputStream dot = new DataOutputStream(output);
                        int numBytesReversed = Integer.reverseBytes(bytes.length);
                        dot.writeInt(numBytesReversed);
                        dot.flush();
                        dot.write(bytes);
                        dot.flush();
                    } catch (Exception e) {
                        Log.e("TCP", "SendPhotosToServer: Error", e);
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (Exception e) {
            Log.e("TCP", "SendPhotosToServer: Error", e);
        }
    }
}