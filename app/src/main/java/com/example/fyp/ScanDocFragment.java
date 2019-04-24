package com.example.fyp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.utils.Claim;
import com.example.fyp.utils.ImageConstant;
import com.example.fyp.utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class ScanDocFragment extends Fragment {
    private User u;
    private SharedPreferences sharedPreferences;
    FloatingActionButton fab, cameraFab, galleryFab;
    Boolean isFABOpen = false;
    private GraphView weeklyGraph, monthlyGraph;
    private LineGraphSeries<DataPoint> weeklySeries, monthlySeries;
    private FirebaseFirestore fireStore;
    private int xPointW = 0;
    private double weeklyTotal = 0.0, yearlyTotal, xPointY = 0.0;
    private TextView weeklyText, yearlyText;
    private FrameLayout frameLayout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_document, container,false);

        fab = view.findViewById(R.id.fab);
        cameraFab = view.findViewById(R.id.camera_fab);
        galleryFab = view.findViewById(R.id.gallery_fab);
        frameLayout = view.findViewById(R.id.frameLayout);
        fireStore = FirebaseFirestore.getInstance();

        Gson gson = new Gson();
        sharedPreferences = this.getActivity().getSharedPreferences("sharePreferences",MODE_PRIVATE);
        String json = sharedPreferences.getString("CurrentUser", null);
        u = gson.fromJson(json, User.class);

        //weekly graph
        weeklyGraph = view.findViewById(R.id.weekly_graph);
        initWeeklyGraph(weeklyGraph);
        weeklyText = view.findViewById(R.id.weeklyTotal);

        //monthly graph
        monthlyGraph = view.findViewById(R.id.monthly_graph);
        initMonthlyGraph(monthlyGraph);
        yearlyText = view.findViewById(R.id.yearlyTotal);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage(view, ImageConstant.REQUEST_IMAGE_CAPTURE);
            }
        });

        galleryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage(view, ImageConstant.REQUEST_IMAGE_GALLERY);
            }
        });

        return view;
    }



