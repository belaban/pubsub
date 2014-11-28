package org.demo;

import org.jgroups.JChannel;
import org.jgroups.util.AsciiString;
import org.jgroups.util.Bits;
import org.jgroups.util.ByteArrayDataOutputStream;
import org.jgroups.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bela Ban
 * @since x.y
 */
public class TopicImpl<T> implements ITopic<T> {
    protected final JChannel                 ch;
    protected final AsciiString              name;
    protected final List<MessageListener<T>> listeners=new ArrayList<>();

    public TopicImpl(JChannel ch, AsciiString name) {
        this.ch=ch;
        this.name=name;
    }

    public void addMessageListener(MessageListener<T> listener) {
        if(listener != null && !listeners.contains(listener))
            listeners.add(listener);
    }

    public void publish(T msg) throws Exception {
        ByteArrayDataOutputStream out=new ByteArrayDataOutputStream();
        Bits.writeAsciiString(name,out); // write the topic name first
        Util.objectToStream(msg, out);
        org.jgroups.Message jgroups_msg=new org.jgroups.Message(null, out.buffer(), 0, out.position());
        ch.send(jgroups_msg);
    }

    protected void notifyListeners(T msg) {
        for(MessageListener<T> listener: listeners)
            listener.onMessage(new Message<T>(msg));
    }
}
