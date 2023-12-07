package me.yarukon.value.impl;

import me.yarukon.value.ValueBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class MultiBooleanValue extends ValueBase {
    private final LinkedHashMap<String, MultiBoolean> values = new LinkedHashMap<>();

    public MultiBooleanValue(String key, ArrayList<ValueBase> targetList, MultiBoolean... multiBooleans) {
        this.key = key;

        for(MultiBoolean mb : multiBooleans)
            this.values.put(mb.name, mb);

        targetList.add(this);
    }

    public HashMap<String, MultiBoolean> getValues() {
        return this.values;
    }

    public MultiBoolean getSetting(String name) {
        return this.values.get(name);
    }

    public boolean getValue(String name) {
        MultiBoolean bol = this.values.get(name);
        if (bol != null)
            return bol.state;

        return false;
    }
}
