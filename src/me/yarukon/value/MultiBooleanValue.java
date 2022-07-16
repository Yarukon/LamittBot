package me.yarukon.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MultiBooleanValue extends Value {
    private final LinkedHashMap<String, MultiBoolean> values = new LinkedHashMap<>();

    public MultiBooleanValue(String key, ArrayList<Value> targetList, MultiBoolean... multiBooleans) {
        this.key = key;

        for(MultiBoolean mb : multiBooleans) {
            this.values.put(mb.name, mb);
        }

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

        if (bol != null) {
            return bol.state;
        }

        return false;
    }
}
