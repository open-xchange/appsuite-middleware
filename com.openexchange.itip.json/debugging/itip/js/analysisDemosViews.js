ox.retrieveSession.done(function (session) {
   $("body").empty();
   var $menu = $("<div/>").addClass("menu").appendTo($("body"));
   var $log = $("<div/>").addClass("log").appendTo($("body"));
   
   ox.demos.on("run", function (evt) {
       console.log("run", evt);
       $log.append($("<p/>").text(evt.name+": Running"));
   });

   ox.demos.on("analysis", function (evt) {
       console.log("analysis", evt);
       var $analysisDiv = $("<div/>").addClass("analysis").appendTo($log);
       $("<pre/>").text(evt.ical).appendTo($analysisDiv);
       $("<div/>").text("Analysis:").appendTo($analysisDiv);    
       var $responseNode = $("<pre/>").appendTo($analysisDiv);
       $responseNode.text(JSON.stringify(evt.resp, null, 4));
   });

   ox.demos.on("done", function (evt) {
       console.log("done", evt);
       $log.append($("<p/>").text(evt.name+": Done"));
   });
   
   _(ox.demos.demos).each(function (demo) {
       var $button = $("<a href='#'/>")
       $button.text(demo.name)
       $button.on("click", function () {
          $log.empty();
          ox.demos.run(demo.name); 
       });
       $button.appendTo($("<div/>").appendTo($menu));
   });
});
