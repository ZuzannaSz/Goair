package com.example.goairtest;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.example.goairtest.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.protobuf.StringValue;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class SettingsFragment extends Fragment {
    private ImageView img;
    private TextView username, email;
    private FirebaseUser user;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            Uri uri = user.getPhotoUrl();
            img = view.findViewById(R.id.profileSet);
            String internetUrl = "https://www.notebookcheck.net/fileadmin/Notebooks/News/_nc3/android_wallpaper5_2560x1600_1.jpg";
            if(uri!=null)
            {
                Glide.with(view).load(uri).error(R.mipmap.ic_launcher)
                        .placeholder(R.drawable.person)
                        .into(img);
                //Picasso.with(getContext()).load(uri.toString()).into(img);
            }
            username = view.findViewById(R.id.nameSet);
            email = view.findViewById(R.id.mail);
            username.setText(user.getDisplayName());
            // username.setText(uri.toString());
            email.setText(user.getEmail());

        }


        Button ch = view.findViewById(R.id.changePassword);
        ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user!=null)
                {
                    Intent intent = new Intent(getActivity(),EditProfileActivity.class);
                    startActivity(intent);
                }

            }
        });
        return view;
    }
    public static FragmentActivity getActivity(Context context) {
        if (context instanceof FragmentActivity) {
            return (FragmentActivity) context;
        }

        while (context instanceof ContextWrapper) {
            if (context instanceof FragmentActivity) {
                return (FragmentActivity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
