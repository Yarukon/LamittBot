package me.yarukon.value;

public class Value {
    protected String key;
    protected Object value;

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getKey() {
    	return key;
    }
}
