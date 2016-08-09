package se.thefarm.greger.pokeflutego;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import se.thefarm.greger.pokeflutego.services.WakeService;

public class MainActivity extends AppCompatActivity
{

    private Button serviceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceButton = (Button)findViewById(R.id.button);


        serviceButton.setText(WakeService.isServiceRunning ? R.string.stop_service : R.string.start_service);
        serviceButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!WakeService.isServiceRunning)
                {
                    startService(new Intent(MainActivity.this, WakeService.class));
                    serviceButton.setText(R.string.stop_service);
                }
                else
                {
                    stopService(new Intent(MainActivity.this, WakeService.class));
                    serviceButton.setText(R.string.start_service);
                }
            }
        });
    }
}
