package com.kmeb.remindme.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.kmeb.remindme.R;
import com.kmeb.remindme.activity.Singin;
import com.kmeb.remindme.receiver.RemindReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by mooning on 2015/7/7.
 */
public class RemindService extends Service {
    private String TAG = "RemindService";
    private int index;                                  //当日下一次提醒
    public final static int NOTIFICATION_ID = 1206;     //提醒通知的ID
    public final static int SIGN_IN_COUNTS = 4;         //每日签到数
    public static boolean isFromReceiver = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate(){
        super.onCreate();
        index = 0;
        Log.d(TAG, "onCreate");
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Log.d(TAG, "onStartConmmand");
        new doSend().execute();
        /*sendNotification();*/
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        /**/
        SharedPreferences pref = getSharedPreferences("time", Context.MODE_PRIVATE);
        Boolean isServiceOpened = pref.getBoolean("isServiceOpened", false);
        if(!isServiceOpened) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent remindIntent = new Intent(this, RemindReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, remindIntent, 0);
            alarmManager.cancel(pi);
        }else{
            Intent i = new Intent(this, RemindService.class);
            startService(i);
        }
    }
    class doSend extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... params) {
            return sendNotification();
        }
        @Override
        protected void onPostExecute(String result){
            if(result!=null&&!result.isEmpty())
                Toast.makeText(RemindService.this, "下次提醒时间：" + result, Toast.LENGTH_SHORT).show();
        }
    }
    private String sendNotification(){
        String result = null;
        SharedPreferences pref = getSharedPreferences("time",MODE_PRIVATE);
        Boolean isWorkday = pref.getBoolean("isWorkday", true);
        //发送通知消息
        if(index != 0 && isWorkday && isFromReceiver) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
            Intent i = new Intent(this, Singin.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);        //如果Intent要启动的Activity在栈顶，则无须创建新的实例
            PendingIntent pendingIntent= PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            Notification notification = new Notification.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setTicker(getTicker())                             //状态栏文本
                    .setSmallIcon(R.drawable.ic_launcher)           //状态栏图标
                    .setAutoCancel(true)
                    .setSound(Uri.parse("android.resource://com.kmeb.remindme/" + R.raw.ring))
                    .setContentTitle(getTitle())
                    .setContentText(getText())
                    .setVibrate(new long[]{0, 200, 300, 200, 400, 300})
                    .getNotification();
            notificationManager.notify(NOTIFICATION_ID, notification);
            //通知发送后index加一
            index = (index) % SIGN_IN_COUNTS + 1;
        }
        isFromReceiver = false;
        //取消其他提醒
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent remindIntent = new Intent(this, RemindReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, remindIntent, 0);
        alarmManager.cancel(pi);
        //非工作日设置为第二天提醒
        if(!isWorkday) {
            index = SIGN_IN_COUNTS + 1;
        }
        //计算下次提醒时间
        for (;index<=SIGN_IN_COUNTS*2;index++){
            String nextTime = pref.getString("time" + ((index-1)%SIGN_IN_COUNTS+1), null);
            String[] time;
            if(nextTime==null||nextTime.isEmpty()||(time=nextTime.split(":")).length<2)continue;
            int aheadTime = pref.getInt("ahead" + ((index-1)%SIGN_IN_COUNTS+1),0);
            //计算nextTime对应的时间
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(time[1])-aheadTime);
            calendar.set(Calendar.SECOND, 0);
            if(index>SIGN_IN_COUNTS){
                calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+1);
            }
            if(calendar.after(Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00")))) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Log.d("time", sdf.format(calendar.getTime()));
                result = sdf.format(calendar.getTime());
                //Toast.makeText(this, "下次提醒时间：" + sdf.format(calendar.getTime()), Toast.LENGTH_SHORT).show();
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                break;
            }
        }
        index = (index-1)%SIGN_IN_COUNTS+1;
        return result;
    }


    public String getTicker() {
        String ticker ;
        switch (index){
            case 1:
                ticker = "记得打卡哦";
                break;
            case 2:
                ticker = "记得打卡哦";
                break;
            case 3:
                ticker = "记得打卡哦";
                break;
            case 4:
                ticker = "温馨提示：夕阳西下，打卡回家！";
                break;
            default:
                ticker = "";
                break;
        }
        return ticker;
    }

    public String getTitle() {
        String title;
        switch (index){
            case 1:
                title = "打卡提醒";
                break;
            case 2:
                title = "温馨提示";
                break;
            case 3:
                title = "温馨提示";
                break;
            case 4:
                title = "温馨提示";
                break;
            default:
                title = "";
                break;
        }
        return title;
    }
    private String getText() {
        String text;
        switch (index){
            case 1:
                text = "迎着清晨第一缕阳光，梦想在此刻起航！";
                break;
            case 2:
                text = "午饭前不要忘记打卡哦。";
                break;
            case 3:
                text = "赶紧打卡哦，不然要迟到了！";
                break;
            case 4:
                text = "忙碌了一天，下班记得打卡哦。";
                break;
            default:
                text = "";
                break;
        }
        return text;
    }
}
