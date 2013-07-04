
function handleWriteSuccess(tuple, id, code) {
    console.log("displaying write success result...");
    $('#text1').val(tuple.toString() + "\n" + id + "\n" + code);
}

function handleWriteFail(jqXHR, textStatus, errorThrown) {
    console.log("displaying write failure result...");
    $('#text1').val(textStatus + "\n" + errorThrown.toString());
}

$(document).ready(function() {
    $('#button1').click(
        function() {
            var client = new webtuplespace.Client();
            var tuple = new webtuplespace.WebTuple();
            tuple.push(["string", "abc"]);
            tuple.push(["int", 1]);
            client.write(tuple, handleWriteSuccess, handleWriteFail);
        }
    );
});