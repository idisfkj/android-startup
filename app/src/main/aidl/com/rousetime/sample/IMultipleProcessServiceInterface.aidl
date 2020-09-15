// IMultipleProcessServiceInterface.aidl
package com.rousetime.sample;

// Declare any non-default types here with import statements
import com.rousetime.sample.IServiceListenerInterface;

interface IMultipleProcessServiceInterface {

    void addServiceListener(IServiceListenerInterface serviceListener);

    void initStartup();

    void clear();
}
