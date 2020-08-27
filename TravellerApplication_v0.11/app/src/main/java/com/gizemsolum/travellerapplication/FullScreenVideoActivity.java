package com.gizemsolum.travellerapplication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FullScreenVideoActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    String uid;

    private SimpleExoPlayer player;
    private PlayerView playerView;
    TextView textView;
    boolean fullscreen = false;
    ImageView fullscreenButton;
    private String url;
    private boolean playwhenready = false;
    private  int currentWindow = 0;
    private  long playbackposition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_video);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Fullscreen");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        playerView = findViewById(R.id.exoplayer_fullscreen);
        textView = findViewById(R.id.tv_fullscreen);

        fullscreenButton = playerView.findViewById(R.id.exoplayer_fullscreen_icon);

        Intent intent = getIntent();
        url = intent.getStringExtra("Videourl");
        System.out.println("Videourl at line 56: "+url);
        String title = intent.getStringExtra("Videoname");
        System.out.println("Videoname at line 58: "+title);

        textView.setText(title);

        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fullscreen){
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(FullScreenVideoActivity.this,R.drawable.ic_fullscreen_expand));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    if (getSupportActionBar() != null){
                        getSupportActionBar().show();
                    }
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = (int) (200 * getApplicationContext().getResources().getDisplayMetrics().density);
                    playerView.setLayoutParams(params);
                    fullscreen = false;
                    textView.setVisibility(View.VISIBLE);
                }else {
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(FullScreenVideoActivity.this,R.drawable.ic_fullscreen_skrink));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    if (getSupportActionBar() != null){
                        getSupportActionBar().hide();
                    }
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = params.MATCH_PARENT;
                    playerView.setLayoutParams(params);
                    fullscreen = true;
                    textView.setVisibility(View.INVISIBLE);

                }
            }
        });
    }


    private MediaSource buildMediaSource(Uri uri){
        DataSource.Factory datasourcefactory = new DefaultHttpDataSourceFactory("Posts");
        return  new ProgressiveMediaSource.Factory(datasourcefactory)
                .createMediaSource(uri);
    }

    private void initializeplayer(){
        player = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(player);
        String uriString = url;
        Uri uri = Uri.parse(uriString);
        System.out.println("initializeplayer line 112 uri: "+uri);
        MediaSource mediaSource = buildMediaSource(uri);
        player.setPlayWhenReady(playwhenready);
        player.seekTo(currentWindow,playbackposition);
        player.prepare(mediaSource,false,false);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Util.SDK_INT >= 26 ){
            initializeplayer();
            //   textView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Util.SDK_INT >= 26 || player == null ){
            //  initializeplayer();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Util.SDK_INT > 26 ){
            releasePlayer();


        }

    }

    private void checkUserStatus(){

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null){

            //mProfileTv.setText(user.getEmail());
            uid = user.getUid();

        }else {

            startActivity(new Intent(this,MainActivity.class));
            finish();

        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (Util.SDK_INT >= 26 ){
            releasePlayer();
        }
    }

    private void releasePlayer(){
        if (player != null){
            playwhenready = player.getPlayWhenReady();
            playbackposition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player = null;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        player.stop();
        releasePlayer();

        final Intent intent = new Intent();
        setResult(RESULT_OK,intent);
        finish();
    }

}