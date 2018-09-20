package com.streamingnology.snymediaplayer.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.streamingnology.snymediaplayer.fragment.SearchMediaServerFragment.OnListFragmentInteractionListener;
import com.streamingnology.snymediaplayer.fragment.mediaserver.MediaServerContent.MediaServerItem;

import java.util.List;
import com.streamingnology.snymediaplayer.R;
/**
 * {@link RecyclerView.Adapter} that can display a {@link MediaServerItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MediaServerRecyclerViewAdapter extends RecyclerView.Adapter<MediaServerRecyclerViewAdapter.ViewHolder> {

  private final List<MediaServerItem> mValues;
  private final OnListFragmentInteractionListener mListener;

  private OnItemClickListener mOnItemClickListener = null;

  public interface OnItemClickListener {
    void onItemClick(MediaServerItem item);
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.mOnItemClickListener = listener;
  }

  public MediaServerRecyclerViewAdapter(List<MediaServerItem> items, OnListFragmentInteractionListener listener) {
    mValues = items;
    mListener = listener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_mediaserver, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mImageView.setImageResource(mValues.get(position).image_id);
    holder.mContentView.setText(mValues.get(position).addr);

    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.mItem);
          }
          mListener.onListFragmentInteraction(holder.mItem);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final ImageView mImageView;
    public final TextView mContentView;
    public MediaServerItem mItem;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mImageView = (ImageView) view.findViewById(R.id.mediaserver_image);
      mContentView = (TextView) view.findViewById(R.id.mediaserver_content);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}
