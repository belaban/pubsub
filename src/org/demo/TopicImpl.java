package org.demo;

import org.jgroups.JChannel;
import org.jgroups.annotations.MBean;
import org.jgroups.annotations.ManagedAttribute;
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
@MBean(description="A topic")
public class TopicImpl<T> implements ITopic<T> {
    protected final JChannel                 ch;
    protected final AsciiString              name;
    protected final List<MessageListener<T>> listeners=new ArrayList<>();

    @ManagedAttribute(description="Number of published posts to this topic")
    protected int num_published=0;
    @ManagedAttribute(description="Number of received posts to this topic")
    protected int num_received=0;

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
        num_published++;
    }

    protected void notifyListeners(T msg) {
        num_received++;
        for(MessageListener<T> listener: listeners)
            listener.onMessage(new Message<T>(msg));
    }
}
