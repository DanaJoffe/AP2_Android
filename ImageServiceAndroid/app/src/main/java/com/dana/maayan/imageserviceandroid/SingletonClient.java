package com.dana.maayan.imageserviceandroid;

import android.util.Log;
import android.util.Xml;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class SingletonClient {

    private static final String SERVER_IP = "10.0.2.2"; //server IP address
    private static final int SERVER_PORT = 8500;
    private Socket socket;

    // static variable single_instance of type Singleton
    private static SingletonClient single_instance = null;

    // private constructor restricted to this class itself
    private SingletonClient() { }

    // static method to create instance of Singleton class
    public static SingletonClient getInstance()
    {
        if (single_instance == null)
            single_instance = new SingletonClient();
        return single_instance;
    }

    private boolean ConnectToServer() {
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

    private void CloseConnection() {
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

    // TEST
//    private void SendMsgToServer(final String someString) {
//        try {
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try  {
//                        // sends the message to the server
//                        OutputStream output = socket.getOutputStream();
//                        byte[] bytes = someString.getBytes();
//
//                        DataOutputStream dot = new DataOutputStream(output);
//                        int numBytesReversed = Integer.reverseBytes(bytes.length);
//
//                        dot.writeInt(numBytesReversed);
//                        dot.flush();
//
//                        dot.write(bytes);
//                        dot.flush();
//                    } catch (Exception e) {
//                        Log.e("TCP", "Sending String: Error", e);
//                    }
//                }
//            });
//            thread.start();
//            thread.join();
//        } catch (Exception e) {
//            Log.e("TCP", "S: Error", e);
//        }
//    }


    private void SendPhotosToServer(final byte[] imgbyte) {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try  {
                        // sends the message to the server
                        OutputStream output = socket.getOutputStream();
                        byte[] bytes = imgbyte;
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

    public boolean SendPhotosToImageService(final byte[] imgbyte) {
        try {
            if (ConnectToServer()) {
                SendPhotosToServer(imgbyte);
                CloseConnection();
            } else {
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e("TCP", "SendPhotosToImageService: Error", e);
            return false;
        }
    }
}
