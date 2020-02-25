package com.example.myapplication;

import android.content.Context;

import com.hpplay.sdk.source.api.IConnectListener;
import com.hpplay.sdk.source.api.LelinkPlayer;
import com.hpplay.sdk.source.api.LelinkPlayerInfo;
import com.hpplay.sdk.source.browse.api.IAPI;
import com.hpplay.sdk.source.browse.api.IBrowseListener;
import com.hpplay.sdk.source.browse.api.ILelinkServiceManager;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;
import com.hpplay.sdk.source.browse.api.LelinkServiceManager;
import com.hpplay.sdk.source.browse.api.LelinkSetting;

public class AllCast {
    private ILelinkServiceManager mLelinkServiceManager;
    private LelinkPlayer mLelinkPlayer;
    public static final int RESOLUTION_MIDDLE = com.hpplay.sdk.source.api.ILelinkMirrorManager.RESOLUTION_MID;
    public static final int BITRATE_MIDDLE = com.hpplay.sdk.source.api.ILelinkMirrorManager.BITRATE_MID;
    public AllCast(Context context, String appid, String appSecret) {
        initLelinkService(context, appid, appSecret);
    }

    public void setOnBrowseListener(IBrowseListener listener) {
        mLelinkServiceManager.setOnBrowseListener(listener);
    }

    public void setConnectListener(IConnectListener listener) {
        mLelinkPlayer.setConnectListener(listener);
    }

    private void initLelinkPlayer(Context pContext) {
        mLelinkPlayer = new LelinkPlayer(pContext);
    }

    public void browse(int type) {
        mLelinkServiceManager.browse(type);
    }
    public void stopBrowse() {
        mLelinkServiceManager.stopBrowse();
    }
    public void connect(LelinkServiceInfo info) {
        mLelinkPlayer.connect(info);
    }

    public void disConnect(LelinkServiceInfo info) {
        if(info != null){
            mLelinkPlayer.disConnect(info);
        }
    }

    public void playNetMedia(String url, int type, String screenCode) {
        LelinkPlayerInfo lelinkPlayerInfo = new LelinkPlayerInfo();
        lelinkPlayerInfo.setType(type);
        lelinkPlayerInfo.setUrl(url);
        lelinkPlayerInfo.setOption(IAPI.OPTION_6, screenCode);
        // lelinkPlayerInfo.setStartPosition(15);
        mLelinkPlayer.setDataSource(lelinkPlayerInfo);
        mLelinkPlayer.start();
    }
    private void initLelinkService(Context context, String appid, String appSecret) {
        LelinkSetting lelinkSetting = new LelinkSetting.LelinkSettingBuilder(appid, appSecret)
                .build();
        mLelinkServiceManager = LelinkServiceManager.getInstance(context);
        mLelinkServiceManager.setDebug(true);
        mLelinkServiceManager.setLelinkSetting(lelinkSetting);
        mLelinkServiceManager.setOption(IAPI.OPTION_5, false);
        initLelinkPlayer(context);
    }

    public void startMirror(android.app.Activity pActivity, LelinkServiceInfo lelinkServiceInfo,
                            int resolutionLevel, int bitrateLevel, boolean isAudioEnnable, String screenCode) {
        if (mLelinkPlayer != null) {
            LelinkPlayerInfo lelinkPlayerInfo = new LelinkPlayerInfo();
            lelinkPlayerInfo.setType(LelinkPlayerInfo.TYPE_MIRROR);
            lelinkPlayerInfo.setActivity(pActivity);
            lelinkPlayerInfo.setOption(IAPI.OPTION_6, screenCode);
            lelinkPlayerInfo.setLelinkServiceInfo(lelinkServiceInfo);
            lelinkPlayerInfo.setMirrorAudioEnable(isAudioEnnable);
            lelinkPlayerInfo.setResolutionLevel(resolutionLevel);
            lelinkPlayerInfo.setBitRateLevel(bitrateLevel);
            mLelinkPlayer.setDataSource(lelinkPlayerInfo);
            mLelinkPlayer.start();
        }
    }

    public void stopMirror() {
        if (mLelinkPlayer != null) {
            mLelinkPlayer.stop();
        }
    }
}
