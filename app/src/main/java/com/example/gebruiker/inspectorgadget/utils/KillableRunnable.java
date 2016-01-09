package com.example.gebruiker.inspectorgadget.utils;

/**
 * Created by Jan on 8/01/2016.
 */
public abstract class KillableRunnable implements Runnable {
    private boolean killMe = false;

    public abstract void doWork();

    @Override
    public void run() {
        if(!killMe)
            doWork();
    }

    final public void kill(){
        killMe = true;
    }
}
