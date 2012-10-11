define("chat/ui", function () {
   function draw(session) {
       var $node = this, $textarea, $button, $log, socket;
       
       $node.append(
            $textarea = $('<textarea>', {width: "600px", height: "300px"}),
            $button = $("<button>Send</button>"),
            "<br />",
            $log = $('<pre>', {width: "600px", height: "300px"}).css({overflow: "auto"})
       );
        
       socket = new WebSocket("ws://localhost:8031/rt");
       socket.onopen = function () {
           socket.send('{namespace: "ws:handshake", data: {session: ' + session + ', resource: "Browser"}}');
       };
       
       socket.onmessage = function (e) {
           var msg = JSON.parse(e.data);
           console.log(msg);
           
           $log.append(JSON.stringify(msg, null, 4)); 
       };
       
       $button.on("click", function () {
           socket.send($textarea.val());
       });
   }
   return {
       draw: draw
   };
});