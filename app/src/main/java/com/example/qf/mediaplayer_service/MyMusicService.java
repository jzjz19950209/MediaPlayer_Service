package com.example.qf.mediaplayer_service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyMusicService extends Service {
    public MediaPlayer mediaPlayer;
    public static final String path = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "netease" + File.separator +
            "cloudmusic" + File.separator + "Music";
    public File[] files;
    private int currentProgress;
    private String fileName;
    private boolean ran=false;
    private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    private List<String> list = new ArrayList<>();
    private int currentPosition =0;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        files = new File(path).listFiles();
        for (File f : files) {
            list.add(f.getName());
        }
        initMediaPlayer(currentPosition);

    }

    private void initMediaPlayer(int position) {
        //发送正在播放的文件名
        fileName = list.get(position);
        sendFileName(fileName);
        currentPosition=position;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.fromFile(new File(path, fileName)));
            mediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(ran) {
                   choose(getRandom());
                }else {
                    if (currentPosition == list.size() - 1) {
                        currentPosition = -1;
                    }
                    currentPosition += 1;
                    choose(currentPosition);
                }
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getIntExtra("type",0)){
            case 0:
                if (mediaPlayer == null) {
                    initMediaPlayer(currentPosition);
                }
                start();
                updateSeekBar_normal();
                playStatus();
                break;
            case 1:
                pause();
                playStatus();
                break;
            case 2:
                stop();
                //seekBar归零
                Intent clear_current=new Intent("updateUI");
                clear_current.putExtra("what","clear");
                sendBroadcast(clear_current);
                playStatus();
                break;
            case 3:
                last();
                updateSeekBar_normal();
                playStatus();
                break;
            case 4:
                next();
                updateSeekBar_normal();
                playStatus();
                break;
            case 5:
                currentProgress=intent.getIntExtra("currentProgress",0);
                mediaPlayer.seekTo(currentProgress);
                chooseProgress(currentProgress);
                playStatus();
                break;
            case 6:
                choose(intent.getIntExtra("position",0));
                updateSeekBar_normal();
                playStatus();
                break;
            case 7:
                //发送歌曲的文件名列表
                Intent fileList=new Intent("updateUI");
                fileList.putExtra("what","fileList");
                fileList.putStringArrayListExtra("fileList", (ArrayList<String>) list);
                sendBroadcast(fileList);

                updatePlayMode();

                sendFileName(list.get(currentPosition));

                sendTime();
                playStatus();
                break;
            case 8:
                ran=!ran;
                updatePlayMode();
                break;
        }


        return super.onStartCommand(intent, flags, startId);
    }




    public void start() {
        mediaPlayer.start();
        MainActivity.canPlay=false;
    }
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        MainActivity.canPlay=true;
    }
    public void last(){
       // initMediaPlayer(currentPosition);
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.reset();
        }else {
            stop();
        }
        if(ran) {
            currentPosition=getRandom();
        }else {
            if (currentPosition != 0) {
                currentPosition -= 1;
            } else {
                currentPosition = list.size() - 1;
            }
        }

        initMediaPlayer(currentPosition);
        start();
    }
    public void next(){
        if(!mediaPlayer.isPlaying()){

            mediaPlayer.reset();
        }else {
            stop();
        }
        if(ran){
           currentPosition=getRandom();
        }else {
            if (currentPosition != list.size() - 1) {
                currentPosition += 1;
            } else {
                currentPosition = 0;
            }
        }
        initMediaPlayer(currentPosition);
        start();
    }
    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            initMediaPlayer(currentPosition);
            MainActivity.canPlay = true;

        }
    }


    //选择歌曲
    public void choose(int position) {
        if (mediaPlayer.isPlaying()){
            mediaPlayer.reset();
        }
        currentPosition = position;
        initMediaPlayer(currentPosition);
        start();

    }
    //发送当前播放的文件名
    public void sendFileName(String fileName){
        Intent intent_name=new Intent("updateUI");
        intent_name.putExtra("what","change_name");
        intent_name.putExtra("fileName",fileName);
        sendBroadcast(intent_name);
    }
    //发送当前歌曲时间和总时间
    public void sendTime(){
        Intent intent1 = new Intent("updateUI");
        intent1.putExtra("what", "seekBar_normal");
        intent1.putExtra("currentPosition", mediaPlayer.getCurrentPosition());
        intent1.putExtra("duration", mediaPlayer.getDuration());
        sendBroadcast(intent1);
    }

    private void sendMsg(String what){
        Intent intent=new Intent("updateUI");
        intent.putExtra("what",what);
        sendBroadcast(intent);
    }
    //播放状态下自动更新进度条
    public void updateSeekBar_normal(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer.isPlaying()) {
                    while (mediaPlayer.getDuration() != mediaPlayer.getCurrentPosition()) {
                        //发送当前播放进度、总播放进度
                        sendTime();
                        SystemClock.sleep(1000);
                        if (!mediaPlayer.isPlaying()){
                            return;
                        }
                    }
                }
            }
        }).start();
    }
    //选择进度条位置
    public void chooseProgress(int c){
        //initMediaPlayer(currentPosition);
        Intent intent_c=new Intent("updateUI");
        intent_c.putExtra("what","chooseProgress");
        intent_c.putExtra("currentProgress",c);
        sendBroadcast(intent_c);

    }
    //获取随机数
    public int getRandom(){
        Random random=new Random();
        int r=random.nextInt(list.size());
        return r;
    }
    //更新播放模式
    public void updatePlayMode(){
        Intent intent_ran=new Intent("updateUI");
        intent_ran.putExtra("what","playMode");
        if(ran){
            intent_ran.putExtra("ran","随机播放");
        }else {
            intent_ran.putExtra("ran","循环播放");
        }
        sendBroadcast(intent_ran);
    }

    //播放状态
    public void playStatus(){
        if (mediaPlayer.isPlaying()){
            //发送播放状态
            sendMsg("play");
        }else {
            //发送暂停或停止状态
            sendMsg("pause_stop");
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.reset();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

}
