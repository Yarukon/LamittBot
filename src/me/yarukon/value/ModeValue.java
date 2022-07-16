package me.yarukon.value;

import java.util.ArrayList;
import java.util.Arrays;

public class ModeValue extends Value {
    public ArrayList<String> modes = new ArrayList<>();

    public ModeValue(String key, String defaultMode, ArrayList<Value> targetList, String... modes) {
        this.key = key;
        this.value = defaultMode;
        this.modes.addAll(Arrays.asList(modes));

        targetList.add(this);
    }

    @Override
    public String getValue() {
        return (String) this.value;
    }

    public boolean setValue(String value) {

        if(this.modes.contains(value)) {
            this.value = value;
            return true;
        }

        return false;
    }
}
