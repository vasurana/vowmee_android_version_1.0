package net.xvidia.vowmee;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by root on 14/12/15.
 */
public class Followers extends ActionBarActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        // Set a toolbar to replace the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.followers_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Followers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ArrayList<String> list = new ArrayList<String>();
        list.add("item1");
        list.add("item2");
        list.add("item1");
        list.add("item2");
        list.add("item1");
        list.add("item2");
        list.add("item1");
        list.add("item2");
        list.add("item1");
        list.add("item2");
        list.add("item1");
        list.add("item2");
        list.add("item1");
        list.add("item2");
        list.add("item1");
        list.add("item2");
        list.add("item1");
        list.add("item2");

        //instantiate custom adapter
        MyCustomAdapter adapter = new MyCustomAdapter(list, this);

        //handle listview and assign adapter
        ListView lView = (ListView)findViewById(R.id.list);
        lView.setAdapter(adapter);
    }
}
