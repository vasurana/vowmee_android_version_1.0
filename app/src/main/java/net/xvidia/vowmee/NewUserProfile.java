package net.xvidia.vowmee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableRow;
import android.widget.Toast;

import net.xvidia.vowmee.Utils.Utils;

public class NewUserProfile extends AppCompatActivity {
    private final int GALLERY_INTENT_CALLED = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        bindActivity();

    }

    private void bindActivity() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.user_home_toolbar);
        TableRow mLayoutInviteFriends = (TableRow) findViewById(R.id.layout_invite_friends);
        TableRow mLayoutGoLive = (TableRow) findViewById(R.id.layout_go_live_friends);
        TableRow mLayoutShare = (TableRow) findViewById(R.id.layout_share_moments);

        mLayoutInviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewUserProfile.this, Home.class));
            }
        });
        mLayoutGoLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewUserProfile.this, RecordVideo.class));
            }
        });
        mLayoutShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVideo();
            }
        });
        mToolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return true;
    }

    private void selectVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.home_profile_select_video)), GALLERY_INTENT_CALLED);

    }

    //New
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_user_profile:
                Toast.makeText(this, "Action refresh selected", Toast.LENGTH_SHORT)
                        .show();
                break;

            default:
                break;
        }

        return true;
    }


    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImageUri = null;
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_INTENT_CALLED) {
                selectedImageUri = data.getData();
                try {
                    String selectedImagePath = Utils.getPath(this, selectedImageUri);
//					Bitmap bp = ThumbnailUtils.createVideoThumbnail(selectedImagePath, MediaStore.Images.Thumbnails.MINI_KIND);
                    Log.i("Mediapath", selectedImagePath);
                } catch (Exception e) {

                }
            }
        }
    }
}
