package com.example.myapplication;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.hpplay.sdk.source.api.IConnectListener;
import com.hpplay.sdk.source.browse.api.IBrowseListener;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;

import java.util.ArrayList;
import java.util.List;

public class LelinkHelper{

    private static final String APP_ID = "13259";
    private static final String APP_SECRET = "15ac9882fe9ab230a71a1c5b128368d5";
    private static LelinkHelper sLelinkHelper;
    private Context mContext;
    private AllCast mAllCast;
    private UIHandler mUIHandler;
    private List<LelinkServiceInfo> mInfos;



    public static LelinkHelper getInstance(Context context) {
        if (sLelinkHelper == null) {
            sLelinkHelper = new LelinkHelper(context);
        }
        return sLelinkHelper;
    }

    private LelinkHelper(Context context) {
        mContext = context;
        mUIHandler = new UIHandler(Looper.getMainLooper());
        mAllCast = new AllCast(context.getApplicationContext(), APP_ID, APP_SECRET);
        mAllCast.setOnBrowseListener(mBrowseListener);
        mAllCast.setConnectListener(mConnectListener);
    }

    public void setUIUpdateListener(IUIUpdateListener listener) {
        mUIHandler.setUIUpdateListener(listener);
    }

    public void browse(int type) {
        mAllCast.browse(type);
    }
    public void stopBrowse() {
        mAllCast.stopBrowse();
    }

    public void connect(LelinkServiceInfo info) {
        mAllCast.connect(info);
    }

    public void disConnect(LelinkServiceInfo info) {
        mAllCast.disConnect(info);
    }

    public void playNetMedia(String url, int mediaType, String screencode) {
        mAllCast.playNetMedia(url, mediaType, screencode);
    }

    public void startMirror(MainActivity activity, LelinkServiceInfo info, int resolutionLevel,
                            int bitrateLevel, boolean audioEnable, String screencode) {
        mAllCast.startMirror(activity, info, resolutionLevel, bitrateLevel, audioEnable, screencode);
    }

    public void stopMirror() {
        mAllCast.stopMirror();
    }

    private IBrowseListener mBrowseListener = new IBrowseListener() {

        @Override
        public void onBrowse(int resultCode, List<LelinkServiceInfo> list) {
            mInfos = list;
            if (resultCode == IBrowseListener.BROWSE_SUCCESS) {
                StringBuffer buffer = new StringBuffer();
                if (null != mInfos) {
                    for (LelinkServiceInfo info : mInfos) {
                        buffer.append("name：").append(info.getName())
                                .append(" uid: ").append(info.getUid())
                                .append(" type:").append(info.getTypes()).append("\n");
                    }
                    buffer.append("---------------------------\n");
                    if (null != mUIHandler) {
                        // 发送文本信息
                        String text = buffer.toString();
                        if (mInfos.isEmpty()) {
                            mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_SEARCH_NO_RESULT, text,null));
                        } else {
                            mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_SEARCH_SUCCESS, text,mInfos));
                        }
                    }
                }
            } else {
                if (null != mUIHandler) {
                    // 发送文本信息
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_SEARCH_ERROR, "搜索错误：Auth错误",null));
                }
            }
        }

    };

    private IConnectListener mConnectListener = new IConnectListener() {

        @Override
        public void onConnect(final LelinkServiceInfo serviceInfo, final int extra) {
            if (null != mUIHandler) {
                String type = extra == TYPE_LELINK ? "Lelink" : extra == TYPE_DLNA ? "DLNA" : extra == TYPE_NEW_LELINK ? "NEW_LELINK" : "IM";
                String text;
                if (TextUtils.isEmpty(serviceInfo.getName())) {
                    text = "pin码连接" + type + "成功";
                } else {
                    text = serviceInfo.getName() + "连接" + type + "成功";
                }
                List<LelinkServiceInfo> list = new ArrayList<>();
                list.add(serviceInfo);
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_CONNECT_SUCCESS, text,list, serviceInfo));
            }
        }

        @Override
        public void onDisconnect(LelinkServiceInfo serviceInfo, int what, int extra) {
            if (what == IConnectListener.CONNECT_INFO_DISCONNECT) {
                if (null != mUIHandler) {
                    String text;
                    if (TextUtils.isEmpty(serviceInfo.getName())) {
                        text = "pin码连接断开";
                    } else {
                        text = serviceInfo.getName() + "连接断开";
                    }
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_DISCONNECT, text,null));
                }
            } else if (what == IConnectListener.CONNECT_ERROR_FAILED) {
                String text = null;
                if (extra == IConnectListener.CONNECT_ERROR_IO) {
                    text = serviceInfo.getName() + "连接失败";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_WAITTING) {
                    text = serviceInfo.getName() + "等待确认";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_REJECT) {
                    text = serviceInfo.getName() + "连接拒绝";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_TIMEOUT) {
                    text = serviceInfo.getName() + "连接超时";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_BLACKLIST) {
                    text = serviceInfo.getName() + "连接黑名单";
                }
                if (null != mUIHandler) {
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_CONNECT_FAILURE, text,null));
                }
            }
        }

    };

    private Message buildMessageDetail(int state, String text,List<LelinkServiceInfo> list) {
        return buildMessageDetail(state, text, list,null);
    }


    private Message buildMessageDetail(int state, String text,List<LelinkServiceInfo> list, Object object) {
        MessageDeatail deatail = new MessageDeatail();
        deatail.text = text;
        deatail.obj = object;
        deatail.list = list;

        Message message = Message.obtain();
        message.what = state;
        message.obj = deatail;
        return message;
    }

    private static class UIHandler extends Handler {

        private IUIUpdateListener mUIUpdateListener;

        private UIHandler(Looper looper) {
            super(looper);
        }

        private void setUIUpdateListener(IUIUpdateListener pUIUpdateListener) {
            mUIUpdateListener = pUIUpdateListener;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MessageDeatail detail = (MessageDeatail) msg.obj;
            if (null != mUIUpdateListener) {
                mUIUpdateListener.onUpdate(msg.what, detail);
            }
        }
    }
}
