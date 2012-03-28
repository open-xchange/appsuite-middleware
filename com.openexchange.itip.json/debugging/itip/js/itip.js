ox.retrieveSession.done(function (session) {
   $("body").empty().append("<h1>ITip Analysis Tester - Ready</h1>");
   var $container = $("<div/>").appendTo($("body"));
   
   var $first = $("<div/>").appendTo($container);
   var $second = $("<div/>").appendTo($container);
   
   
   
   $textArea = $("<textarea>").css({
       width: "100%",
       height: ($(window).height() * 0.5)+"px"
   });
   $first.append($("<form/>").append($textArea));
   
   
   $first.append($("<button>Analyze</button>").on("click", function () {
       $second.empty();
       $.ajax("/ajax/calendar/itip?action=analyze&session="+ox.session, {
           type: "PUT",
           data: JSON.stringify({ical: $textArea.val()})
       }).always(function (resp) {
          resp = JSON.parse(resp.responseText);
          $second.append($("<pre/>").text(JSON.stringify(resp, null, 4)).addClass("prettyprint").css("margin", "10px"));
          prettyPrint();
       });
   }));
});
