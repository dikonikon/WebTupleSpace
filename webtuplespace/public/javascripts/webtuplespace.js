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

webtuplespace.Client = function() {
    this.subscriptions = [];
    this.notificationsListeners = [];
};

webtuplespace.Client.prototype.setSessionId = function(sid) {
    this.sessionId = sid;
};

webtuplespace.Client.prototype.getSessionId = function() {
    return this.sessionId;
};

webtuplespace.Client.prototype.addNotificationListener = function(listener) {
    this.notificationsListeners.push(listener);
};

webtuplespace.Client.prototype.getNotificationListeners = function() {
    return this.notificationsListeners;
}

webtuplespace.Client.prototype.write = function(tuple, successHandler, failureHandler) {
    var xml = tuple.toXML();
    var request = sendRequest(xml, "write", 'POST', singleTupleSuccessResponseHandlerDecorator(successHandler),
        noopFailureResponseHandlerDecorator(failureHandler));
};


webtuplespace.Client.prototype.read = function(pattern, successHandler, failureHandler) {
    var xml = pattern.toXML();
    var request = sendRequest(xml, "read", 'POST', multipleTupleSuccessResponseHandlerDecorator(successHandler),
        noopFailureResponseHandlerDecorator(failureHandler));
};

webtuplespace.Client.prototype.startSession = function(successHandler, failureHandler) {
    function successDecorator(successHandler, client) {
        return function(xmlDoc, textStatus, jqXHR) {
            console.log("handling start session success");
            var sessionElements = xmlDoc.getElementsByTagName("SessionId");
            var sessionElement = sessionElements[0];
            var sid = sessionElement.textContent;
            client.setSessionId(sid);
            successHandler(sid);
        }
    }

    function failureDecorator(failureHandler) {
        return function(jqXHR, textStatus, error) {
            console.log("handling start session failure");
            this.textStatus = textStatus;
            failureHandler(jqXHR, textStatus, error);
        }
    }

    var request = sendRequest(null, "start", 'GET', successDecorator(successHandler, this),
        failureDecorator(failureHandler));
};


webtuplespace.Client.prototype.addSubscription = function(pattern, successHandler, failureHandler) {
    function successDecorator(successHandler, client) {
        return function(xmlDoc, textStatus, jqXHR) {
            console.log("handling add subscription success");
            client.subscriptions.push(pattern);
            successHandler(pattern);
        }
    }

    function failureDecorator(failureHandler) {
        return function(jqXHR, textStatus, error) {
            console.log("handling add subscription failure");
            this.textStatus = textStatus;
            failureHandler(jqXHR, textStatus, error);
        }
    }

    var path = "subscribe/session/" + this.getSessionId();
    console.log("sending request to subscribe to path: " + path);
    var request = sendRequest(pattern.toXML(), path, 'POST', successDecorator(successHandler, this),
        failureDecorator(failureHandler));
}

webtuplespace.Client.prototype.getNotifications = function(successHandler, failureHandler) {
    function successDecorator(successHandler) {
        return function(xmlDoc, textStatus, jqXHR) {
            console.log("handling get notifications success");
            var subscriptions = [];
            var subscriptionElements = xmlDoc.getElementsByTagName("Subscription");
            for (var i = 0; i < subscriptionElements.length; i++) {
                var patternElements = subscriptionElements[i].getElementsByTagName("Pattern");
                var patternElement = patternElements[0];
                var patternTupleElements = patternElement.getElementsByTagName("Tuple");
                var pattern = new webtuplespace.WebTuple();
                pattern.fromXML(patternTupleElements[0]);
                var subscription = {};
                subscription.pattern = pattern;
                subscription.tuples = [];
                var notificationsElements = subscriptionElements[i].getElementsByTagName("Notifications");
                var notificationsElement = notificationsElements[0];
                var tupleElements = notificationsElement.getElementsByTagName("Tuple");
                for (var j = 0; j < tupleElements.length; j++) {
                    var tupleElement = tupleElements[j];
                    var tuple = new webtuplespace.WebTuple();
                    tuple.fromXML(tupleElement);
                    subscription.tuples.push(tuple);
                }
                subscriptions.push(subscription);
            }
            successHandler(subscriptions);
        }
    }

    function failureDecorator(failureHandler) {
        return function(jqXHR, textStatus, error) {
            console.log("handling get notifications failure");
            this.textStatus = textStatus;
            failureHandler(jqXHR, textStatus, error);
        }
    }

    var path = "notifications/session/" + this.getSessionId();
    console.log("sending request for notifications to path: " + path);
    var request = sendRequest(null, path, 'GET', successDecorator(successHandler),
        failureDecorator(failureHandler));
}



