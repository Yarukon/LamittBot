package me.yarukon.value.impl;

import me.yarukon.value.ValueBase;

import java.util.ArrayList;

public class BooleanValue extends ValueBase {
	
    public BooleanValue(String key, Boolean value, ArrayList<ValueBase> targetList) {
        this.key = key;
        this.value = value;

        targetList.add(this);
    }

    @Override
    public Boolean getValue() {
        return (Boolean) this.value;
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
    }

	public boolean getValueState() {
		return this.getValue();
	}
}