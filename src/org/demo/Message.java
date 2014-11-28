package org.demo;

/**
 * @author Bela Ban
 * @since x.y
 */
public class Message<T> {
    protected T obj;

    public Message(T obj) {
        this.obj=obj;
    }

    public T getMessageObject() {
        return obj;
    }
}
