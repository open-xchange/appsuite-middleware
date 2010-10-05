upsell = {
  config: {
    path: "plugins/com.openexchange.upsell.multiple.gui/",
    features: {
      infostore: {
        name: ["modules/infostore"],
        title: "Enhance your system with &#8222;InfoStore&#8220;",
        template: "infostore.html",
        buttons: {
          trial: {
            content: "Sign-Up for trial",
            action: "upsell._get_purchase_method()",
          },
        },
      },
      calender: {
        name: ["modules/calender"],
        title: "Hinweis zum Teamkalender - Mail Professional",
        template: "calendar.html",
        buttons: {
          trial: {
            content: "Sign-Up for trial",
            action: "upsell._get_purchase_method()",
          },
        },
      },
      mobility: {
        name: ["modules/usm/eas", "modules/mobility"],
        title: "Enhance your system with &#8222;Business Mobility&#8220;",
        product_name: "Mail Push oder Mail Professional",
        template: "mobility.html",
        buttons: {
          trial: {
            content: "Sign-Up for trial",
            action: "upsell._get_purchase_method()",
          },
        },
      },
      outlook: {
        name: ["modules/outlook"],
        title: "Hinweis zu Mail Professional",
        template: "outlook.html",
      },
      order_confirm: {
        name: ["order_confirm"],
        title: "Danke fuer Ihre Bestellung",
        template: "order_confirm.html",
      }
    },
  },
  
  init: function (feature, win) {
    // Bind Lightbox
    jQuery('body').delegate('a.light_box', 'mouseover', function(){
      jQuery('a.light_box.image').fancybox(
        {
          'titleShow':false
        }
      );
      jQuery('a.light_box.swf').fancybox(
        { 
          'padding'			: 0,
				  'autoScale'			: false,
  				'transitionIn'		: 'none',
	  			'transitionOut'		: 'none'
        }
      );
      return false;
    });
    
    // Reset delegated actions
    jQuery('a.light_box').undelegate('mouseout');
    
    // Add feature to config
    upsell.config.feature = feature;
    
    // Find out which feature is selected and configures content
    upsell._get_feature();
    
    // Show upsell
    upsell._show_upsell();
    

  },
  
  _get_feature: function(){
    jQuery.each(upsell.config.features, function(i, val){
      if(jQuery.inArray(upsell.config.feature, val.name) >= 0){
        upsell.config.title = val.title;
        upsell.config.template = upsell.config.path + 'templates/_' + val.template.replace(/.html.*/, "") + "/_" + val.template;
        upsell.config.js = upsell.config.path + 'templates/_' + val.template.replace(/.html.*/, "") + "/_" + val.template.replace(/.html.*/, "") + ".js";
      }
    });
  },
  
  _show_upsell: function(){
    jQuery.ajax({
      url: upsell.config.template,
      dataType: "text", 
      success: function(data) {
        upsell._build_window(data);
      }
    });

  },
  
  _build_window: function(data){
    var data =
      '<div id="upsell_window">' +
        '<div id="headerSection">' +
           '<h2>' +
             upsell.config.title +
           '</h2>' +
          '<a href="#" class="simplemodal-close"></a>' +
         '</div>' +
         '<div id="contentSection">' +
           '<div class="contentSection">' +
               data +
           '</div>' +
         '</div>' +
         '<div id="footerSection">' +
           upsell._build_button();
         '</div>' +
       '</div>';
       
    jQuery.modal(data);
    jQuery.getScript(upsell.config.js);
  },
  
  _build_button: function(data){
    var button = "";
    jQuery.each(upsell.config.features, function(i, val){
      if(jQuery.inArray(upsell.config.feature, val.name) >= 0 && val.buttons){
        jQuery.each(val.buttons, function(ib, valb){
          if(valb.content != "undefined"){
            button += '<a href="#" id="' + ib + '" class="btn"';
            button += 'onClick="' + valb.action + '"';
            button += '><span>';
            button += valb.content;
            button += '</span></a>';
          }
        });
      }
    });
    return button;
  },
  
  _get_purchase_method: function(){
    jQuery.getJSON(
      "/ajax/upsell/multiple?session="+parent.session+"&action=get_method",
      function(data){
        upsell._procces_purchase(data.data.upsell_method);
      }
    );
  },
  
  _procces_purchase: function(data){
    switch (data) {
      case "email":
        jQuery.getJSON(
          "/ajax/upsell/multiple?session="+parent.session+"&action=send_upsell_email&feature_clicked="+upsell.config.feature,
          function(data){
            upsell._do_purchase_mail(data);
          }
        );
        break;
      case "static":
        upsell._get_purchase_redirect();
        break;
      default:
        alert("No Valid Purchasing method");
        break;
    }
  },
  
  _do_purchase_redirect: function(data){
	  jQuery.getJSON(
      "/ajax/upsell/multiple?session="+parent.session+"&action=get_static_redirect_url&feature_clicked="+upsell.config.feature,
      function(data){
        window.open(data.data.upsell_static_redirect_url, '_blank');
      }
    );
  },
  
  _do_purchase_mail: function(data){
	  jQuery.getJSON(
      "/ajax/upsell/multiple?session="+parent.session+"&action=change_context_permissions&upsell_plan=groupware_premium",
      function(data){
        alert("Your provider processed your order successfully. After you press 'OK', an automatic Re-Login will be initiated, and the new feature is going to be available immediately");
        location.reload();
      }
    );
  },
  
};


// Initialize upsell and load dependencies
jQuery.getScript("plugins/com.openexchange.upsell.multiple.gui/jss/fancybox.js");
jQuery.getScript("plugins/com.openexchange.upsell.multiple.gui/jss/modal.js");

jQuery("<link>").appendTo("head").attr({
  rel:  "stylesheet",
  type: "text/css",
  href: "plugins/com.openexchange.upsell.multiple.gui/css/upsell.css"
});

// Register
register("Feature_Not_Available", upsell.init);
