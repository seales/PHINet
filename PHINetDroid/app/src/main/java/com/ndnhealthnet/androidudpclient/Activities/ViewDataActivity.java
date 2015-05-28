package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.StringConst;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Activity deals specifically with interacting with user's own data.
 *
 * Uses include
 * 1. viewing patient data
 */
public class ViewDataActivity extends Activity {

    Button backBtn, intervalSelectionBtn;
    TextView dataStatusText, loggedInText, entityNameText;
    Spinner sensorSelectionSpinner;
    GraphView graph;
    String entityName, currentSensorSelected;

    // --- used by the interval selector ---
    private int startYear = 0, startMonth = 0, startDay = 0;
    private int endYear = 0, endMonth = 0, endDay = 0;

    // title of dialog that allows user to select interval
    private final String INTERVAL_TITLE_1 = "Choose the start interval.";
    private final String INTERVAL_TITLE_2 = "Choose the end interval.";
    // --- used by the interval selector ---

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewdata);

        String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                StringConst.PREFS_LOGIN_USER_ID_KEY, "");

        // get name of entity to determine whose data to display
        entityName = getIntent().getExtras().getString(StringConst.ENTITY_NAME);

        graph = (GraphView) findViewById(R.id.graph); // reset graph when updating

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID);

        entityNameText = (TextView) findViewById(R.id.entityNameView);
        entityNameText.setText(entityName + "'s data");

        // TextView used to notify user whether data for patient exists
        dataStatusText = (TextView) findViewById(R.id.currentDataStatusTextView);

        ArrayList<DBData> myData = DBSingleton.getInstance(getApplicationContext()).getDB().getGeneralCSData(entityName);
        ArrayList<String> sensors = new ArrayList<>();

        if (myData != null) {
            // get all sensors in order to allow user to choose
            for (int i = 0; i < myData.size(); i++) {
                if (!sensors.contains(myData.get(i).getSensorID())) {
                    sensors.add(myData.get(i).getSensorID()); // new sensor detected; store now
                }
            }
        } else {
            sensors.add("No data available");
        }

        sensorSelectionSpinner = (Spinner) findViewById(R.id.sensorSelectorSpinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sensors);
        sensorSelectionSpinner.setAdapter(adapter);

        currentSensorSelected = sensorSelectionSpinner.getSelectedItem().toString(); // TODO - rework in case of no sensors

        sensorSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // reload if different sensor chosen
                if (!currentSensorSelected.equals(adapter.getItem(position).toString())) {
                    // TODO - reload
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        updateGraph(); // TODO - document

        intervalSelectionBtn = (Button) findViewById(R.id.intervalSelectionBtn);
        intervalSelectionBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                AlertDialog.Builder initialInterval = generateIntervalSelector(INTERVAL_TITLE_1);
                initialInterval.show();
            }
        });

        backBtn = (Button) findViewById(R.id.userDataBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * TODO - create a class for this dialog
     *
     * allows users to select date regarding interval of requested data
     *
     * @param title used to set title of dialog
     * @return returns the dialog so that it can be initiated elsewhere
     */
    AlertDialog.Builder generateIntervalSelector(String title) {
        final DatePicker intervalSelector = new DatePicker(ViewDataActivity.this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(ViewDataActivity.this);
        builder.setTitle(title);
        builder.setView(intervalSelector);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // get user input
                int day = intervalSelector.getDayOfMonth();
                int month = intervalSelector.getMonth() + 1; // offset required
                int year = intervalSelector.getYear();

                if (startYear == 0) {
                    // this is the first input, store now and request again

                    startYear = year;
                    startMonth = month;
                    startDay = day;

                    // call again to get end interval
                    final DatePicker intervalSelector = new DatePicker(ViewDataActivity.this);
                    AlertDialog.Builder secondInterval = generateIntervalSelector(INTERVAL_TITLE_2);
                    secondInterval.setView(intervalSelector);

                    // TODO - rework this sloppy nesting

                    secondInterval.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // get user input
                            int day = intervalSelector.getDayOfMonth();
                            int month = intervalSelector.getMonth() + 1; // offset required
                            int year = intervalSelector.getYear();

                            // start input already set, now store end input
                            endYear = year;
                            endMonth = month;
                            endDay = day;

                            // now that interval has been entered, update the graph

                            // TODO - update the graph
                            // reload activity after 3 seconds, so to check if client data arrived
                          //  finish();
                            updateGraph();

                            //startActivity(getIntent());

                        }
                    }); secondInterval.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    secondInterval.show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder;
    }

    /**
     * TODO - document
     *
     */
    public void updateGraph() {

        if (graph!=null) {
            graph.removeAllSeries();
        }

        ArrayList<DBData> myData = DBSingleton.getInstance(getApplicationContext()).getDB().getGeneralCSData(entityName);

        // TODO - provide new data
        ArrayList<Float> myFloatData;

        // TODO - more robust verification

        // attempts to determine whether an interval has been selected
        if (startYear == 0 || endYear == 0) {
            // interval hasn't been selected, provide default now

            Calendar now = Calendar.getInstance();

            startYear = now.get(Calendar.YEAR) - 1;
            endYear = now.get(Calendar.YEAR) + 1;

            startMonth = now.get(Calendar.MONTH);
            endMonth = startMonth;

            startDay = now.get(Calendar.DAY_OF_MONTH);
            endDay = startDay;
        }

        if (myData == null) {
            myFloatData = new ArrayList<>(); // no user data found in cache
        } else {

            // date syntax: yyyy-MM-ddTHH:mm:ss.SSS
            String startDate = Integer.toString(startYear) + "-" + Integer.toString(startMonth);
                startDate += "-" + Integer.toString(startDay) + "T00:00:00.000"; // append zeros at end

            String endDate = Integer.toString(endYear) + "-" + Integer.toString(endMonth);
              endDate += "-" + Integer.toString(endDay) + "T00:00:00.000"; // append zeros at end

            myFloatData = Utils.convertDBRowTFloats(myData, currentSensorSelected, startDate, endDate);  // use data from cache
        }

        String startDate = Integer.toString(startMonth) + "/" + Integer.toString(startDay) + "/";
        startDate +=  Integer.toString(startYear);

        String endDate = Integer.toString(endMonth) + "/" + Integer.toString(endDay) + "/";
        endDate +=  Integer.toString(endYear);

        if (myData != null && myData.size() > 0 && myFloatData.size() > 0) {


            dataStatusText.setText(startDate + " - " + endDate); // notify user of chosen interval

            // TODO - improve presentation
            DataPoint[] dataPoints = new DataPoint[myFloatData.size()];
            for (int i = 0; i < myFloatData.size(); i++) {
                dataPoints[i] = new DataPoint(i, myFloatData.get(i));
            }

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
            graph.addSeries(series);

        } else {
            dataStatusText.setText("Nothing during " + startDate + " - " + endDate);

            //graph.setVisibility(View.INVISIBLE); // no data, make graph go away
        }
        
        // TODO - rework so that resetting isn't necessary
        startDay = 0;
        endDay = 0;
        startMonth = 0;
        endMonth = 0;
        startYear = 0;
        endYear = 0;
    }
}


