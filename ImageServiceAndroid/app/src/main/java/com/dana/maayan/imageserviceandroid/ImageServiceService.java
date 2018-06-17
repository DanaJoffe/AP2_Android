package com.dana.maayan.imageserviceandroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageServiceService extends Service {

    private static final String channel = "my_channel_01";
    private static final String name = "Channel human readable title";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * set a wifi connection listener that transfers all images in DCIM when wifi is ON.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // this method is called once, in service creation.
        // Here put the Code of Service

        //cellphone testings
        //SingletonClient s = SingletonClient.getInstance();
        //s.ConnectToServer();


        setWifiTransferListener();
    }

    private BroadcastReceiver yourReceiver;

    /**
     * set the code that will be executed when wifi is ON.
     */
    private void setWifiTransferListener() {
        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        theFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.yourReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("Service", "setWifiTransferListener: Invoked");
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    //get the different network states
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            // Starting the Transfer
                            startTransfer();
                        }
                    }
                }
            }
        };
        this.registerReceiver(this.yourReceiver, theFilter);
    }

    /**
     * initializing objects for Progress Bar.
     * @param builder a NotificationCompat.Builder
     * @param notificationManager a NotificationManager
     */
    private void setNotificationTools(NotificationCompat.Builder builder, NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            NotificationChannel mChannel = new NotificationChannel(channel,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(mChannel);
        }
        builder.setContentTitle("Picture Transfer").setContentText("Transfer in progress").setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
    }

    /**
     * transferring all images in DCIM to ImageServer.
     */
    private void startTransfer() {
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (dcim == null) {
            return;
        }
        final List<File> pics = new ArrayList<File>();
        listf(dcim.getPath(), pics);
        if (pics.size() == 0)
            return;
        SingletonClient s = SingletonClient.getInstance();
        if(!s.ConnectToServer())
            return;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel);
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        sendAllPicToImageService(pics, builder, notificationManager);
        s.CloseConnection();
    }

    /**
     * set the content text of the Progress Bar.
     * @param builder a builder
     * @param max the maximum value of count, in Progress Bar.
     * @param count the current value in Progress Bar.
     */
    private void setBuilderContent(NotificationCompat.Builder builder, int max, int count) {
        if (count < max/2)
            builder.setContentText("Transfering...").setProgress(max, count, false);
        else if (count < 4*max/5)
            builder.setContentText("Half way through...").setProgress(max, count, false);
        else
            builder.setContentText("Almost done...").setProgress(max, count, false);
    }

    /**
     * send all the pictures in pics to ImageService.
     * opens communication before sending, tnd closes it afterwards.
     * @param pics a pictures array.
     * @param builder a builder (for progress bar)
     * @param notificationManager a notificationManager (for progress bar)
     */
    private void sendAllPicToImageService(final List<File> pics, final NotificationCompat.Builder builder
            , final NotificationManager notificationManager) {
        setNotificationTools(builder, notificationManager);

        int count =0;
        for (File pic : pics) {
            try {
                sendPicToImageService(pic);
                setBuilderContent(builder, pics.size(), count);
                notificationManager.notify(1, builder.build());
                count += 1;
            } catch (Exception e) {}
        }
        // At the End
        builder.setProgress(0, 0, false);
        builder.setContentText("Download complete");
        notificationManager.notify(1, builder.build());
    }

    /**
     * send a single pictures to ImageService.
     * @param pic a single picture.
     * @return true if pic was sent successfully to ImageService, o.w. false.
     */
    private boolean sendPicToImageService(File pic) {
        try {
            SingletonClient s = SingletonClient.getInstance();
            FileInputStream fis = new FileInputStream(pic);
            Bitmap bm = BitmapFactory.decodeStream(fis);
            byte[] imgbyte = getBytesFromBitmap(bm);

            s.SendBytesToServer(pic.getName().getBytes());
            s.SendBytesToServer(imgbyte);
            return true;
        } catch (Exception e) {
            Log.e("Service", "sendPicToImageService: Error", e);
            return false;
        }
    }

    /**
     * searches recursively for images in the given directory path (directoryName )
     * @param directoryName directory path
     * @param files list of files to be filled
     */
    public void listf(String directoryName, List<File> files) {
        File directory = new File(directoryName);

        // Get all the files from a directory.
        File[] fList = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return true;
            }
        });
        for (File file : fList) {
            if (file.isFile() && isImage(file.getName())) {
                files.add(file);
            } else if (file.isDirectory()) {
                listf(file.getAbsolutePath(), files);
            }
        }
    }

    /**
     * checks if a given file is an image.
     * @param img file's name
     * @return true if the file is an image, false o.w
     */
    private boolean isImage(String img) {
        List<String> imageSuffixes = new ArrayList<String>();
        imageSuffixes.add(".jpg");
        imageSuffixes.add(".png");
        imageSuffixes.add(".gif");
        imageSuffixes.add(".bmp");
        for(String suffix : imageSuffixes) {
            if (img.endsWith(suffix))
                return true;
        }
        return false;
    }

    /** parsing bitmap  to byte array.
     *
     * @param bitmap a bitmap.
     * @return the bitmap in bytes.
     */
    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service starting...", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    /**
     * destroys the Service.
     */
    @Override
    public void onDestroy() {
        Toast.makeText(this,"Service ending...", Toast.LENGTH_SHORT).show();
        //SingletonClient s = SingletonClient.getInstance();
        //s.CloseConnection();
    }
}
