package com.example.goairtest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        // Inflate the layout for this fragment
        ((MainActivity) requireActivity()).setFragmentRefreshListener(new MainActivity.FragmentRefreshListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onRefresh() {
                DatabaseHandler db = new DatabaseHandler(getActivity());
                if (db.checkDataPopulated()) {
                    Data d = db.getLast();
                    TextView text = (TextView) requireActivity().findViewById(R.id.pollutionView);
                    text.setText(Integer.toString(d.getPollution()));
                    TextView temp = (TextView) requireActivity().findViewById(R.id.temperature);
                    temp.setText(Integer.toString(db.getLastTH().getTemperature()));
                    TextView hum = (TextView) requireActivity().findViewById(R.id.humidity);
                    hum.setText(Integer.toString(db.getLastTH().getHumidity()));

                } else {
                    TextView text = (TextView) requireActivity().findViewById(R.id.pollutionView);
                    text.setText("---");
                    TextView temp = (TextView) requireActivity().findViewById(R.id.temperature);
                    temp.setText("---");
                    TextView hum = (TextView) requireActivity().findViewById(R.id.humidity);
                    hum.setText("---");
                }

            }
        });

        return inflater.inflate(R.layout.home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DatabaseHandler db = new DatabaseHandler(getActivity());
        if (db.checkDataPopulated()) {
            Data d = db.getLast();
            TextView text = (TextView) requireActivity().findViewById(R.id.pollutionView);
            text.setText(Integer.toString(d.getPollution()));
            TextView temp = (TextView) requireActivity().findViewById(R.id.temperature);
            temp.setText(Integer.toString(db.getLastTH().getTemperature()));
            TextView hum = (TextView) requireActivity().findViewById(R.id.humidity);
            hum.setText(Integer.toString(db.getLastTH().getHumidity()));
        } else {
            TextView text = (TextView) requireActivity().findViewById(R.id.pollutionView);
            text.setText("---");
            TextView temp = (TextView) requireActivity().findViewById(R.id.temperature);
            temp.setText("---");
            TextView hum = (TextView) requireActivity().findViewById(R.id.humidity);
            hum.setText("---");

        }
    }

}
