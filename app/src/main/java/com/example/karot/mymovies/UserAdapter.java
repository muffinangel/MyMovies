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

public class UserAdapter extends ArrayAdapter<User> {

    private Context mContext;
    private List<User> usersList = new ArrayList<>();

    public UserAdapter(@NonNull Context context, @LayoutRes ArrayList<User> list) {
        super(context, 0 , list);
        mContext = context;
        usersList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.line,parent,false);

        User currentUser = usersList.get(position);

        ImageView image = (ImageView)listItem.findViewById(R.id.icon);
        //image.setImageResource(currentMovie.getmImageDrawable());
        //String url = currentMovie.getmImageDrawable();
        //GlideApp.with(mContext /* context */)
        //        .load(url)
        //        .into(image); // is this working??

        String path = currentUser.getAvatar_path();
        // pobrać go ładnie ze Storage
        // TODO

        GlideApp.with(mContext /* context */)
                .load(R.drawable.cat_movie).into(image);


        TextView nick = (TextView) listItem.findViewById(R.id.firstLine);
        nick.setText(currentUser.getNick());

        TextView about = (TextView) listItem.findViewById(R.id.secondLine);
        about.setText(currentUser.getAbout());

        return listItem;
    }
}
