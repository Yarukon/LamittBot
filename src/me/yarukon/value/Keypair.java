package me.yarukon.value;

public class Keypair {
    public String key;
    public String value;

    public Keypair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
