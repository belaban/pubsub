pubsub
======

Sample project showing how to build a pub-sub system in JGroups.
Use ant to download the required dependencies and compile the code into the `./classes` dir. 

There are 3 demos:

* PubSubDemo: simple demo publishing 10 posts to a topic. Run with `bin/ps-demo.sh` (`-h` shows options)
* PubSubDemo2: demo allowing for interactive topic creation, deletion and posting to topics. Run with `bin/ps-demo2.sh`
* PerfTest: interactive perf test, publishing X posts of Y size to a topic and measuring perf. Run with `bin/ps-perf.sh` 

Except for `PubSubDemo`, multiple instances can be started to see how posts are applied to topics in different processes.

There are a number of JGroups configurations that the demos can be run with:

* `config.xml`: a typical JGroups configuration providing reliable, ordered and lossless transmission
* `simple.xml`: a JGroups configuration with only the transport configured. This is similar to using a
   `MulticastSocket` directly and provides no reliability. However, the same demo code can be run with this.
   
To pass a certain configuration to a demo, use the `-props` option, e.g. `bin/ps-perf.sh -props config.xml -name A`.
