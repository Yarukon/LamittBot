package me.yarukon.utils;

public class TimeHelper {
    public long lastMs;

    public TimeHelper() {
        this.lastMs = 0L;
    }

    public long getTime() {
		return System.currentTimeMillis() - this.lastMs;
	}

    public long getTimeTo(long target) {
        return target - getTime();
    }

    public boolean delay(double nextDelay) {
        return System.currentTimeMillis() - lastMs >= nextDelay;
    }
    
    public boolean delay(double nextDelay, boolean reset) {
        if(System.currentTimeMillis() - lastMs >= nextDelay) {
        	if(reset) this.reset();
        	return true;
        }
        
        return false;
    }
    
    public void reset() {
        this.lastMs = System.currentTimeMillis();
    }
}