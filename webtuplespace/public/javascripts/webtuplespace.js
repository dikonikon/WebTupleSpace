// namespace for the webtuplespace client proxy

webtuplespace = {};

webtuplespace.getDefault = function(arg, defval) {
    return (typeof arg === 'undefined' ? defval: arg)
};

webtuplespace.WebTuple = function() {
    this.internal = []
    for (var i = 0; i < arguments.length; i++) {
        this.internal.push(arguments[i])
    }
    this.prototype.toXML = function() {
        var xmlRep = "<Tuple>";
        for (var i = 0; i < this.internal.length; i++) {
            xmlRep += "<Element>";
            xmlRep += "<Type>";
            xmlRep += this.internal[i][0];
            xmlRep += "</Type>";
            xmlRep += "<Value>";
            xmlRep += this.internal[i][1];
            xmlRep += "</Value>";
            xmlRep += "</Element>"
        }
        xmlRep += "</Tuple>"
        return xmlRep
    };
    this.prototype.push = function() {
        this.internal.push(arguments[0])
    };
};

webtuplespace.Client = function(host, portno) {
    this.host = getDefault(host, 'localhost');
    this.portno = getDefault(portno, '80');

    this.prototype.write = function(tuple) {

    }


};

