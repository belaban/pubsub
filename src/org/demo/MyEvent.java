package org.demo;

import org.jgroups.conf.ClassConfigurator;
import org.jgroups.util.Bits;
import org.jgroups.util.Streamable;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Bela Ban
 * @since x.y
 */
public class MyEvent implements Streamable {
    protected static final AtomicInteger cnt=new AtomicInteger(1);
    protected static final short ID=5000;

    static {
        ClassConfigurator.add((short)5000, MyEvent.class);
    }

    protected int  id;
    protected long timestamp;

    public MyEvent() {
    }

    public MyEvent(long timestamp) {
        this.timestamp=timestamp;
        id=cnt.incrementAndGet();
    }


    public boolean isHeavyweight() {
        return id > 3000;
    }

    public void writeTo(DataOutput out) throws Exception {
        Bits.writeInt(id, out);
        out.writeLong(timestamp);
    }

    public void readFrom(DataInput in) throws Exception {
        id=Bits.readInt(in);
        timestamp=in.readLong();
    }

    public String toString() {
        return "Event #" + id + ", created " + new Date(timestamp);
    }


}
