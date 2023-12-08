package me.yarukon.value.impl;

import me.yarukon.value.ValueBase;

import java.util.ArrayList;

public class MultiStringValue extends ValueBase {

    private final ArrayList<String> values = new ArrayList<>();

    public MultiStringValue(String key, ArrayList<ValueBase> targetList) {
        this.key = key;
        targetList.add(this);
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public boolean hasValue(String value) {
        return this.values.contains(value);
    }

    public String getValue(int index) {
        return this.values.get(index);
    }

    public boolean addValue(String value) {
        if (hasValue(value))
            return false;

        return values.add(value);
    }

    public boolean delValue(String value) {
        if (!hasValue(value))
            return false;

        return values.remove(value);
    }
}
