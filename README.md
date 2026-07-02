# Design Criteria for Hybrid Big Data systems

This is the README file for the replication package of the design criteria for hydrid big data architectures

This package is composed by the following elements:
- java-gendata: a java project to generate random data and insert into kafka
- java-kafka-streams: a java project to create kafka streams apps y process the data


## java-gendata
Project can be compiled with the command: mvn clean assembly:single

It has two main process to load cdr data and to load router data into kafka

To execute the load of cdrs it must be executed: `java -cp data-generator-ds-0.0.1-dep.jar main.CdrsKafkaGenerator`
Parameters for this are:
```
usage: KafkaGenerator
 -b,--broker <arg>       List of kafka bootstrap servers (default:
                         127.0.0.1:9092)
 -d,--date <arg>         Date for the input data, format [yyyy-mm-dd]
 -h,--help               Print this help
 -n,--numrecords <arg>   Generate n records per second (default 500)
 -t,--topic <arg>        Topic name (default: topic_in_stream)
```

To execute the load of cdrs it must be executed: `java -cp data-generator-ds-0.0.1-dep.jar main.SOBKafkaGenerator`
```
usage: KafkaGenerator
 -a,--as_json <arg>       Output as JSON or test (default: false)
 -b,--broker <arg>        List of kafka bootstrap servers (default:
                          127.0.0.1:9092)
 -h,--help                Print this help
 -i,--interval <arg>      Generate data every i seconds (default: 15)
 -l,--lost <arg>          % of lost records (default: 0.0)
 -n,--numsetofbox <arg>   Generate data for n set of boxes (default 5)
 -p,--pretty              Pretty format for JSON (default: false)
 -r,--reset               Include reset flag in JSON (default: false)
 -t,--topic <arg>         Topic name (default: topic_in_stream)
 -x,--numthreads <arg>    Number of threads (default: 1)
 -z,--nullkeys <arg>      % of null keys (default: 0.0)
```
For a quick start only parameters -b <broker-list> and -t <topic> should be enough

