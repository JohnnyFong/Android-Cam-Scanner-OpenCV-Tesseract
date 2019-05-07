package com.example.fyp;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.fyp.utils.Claim;
import com.example.fyp.utils.Department;
import com.example.fyp.utils.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class DepartmentFragment extends Fragment {

    private GraphView departmentGraph;
    private TextView departmentText;
    private FirebaseFirestore fireStore;
    private FrameLayout frameLayout;
    private Double departmentTotal = 0.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_department, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Departments Claim");

        frameLayout = view.findViewById(R.id.frameLayout);

        fireStore = FirebaseFirestore.getInstance();
        //subMonthly graph
        departmentGraph = view.findViewById(R.id.department_graph);
        initSubGraph(departmentGraph);
        departmentText = view.findViewById(R.id.departmentTotal);

        return view;
    }

    private void initSubGraph(final GraphView departmentGraph){

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

        final Date startOfMonth1 = date1.getTime();
        final Date startOfMonth2 = date2.getTime();

        fireStore.collection("department").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    //for each employee found, create a new series and add that series to the graph
                    Double xPointS = 0.0;
                    Department department = documentSnapshot.toObject(Department.class);
                    department.setId(documentSnapshot.getId());
                    LineGraphSeries<DataPoint> departmentSeries= addDepartmentSeries(department, departmentGraph);
                    departmentGraph.addSeries(departmentSeries);
                    getDepartmentQuery(startOfMonth1, startOfMonth2, department, departmentSeries, xPointS);
                }

            }
        });

        //set label
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(departmentGraph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
        departmentGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        departmentGraph.getGridLabelRenderer().setHorizontalLabelsAngle(120);

    }

    private LineGraphSeries<DataPoint> addDepartmentSeries(final Department department, GraphView departmentGraph){
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        LineGraphSeries<DataPoint> subSeries = new LineGraphSeries<>();
//        subSeries.setDrawBackground(true);
        subSeries.setAnimated(true);
        subSeries.setDrawDataPoints(true);
        subSeries.setColor(color);
        subSeries.setTitle(department.getName());

        departmentGraph.getLegendRenderer().setVisible(true);
        departmentGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        subSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Double xPoint = dataPoint.getX();

                BigDecimal x = new BigDecimal(xPoint);
                if(xPoint < 1.0){

                    x = x.setScale(2, RoundingMode.DOWN);
                    xPoint = x.doubleValue();
                }else if (xPoint>1.4){
                    x = x.setScale(2, RoundingMode.UP);
                    xPoint = x.doubleValue();
                }

                String month ="";
                if(xPoint.equals(0.0)){
                    month = "January";
                }else if(xPoint.equals(0.2)){
                    month = "February";
                }else if(xPoint.equals(0.4)){
                    month = "March";
                }else if(xPoint.equals(0.6)){
                    month = "April";
                }else if(xPoint.equals(0.8)){
                    month = "May";
                }else if(xPoint.equals(1.0)){
                    month = "June";
                }else if(xPoint.equals(1.2)){
                    month = "July";
                }else if(xPoint.equals(1.4)){
                    month = "August";
                }else if(xPoint.equals(1.6)){
                    month = "September";
                }else if(xPoint.equals(1.8)){
                    month = "October";
                }else if(xPoint.equals(2.0)){
                    month = "November";
                }else if(xPoint.equals(2.2)){
                    month = "December";
                }

                Snackbar snackbar = Snackbar.make(frameLayout, department.getName()+ " had claimed RM "+ dataPoint.getY()+" on "+ month+".", Snackbar.LENGTH_SHORT);

                View snackbarView = snackbar.getView();
                TextView snacbarText = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                snacbarText.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_done_black_24dp,0);
                snackbar.show();
            }
        });

        return subSeries;
    }

    private void getDepartmentQuery(Date startOfMonth1, Date startOfMonth2, final Department department, final LineGraphSeries<DataPoint> departmentSeries,final Double xPointS){
        Calendar c1 = Calendar.getInstance();
        c1.setTime(startOfMonth1);
        c1.add(Calendar.MONTH, 1);
        final Date nextDate1 = c1.getTime();
//        final Date d = startOfMonth1;
        Calendar c2 = c1;
        //change the date to last of Jan
        c2.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.getActualMaximum(Calendar.DATE),23,59,59);
        final Date nextDate2 = c2.getTime();

        Query query;
        query = fireStore.collection("claims")
                .whereEqualTo("department", department.getId())
                .whereEqualTo("status","Approved")
                .whereGreaterThanOrEqualTo("date", startOfMonth1)
                .whereLessThanOrEqualTo("date",startOfMonth2);


        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Double yPoint = 0.0;
                for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    Claim claim = documentSnapshot.toObject(Claim.class);
                    yPoint += claim.getAmount();
                    departmentTotal = departmentTotal + claim.getAmount();
                    departmentText.setText("RM "+String.valueOf(departmentTotal));
                }
                departmentSeries.appendData(new DataPoint(xPointS, yPoint),false,13);
                Double xpoint = xPointS + 0.2;
                if(xpoint <=2.2) {
                    getDepartmentQuery(nextDate1, nextDate2, department, departmentSeries, xpoint);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("fb", "FAILED");
            }
        });
    }

}
