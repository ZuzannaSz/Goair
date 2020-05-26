package com.example.goairtest;

import android.content.Context;
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

import com.bumptech.glide.Glide;
import com.example.goairtest.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class SettingsFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    private ImageView img;
    private TextView username, email;
    private FirebaseUser user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            Uri uri = user.getPhotoUrl();
            img = view.findViewById(R.id.profileSet);
            username = view.findViewById(R.id.nameSet);
            email = view.findViewById(R.id.mail);
            username.setText(user.getDisplayName());
            email.setText(user.getEmail());
            Toast.makeText( getActivity(), uri.toString(),
                    Toast.LENGTH_SHORT).show();
            if(uri!=null)
            {
                Glide.with(this).load(uri).error(R.mipmap.ic_launcher)
                    .placeholder(R.drawable.person)
                    .into(img);
                //Picasso.with(getContext()).load(uri.toString()).into(img);
            }
        }
        Button ch = view.findViewById(R.id.changePassword);
        ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
                Intent intent = new Intent(getActivity(),EditProfileActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
