
var result, resultId, resultCode, done, global_client, before, after;


function handleWriteSuccess(tuple, id, status) {
    console.log("handling write success...");
    result = tuple;
    resultId = id;
    resultCode = status;
    done = true;
}

function handleWriteFail(jqXHR, textStatus, errorThrown) {
    console.log("handling write fail...");
    throw errorThrown;
}

function handleReadSuccess(tuples, id, status) {
    console.log("handling read success...");
    result = tuples;
    resultId = id;
    resultCode = status;
    done = true;
}

function handleReadFail(jqXHR, textStatus, errorThrown) {
    console.log("handling read fail...");
    throw errorThrown;
}


function writeTestTuple1() {
    done = false;
    var client = new webtuplespace.Client();
    var tuple = new webtuplespace.WebTuple();
    tuple.push(["string", "abc"]);
    tuple.push(["int", 1]);
    client.write(tuple, handleWriteSuccess, handleWriteFail);
}

function writeTestTuple2() {
    done = false;
    var client = new webtuplespace.Client();
    var tuple = new webtuplespace.WebTuple();
    tuple.push(["string", "xyz"]);
    tuple.push(["int", 1]);
    client.write(tuple, handleWriteSuccess, handleWriteFail);
}

function readTestTuple1() {
    done = false;
    var client = new webtuplespace.Client();
    var pattern = new webtuplespace.WebTuple(["string", "abc"]);
    client.read(pattern, handleReadSuccess, handleReadFail);
}

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
    sendRequest(null, "reset", "GET", noopSuccessResponseHandlerDecorator(handleResetSuccess),
        noopFailureResponseHandlerDecorator(handleResetFailure))
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
        }, "handleReadSuccess should have been called", 5000);

        runs(function() {
            expect(result.length).toBe(2);
        });
    });
});

describe("webtuplespace subscription function, given an initially empty space", function() {

    function startSessionSuccess(sid) {
        console.log("in startSessionSuccess sessionId is: " + sid);
        global_client.setSessionId(sid);
        done = true;
    }

    function startSessionFail(jqXHR, textStatus, error) {
        console.log("start session failed:");
        console.log(textStatus);
        console.log(error.toString());
    }

    it("when a session is created then session id should be returned", function() {

        runs(resetTupleSpace);

        waitsFor(function() {
            return done;
        }, "reset should have been returned", 5000);

        runs(function() {
            done = false;
            global_client = new webtuplespace.Client();
            global_client.startSession(startSessionSuccess, startSessionFail);
        })

        waitsFor(function() {
           return done;
        }, "session start should have completed", 5000);

        runs(function() {
            console.log("sessionId is: " + global_client.getSessionId());
            expect(global_client.getSessionId()).toBeDefined();
        });
    });

    it("then when a subscription is created then the request succeeds and an additional pattern is\nadded to the proxy",
        function() {
            runs(function() {
                function successHandler() {
                    console.log("add subscription succeeded");
                    done = true;
                }
                function failureHandler(jqXHR, statusText, error) {
                    console.log("add subscription failed:");
                    console.log(error.toString());
                }
                before = global_client.subscriptions.length;
                done = false;
                var pattern = new webtuplespace.WebTuple(["string", "abc"]);
                global_client.addSubscription(pattern, successHandler, failureHandler);
            });

            waitsFor(function() {
                return done;
            }, "add subscription should have returned", 5000);

            runs(function() {
                after = global_client.subscriptions.length;
                expect(after).toBe(before + 1);
            });

    });

    it ("then when tuples are added, two matching and one on non-matching, the subscription pattern\n, two notifications should be returned",
        function() {
            function successHandler(notificationSet) {
                console.log("handling notification request success");
                result = notificationSet;
                done = true;
            }
            function failureHandler(jqXHR, textStatus, error) {
                console.log("notification request failed with error:");
                console.log(error.toString());
            }

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

            runs(function() {
                done = false;
                global_client.getNotifications(successHandler, failureHandler);
            });

            waitsFor(function() {
                return done;
            }, "get notifications should have returned", 5000);

            runs(function() {
                expect(result.length).toBe(1);
                var firstNotifications = result[0];
                console.log(firstNotifications.pattern.toString());
                //console.log(firstNotifications.tuples[0].toString());
                expect(firstNotifications.pattern).toBeDefined();
                expect(firstNotifications.tuples.length).toBe(2);
            })
        });
});

// todo: this last test is not working because the writes have been done after the subscription - they should be picked
// up in the subscription but apparently at the moment they are not - check write