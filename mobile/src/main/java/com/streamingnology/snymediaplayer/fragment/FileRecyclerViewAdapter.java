package com.streamingnology.snymediaplayer.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.streamingnology.snymediaplayer.fragment.FileExplorerFragment.OnListFragmentInteractionListener;
import com.streamingnology.snymediaplayer.fragment.File.FileContent.FileItem;

import java.util.List;
import com.streamingnology.snymediaplayer.R;
/**
 * {@link RecyclerView.Adapter} that can display a {@link FileItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class FileRecyclerViewAdapter extends RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder> {

  private final List<FileItem> mValues;
  private final OnListFragmentInteractionListener mListener;

  private OnItemClickListener mOnItemClickListener = null;

  public interface OnItemClickListener {
    void onItemClick(FileItem item);
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.mOnItemClickListener = listener;
  }

  public FileRecyclerViewAdapter(List<FileItem> items, OnListFragmentInteractionListener listener) {
    mValues = items;
    mListener = listener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_file, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mImageView.setImageResource(mValues.get(position).image_id);
    holder.mContentView.setText(mValues.get(position).name);

    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onListFragmentInteraction(holder.mItem);
          if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.mItem);
          }
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
    public FileItem mItem;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mImageView = (ImageView) view.findViewById(R.id.item_image_id);
      mContentView = (TextView) view.findViewById(R.id.content);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}
