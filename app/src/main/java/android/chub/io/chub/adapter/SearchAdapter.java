package android.chub.io.chub.adapter;

import android.chub.io.chub.R;
import android.chub.io.chub.data.api.model.GoogleAddress;
import android.chub.io.chub.data.api.model.TermsTypeAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by guillaume on 11/12/14.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private List<GoogleAddress> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView1;
        public TextView mTextView2;
        public ViewHolder(View view) {
            super(view);
            mTextView1 = (TextView) view.findViewById(android.R.id.text1);
            mTextView2 = (TextView) view.findViewById(android.R.id.text2);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SearchAdapter(List<GoogleAddress> addresses) {
        mDataset = addresses;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
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
        TermsTypeAdapter.Terms terms = mDataset.get(position).terms;
        holder.mTextView1.setText(terms.address);
        holder.mTextView2.setText(terms.city + ", " + terms.state);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void updateItems(List<GoogleAddress> addresses) {
        mDataset.clear();
        mDataset.addAll(addresses);
        notifyDataSetChanged();
    }
}