//    private DataPoint[] getMonthlyDataPoint(){
//        //do query to find out the daily expenditure amount, then plot the graph
//        DataPoint[] dp = new DataPoint[]{
//                new DataPoint(0,3),
//                new DataPoint(0.2,11),
//                new DataPoint(0.4,7),
//                new DataPoint(0.6,6),
//                new DataPoint(0.8,1),
//                new DataPoint(1,2),
//                new DataPoint(1.2,2),
//                new DataPoint(1.4,2),
//                new DataPoint(1.6,2),
//                new DataPoint(1.8,2),
//                new DataPoint(2,2),
//                new DataPoint(2.2,2),
//        };
//        return dp;
//    }

    private void initWeeklyGraph(GraphView weeklyGraph){
        weeklySeries = new LineGraphSeries<>();
        weeklySeries.setDrawBackground(true);
        weeklySeries.setAnimated(true);
        weeklySeries.setDrawDataPoints(true);
        weeklySeries.setTitle("Weekly Expenses");
        weeklyGraph.addSeries(weeklySeries);
        weeklyGraph.getLegendRenderer().setVisible(true);
        weeklyGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        Date currentDate = Calendar.getInstance().getTime();
        Calendar date1 = Calendar.getInstance();
        date1.setTime(currentDate);
        date1.set(Calendar.DAY_OF_WEEK, 2);
        date1.set(date1.get(Calendar.YEAR), date1.get(Calendar.MONTH), date1.get(Calendar.DATE),0,0,0);

        Calendar date2 = Calendar.getInstance();
        date2.setTime(currentDate);
        date2.set(Calendar.DAY_OF_WEEK, 2);
        date2.set(date2.get(Calendar.YEAR), date2.get(Calendar.MONTH), date2.get(Calendar.DATE),23,59,59);

        Date startOfWeek1 = date1.getTime();
        Date startOfWeek2 = date2.getTime();
        getWeeklyQuery(startOfWeek1, startOfWeek2);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(weeklyGraph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"});
        weeklyGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        weeklyGraph.getGridLabelRenderer().setHorizontalLabelsAngle(120);

        weeklySeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Double xPoint = dataPoint.getX();
                String day ="";
                if(xPoint.equals(0.0)){
                    day = "Monday";
                }else if(xPoint.equals(2.0)){
                    day = "Tuesday";
                }else if(xPoint.equals(4.0)){
                    day = "Wednesday";
                }else if(xPoint.equals(6.0)){
                    day = "Thursday";
                }else if(xPoint.equals(8.0)){
                    day = "Friday";
                }else if(xPoint.equals(10.0)){
                    day = "Saturday";
                }else if(xPoint.equals(12.0)){
                    day = "Sunday";
                }

                Snackbar snackbar = Snackbar.make(frameLayout, "RM "+ dataPoint.getY()+" had been claimed on "+ day+".", Snackbar.LENGTH_SHORT);

                View snackbarView = snackbar.getView();
                TextView snacbarText = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                snacbarText.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_done_black_24dp,0);
                snackbar.show();
            }
        });

    }

    private void getWeeklyQuery(Date startOfWeek1, Date startOfWeek2){
        Calendar c1 = Calendar.getInstance();
        c1.setTime(startOfWeek1);
        c1.add(Calendar.DATE, 1);
        final Date nextDate1 = c1.getTime();

        Calendar c2 = Calendar.getInstance();
        c2.setTime(startOfWeek2);
        c2.add(Calendar.DATE, 1);
        final Date nextDate2 = c2.getTime();

        fireStore.collection("claims")
                .whereEqualTo("userID", u.getId())
                .whereEqualTo("status","Approved")
                .whereGreaterThanOrEqualTo("date", startOfWeek1)
                .whereLessThanOrEqualTo("date",startOfWeek2).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                    Log.d("fb","success");
//                    Log.d("query", String.valueOf(queryDocumentSnapshots.getDocuments().size()));
                    Double yPoint = 0.0;
                    for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                        Claim claim = documentSnapshot.toObject(Claim.class);
                        yPoint += claim.getAmount();
                        weeklyTotal = weeklyTotal + claim.getAmount();
                        weeklyText.setText("RM "+String.valueOf(weeklyTotal));
                    }
                    weeklySeries.appendData(new DataPoint(xPointW, yPoint),false,10);
                    xPointW = xPointW +2;
                    if(xPointW <=12) {
                        getWeeklyQuery(nextDate1, nextDate2);
                    }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("fb", "FAILED");
            }
        });
    }

    private void initMonthlyGraph(GraphView monthlyGraph){
        monthlySeries = new LineGraphSeries<>();
        monthlySeries.setDrawBackground(true);
        monthlySeries.setAnimated(true);
        monthlySeries.setDrawDataPoints(true);
        monthlySeries.setTitle("Monthly Expenses");
        monthlyGraph.addSeries(monthlySeries);
        monthlyGraph.getLegendRenderer().setVisible(true);
        monthlyGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        //get the date instance
        Date currentDate = Calendar.getInstance().getTime();
        Calendar date1 = Calendar.getInstance();
        date1.setTime(currentDate);
        //change the date to 1st of Jan
        date1.set(date1.get(Calendar.YEAR), 0, 1,0,0,0);

        Calendar date2 = Calendar.getInstance();
        date2.setTime(currentDate);
        //change the date to last of Jan
        date2.set(date2.get(Calendar.YEAR), 0, date2.getActualMaximum(Calendar.DATE),23,59,59);

        Date startOfMonth1 = date1.getTime();
        Date startOfMonth2 = date2.getTime();
        getMonthlyQuery(startOfMonth1, startOfMonth2);

        //set label
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(monthlyGraph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
        monthlyGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        monthlyGraph.getGridLabelRenderer().setHorizontalLabelsAngle(120);
    }

    private void getMonthlyQuery(Date startOfMonth1, Date startOfMonth2){

        Calendar c1 = Calendar.getInstance();
        c1.setTime(startOfMonth1);
        c1.add(Calendar.MONTH, 1);
        final Date nextDate1 = c1.getTime();
        final Date d = startOfMonth1;
        Calendar c2 = c1;
        //change the date to last of Jan
        c2.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.getActualMaximum(Calendar.DATE),23,59,59);
        final Date nextDate2 = c2.getTime();

        fireStore.collection("claims")
                .whereEqualTo("userID", u.getId())
                .whereEqualTo("status","Approved")
                .whereGreaterThanOrEqualTo("date", startOfMonth1)
                .whereLessThanOrEqualTo("date",startOfMonth2).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    Log.d("fb","success");
                    //Log.d("fb", d.toString());
                    Log.d("query", String.valueOf(queryDocumentSnapshots.getDocuments().size()));
                    Double yPoint = 0.0;
                    for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                        Claim claim = documentSnapshot.toObject(Claim.class);
                        yPoint += claim.getAmount();
                        yearlyTotal = yearlyTotal + claim.getAmount();
                        yearlyText.setText("RM "+String.valueOf(yearlyTotal));
                    }
                    monthlySeries.appendData(new DataPoint(xPointY, yPoint),false,13);
                    xPointY = xPointY + 0.2;
                    if(xPointY <=2.2) {
                        getMonthlyQuery(nextDate1, nextDate2);
                    }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("fb", "FAILED");
            }
        });

    }

    private void showFABMenu(){
        isFABOpen=true;
        cameraFab.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        galleryFab.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        cameraFab.animate().translationY(0);
        galleryFab.animate().translationY(0);
    }

    private void getImage(View view, int REQUEST_CODE){
        Gson gson = new Gson();
        sharedPreferences = this.getActivity().getSharedPreferences("sharePreferences",MODE_PRIVATE);
        String json = sharedPreferences.getString("CurrentUser", null);
        u = gson.fromJson(json, User.class);
        if(u.getLineManager().equals("")){
            Toast.makeText(this.getContext(),"Please ensure that you have a line manager before submitting a claim.", Toast.LENGTH_LONG).show();
        }else{
            Intent MainIntent = new Intent(view.getContext(), CameraActivity.class);
            MainIntent.putExtra("ImageMethod", REQUEST_CODE);
            startActivity(MainIntent);
        }

    }



}
