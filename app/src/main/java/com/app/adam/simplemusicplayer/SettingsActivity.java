package com.app.adam.simplemusicplayer;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;


public class SettingsActivity extends Activity {

    private CheckBox contextuality;
    private Button saveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent=getIntent();
        Boolean datas= intent.getBooleanExtra("contextOn",false);



        contextuality = (CheckBox)findViewById(R.id.contextalityCB);
        saveButton = ((Button)findViewById(R.id.saveButton));
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(),MainActivity.class);

                if(contextuality.isChecked()){
                    i.putExtra("contextOn",true);
                }
                else{
                    i.putExtra("contextOn",false);
                }
                setResult(200, i);
                finish();
            }
        });
        if (datas) {
            contextuality.setChecked(true);
        }
        else{
            contextuality.setChecked(false);
        }


    }


}
