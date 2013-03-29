define("ui/ui", function () {
  
  function drawUI(session) {
    var $ui = $('#mainContent');
    $ui.append('<div id="status"><div>');
    $ui.append('<div id="content"></div>');
    $ui.append('<textarea disabled="disabled" id="input">'),
    $ui.append('<button id="send">Send</button>'),
    "<br />",
    $log = $('<pre>', {width: "600px", height: "300px"}).css({overflow: "auto"})
  };
  
  function init(session) {
    
    drawUI();
    
    var status = $('#status');
    var content = $('#content');
    var input = $('#input');
    var send = $('#send')

    var myName = ox.userName;
    var socket = $.atmosphere;
    var splits = document.location.toString().split('/');
    var proto = splits[0]; var host = splits[2];
    var url = proto+"//"+host+"/realtime/atmosphere/rt";
    
    var request = {
        url: url+'?session='+session,
        contentType : "application/json",
        logLevel : 'debug',
        transport : 'long-polling' ,
        fallbackTransport: 'long-polling',
        timeout: 50000,
        maxRequests : 3
        };


    //------------------------------------------------------------------------------
    //request callbacks 

    request.onOpen = function(response) {
        status.html($('<p>', { text: 'Atmosphere connected using ' + response.transport }));
        input.removeAttr('disabled').focus();
    };

    request.onReconnect = function (request, response) {
        socket.info("Reconnecting")
    };

    request.onMessage = function (response) {
        var message = response.responseBody;
        try {
            var json = jQuery.parseJSON(message);
        } catch (e) {
            console.log('This doesn\'t look like a valid JSON: ', message);
            return;
        }
        addMessage(message, new Date());

    };

    request.onClose = function(response) {
        socket.info("Closing")
    };

    request.onError = function(response) {
        status.html($('<p>', { text: 'Sorry, but there\'s some problem with your '
            + 'socket or the server is down' }));
    };
    

    send.click(function() {
    	console.log("button clicked");
            var msg = input.val();
            console.log("message was:"+msg);
            var json = jQuery.parseJSON(msg);
            json.session = session;
            subSocket.push(jQuery.stringifyJSON(json));
            console.log("pushed message:"+jQuery.stringifyJSON(json));
            //input.val('');
    });

    function addMessage(message, datetime) {
    	var time =(datetime.getHours() < 10 ? '0' + datetime.getHours() : datetime.getHours()) + ':'
          + (datetime.getMinutes() < 10 ? '0' + datetime.getMinutes() : datetime.getMinutes()) + ':'
          + (datetime.getSeconds() < 10 ? '0' + datetime.getSeconds() : datetime.getSeconds());
          
        content.append('<p>' + time + ': ' + message + '</p>');
    };
    
    //Subscribe
    console.log("Going to subscribe");
    var subSocket = socket.subscribe(request);
    console.log("Subscribed");
    
//  subSocket.push(jQuery.stringifyJSON({
//  session: session,
//  ns: 'ox:handshake',
//  data: {
//    step: 'open',
//    resource: 'Browser'
//  }
//  }));
//  subSocket.push(jQuery.stringifyJSON({author: ox.userName, message: "logged into chat"}));

    
  }; //end draw function
  return {init: init}
});
