package com.example.qf.mediaplayer_service;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SeekBar seekBar;
    public static final String path = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "netease" + File.separator +
            "cloudmusic" + File.separator + "Music";
    public File[] files;
    private int currentProgress;
    private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    private TextView currentTime, totalTime, name,now;
    private MyMusicService myMusicService;
    private List<String> list = new ArrayList<>();
    private int currentPosition = 0;
    private Button btn;
    private View pwView;
    private ObjectAnimator rotation;
    private Intent to_service;
    private ImageView image_music;
    private ImageView imageView;
    public static boolean canPlay = true, isPlaying = false;
    private MyReceiver myReceiver;
    private PopupWindow pw;
    private RotateAnimation rotateAnimation;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String str = (String) msg.obj;
                    name.setText(str);
                    break;
                case 2:
                    imageView.startAnimation(rotateAnimation);
                    btn.setBackgroundResource(R.drawable.player_toolbar_pause_normal);
                    break;
                case 3:
                    imageView.clearAnimation();
                    btn.setBackgroundResource(R.drawable.player_toolbar_play_normal);
                    break;
                case 4:
                    seekBar.setProgress(msg.arg1);
                    seekBar.setMax(msg.arg2);
                    totalTime.setText(sdf.format(new Date(msg.arg2)));
                    currentTime.setText(sdf.format(new Date(msg.arg1)));
                    break;
                case 5:
                    seekBar.setProgress(msg.arg1);
                    currentTime.setText(sdf.format(new Date(msg.arg1)));
                    break;
                case 6:
                    currentTime.setText(sdf.format(new Date(0)));
                    seekBar.setProgress(0);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        to_service = new Intent(this, MyMusicService.class);
        register();

        btn = (Button) findViewById(R.id.play_pause);
        name = (TextView) findViewById(R.id.name);
        imageView = (ImageView) findViewById(R.id.image);
        image_music = (ImageView) findViewById(R.id.image_music);
        now= (TextView) findViewById(R.id.now);
        myMusicService = new MyMusicService();

        rotateAnimation = new RotateAnimation(0, 359, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(25000);
        rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());

//        rotation = ObjectAnimator.ofFloat(imageView,"rotation", 0, 359);
//        rotation.setDuration(30000);
//        rotation.setRepeatMode(ValueAnimator.INFINITE);

        currentTime = (TextView) findViewById(R.id.currentTime);
        totalTime = (TextView) findViewById(R.id.totalTime);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        pwView = LayoutInflater.from(this).inflate(R.layout.listview, null);

        pw = new PopupWindow(pwView, LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics()));

        pw.setOutsideTouchable(true);
        pw.setBackgroundDrawable(new BitmapDrawable());
        pw.setFocusable(true);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                to_service.putExtra("type", 5);
                to_service.putExtra("currentProgress", currentProgress);
                startService(to_service);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //启动服务更新界面
        to_service.putExtra("type", 7);
        startService(to_service);
        //根据播放状态改变button的背景（点击back退出后回来）
        if (isPlaying) {
            btn.setBackgroundResource(R.drawable.player_toolbar_pause_normal);
        } else {
            btn.setBackgroundResource(R.drawable.player_toolbar_play_normal);
        }

    }

    public void initListView(View pwView) {
        ListView listView = (ListView) pwView.findViewById(R.id.lv);
        MyAdapter adapter = new MyAdapter(MainActivity.this, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, list.get(position), Toast.LENGTH_SHORT).show();
                to_service.putExtra("type", 6);
                to_service.putExtra("position", position);
                startService(to_service);
                pw.dismiss();
            }
        });

    }

    public void openList(View view) {
        initListView(pwView);
        pw.setAnimationStyle(R.style.pwStyle);
        pw.showAtLocation(view, Gravity.BOTTOM, 0, 0);

    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String what = intent.getStringExtra("what");
            switch (what) {
                case "change_name":
                    Message msg_2 = Message.obtain();
                    msg_2.obj = intent.getStringExtra("fileName");
                    msg_2.what = 1;
                    mHandler.sendMessage(msg_2);
                case "play":
                    mHandler.sendEmptyMessage(2);
                    isPlaying = true;
                    now.setText("正在播放：");
                    break;
                case "pause_stop":
                    isPlaying = false;
                    mHandler.sendEmptyMessage(3);
                    now.setText("当前选择：");
                    break;
                case "seekBar_normal":
                    Message msg = Message.obtain();
                    msg.what = 4;
                    msg.arg1 = intent.getIntExtra("currentPosition", 0);
                    msg.arg2 = intent.getIntExtra("duration", 0);
                    mHandler.sendMessage(msg);
                    break;
                case "chooseProgress":
                    Message msg_choose = Message.obtain();
                    msg_choose.what = 5;
                    msg_choose.arg1 = intent.getIntExtra("currentProgress", 0);
                    mHandler.sendMessage(msg_choose);
                    break;
                case "clear":
                    mHandler.sendEmptyMessage(6);
                    break;
                case "fileList":
                    list = intent.getStringArrayListExtra("fileList");
                    break;
            }
        }
    }

    public void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("updateUI");
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver, filter);
    }
    public void last(View view) {
        to_service.putExtra("type", 3);
        startService(to_service);
    }

    public void start_pause(View view) {
        if (canPlay) {
            to_service.putExtra("type", 0);
            startService(to_service);
        } else {
            to_service.putExtra("type", 1);
            startService(to_service);
        }
    }

    public void next(View view) {
        to_service.putExtra("type", 4);
        startService(to_service);
    }

    public void stop(View view) {
        to_service.putExtra("type", 2);
        startService(to_service);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

}
