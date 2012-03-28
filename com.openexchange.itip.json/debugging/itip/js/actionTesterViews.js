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
       var $icalDiv = $("<pre/>").text(evt.ical);
       $icalDiv.appendTo($analysisDiv).hide();
       $("<a href='#' />").text("Show ical").on("click", function () {
           $(this).remove();
           $icalDiv.show();
           return false;
       }).appendTo($analysisDiv);
       
       $("<br/>").appendTo($analysisDiv);
       
       var $responseNode = $("<pre/>").appendTo($analysisDiv);
       $responseNode.text(JSON.stringify(evt.resp, null, 4));
       $responseNode.hide();
       
       $("<a href='#' />").text("Show analysis").on("click", function (evt) {
           $(this).remove();
           $responseNode.show();
           return false;
       }).appendTo($analysisDiv)
       $("<hr/>").appendTo($analysisDiv);
       
       $actionsList = $("<ul/>").appendTo($analysisDiv);
       _(evt.resp.data[0].actions).each(function (action) {
           $("<li/>").append($("<a href='#' />").text(action).on("click", function () {
               evt.env.perform(action, evt.ical);
               return false;
           })).appendTo($actionsList);
       });
       $actionsList.append($("<li/>").append($("<a href='#'/>").text("CleanUp").on("click", function () {
           evt.env.cleanUp();
           return false;
       })));
   });
   
   ox.demos.on("performed", function (evt) {
       var $performedDiv = $("<div/>").addClass("analysis").appendTo($log);
       var $responseNode = $("<pre/>").appendTo($performedDiv);
       $responseNode.text(JSON.stringify(evt.resp, null, 4));
   });

   
   _(ox.demos.demos).each(function (demo) {
       var $button = $("<a href='#'/>")
       $button.text(demo.name)
       $button.on("click", function () {
          $log.empty();
          ox.demos.offerActions(demo.name); 
          return false;
       });
       $button.appendTo($("<div/>").appendTo($menu));
   });
});
