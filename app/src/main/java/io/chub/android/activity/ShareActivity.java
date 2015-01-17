package io.chub.android.activity;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

import io.chub.android.R;

/**
 * Created by guillaume on 11/30/14.
 */
public class ShareActivity extends BaseActivity {

    private ActionBar mActionBar;
    private ImageButton mCarButton;
    private ImageButton mTransitButton;
    private ImageButton mBikeButton;
    private ImageButton mWalkButton;
    private ArrayList<ImageButton> mButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mCarButton = (ImageButton) findViewById(R.id.car_button);
        mTransitButton = (ImageButton) findViewById(R.id.transit_button);
        mBikeButton = (ImageButton) findViewById(R.id.bike_button);
        mWalkButton = (ImageButton) findViewById(R.id.walk_button);
        mButtons = new ArrayList<>(4);
        mButtons.add(mCarButton);
        mButtons.add(mTransitButton);
        mButtons.add(mBikeButton);
        mButtons.add(mWalkButton);
        for (ImageButton button : mButtons) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (ImageButton button : mButtons) {
                        button.setActivated(button == v);
                    }
                }
            });
        }
        if (savedInstanceState == null) {
            mCarButton.setActivated(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
