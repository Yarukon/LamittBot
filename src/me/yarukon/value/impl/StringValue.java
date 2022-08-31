package me.yarukon.value.impl;

import me.yarukon.value.ValueBase;

import java.util.ArrayList;

public class StringValue extends ValueBase {

	public StringValue(String key, String value, ArrayList<ValueBase> targetList) {
        this.key = key;
        this.value = value;

        targetList.add(this);
    }
	
	@Override
    public String getValue() {
        return (String) this.value;
    }

    public void setValue(String value) {
    	this.value = value;
    }
}
