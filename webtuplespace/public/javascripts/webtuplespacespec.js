



describe("webtuplespace write function", function() {
   var resultTuple, resultId, resultCode, done;

    function handleWriteSuccess(tuple, id, status) {
        console.log("handling write success...");
        resultTuple = tuple;
        resultId = id;
        resultCode = status;
        done = true;
    }

    function handleWriteFail(jqXHR, textStatus, errorThrown) {
        console.log("handling write fail...");
        throw errorThrown;
    }

   it("if successful should return a copy of the tuple, with its id populated", function() {
       runs(function() {
           var client = new webtuplespace.Client();
           var tuple = new webtuplespace.WebTuple();
           tuple.push(["string", "abc"]);
           tuple.push(["int", 1]);
           client.write(tuple, handleWriteSuccess, handleWriteFail);
       });

       waitsFor(function() {
            return done;
       }, "handleWriteSuccess should have been called", 5000);

       runs(function() {
           expect(resultId).toBeDefined();
           var resultEl = resultTuple.getElement(0);
           expect(resultEl).toBe("abc");
       })
   });
});