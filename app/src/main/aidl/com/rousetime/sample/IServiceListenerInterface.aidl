// IServiceListenerInterface.aidl
package com.rousetime.sample;

// Declare any non-default types here with import statements

interface IServiceListenerInterface {

    void onCompleted(String result, long totalMainThreadCostTime);

}
