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

## Stateless Servers, Persistent Queueing of Requests and Notifications

As the name implies WebTupleSpace is intended to be used over a WAN or internet, and to scale out to large numbers of tuples and clients. Clearly then it must tolerate failure and must scale horizontally. WebTupleSpace aims to keep the processes providing the request processing and matching behaviour stateless and to keep any processing of requests short-lived, persisting them where necessary and processing them in the background using the Akka actors framework.

In this way it is possible to configure a tuplespace that is reliable simply by starting clusters of WebTupleSpace Play server instances, and by utilising MongoDB's clustering features. It can be scaled horizontally simply by adding more WebTupleSpace and mongod processes.

## Reliability of Notifications

WebTupleSpace aims to operate in a stateless, connectionless mode, often in an unreliable networking environment such as a WAN or the internet. It is therefore possible for a request for notifications to complete the transaction used to retrieve them from MongoDB and subsequently to fail to successfully transmit them to the client.

WebTupleSpace therefore supports a strategy that allows the client to ensure that notifications are not lost, by employing a notification history that is updated in the same transaction context as updating the notification list. A transaction in MongoDB is any one read or update on a single collection.

The notification history is the list of notifications that TupleSpaceServer thinks it has sent to the client as a result of a call to readNotifications. Since it is possible for the read transaction to succeed and the notifications still to fail to get to the client, the following strategy is employed:
- when notifications are read and returned to the client they are, as part of that transaction, appended to the notification history.
- if the client experiences a system or communications failure during a read, it can read both the notifications again and the notification history. If this fails again the notification history may be re-read as many times as necessary - it is never deleted until the client calls clearNotificationHistory.
- once a call to readNotificationHistory succeeds the client can call clearNotificationHistory to remove it. In this way it is possible that notifications will be received more than once - for example if readNotifications succeeds and readNotificationHistory is subsequently called, but, subject to the transactional integrity of the MongoDB server, notifications should not be lost.


