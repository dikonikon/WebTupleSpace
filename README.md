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

Current work focused on creating a version using MongoDB for persistence of tuples. The aim is to use MongoDB's autosharding to arrive at constant time matching using consistent hashing of key fields of the tuples to yield both index fields in the tuple elements and a sharding field in the tuple itself, comprising a hash of each of the element hashes. 

This approach utilizes a MongoDB document representation of the tuples utilizing embedded documents:

{
  _id: ObjectId
  e1: { type: <String rep of type>, value: <base64 or other encoding of value>, hash: <RSA256 hash of type concat value> },
  e2: ...
}
	

