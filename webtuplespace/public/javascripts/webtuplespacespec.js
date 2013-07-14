
var resultTuple, resultId, resultCode, done;


function handleWriteSuccess(tuple, id, status) {
    console.log("handling write success...");
    resultTuple = tuple;
    resultId = id;
    resultCode = status;
    done = true;
};

function handleWriteFail(jqXHR, textStatus, errorThrown) {
    console.log("handling write fail...");
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
    var pattern = new webtuplespace.WebTuple([["string", "abc"]]);
    client.read(pattern, handleWriteSuccess, handleWriteFail);
};

describe("webtuplespace write function", function() {

   it("if successful should return a copy of the tuple, with its id populated", function() {
       runs(writeTestTuple1());

       waitsFor(function() {
            return done;
       }, "handleWriteSuccess should have been called", 5000);

       runs(function() {
           expect(resultId).toBeDefined();
           var resultEl = resultTuple.getElement(0);
           expect(resultEl).toBe("abc");
       });
   });
});

describe("webtuplespace read function, given two matching tuples in the db, and one non-matching", function() {
   var resultTuples, resultId, resultCode, done;

    it("should return the two matching tuples and not the non-matching one", function() {
        runs(writeTestTuple1());

        waitsFor(function() {
            return done;
        }, "handleWriteSuccess should have been called", 5000);

        runs(writeTestTuple1());

        waitsFor(function() {
            return done;
        }, "handleWriteSuccess should have been called", 5000);

        runs(writeTestTuple2());

        waitsFor(function() {
            return done;
        }, "handleWriteSuccess should have been called", 5000);

        runs(readTestTuple1());

        waitsFor(function() {
            return done;
        }, "handleWriteSuccess should have been called", 5000);

        runs(function() {
            expect(resultTuple.length).toBe(2);
        });
    });
});