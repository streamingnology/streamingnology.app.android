package com.streamingnology.snymediaplayer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.streamingnology.snymediaplayer.R;
import com.streamingnology.snymediaplayer.fragment.mediaserver.MediaServerContent;
import com.streamingnology.snymediaplayer.fragment.mediaserver.MediaServerContent.MediaServerItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static android.support.constraint.Constraints.TAG;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SearchMediaServerFragment extends Fragment {
  private static String TAG                    = "SearchMediaServerFragment";
  private static final byte[] SSMS             = "SearchSnyMediaServer".getBytes();
  private static final String ARG_COLUMN_COUNT = "column-count";
  private int mColumnCount                     = 1;
  private OnListFragmentInteractionListener mListener;

  private MediaServerContent mediaServerContent = null;
  private MediaServerRecyclerViewAdapter mediaServerRecyclerViewAdapter = null;

  private UdpBroadcastThread udpBroadcastThread = null;

  private NumberProgressBar npb = null;
  private int MAX_SEARCH_TIME_MILLISECOND = 20000;
  private int TIMER_PERIOD_MILLISECOND    = 500;
  private int elapsedTime = 0;
  private Timer timer = null;

  public SearchMediaServerFragment() {
  }

  // TODO: Customize parameter initialization
  @SuppressWarnings("unused")
  public static SearchMediaServerFragment newInstance(int columnCount) {
    SearchMediaServerFragment fragment = new SearchMediaServerFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_COLUMN_COUNT, columnCount);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_search_mediaserver, container, false);
    RecyclerView recyclerView = view.findViewById(R.id.list);
    // Set the adapter
    if (recyclerView instanceof RecyclerView) {
      Context context = recyclerView.getContext();
      if (mColumnCount <= 1) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
      } else {
        recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
      }
      if (mediaServerContent == null) {
        mediaServerContent = new MediaServerContent();
      }
      if (mediaServerRecyclerViewAdapter == null) {
        mediaServerRecyclerViewAdapter = new MediaServerRecyclerViewAdapter(mediaServerContent.ITEMS, mListener);
      }
      mediaServerRecyclerViewAdapter.setOnItemClickListener(new MediaServerRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(MediaServerItem item) {
          SharedPreferences preferences = getActivity().getSharedPreferences("UserSettings", MODE_PRIVATE);
          String snyRemoteMediaServer = preferences.getString("select_mediaserver","");
          boolean serverChanged = false;
          if (snyRemoteMediaServer.compareTo(item.addr) != 0) {
            serverChanged = true;
          }
          SharedPreferences.Editor editor = preferences.edit();
          editor.putString("select_mediaserver",item.addr);
          editor.putBoolean("changedServer", serverChanged);
          editor.apply();
        }
      });
      recyclerView.setAdapter(mediaServerRecyclerViewAdapter);
    }

    if (npb == null) {
      npb = view.findViewById(R.id.progressBar);
    }
    npb.setMax(MAX_SEARCH_TIME_MILLISECOND);
    npb.setProgress(0);

    if (timer == null) {
      timer = new Timer();
    }
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        FragmentActivity mainActivity = getActivity();
        if (mainActivity != null) {
          mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              elapsedTime += TIMER_PERIOD_MILLISECOND;
              if (elapsedTime >= MAX_SEARCH_TIME_MILLISECOND) {
                elapsedTime = MAX_SEARCH_TIME_MILLISECOND;
                udpBroadcastThread.interrupt();
              }
              if (npb != null) {
                npb.setProgress(elapsedTime);
              }
            }
          });
        }
      }
    }, 0, TIMER_PERIOD_MILLISECOND);

    udpBroadcastThread = new UdpBroadcastThread("UdpBroadcastThread");
    udpBroadcastThread.start();
    return view;
  }


  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnListFragmentInteractionListener) {
      mListener = (OnListFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
              + " must implement OnListFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnListFragmentInteractionListener {
    // TODO: Update argument type and name
    void onListFragmentInteraction(MediaServerItem item);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onStart() {
    super.onStart();
    Log.d("Fragment 1", "onStart");
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d("Fragment 1", "onResume");
  }

  @Override
  public void onPause() {
    super.onPause();
    if (udpBroadcastThread != null) {
      udpBroadcastThread.interrupt();
      udpBroadcastThread = null;
    }
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
    if (npb != null) {
      npb = null;
    }
    elapsedTime = 0;
    Log.d("Fragment 1", "onPause");
  }

  @Override
  public void onStop() {
    super.onStop();
    Log.d("Fragment 1", "onStop");
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    Log.d("Fragment 1", "onDestroyView");
  }

  public class UdpBroadcastThread extends Thread {
    private String         mThreadName    = null;
    private DatagramSocket mBroadcastSocket = null;
    private DatagramPacket mBroadcastPkt    = null;
    private DatagramPacket mReceivePkt      = new DatagramPacket(new byte[100], 100);
    private InetAddress boardCastAddress    = null;

    UdpBroadcastThread(String name) {
      mThreadName = name;
    }

    @Override
    public void run() {
      Log.d(TAG, "UdpBroadcastThread");
      super.run();
      int i = 0;
      try {
        boardCastAddress = getBoardCastAddress();
        mBroadcastSocket = new DatagramSocket();
        mBroadcastPkt    = new DatagramPacket(SSMS, SSMS.length);
        mBroadcastSocket.setBroadcast(true);
        mBroadcastSocket.setSoTimeout(100);
      } catch (SocketException e) {
        e.printStackTrace();
        return;
      }

      while(!isInterrupted()){  // 判断线程是否被打断
        try {
          mBroadcastPkt.setAddress(boardCastAddress);
          int mediaServerPort = 8065;
          mBroadcastPkt.setPort(mediaServerPort);
          mBroadcastSocket.send(mBroadcastPkt);

          for (int j = 0; j < 5; j++) {
            try {
              mBroadcastSocket.receive(mReceivePkt);
              String remoteIp = mReceivePkt.getAddress().getHostName();
              String msg = new String(mReceivePkt.getData(), 0, mReceivePkt.getLength());
              Log.i(TAG, "Received: " + msg);

              FragmentActivity mainActivity = getActivity();
              if (mainActivity != null) {
                mainActivity.runOnUiThread(() -> {
                  try {
                    int image_id = R.drawable.linux;
                    JSONObject json = new JSONObject(msg);
                    int port = json.getInt("port");
                    if (json.has("system")) {
                      String system = json.getString("system");
                      if (system.contains("mac")) {
                        image_id = R.drawable.mac;
                      } else if (system.contains("linux")) {
                        image_id = R.drawable.linux;
                      } else if (system.contains("windows")) {
                        image_id = R.drawable.windows;
                      } else {
                        image_id = R.drawable.linux;
                      }
                    }
                    String mediaServerAddr = remoteIp + ":" + port;
                    Log.i(TAG, mediaServerAddr);
                    boolean found = false;

                    for ( MediaServerItem iter : mediaServerContent.ITEMS) {
                      if (iter.addr.equals(mediaServerAddr)) {
                        Log.i(TAG, "found");
                        found = true;
                      }
                    }
                    if (!found) {
                      Log.i(TAG, "insert");
                      mediaServerContent.addItem(new MediaServerItem(image_id, mediaServerAddr));
                      mediaServerRecyclerViewAdapter.notifyDataSetChanged();
                    }
                  } catch (JSONException e) {
                    Log.i(TAG, e.toString());
                    e.printStackTrace();
                  }
                });
              }
            } catch (SocketTimeoutException e) {
              Log.i(TAG, "Received: " + "timeout");
            }
          }

          Thread.sleep(500);
          i++;
          Log.i(TAG,Thread.currentThread().getName()+":Running()_Count:"+i);
        } catch (InterruptedException e) {
          e.printStackTrace();
          Log.i(TAG,Thread.currentThread().getName()+"异常抛出，停止线程");
          break;// 抛出异常跳出循环
        } catch (IOException e) {
          Log.e(TAG, e.toString());
          e.printStackTrace();
          break;
        }
      }
    }
  }

  public InetAddress getBoardCastAddress() {
    try {
      final InetAddress defaultAddr = InetAddress.getByName("255.255.255.255");
      WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
      DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
      if (null == dhcpInfo) {
        return defaultAddr;
      }
      int boardcast = (dhcpInfo.ipAddress & dhcpInfo.netmask) | ~dhcpInfo.netmask;
      byte[] quads = new byte[4];
      for (int i = 0; i < 4; i++) {
        quads[i] = (byte)((boardcast >> i * 8) & 0xff);
      }
      return InetAddress.getByAddress(quads);
    }
    catch (java.net.UnknownHostException e) {
      Log.e(TAG, e.toString());
    }
    return null;
  }
}
