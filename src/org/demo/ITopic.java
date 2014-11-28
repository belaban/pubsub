package org.demo;

/**
 * @author Bela Ban
 * @since x.y
 */
public interface ITopic<T> {
    void addMessageListener(MessageListener<T> listener);
    void publish(T msg) throws Exception;
}
