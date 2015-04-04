package org.teamscavengr.scavengr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.teamscavengr.scavengr.createhunt.MyHuntsActivity;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        Hunt.loadAllHuntsInBackground(new Hunt.HuntLoadedCallback() {
            @Override
            public void huntLoaded(final Hunt hunt) {
                Toast.makeText(MainActivity.this, "loaded hunt " + hunt.getId(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void huntFailedToLoad(final Exception ex) {
                ex.printStackTrace();
                Toast.makeText(MainActivity.this, "failed to load hunts", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void numHuntsFound(final int i) {}

        }, true);
        switch(view.getId()) {
            case R.id.go_on_hunt:
                //Intent hunt = new Intent(this, HuntsList.class);
                Intent hunt = new Intent(this, HuntDetailsActivity.class);
                // Pass in Geo Location of user
                this.startActivity(hunt);
                break;
            case R.id.create_hunt:
                Intent createHuntIntent = new Intent(this, MyHuntsActivity.class);
                this.startActivity(createHuntIntent);
                break;
            default:
                break;
        }

    }
}
