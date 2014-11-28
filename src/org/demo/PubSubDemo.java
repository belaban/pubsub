
package org.demo;

import org.jgroups.util.Util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;



public class PubSubDemo implements MessageListener<MyEvent> {


    public static void main( String[] args ) throws Exception {
        String props="config.xml";
        for(int i=0; i < args.length; i++) {
            if(args[i].equals("-props")) {
                props=args[++i];
                continue;
            }
            System.out.println("PubSub [-props <config>]");
            return;
        }

        PubSubDemo sample = new PubSubDemo();
        JGroupsInstance jgroupsInstance = JGroups.newJGroupsInstance(props);
        ITopic topic = jgroupsInstance.getTopic("default");
        topic.addMessageListener( sample );

        for(int i=0; i < 10; i++)
            topic.publish(new MyEvent(System.currentTimeMillis()));

        Util.sleep(1000);
        jgroupsInstance.destroy();
    }

    public void onMessage( Message<MyEvent> message ) {
        final MyEvent myEvent = message.getMessageObject();
        System.out.println( "Message received = " + myEvent.toString() );
        if ( myEvent.isHeavyweight() ) {
            messageExecutor.execute( new Runnable() {
                public void run() {
                    doHeavyweightStuff( myEvent );
                }
            } );
        }
    }

    protected void doHeavyweightStuff(MyEvent evt) {

    }

    // ...

    private final Executor messageExecutor = Executors.newSingleThreadExecutor();

}
