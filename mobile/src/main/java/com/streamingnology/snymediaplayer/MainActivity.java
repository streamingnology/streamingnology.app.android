package com.streamingnology.snymediaplayer;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.github.ikidou.fragmentBackHandler.BackHandlerHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.streamingnology.snymediaplayer.fragment.File.FileContent;
import com.streamingnology.snymediaplayer.fragment.FileExplorerFragment;
import com.streamingnology.snymediaplayer.fragment.PlayFragment;
import com.streamingnology.snymediaplayer.fragment.SearchMediaServerFragment;
import com.streamingnology.snymediaplayer.fragment.SettingFragment;
import com.streamingnology.snymediaplayer.fragment.mediaserver.MediaServerContent;


public class MainActivity extends FragmentActivity
                          implements FileExplorerFragment.OnListFragmentInteractionListener ,
                                     FileExplorerFragment.OnPlayVideoListener,
                                     SearchMediaServerFragment.OnListFragmentInteractionListener,
                                     PlayFragment.OnFragmentInteractionListener{
  private static String TAG = "MainActivity";
  private AdView adView;
  private SearchMediaServerFragment searchMediaServerFragment = null;
  private FileExplorerFragment fileExplorerFragment = null;
  private SettingFragment settingFragment = null;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
          = item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
              case R.id.navigation_search:
                if (searchMediaServerFragment == null) {
                  searchMediaServerFragment = new SearchMediaServerFragment();
                }
                selectedFragment = searchMediaServerFragment;
                break;
              case R.id.navigation_fileexplorer:
                if (fileExplorerFragment == null) {
                  fileExplorerFragment = new FileExplorerFragment();
                }
                selectedFragment = fileExplorerFragment;
                break;
              case R.id.navigation_settings:
                if (settingFragment == null) {
                  settingFragment = new SettingFragment();
                }
                selectedFragment = settingFragment;
                break;
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, selectedFragment);
            transaction.commit();
            return true;
          };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    MobileAds.initialize(this, getString(R.string.admob_app_id));
    adView = findViewById(R.id.ad_view);
    AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .build();
    adView.loadAd(adRequest);

    BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    if (searchMediaServerFragment == null) {
      searchMediaServerFragment = new SearchMediaServerFragment();
    }
    transaction.replace(R.id.frame_container, searchMediaServerFragment);
    transaction.commit();
  }

  /** Called when leaving the activity */
  @Override
  public void onPause() {
    if (adView != null) {
      adView.pause();
    }
    super.onPause();
  }

  /** Called when returning to the activity */
  @Override
  public void onResume() {
    super.onResume();
    if (adView != null) {
      adView.resume();
    }
  }

  /** Called before the activity is destroyed */
  @Override
  public void onDestroy() {
    if (adView != null) {
      adView.destroy();
    }
    super.onDestroy();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    // TODO Auto-generated method stub
    super.onConfigurationChanged(newConfig);
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      hideSystemUI();
      BottomNavigationView navigation = findViewById(R.id.navigation);
      navigation.setVisibility(View.GONE);
      //adView.setVisibility(View.GONE);
    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
      //unhide your objects here.
      showSystemUI();
      BottomNavigationView navigation = findViewById(R.id.navigation);
      navigation.setVisibility(View.VISIBLE);
      //adView.setVisibility(View.VISIBLE);
    }
  }

  private void loadFragment(Fragment fragment) {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.frame_container, fragment);
    transaction.addToBackStack(null);
    transaction.commit();
  }

  @Override
  public void onBackPressed() {
    if (!BackHandlerHelper.handleBackPress(this)) {
      super.onBackPressed();
    }
  }

  @Override
  public void onListFragmentInteraction(FileContent.FileItem item) {
    Log.d(TAG, "onListFragmentInteraction: " + item.name);
  }

  @Override
  public void OnPlayVideo(String video_uri) {
    Log.d(TAG, "OnPlayVideo: " + video_uri);
    Bundle bundle = new Bundle();
    bundle.putString( "video_uri", video_uri);
    PlayFragment frag = new PlayFragment();
    frag.setArguments(bundle);
    loadFragment(frag);
  }

  @Override
  public void onListFragmentInteraction(MediaServerContent.MediaServerItem item) {
    Log.d(TAG, "onListFragmentInteraction: " + item.addr);
    SharedPreferences preferences = getSharedPreferences("UserSettings", MODE_PRIVATE);
    boolean serverChanged = preferences.getBoolean("changedServer", false);
    if (serverChanged) {
      fileExplorerFragment = null;
      SharedPreferences.Editor editor = preferences.edit();
      editor.putBoolean("changedServer", false);
    }
    BottomNavigationView navigation = findViewById(R.id.navigation);
    navigation.setSelectedItemId(R.id.navigation_fileexplorer);
  }

  @Override
  public void onFragmentInteraction(Uri uri) {
    Log.d(TAG, "onListFragmentInteraction: " + uri.toString());
  }


  private void hideSystemUI() {
    View decorView = getWindow().getDecorView();
    decorView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
  }

  private void showSystemUI() {
    View decorView = getWindow().getDecorView();
    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
  }
}
