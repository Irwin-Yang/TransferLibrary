package com.irwin.transfer;

/**
 * Call back interface for asynchronous operation.
 * @param <RESULT> Result data type.
 */
public interface ICallback<RESULT> {

    public void onSuccess(RESULT result);

    public void onFail(Throwable info);

}
