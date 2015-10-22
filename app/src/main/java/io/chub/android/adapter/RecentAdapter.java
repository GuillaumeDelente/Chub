package io.chub.android.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import io.chub.android.R;
import io.chub.android.data.api.model.RealmContact;
import io.chub.android.data.api.model.RealmRecentChub;
import io.realm.RealmResults;

/**
 * Created by guillaume on 11/12/14.
 */
public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
    private static final String TAG = "SearchAdapter";
    private RealmResults<RealmRecentChub> mDataset;
    private OnRecentChubClickListener mRecentChubClickListener;

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
    public RecentAdapter(RealmResults<RealmRecentChub> recentChubs) {
        mDataset = recentChubs;
    }

    public void setOnRecentChubClickListener(OnRecentChubClickListener clickListener) {
        mRecentChubClickListener = clickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.address_row, parent, false);
        final ViewHolder holder = new ViewHolder(v);
        holder.mImageView.setImageResource(R.drawable.ic_history);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRecentChubClickListener != null) {
                    mRecentChubClickListener.onRecentChubClicked(mDataset.get(holder.getPosition()));
                }
            }
        });
        return holder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final RealmRecentChub recentChub = mDataset.get(position);
        holder.mTextView1.setText(recentChub.getDestination().getName());
        List<String> names = new ArrayList<>(recentChub.getContacts().size());
        for (RealmContact contact : recentChub.getContacts()) {
            names.add(contact.getName());
        }
        String displayedNames = StringUtils.join(names, ", ");
        holder.mTextView2.setText(displayedNames);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface OnRecentChubClickListener {
        void onRecentChubClicked(RealmRecentChub recentChub);
    }
}