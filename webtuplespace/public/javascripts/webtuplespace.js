// namespace for the webtuplespace client proxy

webtuplespace = {};

webtuplespace.getDefault = function(arg, defval) {
    return (typeof arg === 'undefined' ? defval: arg)
};

////////////////////////////////////////////////////////////////////////////////////////
// WebTuple - an object used to encapsulate the conversion of a tuple
// to/from JS form (an array of arrays representing the type and value of each eleement)
// and the XML form understood by the space
////////////////////////////////////////////////////////////////////////////////////////

webtuplespace.WebTuple = function() {
    this.internal = []
    for (var i = 0; i < arguments.length; i++) {
        this.internal.push(arguments[i]);
    }
}

webtuplespace.WebTuple.prototype.toXML = function() {
    var xmlRep = "<Tuple>";
    for (var i = 0; i < this.internal.length; i++) {
        xmlRep += "<Element>";
        xmlRep += "<Type>";
        xmlRep += this.internal[i][0];
        xmlRep += "</Type>";
        xmlRep += "<Value>";
        xmlRep += this.internal[i][1];
        xmlRep += "</Value>";
        xmlRep += "</Element>";
    }
    xmlRep += "</Tuple>";
    return xmlRep;
};

webtuplespace.WebTuple.prototype.toString = function() {
    var r = "[ ";
    for (var i = 0; i < this.internal.length; i++) {
        r += "[";
        var e = this.internal[i];
        r += e[0];
        r += ",";
        r += e[1];
        r += "] ";
    }
    r += "]";
    return r;
}

webtuplespace.WebTuple.prototype.push = function(element) {
    this.internal.push(element);
};

webtuplespace.WebTuple.prototype.getElement = function(index) {
    return this.internal[index][1];
};

webtuplespace.WebTuple.prototype.getType = function(index) {
    return this.internal[index][0];
};

webtuplespace.WebTuple.prototype.getTypeAndElement = function(index) {
    return this.internal[index];
};

webtuplespace.WebTuple.prototype.fromXML = function(xmlDoc) {
    var idElements = xmlDoc.getElementsByTagName("Id");
    if (idElements.length > 0) {
        var idElement = idElements[0];
        this.tupleId = idElement.textContent;
    }
    var elements = xmlDoc.getElementsByTagName("Element");
    for (var i = 0; i < elements.length; i++) {
        var e = elements[i];
        var typeElements = e.getElementsByTagName("Type");
        var typeElement = typeElements[0]
        var valueElements = e.getElementsByTagName("Value");
        var valueElement = valueElements[0]
        var type = typeElement.textContent;
        var value = valueElement.textContent;
        var tupleElement = [];
        console.log("got type: ", type);
        console.log("got value: ", value);
        tupleElement.push(type);
        tupleElement.push(value);
        this.internal.push(tupleElement);
    };
};

webtuplespace.WebTuple.prototype.getId = function() {
    if (this.tupleId) {
        return this.tupleId;
    } else {
        return "";
    }
}

//////////////////////////////////////////////////////
// Client - an object used as a proxy to WebTupleSpace
// which handles all interaction with it
//////////////////////////////////////////////////////

function singleTupleSuccessResponseHandlerDecorator(responseHandler) {
    return function(xmlDoc, textStatus, jqXHR) {
        var tuple = new webtuplespace.WebTuple();
        tuple.fromXML(xmlDoc)
        var idElements = xmlDoc.getElementsByTagName("Id");
        var idElement = idElements[0];
        var id = idElement.textContent;
        responseHandler(tuple, id, textStatus);
    }
};

function multipleTupleSuccessResponseHandlerDecorator(responseHandler) {
    return function(xmlDoc, textStatus, jqXHR) {
        var tuples = [];
        var ids = [];
        var tupleElements = xmlDoc.getElementsByTagName("Tuple");
        for (var i = 0; i < tupleElements.length; i++) {
            var tuple = new webtuplespace.WebTuple();
            tuple.fromXML(tupleElements[i]);
            tuples.push(tuple);
            var idElements = tupleElements[i].getElementsByTagName("Id");
            var idElement = idElements[0];
            var id = idElement.textContent;
            ids.push(id);
        };
        responseHandler(tuples, ids, textStatus);
    }
}

function noopFailureResponseHandlerDecorator(failureHandler) {
    return function(jqXHR, textStatus, errorThrown) {
        failureHandler(jqXHR, textStatus, errorThrown);
    };
};

function noopSuccessResponseHandlerDecorator(successHandler) {
    return function(xmlDoc, textStatus, jqXHR) {
        successHandler(xmlDoc, textStatus, jqXHR)
    }
}

function sendRequest(xml, path, verb, successHandler, failureHandler) {
    var request = $.ajax({
        url: "/webtuplespace/" + path,
        type: verb,
        data: xml,
        dataType: "xml",
        accepts: "text/xml",
        contentType: "text/xml",
        processData: false,
        success: successHandler,
        error: failureHandler
    });
    return request;
}

webtuplespace.read = function(pattern, successHandler, failureHandler) {
    var xml = pattern.toXML();
    var request = sendRequest(xml, "read", 'POST', multipleTupleSuccessResponseHandlerDecorator(successHandler),
        noopFailureResponseHandlerDecorator(failureHandler));
};

webtuplespace.write = function(tuple, successHandler, failureHandler) {
    var xml = tuple.toXML();
    var request = sendRequest(xml, "write", 'POST', singleTupleSuccessResponseHandlerDecorator(successHandler),
        noopFailureResponseHandlerDecorator(failureHandler));
};

webtuplespace.Client = function() {
    this.sessions = [];
};

webtuplespace.Client.prototype.write = webtuplespace.write;

webtuplespace.Client.prototype.read = webtuplespace.read;

///////////////////////////////////////////////////////////////
// webtuplespace.Session: uses WebSockets to maintain an open
// connection with the server to receive notifications.
// Terminated by sending a message "END_SESSION".
///////////////////////////////////////////////////////////////


webtuplespace.Session = function(client, pattern, notificationCallback) {
    this.client = client;
    this.pattern = pattern;
    this.history = [];
    this.notificationCallback = notificationCallback;
}

webtuplespace.Session.prototype.start = function() {
    this.runId = setInterval(pollTupleSpaceFor(this), 2000);    
}

webtuplespace.Session.prototype.stop = function() {
    clearInterval(this.runId);
}





