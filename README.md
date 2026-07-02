# design-criteria

This is the README file for the replication package of the desing criteria for hydrid big data architectures

This package is composed by the following elements:
- java-gendata: a java project to generate random data and insert into kafka
- java-kafka-streams: a java project to create kafka streams apps y process the data


!java-gendata
Project can be compiled with the command: mvn clean assembly:single

It has two main process to load cdr data and to load router data into kafka

To execute the load of cdrs it must be executed: java -cp data-generator-ds-0.0.1-dep.jar main.CdrsKafkaGenerator


