
package org.demo;

import org.jgroups.jmx.JmxConfigurator;
import org.jgroups.util.Bits;
import org.jgroups.util.Streamable;
import org.jgroups.util.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;


public class PerfTest implements MessageListener<PerfTest.PerfData> {
    protected JGroupsInstance jgroupsInstance;
    protected String          default_topic="perf";
    protected int             num_msgs=1000; // number of msgs to post to topic "perf"
    protected int             msg_size=1000; // msg size in bytes
    protected long            start; // timestamp start test
    protected long            time;  // time in ms for the test
    protected int             msgs_received;  // number of messages received in a start-stop cycle
    protected long            bytes_received; // number of bytes received in a start-stop cycle


    protected void start(String props, String name) throws Exception {
        jgroupsInstance = JGroups.newJGroupsInstance(props, "pubsub", name);
        JmxConfigurator.register(jgroupsInstance,Util.getMBeanServer(),"jgroups:type=pubsub-perf");
        ITopic topic=jgroupsInstance.getTopic(default_topic);
        topic.addMessageListener(this);
        loop();
    }

    public void onMessage(Message<PerfData> msg) {
        PerfData data=msg.getMessageObject();
        switch(data.type) {
            case PerfData.DATA:
                if(start == 0) {
                    start=System.currentTimeMillis();
                    msgs_received=0; bytes_received=0;
                }
                msgs_received++;
                bytes_received+=data.length();
                break;
            case PerfData.START:
                start=System.currentTimeMillis();
                msgs_received=0; bytes_received=0;
                break;
            case PerfData.STOP:
                time=System.currentTimeMillis() - start;

                // compute and print stats
                double msgs_per_sec=msgs_received / (time / 1000.0);
                double throughput=bytes_received / (time / 1000.0);
                System.out.printf("received %d messages (%s): %.2f msgs/sec, %s/sec\n",
                                  msgs_received, Util.printBytes(bytes_received), msgs_per_sec, Util.printBytes(throughput));
                msgs_received=0; bytes_received=0;
                start=0;
                break;
            default:
                System.err.printf("Type %d not known\n", data.type);
                break;
        }
    }

    protected void loop() {
        boolean looping=true;
        while(looping) {
            int key=keyPress("[1] Publish " + num_msgs + " msgs [2] Set num msgs [3] Set msg size [x] Exit");
            switch(key) {
                case '1':
                    perfTest();
                    break;
                case '2':
                    set("num_msgs");
                    break;
                case '3':
                    set("msg_size");
                    break;
                case 'x':
                    looping=false;
                    break;
            }
        }
        jgroupsInstance.destroy();
    }

    protected void perfTest() {
        System.out.printf("-- Posting %d messages\n",num_msgs);
        ITopic<PerfData> topic=jgroupsInstance.getTopic(default_topic);
        byte[] buffer=new byte[msg_size];
        final int print=num_msgs / 10;
        try {
            topic.publish(new PerfData(PerfData.START,null));

            for(int i=0; i < num_msgs; i++) {
                topic.publish(new PerfData(PerfData.DATA, buffer));
                if(i > 0 && i % print == 0)
                    System.out.printf("-- %d\n",i);
            }

            topic.publish(new PerfData(PerfData.STOP, null));
        }
        catch(Throwable t) {
            System.err.println("failed publishing to topic: " + t);
        }
    }

    protected void set(String name) {
        try {
            int val=Util.readIntFromStdin(name + ":");
            Field field=Util.getField(getClass(), name);
            field.setInt(this, val);
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
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



    public static int keyPress(String msg) {
        System.out.println(msg);

        try {
            int ret=System.in.read();
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
            System.out.println("PerfTest [-props <config>] [-name <name>]");
            return;
        }

        PerfTest sample = new PerfTest();
        sample.start(props, name);


    }

    protected static class PerfData implements Streamable {
        protected static final byte START = 1;
        protected static final byte STOP  = 2;
        protected static final byte DATA  = 3;

        protected byte   type;
        protected byte[] data;

        public PerfData() {
        }

        public PerfData(byte type,byte[] data) {
            this.type=type;
            this.data=data;
        }

        protected int length() {return data != null? data.length : 0;}

        public void writeTo(DataOutput out) throws Exception {
            out.writeByte(type);
            if(type == DATA) {
                Bits.writeInt(data.length, out);
                out.write(data);
            }
        }

        public void readFrom(DataInput in) throws Exception {
            type=in.readByte();
            if(type == DATA) {
                int len=Bits.readInt(in);
                data=new byte[len];
                in.readFully(data);
            }
        }
    }



}
