package com.example.musicplayerr;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private MusicListAdapter adapter;
    private MusicPlayer musicPlayer;
    private SeekBar seekBar;
    private TextView textCurrentTime, textTotalTime;
    private boolean isUserSeeking = false;

    private static final int PICK_AUDIO_REQUEST = 1;
    private static final int SEEK_BAR_UPDATE_INTERVAL = 1000; // 1 second
    private static final String CHANNEL_ID = "music_player_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        adapter = new MusicListAdapter(this, R.layout.item_music, new ArrayList<Music>());
        listView.setAdapter(adapter);

        musicPlayer = new MusicPlayer();

        seekBar = findViewById(R.id.seekBar);
        textCurrentTime = findViewById(R.id.textCurrentTime);
        textTotalTime = findViewById(R.id.textTotalTime);

        Button pickAudioButton = findViewById(R.id.pickButton);
        pickAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });


        ImageButton playPauseButton = findViewById(R.id.button1);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });

        ImageButton playNextButton = findViewById(R.id.button2);
        playNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });

        ImageButton playPreviousButton = findViewById(R.id.button3);
        playPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevious();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music selectedMusic = adapter.getItem(position);
                try {
                    startMusic(selectedMusic);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error playing music", Toast.LENGTH_SHORT).show();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    isUserSeeking = true;
                    textCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (musicPlayer.isPlaying()) {
                    int progress = seekBar.getProgress();
                    musicPlayer.seekTo(progress);
                    isUserSeeking = false;
                }
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null) {
            return null;
        }
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        Log.d("AudioPath", "Path: " + path);
        cursor.close();
        return path;
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedAudioUri = data.getData();
            String selectedAudioPath = getRealPathFromURI(selectedAudioUri);

            if (selectedAudioPath != null) {
                // Get title and artist from URI using MediaMetadataRetriever
                String[] audioInfo = getAudioInfo(selectedAudioUri);
                String title = audioInfo[0];
                String artist = audioInfo[1];

                // Create Music object with title, artist, and path
                Music selectedMusic = new Music(title, artist, selectedAudioPath);

                // Add the selected music to the adapter
                adapter.add(selectedMusic);

                try {
                    // Start playing the selected music
                    startMusic(selectedMusic);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error playing music", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Error: Path is null", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Error: URI is null", Toast.LENGTH_SHORT).show();
        }
    }

    private String[] getAudioInfo(Uri audioUri) {
        String[] result = {"Unknown Title", "Unknown Artist"};

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, audioUri);

            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            if (title != null && !title.isEmpty()) {
                result[0] = title;
            }

            if (artist != null && !artist.isEmpty()) {
                result[1] = artist;
            }

            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void startMusic(Music music) throws IOException {
        if (musicPlayer == null) {
            musicPlayer = new MusicPlayer();
        }
        currentPlayingIndex = adapter.getPosition(music);
        if (!musicPlayer.isPlaying()) {
            musicPlayer.setDataSource(music.getPath());
            musicPlayer.start();
            updateSeekBar();
        }
    }


    private void togglePlayPause() {
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause();
        } else {
            musicPlayer.start();
            updateSeekBar();
        }
    }

    private int currentPlayingIndex = -1; // Track the index of the currently playing music

    private void playNext() {
        if (currentPlayingIndex < adapter.getCount() - 1) {
            currentPlayingIndex++;
            Music nextMusic = adapter.getItem(currentPlayingIndex);
            try {
                startMusic(nextMusic);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error playing next music", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "No more next music", Toast.LENGTH_SHORT).show();
        }
    }

    private void playPrevious() {
        if (currentPlayingIndex > 0) {
            currentPlayingIndex--;
            Music previousMusic = adapter.getItem(currentPlayingIndex);
            try {
                startMusic(previousMusic);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error playing previous music", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "No more previous music", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateSeekBar() {
        seekBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (musicPlayer != null) {
                    int currentPosition = musicPlayer.getCurrentPosition();
                    int totalDuration = musicPlayer.getDuration();

                    seekBar.setMax(totalDuration);
                    seekBar.setProgress(currentPosition);

                    textCurrentTime.setText(formatTime(currentPosition));
                    textTotalTime.setText(formatTime(totalDuration));

                    if (!isUserSeeking) {
                        updateSeekBar();
                    }
                }
            }
        }, SEEK_BAR_UPDATE_INTERVAL);
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicPlayer.release();
        musicPlayer = null;
    }
    // Inside your MainActivity class
}
