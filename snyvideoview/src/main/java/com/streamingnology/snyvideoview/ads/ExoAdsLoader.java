package com.streamingnology.snyvideoview.ads;

import android.view.ViewGroup;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ads.AdsLoader;

import java.io.IOException;

/**
 * Created by mo on 17-11-30.
 * 剑气纵横三万里 一剑光寒十九洲
 */

public class ExoAdsLoader extends Player.DefaultEventListener implements AdsLoader {

    @Override
    public void setSupportedContentTypes(int... contentTypes) {

    }

    @Override
    public void attachPlayer(ExoPlayer player, EventListener eventListener, ViewGroup adUiViewGroup) {

    }

    @Override
    public void detachPlayer() {

    }

    @Override
    public void release() {

    }

    @Override
    public void handlePrepareError(int adGroupIndex, int adIndexInAdGroup, IOException exception) {

    }
}
