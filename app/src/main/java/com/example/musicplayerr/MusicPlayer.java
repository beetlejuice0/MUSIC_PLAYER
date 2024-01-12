package com.example.musicplayerr;

import android.media.MediaPlayer;

import java.io.IOException;

public class MusicPlayer {
    private MediaPlayer mediaPlayer;
    private boolean isPaused;
    private OnMusicPlayerListener onMusicPlayerListener;

    public MusicPlayer() {
        mediaPlayer = new MediaPlayer();
        isPaused = false;
        setupMediaPlayer();
    }

    public interface OnMusicPlayerListener {
        void onPrepared();
        void onCompletion();
        void onError(String errorMessage);
    }

    public void setOnMusicPlayerListener(OnMusicPlayerListener listener) {
        this.onMusicPlayerListener = listener;
    }

    private void setupMediaPlayer() {
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (onMusicPlayerListener != null) {
                    onMusicPlayerListener.onPrepared();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (onMusicPlayerListener != null) {
                    onMusicPlayerListener.onCompletion();
                }
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String errorMessage = "Error during playback (what=" + what + ", extra=" + extra + ")";
                if (onMusicPlayerListener != null) {
                    onMusicPlayerListener.onError(errorMessage);
                }
                return false;
            }
        });
    }

    public void setDataSource(String path) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            if (onMusicPlayerListener != null) {
                onMusicPlayerListener.onError(e.getMessage());
            }
        }
    }

    public void start() {
        if (isPaused) {
            mediaPlayer.start();
            isPaused = false;
        } else {
            mediaPlayer.start();
        }
    }

    public void pause() {
        mediaPlayer.pause();
        isPaused = true;
    }

    public void stop() {
        mediaPlayer.stop();
        isPaused = false;
    }

    public void release() {
        mediaPlayer.release();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public void playNext(String nextFilePath) {
        if (nextFilePath != null && !nextFilePath.isEmpty()) {
            setDataSource(nextFilePath);
            start();
        }
    }

    public void playPrevious(String previousFilePath) {
        if (previousFilePath != null && !previousFilePath.isEmpty()) {
            setDataSource(previousFilePath);
            start();
        }
    }
}
