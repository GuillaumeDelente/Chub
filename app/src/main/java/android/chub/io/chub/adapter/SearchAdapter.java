package android.chub.io.chub.adapter;

import android.chub.io.chub.BuildConfig;
import android.chub.io.chub.R;
import android.chub.io.chub.data.api.model.GoogleAddress;
import android.chub.io.chub.data.api.model.Terms;
import android.chub.io.chub.data.api.model.TermsTypeAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by guillaume on 11/12/14.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private static final String TAG = "SearchAdapter";
    private List<GoogleAddress> mDataset;
    private Resources mResources;
    private LocationClickListener mLocationClickListener;

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
    public SearchAdapter(List<GoogleAddress> addresses, Context context) {
        mDataset = addresses;
        mResources = context.getResources();
    }

    public void setOnLocationClickListener(LocationClickListener clickListener) {
        mLocationClickListener = clickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
        final GoogleAddress address = mDataset.get(position);
        Terms terms = address.terms;
        final SpannableString spannableString = new SpannableString(String.format("%s %s",
                TextUtils.isEmpty(terms.number) ? "" : terms.number,
                TextUtils.isEmpty(terms.address) ? "" : terms.address));
        ForegroundColorSpan span = new ForegroundColorSpan(
                mResources.getColor(R.color.search_result_matched));
        for (GoogleAddress.MatchedString matchedString : address.matched_substrings) {
            spannableString.setSpan(span, matchedString.offset,
                    matchedString.offset + matchedString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Matched string " + matchedString.offset + " " + matchedString.length);
        }
        holder.mTextView1.setText(spannableString);
        holder.mTextView2.setText(String.format("%s %s",
                TextUtils.isEmpty(terms.city) ? "" : terms.city.trim(),
                TextUtils.isEmpty(terms.state) ? "" : ", " + terms.state));
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

    public static interface LocationClickListener {
        public void onLocationClicked(GoogleAddress address);
    }
}