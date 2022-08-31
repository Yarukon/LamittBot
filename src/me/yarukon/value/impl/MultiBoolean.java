package me.yarukon.value.impl;

public class MultiBoolean {
    public String name;
    public boolean state;

    public MultiBoolean(String name, boolean state) {
        this.name = name;
        this.state = state;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}