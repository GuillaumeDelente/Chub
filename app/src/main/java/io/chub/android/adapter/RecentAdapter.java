package io.chub.android.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.chub.android.R;
import io.chub.android.data.api.model.GoogleAddress;
import io.chub.android.data.api.model.RealmRecentChub;
import io.realm.RealmResults;

/**
 * Created by guillaume on 11/12/14.
 */
public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
    private static final String TAG = "SearchAdapter";
    private RealmResults<RealmRecentChub> mDataset;
    private Resources mResources;
    private LocationClickListener mLocationClickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;
        public ViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.icon);
            mTextView1 = (TextView) view.findViewById(android.R.id.text1);
            mTextView2 = (TextView) view.findViewById(android.R.id.text2);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecentAdapter(RealmResults<RealmRecentChub> recentChubs, Context context) {
        mDataset = recentChubs;
        mResources = context.getResources();
    }

    public void setOnLocationClickListener(LocationClickListener clickListener) {
        mLocationClickListener = clickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.address_row, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final RealmRecentChub recentChub = mDataset.get(position);
        holder.mTextView1.setText(recentChub.getDestination().getName());
        holder.mTextView2.setText("");
        holder.mImageView.setImageResource(R.drawable.ic_history);
        /*
        if (mLocationClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLocationClickListener.onLocationClicked(address);
                }
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
        */
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static interface LocationClickListener {
        public void onLocationClicked(GoogleAddress address);
    }
}