// namespace for the webtuplespace client proxy

webtuplespace = {};

webtuplespace.getDefault = function(arg, defval) {
    return (typeof arg === 'undefined' ? defval: arg)
};

webtuplespace.WebTuple = function() {
    this.internal = []
    for (var i = 0; i < arguments.length; i++) {
        this.internal.push(arguments[i]);
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
        console.log("creating stringified WebTuple...")
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
        console.log("stringified WebTuple is: ", r);
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
}


webtuplespace.Client = function() {
    // responseHandler will be called with the status code as a string, followed by a WebTuple
    webtuplespace.Client.prototype.write = function(tuple, successHandler, failureHandler) {
        var xml = tuple.toXML()
        var request = $.ajax({
            url: "/webtuplespace/write",
            type: 'POST',
            data: xml,
            dataType: "xml",
            accepts: "text/xml",
            contentType: "text/xml",
            processData: false,
            success: writeSuccess(successHandler),
            error: writeFailure(failureHandler)
            });
    };

    function writeSuccess(responseHandler) {
        return function(xmlDoc, textStatus, jqXHR) {
            var tuple = new webtuplespace.WebTuple();
            tuple.fromXML(xmlDoc)
            var idElements = xmlDoc.getElementsByTagName("Id");
            var idElement = idElements[0];
            var id = idElement.textContent;
            responseHandler(tuple, id, textStatus);
        }
    };

    function writeFailure(failureHandler) {
        return function(jqXHR, textStatus, errorThrown) {
            failureHandler(jqXHR, textStatus, errorThrown);
        };
    };
};

