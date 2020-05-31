package com.example.goairtest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.example.goairtest.R;

import java.util.Objects;

public class HomeFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);
        ((MainActivity) requireActivity()).setFragmentRefreshListener(new MainActivity.FragmentRefreshListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onRefresh() {
                DatabaseHandler db = new DatabaseHandler(getActivity());
                if (db.checkDataPopulated()) {
                    Data d = db.getLast();
                    TextView text = requireActivity().findViewById(R.id.pollutionView);
                    text.setText(Integer.toString(d.getPollution()));
                    TextView temp = requireActivity().findViewById(R.id.temperature);
                    temp.setText(Integer.toString(db.getLastTH().getTemperature()));
                    TextView hum = requireActivity().findViewById(R.id.humidity);
                    hum.setText(Integer.toString(db.getLastTH().getHumidity()));

                } else {
                    TextView text = requireActivity().findViewById(R.id.pollutionView);
                    text.setText("---");
                    TextView temp = requireActivity().findViewById(R.id.temperature);
                    temp.setText("---");
                    TextView hum = requireActivity().findViewById(R.id.humidity);
                    hum.setText("---");
                }

            }
        });



            return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DatabaseHandler db = new DatabaseHandler(getActivity());
        if (db.checkDataPopulated()) {
            Data d = db.getLast();
            TextView text = requireActivity().findViewById(R.id.pollutionView);
            text.setText(Integer.toString(d.getPollution()));
            TextView temp = requireActivity().findViewById(R.id.temperature);
            temp.setText(Integer.toString(db.getLastTH().getTemperature()));
            TextView hum = requireActivity().findViewById(R.id.humidity);
            hum.setText(Integer.toString(db.getLastTH().getHumidity()));
        } else {
            TextView text = requireActivity().findViewById(R.id.pollutionView);
            text.setText("---");
            TextView temp = requireActivity().findViewById(R.id.temperature);
            temp.setText("---");
            TextView hum = requireActivity().findViewById(R.id.humidity);
            hum.setText("---");

        }
    }

}
