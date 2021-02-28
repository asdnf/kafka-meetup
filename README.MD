# Definitions

* topic
* partition
* replication factor
* consumer group
* kafka stream
* ksql

Download kafka from 
https://apache-mirror.rbc.ru/pub/apache/kafka/2.7.0/kafka_2.13-2.7.0.tgz

Kafka works on top of zookeeper. ZK must be started first, 
and then run Kafka on top of it.
Kafka is using Apache Zookeeper to manage its cluster metadata, 
so we need a running Zookeeper cluster.

Start zk first
```shell
$ bin/zookeeper-server-start.sh config/zookeeper.properties
```
This will start a Zookeeper service listening on port 2181. After this, 
we can use another script to run the Kafka server:
```shell
$ bin/kafka-server-start.sh config/server.properties
```

After a while, a Kafka broker will start. Let's add a few topics to this simple cluster:
```shell
$ bin/kafka-topics.sh --create --topic users.registrations --replication-factor 1 \
  --partitions 2  --zookeeper localhost:2181
$ bin/kafka-topics.sh --create --topic users.verfications --replication-factor 1 \
  --partitions 2  --zookeeper localhost:2181
```

To list all Kafka topics in a cluster, we can use the bin/kafka-topics.sh 
shell script bundled in the downloaded Kafka distribution. All we have 
to do is to pass the –list option along with the information about the cluster. 
For instance, we can pass the Zookeeper service address:
```shell
$ bin/kafka-topics.sh --list --zookeeper localhost:2181
users.registrations
users.verfications
```

**In order to talk to the Kafka cluster, we need 
to pass the Zookeeper service URL using the ``-–zookeeper`` option.**



Once we've found a list of topics, we can take a peek at the details 
of one specific topic. To do that, we can use the ``–describe –topic <topic name>`` 
combination of options:
```shell
$ bin/kafka-topics.sh --bootstrap-server=localhost:9092 --describe --topic topic

Topic: users.registrations      PartitionCount: 2       ReplicationFactor: 1    Configs: segment.bytes=1073741824
        Topic: users.registrations      Partition: 0    Leader: 0       Replicas: 0     Isr: 0
        Topic: users.registrations      Partition: 1    Leader: 0       Replicas: 0     Isr: 0
```

Watch for messages by partition:
```shell
$ bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic topic --partition 5  --offset
```

See all topic stats:
```shell
$ bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group testGroup --describe
```


https://www.youtube.com/watch?v=6UDwdUAqCOE
1:01:05