package net.xvidia.vowmee;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.xvidia.vowmee.videoplayer.IVideoDownloadListener;
import net.xvidia.vowmee.videoplayer.Video;
import net.xvidia.vowmee.videoplayer.VideosAdapter;
import net.xvidia.vowmee.videoplayer.VideosDownloader;

import java.util.ArrayList;

public class Home extends AppCompatActivity implements IVideoDownloadListener {

    private static String TAG = "MainActivity";

    private Context context;
    private RecyclerView mRecyclerView;
//    private ProgressBar progressBar;
    private VideosAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Video> urls;
    VideosDownloader videosDownloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbars();
        context = Home.this;
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        urls = new ArrayList<Video>();
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new VideosAdapter(Home.this, urls);
        mRecyclerView.setAdapter(mAdapter);

        videosDownloader = new VideosDownloader(context);
        videosDownloader.setOnVideoDownloadListener(this);

//        if(Utils.getInstance().hasConnection(context))
//        {
            getVideoUrls();

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                        int findFirstCompletelyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();

                        Video video;
                        if (urls != null && urls.size() > 0)
                        {
                            if (findFirstCompletelyVisibleItemPosition >= 0) {
                                video = urls.get(findFirstCompletelyVisibleItemPosition);
                                mAdapter.videoPlayerController.setcurrentPositionOfItemToPlay(findFirstCompletelyVisibleItemPosition);
                                mAdapter.videoPlayerController.handlePlayBack(video);
                            }
                            else
                            {
                                video = urls.get(firstVisiblePosition);
                                mAdapter.videoPlayerController.setcurrentPositionOfItemToPlay(firstVisiblePosition);
                                mAdapter.videoPlayerController.handlePlayBack(video);
                            }
                        }
                    }
                }
            });
//        }
//        else
//            Toast.makeText(context, "No internet available", Toast.LENGTH_LONG).show();
    }
    private void initToolbars() {
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.user_home_list_toolbar);
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarTop.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
//        toolbarTop.setNavigationIcon(R.drawable.com_facebook_button_send_icon);
        toolbarTop.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        // Inflate a menu to be displayed in the toolbar
        toolbarTop.inflateMenu(R.menu.menu_user_profile);
    }

    //New
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        initToolbars();
        return true;
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
    @Override
    public void onVideoDownloaded(Video video) {
        mAdapter.videoPlayerController.handlePlayBack(video);
    }

    private void getVideoUrls()
    {
        Video video1 = new Video("0", "1", "21.mp4");
        urls.add(video1);
        Video video2 = new Video("1", "2", "22.mp4");
        urls.add(video2);
        Video video3 = new Video("2", "3", "24.mp4");
        urls.add(video3);
        Video video4 = new Video("3", "4", "23.mp4");
        urls.add(video4);
        Video video5 = new Video("4", "5", "21.mp4");
        urls.add(video5);
        Video video6 = new Video("5", "6", "25.mp4");
        urls.add(video6);
        Video video7 = new Video("6", "7", "21.mp4");
        urls.add(video7);
        Video video8 = new Video("7", "8", "23.mp4");
        urls.add(video8);
        Video video9 = new Video("8", "9", "24.mp4");
        urls.add(video9);
        mAdapter.notifyDataSetChanged();
//        progressBar.setVisibility(View.GONE);
        videosDownloader.startVideosDownloading(urls);
    }
}