package net.xvidia.vowmee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by root on 14/12/15.
 */
public class MyCustomAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private Context context;
    static boolean mFollowing = false;


    public MyCustomAdapter(ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.activity_followers_single_listitem, null);
        }

        TextView listItemText = (TextView)view.findViewById(R.id.follower_name);
        listItemText.setText(list.get(position));
        ImageView userPicture = (ImageView)view.findViewById(R.id.icon);

        //Handle buttons and add onClickListeners
        final Button follow = (Button)view.findViewById(R.id.follow);

        follow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!mFollowing)
                {
                    mFollowing = true;
                    follow.setText("Unfollow");
                }
                else
                {
                    mFollowing = false;
                    follow.setText("Follow");
                }
            }
        });

        return view;
    }
}
