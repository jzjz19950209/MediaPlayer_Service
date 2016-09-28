package com.example.qf.mediaplayer_service;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MyMusicService extends Service {
    public MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    public static final String path = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "netease" + File.separator +
            "cloudmusic" + File.separator + "Music";
    public File[] files;
    private int currentProgress;
    private String fileName;
    private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    private TextView currentTime, totalTime, name;
    private MyMusicService myMusicService;
    private ServiceConnection serviceConnection;
    private List<String> list = new ArrayList<>();
    private int currentPosition =0;
    private Button btn;
    private ListView lv;

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
//        Intent intent_name=new Intent("updateUI");
//        intent_name.putExtra("what","change_name");
//        intent_name.putExtra("fileName",fileName);
//        sendBroadcast(intent_name);
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
                if(currentPosition==list.size()-1) {
                    currentPosition=-1;
                }
                currentPosition += 1;
                choose(currentPosition);
            }
        });

    }
    public void choose(int position) {
        fileName = list.get(position);
        if (mediaPlayer.isPlaying()){
            mediaPlayer.reset();
        }
        currentPosition = position;
        initMediaPlayer(currentPosition);
        start();

    }

    public void sendFileName(String fileName){
        Intent intent_name=new Intent("updateUI");
        intent_name.putExtra("what","change_name");
        intent_name.putExtra("fileName",fileName);
        sendBroadcast(intent_name);
    }
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getIntExtra("type",0)){
            case 0:
                if (mediaPlayer == null) {
                    initMediaPlayer(currentPosition);
                }
                start();
                updateSeekBar_normal();
                break;
            case 1:
                pause();
                break;
            case 2:
                stop();
                //seekBar归零
                Intent clear_current=new Intent("updateUI");
                clear_current.putExtra("what","clear");
                sendBroadcast(clear_current);
                break;
            case 3:
                last();
                updateSeekBar_normal();
                break;
            case 4:
                next();
                updateSeekBar_normal();
                break;
            case 5:
                chooseProgress(intent.getIntExtra("currentProgress",0));
                break;
            case 6:
                chooseMusic(intent.getIntExtra("position",0));
                updateSeekBar_normal();
                break;
            case 7:
                //发送歌曲的文件名列表
                Intent fileList=new Intent("updateUI");
                fileList.putExtra("what","fileList");
                fileList.putStringArrayListExtra("fileList", (ArrayList<String>) list);
                sendBroadcast(fileList);
                //initMediaPlayer(currentPosition);
                sendFileName(list.get(currentPosition));

                sendTime();

                break;
        }

        if (mediaPlayer.isPlaying()){
            //发送播放状态
            sendMsg("play");
        }else {
            //发送暂停或停止状态
            sendMsg("pause_stop");
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
        if(currentPosition!=0){
            currentPosition-=1;
        }else {
            currentPosition=list.size()-1;
        }
        initMediaPlayer(currentPosition);
        start();
    }
    public void next(){
        //initMediaPlayer(currentPosition);
        if(!mediaPlayer.isPlaying()){

            mediaPlayer.reset();
        }else {
            stop();
        }
        if(currentPosition!=list.size()-1) {
            currentPosition += 1;
        }else {
            currentPosition=0;
        }
        initMediaPlayer(currentPosition);
        start();
    }
    public void stop() {
        //initMediaPlayer(currentPosition);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            initMediaPlayer(currentPosition);
            MainActivity.canPlay = true;

        }
    }
    public void updateSeekBar_normal(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer.isPlaying()) {
                    while (mediaPlayer.getDuration() != mediaPlayer.getCurrentPosition()) {
                        //发送当前播放进度、总播放进度
//                        Intent intent1 = new Intent("updateUI");
//                        intent1.putExtra("what", "seekBar_normal");
//                        intent1.putExtra("currentPosition", mediaPlayer.getCurrentPosition());
//                        intent1.putExtra("duration", mediaPlayer.getDuration());
//                        sendBroadcast(intent1);
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
    public void chooseProgress(int c){
        //initMediaPlayer(currentPosition);
        Intent intent_c=new Intent("updateUI");
        intent_c.putExtra("what","chooseProgress");
        intent_c.putExtra("currentProgress",c);
        sendBroadcast(intent_c);

    }
    public void chooseMusic(int position){
            stop();
            initMediaPlayer(position);

            start();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.reset();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

//    public interface UpdateListView{
//        public void updateListView(List<String> list);
//    }
//    private  UpdateListView updateListView;
//
//    public void setUpdateListView(UpdateListView updateListView) {
//        this.updateListView = updateListView;
//    }
}
