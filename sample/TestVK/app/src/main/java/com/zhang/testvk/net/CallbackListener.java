package com.zhang.testvk.net;


/**
 * Created by zhouwm on 15/6/30.
 */
public interface CallbackListener {

    void onSuccess(Object object);

    void onFailed(String error);

}
