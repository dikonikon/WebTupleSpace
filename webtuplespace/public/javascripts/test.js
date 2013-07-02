
function handleResult(tuple, code) {
    console.log("writing result...");
    $('#text1').val(tuple.toString());
}

$(document).ready(function() {
    $('#button1').click(
        function() {
            var client = new webtuplespace.Client();
            var tuple = new webtuplespace.WebTuple();
            tuple.push(["string", "abc"]);
            tuple.push(["int", 1]);
            client.write(tuple, handleResult);
        }
    );
});