package com.userdebug.txz;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.media.MediaPlayer;
import java.io.File;
import java.util.ArrayList;

public class DesktopVideoPlayer extends Activity {
    
    private VideoView videoPlayer;
    private ImageView imageView;
    private FrameLayout screen;
    
    private ArrayList<String> mediaFiles = new ArrayList<>();
    private int currentPos = 0;
    private int clickCounter = 0;
    private boolean showingImage = false;
    
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        
        // 全屏无UI
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // 隐藏系统栏
        hideSystemUI();
        
        // 创建界面
        screen = new FrameLayout(this);
        screen.setBackgroundColor(0xFF000000);
        setContentView(screen);
        
        // 视频播放器
        videoPlayer = new VideoView(this);
        videoPlayer.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        // 图片显示
        imageView = new ImageView(this);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        // 添加视图
        screen.addView(videoPlayer);
        screen.addView(imageView);
        imageView.setVisibility(View.INVISIBLE);
        
        // 设置
        videoPlayer.setMediaController(null);
        
        videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                videoPlayer.start();
            }
        });
        
        videoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoPlayer.start();
            }
        });
        
        videoPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                nextMedia();
                return true;
            }
        });
        
        // 点击14次切换
        screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCounter++;
                if (clickCounter >= 14) {
                    clickCounter = 0;
                    nextMedia();
                }
            }
        });
        
        // 加载媒体
        loadMedia();
    }
    
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }
    
    private void loadMedia() {
        File dir = new File("/storage/emulated/0/1234");
        if (dir.exists() && dir.isDirectory()) {
            findFiles(dir);
        }
        
        if (!mediaFiles.isEmpty()) {
            playCurrent();
        }
    }
    
    private void findFiles(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                findFiles(file);
            } else if (isMediaFile(file)) {
                mediaFiles.add(file.getAbsolutePath());
            }
        }
    }
    
    private boolean isMediaFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp4") || name.endsWith(".3gp") || 
               name.endsWith(".avi") || name.endsWith(".mkv") ||
               name.endsWith(".mov") || name.endsWith(".jpg") || 
               name.endsWith(".jpeg") || name.endsWith(".png") ||
               name.endsWith(".gif") || name.endsWith(".bmp") ||
               name.endsWith(".webp");
    }
    
    private void playCurrent() {
        if (mediaFiles.isEmpty()) return;
        
        if (currentPos < 0) currentPos = 0;
        if (currentPos >= mediaFiles.size()) currentPos = 0;
        
        String path = mediaFiles.get(currentPos);
        File file = new File(path);
        
        if (!file.exists()) {
            nextMedia();
            return;
        }
        
        String name = path.toLowerCase();
        
        if (name.endsWith(".mp4") || name.endsWith(".3gp") || 
            name.endsWith(".avi") || name.endsWith(".mkv") ||
            name.endsWith(".mov")) {
            // 播放视频
            showingImage = false;
            imageView.setVisibility(View.INVISIBLE);
            videoPlayer.setVisibility(View.VISIBLE);
            
            try {
                Uri uri = Uri.fromFile(file);
                videoPlayer.setVideoURI(uri);
            } catch (Exception e) {
                nextMedia();
            }
            
        } else {
            // 显示图片
            showingImage = true;
            videoPlayer.setVisibility(View.INVISIBLE);
            videoPlayer.stopPlayback();
            imageView.setVisibility(View.VISIBLE);
            
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    nextMedia();
                }
            } catch (Exception e) {
                nextMedia();
            }
        }
    }
    
    private void nextMedia() {
        if (mediaFiles.isEmpty()) {
            videoPlayer.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(null);
            return;
        }
        
        if (!showingImage) {
            videoPlayer.stopPlayback();
        }
        
        currentPos++;
        if (currentPos >= mediaFiles.size()) {
            currentPos = 0;
        }
        
        playCurrent();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if (!showingImage && videoPlayer != null && !videoPlayer.isPlaying()) {
            videoPlayer.start();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (!showingImage && videoPlayer != null && videoPlayer.isPlaying()) {
            videoPlayer.pause();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null) {
            videoPlayer.stopPlayback();
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
}