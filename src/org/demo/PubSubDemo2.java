
package org.demo;

import org.jgroups.jmx.JmxConfigurator;
import org.jgroups.util.Util;

import java.io.IOException;


public class PubSubDemo2 implements MessageListener<String> {
    protected JGroupsInstance jgroupsInstance;
    protected String          default_topic="default";

    protected void start(String props, String name) throws Exception {
        jgroupsInstance = JGroups.newJGroupsInstance(props, "pubsub", name);
        JmxConfigurator.register(jgroupsInstance,Util.getMBeanServer(),"jgroups:type=pubsub");
        ITopic topic=jgroupsInstance.getTopic(default_topic);
        topic.addMessageListener(this);
        loop();
    }

    public void onMessage(Message<String> msg) {
        String message=msg.getMessageObject();
        System.out.println("received msg: " + message);
    }

    protected void loop() {
        boolean looping=true;
        while(looping) {
            int key=keyPress("[1] Create topic [2] Delete topic [3] Shop topics [4] Post to topic " +
                               (default_topic != null? " (default=" + default_topic + ")" : "") + " [x] Exit", false);
            switch(key) {
                case '1':
                    createTopic();
                    break;
                case '2':
                    destroyTopic();
                    break;
                case '3':
                    listTopics();
                    break;
                case '4':
                    postToTopic();
                    break;
                case 'x':
                    looping=false;
                    break;
                default:
                    postToDefaultTopic((char)key);
                    break;
            }
        }
        jgroupsInstance.destroy();
    }

    protected void createTopic() {
        try {
            String name=Util.readStringFromStdin("name: ");
            ITopic topic=jgroupsInstance.getTopic(name);
            topic.addMessageListener(this);
            default_topic=name;
        }
        catch(Throwable t) {
            System.err.println("failed creating topic: " + t);
        }
    }

    protected void destroyTopic() {
        try {
            String name=Util.readStringFromStdin("name: ");
            jgroupsInstance.deleteTopic(name);
            if(default_topic != null && default_topic.equals(name))
                default_topic=null;
        }
        catch(Throwable t) {
            System.err.println("failed destroying topic: " + t);
        }
    }

    protected void listTopics() {
        String topics=jgroupsInstance.getTopics();
        System.out.println("topics = " + topics);
    }

    protected void postToTopic() {
        try {
            String name=null;
            if(default_topic == null) {
                name=Util.readStringFromStdin("topic name: ");
                default_topic=name;
            }
            ITopic<String> topic=jgroupsInstance.getTopic(default_topic);
            String message=Util.readStringFromStdin("message: ");
            topic.publish(message);
        }
        catch(Throwable t) {
            System.err.println("failed posting to topic: " + t);
        }
    }

    protected void postToDefaultTopic(char first) {
        try {
            String message=first + Util.readLine(System.in);
            System.in.skip(System.in.available());
            ITopic<String> topic=jgroupsInstance.getTopic(default_topic);
            topic.publish(message);
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
    }

    public static int keyPress(String msg, boolean skip) {
        System.out.println(msg);

        try {
            int ret=System.in.read();
            if(skip)
                System.in.skip(System.in.available());
            return ret;
        }
        catch(IOException e) {
            return 0;
        }
    }

    public static void main( String[] args ) throws Exception {
        String props="config.xml";
        String name=null;
        for(int i=0; i < args.length; i++) {
            if(args[i].equals("-props")) {
                props=args[++i];
                continue;
            }
            if(args[i].equals("-name")) {
                name=args[++i];
                continue;
            }
            System.out.println("PubSub [-props <config>] [-name <name>]");
            return;
        }

        PubSubDemo2 sample = new PubSubDemo2();
        sample.start(props, name);


    }



}
