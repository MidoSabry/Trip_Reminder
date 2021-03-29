package com.example.tripreminderiti.ui.upcoming_trips;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tripreminderiti.GeoLocation;
import com.example.tripreminderiti.ui.add_trip.AddTripActivity;
import com.example.tripreminderiti.R;
import com.example.tripreminderiti.database.TripDatabase;
import com.example.tripreminderiti.database.note.Note;
import com.example.tripreminderiti.database.trip.Trip;
import com.example.tripreminderiti.databinding.FragmentUpcomingBinding;
import com.example.tripreminderiti.ui.dashboard.DashboardFragment;
import com.example.tripreminderiti.ui.dashboard.DashboardViewModel;
import com.example.tripreminderiti.ui.trip_details.TripDetailsActivity;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class UpcomingTripsFragment extends Fragment {
    private FragmentUpcomingBinding binding;
    public static final String UPCOMING_DETAILS_EXTRA = "UPCOMING_DETAILS_EXTRA";
    private final UpcomingTripAdapter upcomingTripAdapter = new UpcomingTripAdapter();
    private AlertDialog addNoteDialog;
    private AlertDialog addToHistory;
    private UpcomingTripsViewModel tripsViewModel;

    List<Trip> trips;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUpcomingBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        tripsViewModel = new ViewModelProvider(this).get(UpcomingTripsViewModel.class);

        binding.homeRvTrips.setAdapter(upcomingTripAdapter);

        tripsViewModel.getTripsListLiveData().observe(getViewLifecycleOwner(), trips -> {
            upcomingTripAdapter.changeData(trips);
            if (trips != null && trips.size() > 0) {
                tripsViewModel.setAlarm(trips.get(0));
            }
        });

        upcomingTripAdapter.setAddNoteClickListener = new UpcomingTripAdapter.AddNoteClickListener() {
            @Override
            public void onClick(Trip trip) {
                showAddNoteDialog(trip.getId());
            }
        };

        upcomingTripAdapter.setTripClickListener = new UpcomingTripAdapter.TripClickListener() {
            @Override
            public void onClick(Trip trip) {
                Intent intent = new Intent(getActivity(), TripDetailsActivity.class);
                intent.putExtra(UPCOMING_DETAILS_EXTRA, trip);
                startActivity(intent);
            }
        };

        binding.homeBtnAddTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddTripActivity.class));
            }
        });


        //Mido
        //button to start trip
        upcomingTripAdapter.setStartTrip =new UpcomingTripAdapter.StartTrip() {
            @Override
            public void onClick(Trip trip) {

                showTripDialog(trip);
                //getLangLat(trip.getStartPoint());
                //getLangLat(trip.getEndPoint());
            }
        };

        upcomingTripAdapter.setDeletTrip = new UpcomingTripAdapter.DeletTrip() {
            @Override
            public void onClick(Trip trip) {
                Toast.makeText(getActivity(), "trip Deleted", Toast.LENGTH_SHORT).show();
                TripDatabase.getInstance(getActivity()).tripDao().delete(trip);

//                trips = TripDatabase.getInstance(getActivity()).tripDao().getAll();
//                upcomingTripAdapter.changeData(trips);
            }
        };

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void showAddNoteDialog(int id) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View view = factory.inflate(R.layout.dialog_add_note, null);
        addNoteDialog = new AlertDialog.Builder(getActivity()).create();
        addNoteDialog.setView(view);
        TextInputLayout title = view.findViewById(R.id.ed_note_ttle);
        TextInputLayout description = view.findViewById(R.id.ed_note_disc);
        view.findViewById(R.id.btn_add_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "" + title.getEditText().getText(), Toast.LENGTH_SHORT).show();
                TripDatabase.getInstance(getActivity()).noteDao().insertNote(new Note(id, title.getEditText().getText().toString(), description.getEditText().getText().toString()));
                addNoteDialog.dismiss();
            }
        });
        addNoteDialog.show();
    }
    //Mido
    //to display track on map from user location to destination
    private void DisplayTrack( String sDestination) {
        //if device dosnt have mape installed then redirect it to play store

        try {
            //when google map installed
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir/" + "/" + sDestination);

            //Action view with uri
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            //set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);


        } catch (ActivityNotFoundException e) {
            //when google map is not initialize
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            //set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }
    }


    //show dialog to history
    public void showTripDialog(Trip trip) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View view = factory.inflate(R.layout.custom_dialog_for_trip, null);
        addToHistory = new AlertDialog.Builder(getActivity()).create();
        addToHistory.setView(view);
        view.findViewById(R.id.btn_okay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "OK", Toast.LENGTH_SHORT).show();
                DisplayTrack(trip.getEndPoint());
                trip.setOK(true);
               // TripDatabase.getInstance(getActivity()).tripDao().updateTrip(trip.getName(),trip.getStartPoint(),trip.getEndPoint(),trip.getDate(),trip.getTime(),trip.getDate_time(),trip.getIsAlarmPrepared(),trip.isOK(),trip.getSpinner(),trip.getId());
                TripDatabase.getInstance(getActivity()).tripDao().update(trip);

                addToHistory.dismiss();
            }
        });

        addToHistory.show();
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        trips = TripDatabase.getInstance(getActivity()).tripDao().getAll();
//        upcomingTripAdapter.changeData(trips);
//    }




}