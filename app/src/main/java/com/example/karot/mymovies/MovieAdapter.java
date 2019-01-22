package com.example.karot.mymovies;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends ArrayAdapter<Movie> {

    private Context mContext;
    private List<Movie> moviesList = new ArrayList<>();

    public MovieAdapter(@NonNull Context context, @LayoutRes ArrayList<Movie> list) {
        super(context, 0 , list);
        mContext = context;
        moviesList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.line,parent,false);

        Movie currentMovie = moviesList.get(position);

        ImageView image = (ImageView)listItem.findViewById(R.id.icon);
        //image.setImageResource(currentMovie.getmImageDrawable());
        String url = currentMovie.getmImageDrawable();
        GlideApp.with(mContext /* context */)
                .load(url)
                .into(image); // is this working??

        TextView name = (TextView) listItem.findViewById(R.id.firstLine);
        name.setText(currentMovie.getmName());

        TextView release = (TextView) listItem.findViewById(R.id.secondLine);
        release.setText(currentMovie.getmRelease());

        return listItem;
    }
}