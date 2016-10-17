package org.demo;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.annotations.MBean;
import org.jgroups.annotations.ManagedAttribute;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.jgroups.util.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Bela Ban
 * @since x.y
 */
@MBean(description="Handles the JGroups channel and the topics")
public class JGroupsInstance<T> extends ReceiverAdapter {
    protected JChannel                                      ch;
    protected final ConcurrentMap<AsciiString,TopicImpl<T>> topics=new ConcurrentHashMap<>();
    protected final Log                                     log=LogFactory.getLog(JGroupsInstance.class);

    public JGroupsInstance() throws Exception {
        this("config.xml", "pubsub", null);
    }

    public JGroupsInstance(String cfg) throws Exception {
        this(cfg, "pubsub", null);
    }

    public JGroupsInstance(String cfg, String clusterName, String name) throws Exception {
        ch=new JChannel(cfg);
        if(name != null)
            ch.name(name);
        ch.setReceiver(this);
        ch.connect(clusterName);
    }

    @ManagedAttribute public String getTopics() {
        return topics.keySet().toString();
    }

    @ManagedAttribute public int numTopics() {return topics.size();}

    public ITopic<T> getTopic(String topic_name) {
        AsciiString name=new AsciiString(topic_name);
        TopicImpl<T> retval=topics.get(name);
        if(retval != null)
            return retval;
        TopicImpl<T> impl=new TopicImpl<>(ch, name);
        retval=topics.putIfAbsent(name, impl);
        return retval != null? retval : impl;
    }

    public ITopic<T> deleteTopic(String name) {
        return topics.remove(new AsciiString(name));
    }

    public void receive(Message msg) {
        byte[] buf=msg.getRawBuffer();
        ByteArrayDataInputStream in=new ByteArrayDataInputStream(buf, msg.getOffset(), msg.getLength());
        try {
            AsciiString topic_name=Bits.readAsciiString(in);
            TopicImpl<T> topic=topics.get(topic_name);
            if(topic == null) {
                // log.error("failed dispatching to local topic %s: not found", topic_name);
                return;
            }
            T data=Util.objectFromStream(in);
            topic.notifyListeners(data);
        }
        catch(Throwable t) {
            log.error("failed dispatching received message", t);
        }
    }

    public void destroy() {
        Util.close(ch);
    }
}
