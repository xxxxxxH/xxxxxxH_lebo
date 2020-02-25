package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hpplay.sdk.source.browse.api.ILelinkServiceManager;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {


    private LelinkHelper mLelinkHelper;
    RecyclerView recyclerView;
    BrowseAdapter adapter;
    Context mContext;
    private UIHandler mDelayHandler;
    private static final int REQUEST_MUST_PERMISSION = 1;
    private boolean isFirstBrowse = true;
    private String url = "https://v.mifile.cn/b2c-mimall-media/ed921294fb62caf889d40502f5b38147.mp4";
    int serachCount = 0;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        initView();
    }

    private void initView() {
        Button browse = findViewById(R.id.brower);
        recyclerView = findViewById(R.id.rv);
        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browse();
            }
        });

        Button play = findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                play();
                startMirror();
            }
        });

        findViewById(R.id.diconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMirror();
            }
        });
        mDelayHandler = new UIHandler(MainActivity.this);
        if (ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_DENIED) {
            initLelinkHelper();
        } else {
            // 若没有授权，会弹出一个对话框（这个对话框是系统的，开发者不能自己定制），用户选择是否授权应用使用系统权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_MUST_PERMISSION);
        }
    }

    private void initLelinkHelper() {
        mLelinkHelper = MyApplication.getMyApplication().getLelinkHelper();
        mLelinkHelper.setUIUpdateListener(mUIUpdateListener);
    }


    private void browse() {
        if (null != mLelinkHelper) {
            if (!isFirstBrowse) {
                isFirstBrowse = true;
            }
            mLelinkHelper.browse(ILelinkServiceManager.TYPE_ALL);
        } else {
            ToastUtil.show(mContext, "权限不够");
        }
    }

    private void stopBrowse() {
        if (null != mLelinkHelper) {
            isFirstBrowse = false;
            mLelinkHelper.stopBrowse();
        } else {
            ToastUtil.show(mContext, "未初始化");
        }
    }


    private void setRecyclerView(List<LelinkServiceInfo> list) {
        adapter = new BrowseAdapter(mContext, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
        adapter.setmItemClickListener(this);
    }

    @Override
    public void onClick(int position, LelinkServiceInfo pInfo) {
        mLelinkHelper.connect(pInfo);
        adapter.setSelectInfo(pInfo);
        adapter.notifyDataSetChanged();
    }

    private void play() {
        mLelinkHelper.playNetMedia(url, 102, null);
    }

    private void startMirror() {
        if (null == mLelinkHelper) {
            ToastUtil.show(mContext, "未初始化");
            return;
        }
        LelinkServiceInfo info = adapter.getSelectInfo();
        if (null == info) {
            ToastUtil.show(mContext, "请在连接列表选中设备");
            return;
        }

        // 开启镜像声音需要权限
        if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED) {

            mLelinkHelper.startMirror(MainActivity.this, info, AllCast.RESOLUTION_MIDDLE, AllCast.BITRATE_MIDDLE,
                    true, null);
        } else {
            // 不同意，则去申请权限
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

    }

    private void stopMirror() {
        mLelinkHelper.stopMirror();
    }

    private static class UIHandler extends Handler {

        private WeakReference<MainActivity> mReference;

        UIHandler(MainActivity reference) {
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mReference.get();
            if (mainActivity == null) {
                return;
            }
            switch (msg.what) {
                case IUIUpdateListener.STATE_SEARCH_SUCCESS:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private IUIUpdateListener mUIUpdateListener = new IUIUpdateListener() {

        @Override
        public void onUpdate(int what, MessageDeatail deatail) {
            switch (what) {
                case IUIUpdateListener.STATE_SEARCH_SUCCESS:
                    if (isFirstBrowse) {

                        isFirstBrowse = false;
                        ToastUtil.show(mContext, "搜索成功");
                    }
                    if (null != mDelayHandler) {
                        mDelayHandler.removeCallbacksAndMessages(null);
                        mDelayHandler.sendEmptyMessageDelayed(IUIUpdateListener.STATE_SEARCH_SUCCESS,
                                TimeUnit.SECONDS.toMillis(1));
                        setRecyclerView(deatail.list);
                    }
                    break;
                case IUIUpdateListener.STATE_SEARCH_ERROR:
                    ToastUtil.show(mContext, "Auth错误");
                    break;
                case IUIUpdateListener.STATE_SEARCH_NO_RESULT:
                    if (null != mDelayHandler) {
                        mDelayHandler.removeCallbacksAndMessages(null);
                        mDelayHandler.sendEmptyMessageDelayed(IUIUpdateListener.STATE_SEARCH_SUCCESS,
                                TimeUnit.SECONDS.toMillis(1));
                    }
                    break;
                case IUIUpdateListener.STATE_CONNECT_SUCCESS:
                    ToastUtil.show(mContext, deatail.text);
                    break;
                case IUIUpdateListener.STATE_DISCONNECT:
                    ToastUtil.show(mContext, deatail.text);
                    break;
                case IUIUpdateListener.STATE_CONNECT_FAILURE:

                    break;
                case IUIUpdateListener.STATE_PLAY:

                    break;
                case IUIUpdateListener.STATE_LOADING:

                    break;
                case IUIUpdateListener.STATE_PAUSE:


                    break;
                case IUIUpdateListener.STATE_STOP:

                    break;
                case IUIUpdateListener.STATE_SEEK:

                    break;
                case IUIUpdateListener.STATE_PLAY_ERROR:

                    break;
                case IUIUpdateListener.STATE_POSITION_UPDATE:

                    break;
                case IUIUpdateListener.STATE_COMPLETION:

                    break;
                case IUIUpdateListener.STATE_INPUT_SCREENCODE:

                    break;
                case IUIUpdateListener.RELEVANCE_DATA_UNSUPPORT:


                    break;
                case IUIUpdateListener.STATE_SCREENSHOT:

                    break;
            }
        }

    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_MUST_PERMISSION) {
            boolean denied = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    denied = true;
                }
            }
            if (denied) {
                // 拒绝
                ToastUtil.show(mContext, "您拒绝了权限");
            } else {
                // 允许
                initLelinkHelper();
            }
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            boolean denied = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    denied = true;
                }
            }
            if (denied) {
                // 拒绝
                ToastUtil.show(mContext, "您录制音频的权限");
            } else {
                // 允许
                startMirror();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
