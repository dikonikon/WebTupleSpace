WebTupleSpace
=============

A web enabled, horizontally scalable tuplespace, picking up where PyTupleSpace left off: https://github.com/dikonikon/PyTupleSpace

WebTupleSpace aims to provide a place for dynamic and loosely coupled interaction and exchange of objects in a distributed system such as the internet.

It implements a tuple space where the interface is defined and implemented using web services rather than being
language dependent, but provides bindings on to Scala, Python and JavaScript as starting point. 

It uses mongodb for storage, both for its ability to scale horizontally and for its natural support for tuple-like data structures.

# Use Cases

## Loose coupling of virtual organisations

The Cloud is a lot about agility - fast set up and tear down of processes and organisations to meet needs in a fraction of the time and a fraction of the cost that would traditionally have been required.

One powerful use case revolves around the idea of creating 'virtual organisations' that support loosely coupled business processes. Each organisation interacts with the others using the tuplespace as a central place to do business.

Using the tuplespace the participants can both dynamically refine the common information model that they use to interact, and also use the ability of the tuplespace to notify subscribers about the presence of matching tuples to drive the loosely coupled exchange of information.

WebTupleSpace aims as a starting point to support this process in a resilient and horizontally scalable implementation. It will then go on to consider issues of identity, shared domains and authorization.

## Common model integration

One approach to complex systems integration is to integrate many systems around a common information model. One approach to common model integration is to place a layer of services over the systems that conform in their interfaces to a common model. Another way is to provide a more concrete realisation of the common model, and to integrate by mediating each systems interaction with that model.

A simple way to achieve this latter style of integration is to use a tuplespace as the platform for the realisation of that model.

# Current Status

Currently working on a test implementation in Scala which is basically a port of the previous Python-based test server.

Current work focuses on:

* richer representation of tuples including explicit annotation of the types of elements: current representation is as a list of byte arrays, which are assumed to incorporate type information. This has the benefit of being simple both to implement and to create language bindings for, but it has significant drawbacks. For example semantically equivalent tuples from different bindings are not equivalent, and it presents fewer opportunities for sharding (see next bullet)
* implementation using Mongodb thinking about approach to auto-sharding. The objective here is to achieve constant time for matching, scaling over very large numbers of tuples.

# Constant time matching

The following describes some of the challenges and proposed approach to solving them, when trying to achieve close to constant time tuple matching for large tuplespaces.

One of the challenges of tuple matching is that you do not in advance know which elements of a tuple will be matched against.

Order and type are significant in tuples: (1, "one") does not match ("one", 1) so it isn't possible to model tuples straightforwardly as mongo documents for example.

So, here in outline is the way WebTupleSpace aims to model tuples with the aim of achieving constant time with the length of tuples:

- model a tuple as a combination of a 'core' tuple which identifies it and a sequence of three-tuples comprising its type, its value and a consistent hash of the value, where the value is the object itself serialised into a byte array:

    `{
	    type: "integer",
	    value: 0xAB, 0x67, 0x03
	    hash: ...some consistent hash value...	
    }`

- these three-tuples are stored in mongodb collections with a separate collection for each element named "1" to "n" where n is the largest length of tuple you intend to support, and sharded on the 'hash' key.

- matching is done by deconstructing the matching template in this way and concurrently looking up in the appropriate collection by issuing multiple concurrent requests using akka, and collecting the results into a sequence of collections. If the same tuple is referenced in all the resulting sets of tuples then this is a match.

- akka should ensure that the requests to mongodb are non-blocking, and provides a convenient way of coordinating the requests and collecting the results.

- locating each tuple-element collection in a separate collection should yield constant lookup time for short to long tuples.

- provided the hash is in effect random, but consistent, the auto-sharding behaviour of MongoDB should provide a good distribution of storage and compute for lookup, and route each element lookup to the correct chunk in constant time
	

