package me.yarukon.value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class MultiMapValue extends Value {
    private final LinkedHashMap<String, Keypair> values = new LinkedHashMap<>();

    public MultiMapValue(String key, ArrayList<Value> targetList) {
        this.key = key;
        targetList.add(this);
    }

    public HashMap<String, Keypair> getValues() {
        return this.values;
    }

    public Keypair getValue(String key) {
        return this.values.get(key);
    }

    public boolean hasKey(String key) {
        return this.values.containsKey(key);
    }

    public String[] getKeys() {
        return this.values.keySet().toArray(new String[0]);
    }

    public void addValue(String key, Keypair value) {
        this.values.put(key, value);
    }

    public boolean removeValue(String key) {
        if (this.values.containsKey(key)) {
            this.values.remove(key);
            return true;
        }

        return false;
    }
}
