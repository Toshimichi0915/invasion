package net.toshimichi.invasion;

public class Holder<T> {
    private T obj;

    public T get() {
        return obj;
    }

    public void set(T obj) {
        this.obj = obj;
    }
}
