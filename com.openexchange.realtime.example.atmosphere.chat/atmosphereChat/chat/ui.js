define("chat/ui", function () {
  
  function drawUI(session) {
    var $ui = $('#mainContent');
    $ui.append('<div id="header"><h3>Atmosphere Chat. Default transport is WebSocket, fallback is long-polling</h3></div>');
    $ui.append('<div id="detect"><h3>Detecting what the browser and server are supporting</h3></div>');
    $ui.append('<div id="content"></div>');
    $ui.append('<div><span id="status">' + ox.userName + '</span><input type=text id="input" disabled="disabled"/></div>');
  };
  
  function draw(session) {
    
    drawUI();
    
    //TEST ALL THE TRANSPORTS \o/
    var detect = $('#detect');
    var header = $('#header');
    var content = $('#content');
    var input = $('#input');
    var status = $('#status');
    var myName = ox.userName;
    var author = null;
    var logged = true;
    var socket = $.atmosphere;

    <!-- The following code is just here for demonstration purpose and not required -->
    <!-- Used to demonstrate the request.onTransportFailure callback. Not mandatory -->
    var sseSupported = false;

    var transports = new Array();
    transports[0] = "websocket";
    transports[1] = "sse";
    transports[2] = "jsonp";
    transports[3] = "long-polling";
    transports[4] = "streaming";
    transports[5] = "ajax";

    $.each(transports, function (index, transport) {
        var req = new $.atmosphere.AtmosphereRequest();

	req.url = "http://marens.netline.de/realtime/atmosphere/rt";
        req.contentType = "application/json";
        req.transport = transport;
        req.headers = { "negotiating" : "true", session: session };

        req.onOpen = function(response) {
            detect.append('<p><span style="color:blue">' + transport + ' supported: '  + '</span>' + (response.transport == transport));
        }

        req.onReconnect = function(request) {
            request.close();
        }

        socket.subscribe(req)
    });
    
    var request = {
        url: "http://marens.netline.de/realtime/atmosphere/rt",
        contentType : "application/json",
        logLevel : 'debug',
        transport : 'websocket' ,
        fallbackTransport: 'long-polling',
        headers : {session: session}
        };


    //------------------------------------------------------------------------------
    //request callbacks 

    request.onOpen = function(response) {
        content.html($('<p>', { text: 'Atmosphere connected using ' + response.transport }));
        input.removeAttr('disabled').focus();
    };

    //For demonstration of how you can customize the fallbackTransport based on the browser
//    request.onTransportFailure = function(errorMsg, request) {
//        jQuery.atmosphere.info(errorMsg);
//        if ( window.EventSource ) {
//            request.fallbackTransport = "long-polling";
//        }
//        header.html($('<h3>', { text: 'Atmosphere Chat. Default transport is WebSocket, fallback is ' + request.fallbackTransport }));
//    };

    request.onReconnect = function (request, response) {
        socket.info("Reconnecting")
    };

    request.onMessage = function (response) {
        var message = response.responseBody;
	console.log('Got message from server');
	console.log(message);
        try {
            var json = jQuery.parseJSON(message);
        } catch (e) {
            console.log('This doesn\'t look like a valid JSON: ', message.data);
            return;
        }

        var me = json.author == myName;
        var date = typeof(json.time) == 'string' ? parseInt(json.time) : json.time;
        addMessage(json.from, json.data.message, me ? 'blue' : 'black', new Date());

    };

    request.onClose = function(response) {
        logged = false;
    };

    request.onError = function(response) {
        content.html($('<p>', { text: 'Sorry, but there\'s some problem with your '
            + 'socket or the server is down' }));
    };
    
    
    var subSocket = socket.subscribe(request);
    
    subSocket.push(jQuery.stringifyJSON({
    session: session,
    ns: 'ox:handshake',
    data: {
      resource: 'Browser'
    }
    }));
//    subSocket.push(jQuery.stringifyJSON({author: ox.userName, message: "logged into chat"}));
    status.css('color', 'blue');

    input.keydown(function(e) {
        if (e.keyCode === 13) {
            var msg = $(this).val();

            //subSocket.push(jQuery.stringifyJSON({ author: ox.userName, message: msg }));
            subSocket.push(jQuery.stringifyJSON({
              session: session,
              ns: 'chat',
              to: ox.userName,
              data: {
                message: msg,
                priority: 2
              }
            }));
            $(this).val('');
        }
    });

    function addMessage(author, message, color, datetime) {
    	var time =(datetime.getHours() < 10 ? '0' + datetime.getHours() : datetime.getHours()) + ':'
          + (datetime.getMinutes() < 10 ? '0' + datetime.getMinutes() : datetime.getMinutes()) + ':'
          + (datetime.getSeconds() < 10 ? '0' + datetime.getSeconds() : datetime.getSeconds());
          
        content.append('<p>' + time + ': ' + '<span style="color:' + color + '">' + author + '</span>' 
            + ': ' + message + '</p>');
    };
    
  }; //end draw function
  return {draw: draw}
});
