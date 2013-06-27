# Write a tuple into the TupleSpace

## Request:

### Headers:

PUT
"/webtuplespace/write
Content-Type", "text/xml

### Body:

<Tuple><Element><Type>String</Type> <Value>aldjsflajlajs</Value></Element></Tuple>

## Response

### Headers

OK

### Body

<Tuple><Id>lakjsd;fkaljaljsdljf</Id><Element><Type>String</Type> <Value>aldjsflajlajs</Value></Element></Tuple>

# Read Tuples from the TupleSpace that match a pattern

### Headers

PUT
"/webtuplespace/read

### Body


