# Write a tuple into the TupleSpace

## Request:

### Headers:

PUT
"/webtuplespace/write
Content-Type", "text/xml

### Body:

```
<Tuple><Element><Type>String</Type> <Value>aldjsflajlajs</Value></Element></Tuple>
```

## Response

### Headers

OK

### Body

```
<Tuple><Id>lakjsd;fkaljaljsdljf</Id><Element><Type>String</Type> <Value>aldjsflajlajs</Value></Element></Tuple>
```

# Read Tuples from the TupleSpace that match a pattern

### Headers

PUT
"/webtuplespace/read

### Body

# Notifications

After opening a session, send the following messages:

### To add a pattern for matching:

Send:

```
<Add><Tuple>...</Tuple></Add>
```

Response:

Comprises an initial match list based on the current state of the tuplespace. Thereafter whenever a tuple is added
that matches a match list will be sent.

```

```

### To remove a pattern from matching:

```
<Remove><Tuple>...</Tuple></Remove>
```

### To end the session:

```
<EndSession></EndSession>
```

