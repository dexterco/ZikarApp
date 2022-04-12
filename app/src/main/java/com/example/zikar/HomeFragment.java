package com.example.zikar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Madhab;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.Qibla;
import com.batoulapps.adhan.data.DateComponents;
import com.example.zikar.databinding.FragmentHomeBinding;
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.hassanjamil.hqibla.CompassActivity;
import com.hassanjamil.hqibla.Constants;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private FragmentHomeBinding binding;
    private LocationManager locationManager;
    private FusedLocationProviderClient client;
    private Coordinates coordinates;
    private Qibla qibla;
    private PrayerTimes prayerTimes;
    Geocoder geocoder;
    Bundle bundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home, container, false);
        bundle = new Bundle();

         geocoder = new Geocoder(getActivity(), Locale.getDefault());

        Date date = Calendar.getInstance().getTime();
        String fdate = DateFormat.getDateInstance(DateFormat.FULL).format(date);
        String[] sdate = fdate.split(",");
        binding.date.setText(sdate[0] + " " + sdate[1]);

        UmmalquraCalendar cal = new UmmalquraCalendar();
        binding.islamicCalender.setText(cal.get(Calendar.DAY_OF_MONTH) + " " +
                cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH) + " " + cal.get(Calendar.YEAR) + " Hijri");

        client = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }
        else {
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        binding.qibla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CompassActivity.class);
                //intent.putExtra(Constants.TOOLBAR_TITLE, "My App");		// Toolbar Title
                intent.putExtra(Constants.TOOLBAR_BG_COLOR, "#FF000000");		// Toolbar Background color
                intent.putExtra(Constants.TOOLBAR_TITLE_COLOR, "#000000");	// Toolbar Title color
                intent.putExtra(Constants.COMPASS_BG_COLOR, "#FFFFFFFF");		// Compass background color
                intent.putExtra(Constants.ANGLE_TEXT_COLOR, "#000000");		// Angle Text color
                intent.putExtra(Constants.DRAWABLE_DIAL, com.hassanjamil.hqibla.R.drawable.dial);	// Your dial drawable resource
                intent.putExtra(Constants.DRAWABLE_QIBLA, com.hassanjamil.hqibla.R.drawable.qibla); 	// Your qibla indicator drawable resource
                intent.putExtra(Constants.FOOTER_IMAGE_VISIBLE, View.VISIBLE);	// Footer World Image visibility
                intent.putExtra(Constants.LOCATION_TEXT_VISIBLE, View.GONE); // Location Text visibility
                startActivity(intent);
            }
        });

        binding.count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_secondFragment);
            }
        });

        binding.prayerTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_thirdFragment, bundle);
            }
        });

        return binding.getRoot();
    }
    public void getPrayer(Double lat, Double lang){
        DateComponents date = DateComponents.from(new Date());
        CalculationParameters parameters = CalculationMethod.KARACHI.getParameters();
        parameters.madhab = Madhab.SHAFI;
        Coordinates c = new Coordinates(lat,lang);

        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        format.setTimeZone(TimeZone.getDefault());

        prayerTimes = new PrayerTimes(c, date, parameters);

        bundle.putString("fajr", format.format(prayerTimes.fajr));
        bundle.putString("zuhar", format.format(prayerTimes.dhuhr));
        bundle.putString("asar", format.format(prayerTimes.asr));
        bundle.putString("magrib", format.format(prayerTimes.maghrib));
        bundle.putString("isha", format.format(prayerTimes.isha));

    }

    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION,false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                            getCurrentLocation();
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                            getCurrentLocation();
                        } else {
                            // No location access granted.
                            Toast.makeText(getActivity(), "Location Permission Denied!", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null){
                        try {
                           List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            binding.location.setText(addresses.get(0).getAddressLine(0));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        coordinates = new Coordinates(location.getLatitude(),location.getLongitude());
                        getPrayer(location.getLatitude(), location.getLongitude());
                        //Toast.makeText(getActivity(), "Latitude: " + location.getLatitude() + "Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    }else {
                        LocationRequest locationRequest = LocationRequest.create()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);

                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    binding.location.setText(addresses.get(0).getAddressLine(0));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                coordinates = new Coordinates(location.getLatitude(),location.getLongitude());
                                getPrayer(location.getLatitude(), location.getLongitude());
                                //Toast.makeText(getActivity(), "Latitude: " + location.getLatitude() + "Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            }
                        };
                        client.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                }
            });
        }else{
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        }
    }
}