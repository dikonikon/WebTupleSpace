
var result, resultId, resultCode, done;


function handleWriteSuccess(tuple, id, status) {
    console.log("handling write success...");
    result = tuple;
    resultId = id;
    resultCode = status;
    done = true;
};

function handleWriteFail(jqXHR, textStatus, errorThrown) {
    console.log("handling write fail...");
    throw errorThrown;
};

function handleReadSuccess(tuples, id, status) {
    console.log("handling read success...");
    result = tuples;
    resultId = id;
    resultCode = status;
    done = true;
};

function handleReadFail(jqXHR, textStatus, errorThrown) {
    console.log("handling read fail...");
    throw errorThrown;
};


function writeTestTuple1() {
    done = false;
    var client = new webtuplespace.Client();
    var tuple = new webtuplespace.WebTuple();
    tuple.push(["string", "abc"]);
    tuple.push(["int", 1]);
    client.write(tuple, handleWriteSuccess, handleWriteFail);
};

function writeTestTuple2() {
    done = false;
    var client = new webtuplespace.Client();
    var tuple = new webtuplespace.WebTuple();
    tuple.push(["string", "xyz"]);
    tuple.push(["int", 1]);
    client.write(tuple, handleWriteSuccess, handleWriteFail);
};

function readTestTuple1() {
    done = false;
    var client = new webtuplespace.Client();
    var pattern = new webtuplespace.WebTuple(["string", "abc"]);
    client.read(pattern, handleReadSuccess, handleReadFail);
};

function resetTupleSpace() {
    function handleResetSuccess() {
        console.log("reset successful");
        done = true;
    };
    function handleResetFailure(jqXHR, textStatus, errorThrown) {
        console.log("reset failure");
        console.log("error thrown:");
        console.log(errorThrown.toString());
        done = true;
    }
    done = false;
    sendRequest(null, "reset", "GET", noopSuccessResponseHandlerDecorator, noopFailureResponseHandlerDecorator,
        handleResetSuccess, handleResetFailure)
}

describe("webtuplespace write function", function() {

   beforeEach(function() {
       runs(resetTupleSpace);
       waitsFor(function() {
           return done;
       }, "reset should have been returned", 5000);
   });


   afterEach(function() {
       runs(resetTupleSpace);
       waitsFor(function() {
           return done;
       }, "reset should have been returned", 5000);
   });

   it("if successful should return a copy of the tuple, with its id populated", function() {
       runs(writeTestTuple1);

       waitsFor(function() {
            return done;
       }, "handleWriteSuccess should have been called", 5000);

       runs(function() {
           expect(resultId).toBeDefined();
           var resultEl = result.getElement(0);
           expect(resultEl).toBe("abc");
       });
   });
});

describe("webtuplespace read function, given two matching tuples in the db, and one non-matching", function() {

    beforeEach(function() {
        runs(resetTupleSpace);
        waitsFor(function() {
            return done;
        }, "reset should have been returned", 5000);
    });

    afterEach(function() {
        runs(resetTupleSpace);
        waitsFor(function() {
            return done;
        }, "reset should have been returned", 5000);
    });

    it("should return the two matching tuples and not the non-matching one", function() {
        runs(writeTestTuple1);

        waitsFor(function() {
            return done;
        }, "handleWriteSuccess should have been called", 5000);

        runs(writeTestTuple1);

        waitsFor(function() {
            return done;
        }, "handleWriteSuccess should have been called", 5000);

        runs(writeTestTuple2);

        waitsFor(function() {
            return done;
        }, "handleWriteSuccess should have been called", 5000);

        runs(readTestTuple1);

        waitsFor(function() {
            return done;
        }, "handleWriteSuccess should have been called", 5000);

        runs(function() {
            expect(result.length).toBe(2);
        });
    });
});