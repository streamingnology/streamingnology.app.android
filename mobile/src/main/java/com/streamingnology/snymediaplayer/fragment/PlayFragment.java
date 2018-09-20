package com.streamingnology.snymediaplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.ikidou.fragmentBackHandler.BackHandledFragment;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.streamingnology.snyvideoview.media.ExoMediaSource;
import com.streamingnology.snyvideoview.media.SimpleMediaSource;
import com.streamingnology.snyvideoview.media.SimpleQuality;
import com.streamingnology.snyvideoview.ui.ExoVideoPlaybackControlView;
import com.streamingnology.snyvideoview.ui.ExoVideoView;
import com.streamingnology.snymediaplayer.R;

import java.util.ArrayList;
import java.util.List;

import static com.streamingnology.snyvideoview.orientation.OnOrientationChangedListener.SENSOR_LANDSCAPE;
import static com.streamingnology.snyvideoview.orientation.OnOrientationChangedListener.SENSOR_PORTRAIT;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayFragment extends BackHandledFragment {
  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";
  private ExoVideoView videoView;

  // TODO: Rename and change types of parameters
  private String mParam1;
  private String mParam2;

  private OnFragmentInteractionListener mListener;

  public PlayFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param param1 Parameter 1.
   * @param param2 Parameter 2.
   * @return A new instance of fragment PlayFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static PlayFragment newInstance(String param1, String param2) {
    PlayFragment fragment = new PlayFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View v = inflater.inflate(R.layout.fragment_play, container, false);
    videoView = v.findViewById(R.id.videoView);
    videoView.setOrientationListener(orientation -> {
      if (orientation == SENSOR_PORTRAIT) {
        changeToPortrait();
      } else if (orientation == SENSOR_LANDSCAPE) {
        changeToLandscape();
      }
    });

    videoView.setBackListener((view, isPortrait) -> {
      if (isPortrait) {
        //finish();
      }
      return false;
    });

    videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

    Intent intent = getActivity().getIntent();
    Uri[] uris = new Uri[1];

    String video_uri = null;
    if (getArguments() != null) {
      video_uri = getArguments().getString("video_uri");
    }

    SimpleMediaSource mediaSource = new SimpleMediaSource(video_uri);

    mediaSource.setDisplayName("VideoPlaying");
    List<ExoMediaSource.Quality> qualities = new ArrayList<>();
    ExoMediaSource.Quality quality;

    for (int i = 0; i < 6; i++) {
      SpannableString spannableString = new SpannableString("Quality" + i);
      if (i % 2 == 0) {
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.YELLOW);
        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

      } else {
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
      }

      quality = new SimpleQuality(spannableString, mediaSource.uri());
      qualities.add(quality);
    }
    mediaSource.setQualities(qualities);

    videoView.play(mediaSource, true);

    return v;
  }

  // TODO: Rename method, update argument and hook method into UI event
  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
              + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Override
  public void onStop() {
    super.onStop();
    if (Build.VERSION.SDK_INT > 23) {
      videoView.pause();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    videoView.releasePlayer();
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    void onFragmentInteraction(Uri uri);
  }

  private void changeToPortrait() {

    // WindowManager operation is not necessary
    WindowManager.LayoutParams attr = getActivity().getWindow().getAttributes();
        attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
    Window window = getActivity().getWindow();
    window.setAttributes(attr);
    //window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
  }


  private void changeToLandscape() {

    // WindowManager operation is not necessary

    WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
    Window window = getActivity().getWindow();
    window.setAttributes(lp);
    //window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
  }

  public boolean interceptBackPressed() {
    if (!videoView.isPortrait()) {
      videoView.setControllerDisplayMode(ExoVideoPlaybackControlView.CONTROLLER_MODE_BOTTOM);
      return true;
    }
    return false;
  }
}
