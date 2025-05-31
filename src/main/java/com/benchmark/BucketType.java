package com.benchmark;

public enum BucketType {
    POS("pre open session"), CTS("continuous trading session"), L("lunch break"), CAS("close auction session");

    private final String name;

    BucketType(String name) {
        this.name = name;
    }

    public static <E extends Enum<E>> boolean isBucketType(String value, Class<E> enumClass) {
        try {
            Enum.valueOf(enumClass, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String toString() {
        return name;
    }
}
