WebTupleSpace
=============

A web enabled, reliable and horizontally scalable tuplespace, built using Scala, Play, Akka and MongoDB.

WebTupleSpace aims to provide a place for dynamic and loosely coupled interaction and exchange of objects in a distributed system such as the internet.

It implements a tuple space where the interface is defined and implemented using web services rather than being
language dependent, but provides bindings on to Scala, Python and JavaScript as a starting point. 

It uses MongoDB for storage, both for its ability to scale horizontally and for its natural support for tuple-like data structures.

# Use Cases

## Loose coupling of virtual organisations

The Cloud is a lot about agility - fast set up and tear down of processes and organisations to meet needs in a fraction of the time and a fraction of the cost that would traditionally have been required.

One powerful use case revolves around the idea of creating 'virtual organisations' that support loosely coupled business processes. Each organisation interacts with the others using the tuplespace as a central place to do business.

Using the tuplespace the participants can both dynamically refine the common information model that they use to interact, and also use the ability of the tuplespace to notify subscribers about the presence of matching tuples to drive the loosely coupled exchange of information and to spontaneously create automated processes.

WebTupleSpace aims to begin with to support this process in a resilient and horizontally scalable implementation. It will then go on to consider issues of identity, shared domains and authorization.

## Common model integration

One approach to complex systems integration is to integrate many systems around a common information model. One approach to common model integration is to place a layer of services over the systems that conform in their interfaces to a common model. Another way is to provide a more concrete realisation of the common model, and to integrate by mediating each systems interaction with that model.

A simple way to achieve this latter style of integration is to use a tuplespace as the platform for the realisation of that model.

# Notes on WebTupleSpace' Approach

## Objectives

The primary objectives of WebTupleSpace are:

1. To scale out to very large number of tuples maintaining constant time for search and retrieval
1. To scale out to very large numbers of clients
1. To offer consistent and well known behaviour for notifications of matching tuples

With regard to the third objective it is not practically feasible to guarantee that notifications and persisting of tuples of to the space are consistent - that is to guarantee that the client receives a notification when a tuple matching a subscription is added to the space.

WebTupleSpace provides two policies:

1. Notification first - the client may receive notification of tuples that are not in the tuplespace
2. Persist first - tuples may get added to the space with no notification to the client

## Matching using Hashes for Sharding, Indexing and Querying

I've worked with tuplespaces such as JavaSpace in the past, and attempted to develop a scalable tuplespace both in Java, and in .net on Windows Azure, utilizing designs that should provide good scale-out performance such as Rings. It's hard work - there are some challenges with tuplespaces. Tuples themselves have a fairly arbitrary structure, and requests to match on tuples can be on any element within a tuple. When you add a tuple to a space you potentially change the way it should best be indexed. When you match on a tuple you are in effect querying on any element of the tuple.

After previous attempts at it MongoDB seemed like a pretty good platform to solve this problem. It supports documents of varying structure within a collection, so tuples with widely varying numbers and types of elements are easy to store, provided you get the representation of them right.

MongoDB's autosharding is attractive as a means of horizontal scale out, provided you can arrive at a shard key that ensures a sufficiently random and even distribution of hashes. By so doing we naturally parallelize queries, adding more server hosts as required.

WebTupleSpace uses consistent hashing derived in the following way: each element is represented itself as a tuple comprising a string symbolic of its type, a string encoding of its value, and a hash derived by concatenating the type and value fields and applying an RSA256 hashing algorithm to it. The individual element hashes are then concatenated in order and a final RSA256 hash of this byte array is calculated. This becomes the shard key of the tuple. The ordering of the tuple elements is ensured simply by incorporating their index into their names: e1, e2 etc.

The hash at the tuple level is used for sharding, and the hashes at the element level are used to query and index on.

This should result in a fairly random distribution of tuples across a the range of values created by the output of hashing algorithm. Simply by adding additional mongod instances to a cluster the level of parallelism in the search for tuples can be increased.

Each of the individual elements' hash element is indexed by configuring the anticipated maximum number of elements anticipated at startup, which should yield pretty good query performance and of course you only have to query on the elements that are present in the matching pattern.
```
{
  _id: ObjectId
  shardKey: <hash of hashes>
  e1: { type: <String rep of type>, value: <base64 or other encoding of value>, hash: <RSA256 hash of type concat value> },
  e2: ...
}
```

## Scale out and Resilience

As the name implies WebTupleSpace is intended to be used over a WAN or internet, and to scale out to large numbers of tuples and clients. Clearly then it must tolerate failure and must scale horizontally. All primitive operations on the tuplespace are stateless using simple http requests.

A highly resilient and scalable tuplespace for this primitive read, write and take operations can be created simply by configuring a stateless cluster of Play servers and a standard Mongodb cluster configuration. 

Scale out with number of tuples is achieved by adding mongod instances to the Mongodb cluster, and Mongodb should automatically adjust sharding to optimally distribute tuples to distribute and parallelize search and retrieval.

Scale out with number of clients is achieved by simply starting additional instances of Play and adding them to the cluster using a load balancing proxy such as Nginx.

## Notifications

WebTupleSpace aims to avoid querying Mongodb if possible. Push notifications of matches to a pattern are achieved using websockets and [Akka](http://www.akka.io). When a session is started Mongodb is queried once for any existing matches, thereafter as new tuples are added a message is sent to an Akka Actor representing each session and a notification forwarded to the client when a match occurs with the sessions patterns.

This architecture looks something like this:

![alt text][architecture]

[architecture]: https://github.com/dikonikon/WebTupleSpace/blob/master/notes/architecture_v1.jpg "WebTupleSpace Architecture"



