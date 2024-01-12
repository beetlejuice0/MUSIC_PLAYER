package com.example.musicplayerr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class MusicListAdapter extends ArrayAdapter<Music> {
    public MusicListAdapter(Context context, int resource, List<Music> musicList) {
        super(context, resource, musicList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_music, parent, false);
        }

        Music currentMusic = getItem(position);

        // Update the UI elements in the list item
        TextView textTitle = itemView.findViewById(R.id.textTitle);
        TextView textArtist = itemView.findViewById(R.id.textArtist);

        if (currentMusic != null) {
            textTitle.setText(currentMusic.getTitle());
            textArtist.setText(currentMusic.getArtist());
        }

        return itemView;
    }

}
