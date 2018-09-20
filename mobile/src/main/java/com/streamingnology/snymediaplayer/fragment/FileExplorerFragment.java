package com.streamingnology.snymediaplayer.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.ikidou.fragmentBackHandler.BackHandledFragment;
import com.streamingnology.snymediaplayer.R;
import com.streamingnology.snymediaplayer.fragment.File.FileContent;
import com.streamingnology.snymediaplayer.fragment.File.FileContent.FileItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FileExplorerFragment extends BackHandledFragment {

  private static String TAG = "FileExplorerFragment";
  private static final String ARG_COLUMN_COUNT = "column-count";
  // TODO: Customize parameters
  private int mColumnCount = 1;
  private OnListFragmentInteractionListener mListener;
  private OnPlayVideoListener               mPlayVideoListener = null;
  private FileRecyclerViewAdapter fileRecyclerViewAdapter = null;
  private FileContent fileContent = new FileContent();

  private String snyGetDirContentUri  = null;
  private String snyGetMediaInfoUri   = null;
  private Stack<String> directoryCache       = new Stack<>();


  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public FileExplorerFragment() {
  }

  // TODO: Customize parameter initialization
  @SuppressWarnings("unused")
  public static FileExplorerFragment newInstance(int columnCount) {
    FileExplorerFragment fragment = new FileExplorerFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_COLUMN_COUNT, columnCount);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    Log.d(TAG, "onSaveInstanceState");
    savedInstanceState.putString("carloz", "Carlo Zhang");
    ArrayList<String> cacheDir = new ArrayList<>();
    for (String iter: directoryCache) {
      cacheDir.add(iter);
    }
    savedInstanceState.putStringArrayList("cacheDir",cacheDir);
    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      ArrayList<String> cacheDir = savedInstanceState.getStringArrayList("cacheDir");
      for (String iter : cacheDir) {
        directoryCache.push(iter);
      }
    }
    if (getArguments() != null) {
      mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
    }
    initParam();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_file_explorer, container, false);
    RecyclerView recyclerView = view.findViewById(R.id.list);
    // Set the adapter
    if (recyclerView != null) {
      Context context = recyclerView.getContext();
      if (mColumnCount <= 1) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
      } else {
        recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
      }
      if (fileRecyclerViewAdapter == null) {
        fileRecyclerViewAdapter = new FileRecyclerViewAdapter(fileContent.ITEMS, mListener);
      }
      fileRecyclerViewAdapter.setOnItemClickListener(new FileRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(FileItem item) {
          if (item.isFolder) {
            listDirectoryContent(item.path);
            directoryCache.push(item.path);
          } else {
            getMediaInfo(item.path);
          }
        }
      });
      recyclerView.setAdapter(fileRecyclerViewAdapter);
    }
    if (!fileContent.ITEMS.isEmpty()) {
      return view;
    }
    if (directoryCache.empty()) {
      listDirectoryContent(null);
      directoryCache.push("");
    } else {
      String path = directoryCache.lastElement();
      listDirectoryContent(path);
    }

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
    if (context instanceof OnPlayVideoListener) {
      mPlayVideoListener = (OnPlayVideoListener) context;
    } else {
      throw new RuntimeException(context.toString()
              + " must implement OnPlayVideoListener");
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
    void onListFragmentInteraction(FileItem item);
  }

  public interface OnPlayVideoListener {
    // TODO: Update argument type and name
    void OnPlayVideo(String video_uri);
  }

  void postRequest(String postUrl, String postBody) {
    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    RequestBody body = RequestBody.create(JSON, postBody);
    Request request = new Request.Builder().url(postUrl).post(body).build();
    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        call.cancel();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        final String myResponse = response.body().string();
        try {
          getActivity().runOnUiThread(() -> {
            fileContent.clear();
            try {
              JSONObject json = new JSONObject(myResponse);
              JSONArray array = json.getJSONArray("data");
              for (int i = 0; i < array.length(); i++) {
                JSONObject media = array.getJSONObject(i);
                String name = media.getString("label");
                String path = media.getString("path");
                boolean isFolder = !media.getBoolean("last");
                FileItem fileItem = new FileItem(name, path, isFolder);
                fileContent.addFile(fileItem);
              }
              fileRecyclerViewAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
              e.printStackTrace();
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  void listDirectoryContent(String path) {
    if (snyGetDirContentUri== null || snyGetDirContentUri.isEmpty()) {
      Activity activity = getActivity();
      if (activity == null) {
        return;
      }
      Toast.makeText(activity, R.string.sny_select_server_first, Toast.LENGTH_LONG).show();
      return;
    }
    String postBodyInJson = null;
    if (path == null) {
      path = "";
    }
    JSONObject object = new JSONObject();
    try {
      object.put("path", path);
      postBodyInJson = object.toString();
    } catch (JSONException e) {
      e.printStackTrace();
    }
    postRequest(snyGetDirContentUri, postBodyInJson);
  }

  void postGetMediaInfoRequest(String postUrl, String postBody) {
    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    RequestBody body = RequestBody.create(JSON, postBody);
    Request request = new Request.Builder().url(postUrl).post(body).build();
    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        call.cancel();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        final String myResponse = response.body().string();
        try {
          getActivity().runOnUiThread(() -> {
            String playbackUri = null;
            try {
              JSONObject json = new JSONObject(myResponse);
              JSONArray array = json.getJSONArray("data");
              for (int i = 0; i < array.length(); i++) {
                JSONObject media = array.getJSONObject(i);
                String protocol  = media.getString("protocol");
                if (protocol.equals("hls")) {
                  playbackUri = media.getString("uri");
                }
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
            if (playbackUri == null || playbackUri.isEmpty()) {
              return;
            }
            mPlayVideoListener.OnPlayVideo(playbackUri);
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  void getMediaInfo(String path) {
    String postBodyInJson = null;
    if (path == null || path.isEmpty()) {
      return;
    }
    JSONObject object = new JSONObject();
    try {
      object.put("path", path);
      postBodyInJson = object.toString();
    } catch (JSONException e) {
      e.printStackTrace();
    }
    postGetMediaInfoRequest(snyGetMediaInfoUri, postBodyInJson);
  }

  void initParam() {
    SharedPreferences preferences = getActivity().getSharedPreferences("UserSettings", MODE_PRIVATE);
    String snyRemoteMediaServer = preferences.getString("select_mediaserver","");
    if (snyRemoteMediaServer.isEmpty()) {
      return;
    }
    String prefix = "http://";
    snyGetDirContentUri  = prefix + snyRemoteMediaServer + "/system/getDirectoryContent";
    snyGetMediaInfoUri   = prefix + snyRemoteMediaServer + "/media/getMediaInfo";
  }

  public boolean interceptBackPressed() {
    if (directoryCache.size() > 1) {
      directoryCache.pop();
      String path = directoryCache.lastElement();
      listDirectoryContent(path);
      return true;
    } else {
      return false;
    }
  }
}
