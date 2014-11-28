package org.demo;

/**
 * @author Bela Ban
 * @since x.y
 */
public interface MessageListener<T> {
    void onMessage(Message<T> msg);
}
