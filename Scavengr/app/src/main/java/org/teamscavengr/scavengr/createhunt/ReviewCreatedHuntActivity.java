package org.teamscavengr.scavengr.createhunt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.ParseAnalytics;

import org.teamscavengr.scavengr.Hunt;
import org.teamscavengr.scavengr.R;
import org.teamscavengr.scavengr.Task;
import org.teamscavengr.scavengr.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Activity to review a hunt, once created.
 *
 * Created by hzhou1235 on 3/15/15.
 */
public class ReviewCreatedHuntActivity extends ActionBarActivity implements View.OnClickListener {
    Hunt currentHunt;
    User currentUser;
    private int hour, minute;
    ListView listView;
    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_created_hunt);
        currentHunt = getIntent().getParcelableExtra("currentHunt");
        currentUser = getIntent().getParcelableExtra("user");
        if (getIntent().hasExtra("editMode")) {
            editMode = getIntent().getBooleanExtra("editMode", false);
        }
        currentHunt.setEstimatedTime(1L, TimeUnit.HOURS);

        listView = (ListView) findViewById(R.id.list);
        List<String> values = new ArrayList<String>();
        int i = 0;
        for (Task task: currentHunt.getTasks()) {
            i++;
            values.add("Task Number: " + i +
                    " Task Location: " + task.getAnswer());
        }

        values.add("");
        values.add("");
        values.add("");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        listView.setAdapter(adapter);

        if (editMode){
            ((EditText)findViewById(R.id.spoof_latitude)).setText(currentHunt.getName());
            ((EditText)findViewById(R.id.spoof_longitude)).setText(currentHunt.getDescription());
            String timeText;
            timeText = currentHunt.getEstimatedTime().first.toString() + " " + currentHunt.getEstimatedTime().second.toString();
            ((EditText)findViewById(R.id.estimated_time)).setText(timeText);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_review_hunt, menu);
        //TODO: load in waypoints and display
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.confirm:
//                User user = getIntent().getParcelableExtra("currentUser");
                currentHunt.setName(((EditText)findViewById(R.id.spoof_latitude)).getText().toString());
                currentHunt.setDescription(((EditText)findViewById(R.id.spoof_longitude)).getText().toString());
                currentHunt.setCreatorId(currentUser.getId());
                currentHunt.setTimeCreated(System.currentTimeMillis() / 1000L);
                StringBuilder toastString = new StringBuilder();
                if (!currentHunt.checkHunt(toastString)) {
                    Toast.makeText(ReviewCreatedHuntActivity.this, toastString.toString(), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create and show Progress dialog
                final ProgressDialog progress = new ProgressDialog(this);
                progress.setMessage("Saving hunt, please wait");
                progress.show();
                final ReviewCreatedHuntActivity temp = this;

                // Create failed to save hunt dialog
                final AlertDialog.Builder failedDialog = new AlertDialog.Builder(this)
                        .setMessage("Hunt failed to save. Try again?")
                        .setNegativeButton("Give up", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myHunts = new Intent(temp, MyHuntsActivity.class);
                                myHunts.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                myHunts.putExtra("user", currentUser);
                                startActivity(myHunts);
                                finish();
                            }
                        });

                // Now that we can access the failedDialog object, add the positive onclick listener
                // which will show this dialog again if it fails to save again
                failedDialog.setPositiveButton("Save again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progress.show();
                        currentHunt.saveHuntInBackground(currentUser.getId(), new Hunt.HuntSavedCallback() {
                            @Override
                            public void huntSaved() {
                                if (editMode){
                                    Map<String, String> createHuntData = new HashMap<>();
                                    createHuntData.put("huntId", currentHunt.getId());
                                    createHuntData.put("numWaypoints", Integer.toString((currentHunt.getTasks().size())));
                                    createHuntData.put("userId", currentUser.getId());
                                    ParseAnalytics.trackEventInBackground("edit_hunt", createHuntData);
                                } else {
                                    Map<String, String> createHuntData = new HashMap<>();
                                    createHuntData.put("huntId", currentHunt.getId());
                                    createHuntData.put("numWaypoints", Integer.toString((currentHunt.getTasks().size())));
                                    createHuntData.put("userId", currentUser.getId());
                                    ParseAnalytics.trackEventInBackground("create_hunt", createHuntData);
                                }
                                progress.dismiss();
                                // Start the next activity after successful submission to the server
                                Intent myHunts = new Intent(temp, MyHuntsActivity.class);
                                myHunts.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                myHunts.putExtra("user", currentUser);
                                startActivity(myHunts);
                                finish();
                            }

                            @Override
                            public void huntFailedToSave(Exception ex) {
                                progress.dismiss();
                                failedDialog.show();
                            }
                        }, true);
                    }
                });

                // The initial huntSavedCallback
                Hunt.HuntSavedCallback hsc = new Hunt.HuntSavedCallback() {
                    @Override
                    public void huntSaved() {
                        if (editMode){
                            Map<String, String> createHuntData = new HashMap<>();
                            createHuntData.put("huntId", currentHunt.getId());
                            createHuntData.put("numWaypoints", Integer.toString((currentHunt.getTasks().size())));
                            createHuntData.put("userId", currentUser.getId());
                            ParseAnalytics.trackEventInBackground("edit_hunt", createHuntData);
                        } else {
                            Map<String, String> createHuntData = new HashMap<>();
                            createHuntData.put("huntId", currentHunt.getId());
                            createHuntData.put("numWaypoints", Integer.toString((currentHunt.getTasks().size())));
                            createHuntData.put("userId", currentUser.getId());
                            ParseAnalytics.trackEventInBackground("create_hunt", createHuntData);
                        }
                        progress.dismiss();
                        // Start the next activity after successful submission to the server
                        Intent myHunts = new Intent(temp, MyHuntsActivity.class);
                        myHunts.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        myHunts.putExtra("user", currentUser);
                        startActivity(myHunts);
                        finish();
                    }

                    @Override
                    public void huntFailedToSave(Exception ex) {
                        progress.dismiss();
                        failedDialog.show();

                    }
                };
                currentHunt.saveHuntInBackground(currentUser.getId(), hsc, true);


                break;

            case R.id.back:
                this.finish(); //not sure if this works/keeps old stuff
                break;

            case R.id.estimated_time:
                EditText t = (EditText)view;
                TimePickerDialog timePickerDialog = new TimePickerDialog(ReviewCreatedHuntActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(final TimePicker view, final int hourOfDay,
                                                  final int minute) {
                                ReviewCreatedHuntActivity.this.hour = hourOfDay;
                                ReviewCreatedHuntActivity.this.minute = minute;
                                ((EditText) findViewById(R.id.estimated_time)).setText(hourOfDay +
                                        ":" + String.format("%02d", minute));
                                currentHunt.setEstimatedTime(hour * 60L + minute, TimeUnit.MINUTES);
                            }
                        }, 0, 0, true);
                timePickerDialog.setTitle("Enter estimated length");
                timePickerDialog.show();
                break;

            default:
                break;
        }

    }
}