package com.doggonics.h2godemo2;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.doggonics.h2godemo.R;


public class MainActivity<TAG> extends AppCompatActivity {

    private static SeekBar sb_throttle;
    private static ImageView imgv_rpm_indicator;
    private static TextView tv_big_display;
    private static TextView tv_small_display;
    private static ImageButton btn_display_mode;
    private static ImageButton btn_start;
    private static ImageView vw_battery_indicator;
    private static TextView tv_rpm;
    private static ViewGroup ll_throttle;
    private static ViewGroup ll_display;
//    private static ImageButton btn_powerON;

    private static final String TAG="H2GO";
    private static final Integer RPM_SERVICE_START=500;
    private static final Integer RPM_OKAY=2400;
    private static final Integer RPM_TOO_HIGH=3100;
    private static final Integer READY_STATE=0;
    private static final Integer SERVICE_STATE=1;
    private static final Integer PAUSE_STATE=2;

    private ViewGroup myButtonView;
    private int seconds=0;
    private int last_seconds=0;
    private boolean is_ready=true;
    private boolean is_started=false;
    private Integer state=READY_STATE;
    private boolean start_service=false;
    private Long hashCode;


    private int display_mode=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sb_throttle=(SeekBar)findViewById(R.id.sb_throttle);
        tv_big_display=(TextView)findViewById(R.id.tv_displayLine1);
        tv_small_display=(TextView)findViewById(R.id.tv_displayLine2);
        btn_display_mode=(ImageButton)findViewById(R.id.button_display);
        btn_start =(ImageButton)findViewById(R.id.button_start);
//        btn_powerON=(ImageButton)findViewById(R.id.button_power);
        vw_battery_indicator=(ImageView)findViewById(R.id.img_battery);
        tv_rpm=(TextView)findViewById(R.id.tv_rpm);
        myButtonView=(ViewGroup)findViewById(R.id.ll_buttons);
        ll_throttle=(ViewGroup)findViewById(R.id.ll_throttle);
        ll_display=(ViewGroup)findViewById(R.id.ll_display);

        vw_battery_indicator.setVisibility(View.INVISIBLE);
        tv_small_display.setVisibility(View.INVISIBLE);


        /*btn_powerON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                is_started=start_module(is_started,vw_battery_indicator,ll_display,sb_throttle,imgv_rpm_indicator);
            }
        });*/


        btn_start.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(btn_display_mode.isPressed()){
                    Log.i(TAG,"Double Press");
                    is_started=start_module(is_started,vw_battery_indicator,ll_display,sb_throttle,imgv_rpm_indicator);
                }
                return true;
            }
        });
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(start_service){
                    start_service=false;
                    state=READY_STATE;
                    tv_small_display.setText("READY");
                    display_mode=0;
                }
                else {
                    start_service=true;
                    state=SERVICE_STATE;
                    tv_small_display.setText("SERVICE");
                    seconds=0;
                    tv_big_display.setText("00:00:00");
                    if (sb_throttle.getProgress()>RPM_OKAY && sb_throttle.getProgress()<=RPM_TOO_HIGH){
                        imgv_rpm_indicator.setImageResource(R.drawable.grn_led);
                    }else if(sb_throttle.getProgress()>RPM_TOO_HIGH){
                        imgv_rpm_indicator.setImageResource(R.drawable.red_led);
                    }
                }
            }
        });

        btn_display_mode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });

        btn_display_mode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (is_started) {
                    if (display_mode < 1) {
                        display_mode++;
                    } else {
                        display_mode = 0;
                    }
                }
            }
        });

        setSeekBar();
        runTimer();
        tv_big_display.setText("");
        tv_small_display.setText("");

    }
    public  void setSeekBar(){
        imgv_rpm_indicator=(ImageView)findViewById(R.id.img_rpm_status);

        sb_throttle.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress_value;
                    Integer rpm_status=0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        progress_value=progress;
                        tv_rpm.setText(progress_value +" rpm");
                       if(progress_value<RPM_SERVICE_START){rpm_status= 0;}
                        else if(progress_value<RPM_OKAY){rpm_status=1;}
                        else if(progress_value<RPM_TOO_HIGH) {rpm_status=2;}
                        else {rpm_status=3;}
                        if(start_service){
                            switch (rpm_status){
                                case 0:
                                    imgv_rpm_indicator.setImageResource((R.drawable.round_led));
                                    imgv_rpm_indicator.invalidate();
                                    tv_small_display.setText("PAUSE");
                                    break;
                                case 1:
                                    imgv_rpm_indicator.setImageResource(R.drawable.round_led);
                                    imgv_rpm_indicator.invalidate();
                                    tv_small_display.setText("SERVICE");
                                    break;
                                case 2:
                                    imgv_rpm_indicator.setImageResource(R.drawable.grn_led);
                                    imgv_rpm_indicator.invalidate();
                                    tv_small_display.setText("SERVICE");
                                    break;
                                case 3:
                                    imgv_rpm_indicator.setImageResource(R.drawable.red_led);
                                    imgv_rpm_indicator.invalidate();
                                    tv_small_display.setText("SERVICE");
                                    break;

                                default:{};
                            }
                        }
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
    }
    public void runTimer(){
        final Handler handler= new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours=seconds/3600;
                int minutes=(seconds%3600)/60;
                int secs=seconds%60;
                String time=String.format("%02d:%02d:%02d",hours,minutes,secs);
                hashCode= Math.abs(Long.valueOf(Math.abs(time.hashCode())*100));
                String str_hash=Long.toString(hashCode);

                if(display_mode==0 && state==READY_STATE){tv_big_display.setText(time);}

                if(display_mode==1 && state==READY_STATE){tv_big_display.setText("#"+str_hash);}

                if(display_mode==0 && state==SERVICE_STATE){tv_big_display.setText(time);}

                if(display_mode==1 && state==SERVICE_STATE && sb_throttle.getProgress()>RPM_SERVICE_START){
                    tv_big_display.setText(sb_throttle.getProgress()+" rpm");
                }

                if(display_mode==1 && state==SERVICE_STATE && sb_throttle.getProgress()<RPM_SERVICE_START){
                    tv_big_display.setText("#"+str_hash);
                }
                if(start_service &&sb_throttle.getProgress()>RPM_SERVICE_START){ seconds++;}
                handler.postDelayed(this,1000);
            }
        });
    }
    public boolean start_module(Boolean is_started,ImageView battery_indicator,ViewGroup vg_textdisplay,SeekBar throttle,ImageView rpm_indicator){
        TextView big_display= (TextView) vg_textdisplay.getChildAt(1);
        TextView small_display= (TextView) vg_textdisplay.getChildAt(0);;

        if (!is_started) {
            battery_indicator.setVisibility(View.VISIBLE);
            small_display.setVisibility(View.VISIBLE);
            big_display.setVisibility(View.VISIBLE);
            big_display.setText("00:00:00");
            small_display.setText("READY");
            return true;
        } else if (is_started) {
            battery_indicator.setVisibility(View.INVISIBLE);
            tv_small_display.setVisibility(View.INVISIBLE);
            tv_big_display.setVisibility(View.INVISIBLE);
            imgv_rpm_indicator.setImageResource(R.drawable.round_led);
            seconds = 0;
            start_service=false;
            return false;
        } else {return false;}
    }
}
