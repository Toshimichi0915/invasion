package net.toshimichi.invasion;

public class Holder<T> {
    private T obj;

    public Holder(T obj) {
        this.obj = obj;
    }

    public Holder() {
    }

    public T get() {
        return obj;
    }

    public void set(T obj) {
        this.obj = obj;
    }
}
