# M'interessa Kafka backend

Table of Contents
=================

  * [Disclaimer](#disclaimer)
  * [Objective of the project](#objective-of-the-project)
  * [Requirements](#requirements)
    * [If you want to mess with the producer](#if-you-want-to-mess-with-the-producer)
  * [Starting the container(s)](#starting-the-containers)
    * [Starting the producer on its own](#starting-the-producer-on-its-own)
  * [A sample consumer to test everything works](#a-sample-consumer-to-test-everything-works)
  * [How does this thing work exactly?](#how-does-this-thing-work-exactly)

## Disclaimer

This repository stores one component of broader project code-named
"M'interessa" that is expected to evaluate the knowledge acquired in
the postgraduate of
[Data Science at the Universitat de Barcelona](http://www.ub.edu/datascience/postgraduate/).

This is an ongoing process (at least until version 1.0.0, which is
expected to be the one to be presented as a final result), will be
used as a Proof of Concept and by no means is a production-ready
backend setup. Since the main focus of the project is on machine
learning, this backend stands just as a small data pipeline in a much
broader problem.

The links to the repositories of the remaining components will be
provided on further releases. Available right now:

* [M'interessa Web Frontend](https://github.com/malberich/pgds-minteressa-webapp/)

The purpose of this first release is to publicly share the component
with other members of the team, and to be linkable from the
website/blog. After we get a version tagged as 1.0.0, we'll be pleased
to accept and review Pull Requests from other community members
outside of the group.

## Objective of the project

The project is intended to use machine learning models, natural
language processing and scrapping technologies (along with other big
data technologies) to assist the user on choosing and receiving the
most relevant tweets that are being distributed over the Twitter
public streaming api, given a basic set of tweets provided by the user
itself (whether from its timeline or from a twitter search) and from a
progressive tweet selection.

The models are expected to learn progressively and specifically for
each user's need.

You will be able to read more about the project soon in the companion
website of the project, where we explain the overall details and
collect blog posts from the team members.

# Requirements

To run this producer you only need [Docker](https://www.docker.com/)
with [docker-compose](https://docs.docker.com/compose/) (which is part
of Docker). Since the docker-compose file is in version 2.0, you'll
need a version of docker compose at least 1.6. Recommended to update
to 1.8.0-rc1

You will also need a developer account in Twitter with an app setup
([here](https://apps.twitter.com)) where you have to get keys and
access tokens. You can set the callback URL to 127.0.0.1. Once you
have them, edit your .?shrc file to add these exports:

```bash
export TWITTERCONSUMERKEY={your consumer key}
export TWITTERCONSUMERSECRET={your consumer secret}
export TWITTERACCESSTOKEN={your access token}
export TWITTERACCESSTOKENSECRET={your access token secret}
```

The docker-compose file is prepared to interpolate these values into
the producer environment.

You also need to add to your `etc/hosts` the following line:

```
127.0.0.1	kafka
```

## If you want to mess with the producer

If you want to play around with starting/stopping the producer on your
own, you will need `Scala 2.11.8` with `sbt` 0.13. SBT should handle
fetching all the required libraries on its own... If it fails, Google
the error message, cross your fingers and try to apply the
suggestion. Usually it involves removing the cache (delete everything
in `~/.ivy2`) and trying again.

# Starting the container(s)

If you want to run the whole stack (Zookeper, Kafka with one broker and a producer):

```bash
docker-compose -f docker-compose-single-broker.yml up --build
```

although `--build` is optional (only really needed if you change
something from the setup).

This will start Zookeper and Kafka, link them together and bring a
twitter producer up. This producer will be also linked to kafka, so
everything will work as expected. To connect from your host machine to
kafka (for debug purposes, testing a consumer/producer, etc) you need
to connect to kafka:9092 (hence why we modify the hosts file: to make
sure the advertised name from kafka and the name your
producer/consumer get in your host machine match up).

Alternatively, you can start only zookeper and kafka with

```bash
docker-compose -f docker-compose-only-broker.yml up
```

This way you can easily start and stop the producer as you see fit, or
just try writing consumers (keep in mind that to test a consumer you
need a producer: in case of desperate need download Kafka 0.10.0 and
use the command line producer)

## Starting the producer on its own

Remember to set all the exports with the twitter app tokens from above, and also 

```
export KAFKA=127.0.0.1:9092
```

Open the main project folder (where the dockerfiles reside) and start sbt:

```
sbt
```

After quite a long while and maybe fighting some error message, sbt
will finish fetching all required packages to compile the producer and
you'll be in the `sbt` REPL, where you can

```
run
```

which will start the producer. If the Kafka and Zookeper containers are up and correctly setup, a constant stream of tweets will fly into it.

# A sample consumer to test everything works

Using your preferred (`virtualenv` or just `sudo`) install the python kafka API handler (I have only tried with `pip2.7` here, use `python 3.3+` at your own risk)

```
pip install kafka-python
```

Fire up a python interpreter (a notebook, if you installed
`kafka-python` through a notebook handler, just `python` or `bpython`
or `ipython` from the command line otherwise) and just execute

```python
from kafka import KafkaConsumer
consumer = KafkaConsumer('tweets', bootstrap_servers='kafka:9092')
for msg in consumer:
	print msg
```

and this serves as a basic consumer in Python, without any error
checking.

# Deploying on AWS

* Create an instance on EC2 with a standard AMI from Amazon with your access keys
* Modify the hosts file with the correct public ip of the machine (or VPC if not doing it from your machine)
* Be sure to [set up bash for automatic keys](http://docs.ansible.com/ansible/intro_getting_started.html#your-first-commands)
* Execute the following. This will start zookeeper, kafka and the producer
```bash
ansible-playbook -i hosts play.yml -u ec2-user --sudo
```

To connect consumers to this "remote" kafka instance, modify `etc/hosts`'s entry for kafka to point to this IP.


# How does this thing work exactly?

* Zookeper keeps track of who has read from where
* Kafka acts as a kind of queue (through a publish-subscribe mechanism)
* When you connect a consumer to a kafka broker, the broker tells the consumer its own host or the host of the kafka cluster leader. Hence why we need to make sure kafka is in our hosts file
* The consumer subscribes to the `tweets` "queue" (topic, usually, in kafka lingo) and in python it acts as a generator we can query with just a for loop.
* The producer just uses twitter4j to connect to the sample firehose and dump the tweets to kafka. No error checking at all: if something fails (connection stalls, for instance) it just shutdowns. Something to work on for the final version!
