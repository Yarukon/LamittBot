package me.yarukon.value;

import java.util.ArrayList;

public class MultiStringValue extends Value {

    private final ArrayList<String> values = new ArrayList<>();

    public MultiStringValue(String key, ArrayList<Value> targetList) {
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
        return values.add(value);
    }

    public boolean delValue(String value) {
        return values.remove(value);
    }
}
