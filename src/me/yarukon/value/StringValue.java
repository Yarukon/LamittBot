package me.yarukon.value;

import java.util.ArrayList;

public class StringValue extends Value {

	public StringValue(String key, String value, ArrayList<Value> targetList) {
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
