package com.uboxol.USBHostService;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.FileOutputStream;

/**
 * Created by chenpeng on 14-4-11.
 */



public class Tools {

    private final static String FILE_NAME = "/mnt/sdcard/log_com_server";

//    public static  void WriteData(String message)
//    {
//        try{
//
//            FileOutputStream fout = new FileOutputStream(FILE_NAME,true);
//
//            byte [] bytes = message.getBytes();
//
//            fout.write(bytes);
//
//            fout.close();
//
//        }
//
//        catch(Exception e){
//
//            e.printStackTrace();
//
//        }
//    }
//
    public static  void WriteData( int com, byte [] bytes)
    {
        try{
            FileOutputStream fout = new FileOutputStream(FILE_NAME + com + ".txt",true);

            if (fout != null)
            {
                fout.write(bytes);
            }
            fout.close();;
        }

        catch(Exception e){
            e.printStackTrace();
        }
    }
    public final static ComponentName getTopActivityCN(Context context)
    {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn= am.getRunningTasks(1).get(0).topActivity;
        Log.d("TopActivity", "Package ---\t" + cn.getPackageName());
        Log.d("TopActivity", "Class   ---\t" + cn.getClassName());
        return cn;
    }

    public final static void startActivity(Context context, Bundle data, ComponentName cn)
    {
        context.startActivity(new Intent()
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .putExtras(data)
                .setComponent(cn)
        );
    }

    public static void showDialog(Context context, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog ad = builder.create();
        //ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG); //系统中关机对话框就是这个属性
        ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        ad.setCanceledOnTouchOutside(false); //点击外面区域不会让dialog消失
        ad.show();
    }

    public final static void sendBroadCase(Context context, String action, Bundle data)
    {
        context.sendBroadcast(new Intent(action).putExtras(data));
    }
//    public final static void showShortToast(android.content.Context context, java.lang.CharSequence message)
//    {
//        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
//    }
//
//    public final static void showLongToast(android.content.Context context, java.lang.CharSequence message)
//    {
//        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
//    }

    public static void warn( String message)
    {
        Log.w(getClassName() + "::" + getMethodName(), message + getLineNumber());
    }
    public static void error(String message)
    {
        Log.e(getClassName() + "::" + getMethodName(), message + getLineNumber());
    }
    public static void info(String message)
    {
        Log.i(getClassName()+"::"+getMethodName(),message + getLineNumber());
    }
    public static void debug(String message)
    {
        Log.d(getClassName()+"::"+getMethodName(),message + getLineNumber());
    }






    private static String getLineNumber() {
        return "  [" + Thread.currentThread().getStackTrace()[4].getLineNumber() + "]";
    }

    private static String getClassName() {

        String className = Thread.currentThread().getStackTrace()[4].getClassName();
        return className.substring(className.lastIndexOf('.') + 1);
    }

    private static String getMethodName() {
        return Thread.currentThread().getStackTrace()[4].getMethodName();
    }

    private static  String getFileName() {
        return Thread.currentThread().getStackTrace()[4].getFileName();
    }
}
