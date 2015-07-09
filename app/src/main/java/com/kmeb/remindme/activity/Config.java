package com.kmeb.remindme.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.kmeb.remindme.R;
import com.kmeb.remindme.service.RemindService;

public class Config extends Activity {

    private Switch serviceSwitch;
    private Intent startIntent,stopIntent;
    private EditText[] time;
    private EditText[] ahead;
    private Switch workdaySwitch;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        init();
        //restartService();
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getSharedPreferences("time",MODE_PRIVATE).edit();
                editor.putBoolean("isServiceOpened",isChecked);
                editor.apply();
                restartService();
            }
        });
        workdaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getSharedPreferences("time",MODE_PRIVATE).edit();
                editor.putBoolean("isWorkday",isChecked);
                editor.apply();
                restartService();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("time",MODE_PRIVATE).edit();
                editor.clear();
                editor.putBoolean("isServiceOpened",serviceSwitch.isChecked());
                editor.putBoolean("isWorkday",workdaySwitch.isChecked());
                for(int i=1;i<5;i++){
                    String timeString,aheadString;
                    if((timeString = time[i-1].getText().toString())!=null&&!timeString.isEmpty()) {
                        editor.putString("time" + i, timeString);
                    }else {
                        editor.putString("time" + i, "");
                    }
                    if((aheadString = ahead[i-1].getText().toString())!=null&&!aheadString.isEmpty()) {
                        editor.putInt("ahead" + i, Integer.parseInt(aheadString));
                    }
                }
                editor.apply();
                restartService();
                Toast.makeText(Config.this,"保存成功！",Toast.LENGTH_SHORT).show();
            }
        });
    }
    void init(){
        SharedPreferences pref = getSharedPreferences("time",MODE_PRIVATE);
        time = new EditText[4];
        ahead = new EditText[4];
        time[0] = (EditText)findViewById(R.id.time1);
        time[1] = (EditText)findViewById(R.id.time2);
        time[2] = (EditText)findViewById(R.id.time3);
        time[3] = (EditText)findViewById(R.id.time4);
        ahead[0] = (EditText)findViewById(R.id.ahead1);
        ahead[1] = (EditText)findViewById(R.id.ahead2);
        ahead[2] = (EditText)findViewById(R.id.ahead3);
        ahead[3] = (EditText)findViewById(R.id.ahead4);
        time[0].setText(pref.getString("time1","9:10"));
        ahead[0].setText(""+pref.getInt("ahead1", 10));
        time[1].setText(pref.getString("time2", "11:45"));
        ahead[1].setText("" + pref.getInt("ahead2", 0));
        time[2].setText(pref.getString("time3", "13:00"));
        ahead[2].setText("" + pref.getInt("ahead3", 10));
        time[3].setText(pref.getString("time4", "17:30"));
        ahead[3].setText("" + pref.getInt("ahead4", 0));

        serviceSwitch = (Switch)findViewById(R.id.switch1);
        workdaySwitch = (Switch)findViewById(R.id.switch2);
        serviceSwitch.setChecked(pref.getBoolean("isServiceOpened", false));
        workdaySwitch.setChecked(pref.getBoolean("isWorkday", true));

        saveButton = (Button)findViewById(R.id.button);
    }
    void startService(){
        if(startIntent==null) {
            startIntent = new Intent(this, RemindService.class);
        }
        startService(startIntent);
    }
    void stopService(){
        if(stopIntent==null) {
            stopIntent = new Intent(this, RemindService.class);
        }
        stopService(stopIntent);
    }
    void restartService(){
        stopService();
        if(serviceSwitch.isChecked())
            startService();
    }
}
