// ########################################## Upsell #####################################################
/*
possible event trigger

modules/calendar/freebusy
modules/calendar/team
modules/calendar/mini_calender
modules/calendar/new/add_participants
modules/calendar/new/remove_participants
modules/calendar/new/add_attachment
modules/calendar/new/delete_attachment
modules/contacts/new/add_attachment
modules/contacts/new/delete_attachment
modules/mail/save_to_infostore
modules/infostore/send_as_attachment
modules/infostore/send_as_link
modules/infostore/mail/save_to_infostore
modules/tasks/new/add_participants
modules/tasks/new/remove_participants
modules/tasks/new/add_attachment
modules/tasks/new/delete_attachment
configuration/mail/accounts/new
modules/folders/users

modules/infostore
modules/calender
modules/contacts
modules/mail
modules/portal
modules/tasks
modules/configuration

modules/outlook (set in this plugin)
modules/mobility (set in this plugin)

*/

upsell = {
  //global configuration
  config: {
    iframeHeight: "450px", // can be edited
    iframeWidth: "", // should be empty as long a graphic is used in header
    //path to plugin
    path: "plugins/com.openexchange.upsell.multiple.gui/",
    //path to files
    invite: false,
    files: {
      jss: {
        fancy: "fancybox.js",
        modal: "modal.js",
      },
      css: {
        main: "upsell.css",
      }
    },
    features: {
      //feature infostore
      infostore: {
        name: ["modules/infostore","modules/contacts/new/add_attachment", "modules/contacts/new/delete_attachment", "modules/mail/save_to_infostore", "modules/infostore/send_as_attachment", "modules/infostore/send_as_link", "modules/infostore/mail/save_to_infostore", "modules/folders/users"],
        title: _("Enhance your system with &#8222;InfoStore&#8220;"),
        template: "infostore.html",
        init_action: "upsell._set_pause(this, 'Please accept terms first')",
        buttons: {
          trial: {
            content: _("Sign-Up for trial"),
            action: "upsell._get_purchase_method('trial')",
          },
          buy: {
            content: _("Buy"),
            action: "upsell._get_purchase_method('buy')",
          },
        },
        checkboxes: {
          terms:{
            content: _("Accept terms"),
            action: "upsell._set_pause(this, 'Please accept terms first')",
          },
        },
      },

      //feature calender
      calender: {
        name: ["modules/calender", "modules/calendar/freebusy", "modules/calendar/team", "modules/calendar/mini_calender", "modules/calendar/new/add_participants", "modules/calendar/new/remove_participants", "modules/calendar/new/add_attachment", "modules/calendar/new/delete_attachment","modules/tasks/new/add_participants", "modules/tasks/new/remove_participants", "modules/tasks/new/add_attachment", "modules/tasks/new/delete_attachment", "configuration/mail/accounts/new"],
        title: _("Enhance your system with &#8222;Teamwork Capabilities&#8220;"),
        template: "calendar.html",
        init_action: "upsell._set_pause(this, 'Please accept terms first')",
        buttons: {
          trial: {
            content: _("Sign-Up for trial"),
            action: "upsell._get_purchase_method('trial')",
          },
          buy: {
            content: _("Buy"),
            action: "upsell._get_purchase_method('buy')",
          },
        },
        checkboxes: {
          terms:{
            content: _("Accept terms"),
            action: "upsell._set_pause(this, 'Please accept terms first')",
          },
        },
      },

      //feature mobility
      mobility: {
        name: ["modules/usm/eas", "modules/mobility"],
        title: _("Enhance your system with &#8222;Business Mobility&#8220;"),
        product_name: _("Mail Push oder Mail Professional"),
        template: "mobility.html",
        init_action: "upsell._set_pause(this, 'Please accept terms first')",
        buttons: {
          trial: {
            content: _("Sign-Up for trial"),
            action: "upsell._get_purchase_method('trial')",
          },
          buy: {
            content: _("Buy"),
            action: "upsell._get_purchase_method('buy')",
          },
        },
        checkboxes: {
          terms:{
            content: _("Accept terms"),
            action: "upsell._set_pause(this, 'Please accept terms first')",
          },
        },
      },

      //feature outlook
      outlook: {
        name: ["modules/outlook"],
        title: _("Enhance your system with &#8222;Outlook Oxtender&#8220;"),
        template: "outlook.html",
        init_action: "upsell._set_pause(this, 'Please accept terms first')",
        buttons: {
          trial: {
            content: _("Sign-Up for trial"),
            action: "upsell._get_purchase_method('trial')",
          },
          buy: {
            content: _("Buy"),
            action: "upsell._get_purchase_method('buy')",
          },
        },
        checkboxes: {
          terms:{
            content: _("Accept terms"),
            action: "upsell._set_pause(this, 'Please accept terms first')",
          },
        },
      },

      //order confirmation window
      order_confirm: {
        name: ["order_confirm"],
        title: _("Your provider processed your order successfully."),
        template: "order_confirm.html",
        init_action: "upsell._set_pause(this, 'Please accept terms first')",
        buttons: {
          confirm: {
            content: _("Ok"),
            action: "upsell._do_reload()",
          },
        },
        checkboxes: {
          invite:{
            content: _("Invite all my colleagues"),
            action: "upsell._set_invite(this)",
          },
        },
      }
    },
  },

  init: function (feature, win) {

    //Add feature to config
    upsell.config.feature = feature;

    //Find out which feature is selected and configures content
    upsell._get_feature();

    
    upsell._show_upsell();


  },

  _get_required_files: function(){
    jQuery.each(upsell.config.files, function(i, val){
      jQuery.each(val, function(ib, valb){
        if(i == "jss") {
          jQuery.getScript(upsell.config.path + i + "/" + valb);
        } else if (i == "css") {
          jQuery("<link>").appendTo("head").attr({
            rel:  "stylesheet",
            type: "text/css",
            href: upsell.config.path + i + "/" + valb
          });
        }
      })
    });
  },

  //gets current feature and configuration
  _get_feature: function(){
    jQuery.each(upsell.config.features, function(i, val){
      if(jQuery.inArray(upsell.config.feature, val.name) >= 0){
        upsell.config.title = val.title;
        upsell.config.template = upsell.config.path + 'templates/_' + val.template.replace(/.html.*/, "") + "/_" + val.template;
        upsell.config.js = upsell.config.path + 'templates/_' + val.template.replace(/.html.*/, "") + "/_" + val.template.replace(/.html.*/, "") + ".js";
        eval(val.init_action);
        
      }
    });
  },

  //shows OX upsell layer or iframe depeding on server configuration
  _show_upsell: function(){
	
	var callback = function (type) { 
			if (type) {
			    // get redirect URL and show iframe 
				jQuery.getJSON(
						"/ajax/upsell/multiple?session="+parent.session+"&action=get_static_redirect_url&feature_clicked=" + upsell.config.feature,
				        function(data){
							// show iframe incl. URL within overlay
							upsell._build_window_iframe(data.data.upsell_static_redirect_url);
				        }
				 );
				
			} else {
			    jQuery.ajax({
			      url: upsell.config.template,
			      dataType: "text",
			      success: function(data) {
			        upsell._build_window(data);
			      }
			    });
			}
	}
	 // check what type of upsell is configured
	jQuery.getJSON(
	        "/ajax/upsell/multiple?session="+parent.session+"&action=get_method",
	        function(data){
	           if (data.data.upsell_method === "direct"){
	        	   callback(true);
	           } else {
	        	   callback(false);
	           }
	        }
	 );
		

  },

  //builds window and content
  _build_window: function(data){
    var data =
      '<div id="upsell_window">' +
        '<div id="headerSection">' +
           '<h2>' +
             upsell.config.title +
             '<span>' +
               oxProductInfo.product_name +
             '</span>' +
           '</h2>' +
          '<a href="#" class="simplemodal-close"></a>' +
         '</div>' +
         '<div id="contentSection">' +
           '<div class="contentSection">' +
               data +
           '</div>' +
         '</div>' +
         '<div id="footerSection">' +
           upsell._build_inputs();
         '</div>' +
       '</div>';

    jQuery.modal(data);
    jQuery.getScript(upsell.config.js);
  },
  
  //builds window with ifram content
  _build_window_iframe: function(iframeurl){
    var data =
      '<div id="upsell_window">' +
        '<div id="headerSection">' +
           '<h2>' +
             upsell.config.title +
           '</h2>' +
          '<a href="#" class="simplemodal-close"></a>' +
         '</div>' +
         '<div id="contentSection">' +
           '<div class="contentSection" style="height: '+upsell.config.iframeHeight+'; width:'+upsell.config.iframeWidth+'">' +
               '<iframe src="'+ iframeurl +'" width="100%" height="100%"></iframe>'
           '</div>' +
         '</div>' +
         '<div id="footerSection"></div>' +
       '</div>';

    jQuery.modal(data);
    jQuery.getScript(upsell.config.js);
  },
  //close current window
  _close_dialouge: function(){
    jQuery.modal.close();
  },

  //builds required buttons from configuration
  _build_inputs: function(data){
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
      };
      if(jQuery.inArray(upsell.config.feature, val.name) >= 0 && val.checkboxes){
        jQuery.each(val.checkboxes, function(ib, valb){
          if(valb.content != "undefined"){
            button += '<span style="float: left; margin: 10px"><input type="checkbox" name="';
            button += ib;
            button += '"onClick="' + valb.action + '"';
            button += '" value="false">' + valb.content + '</span>';
          }
        });
      }
    });
    return button;
  },

  _set_invite: function(e){
    if(e.checked) {
      upsell.config.invite = true;
    } else {
      upsell.config.invite = false;
    }
  },

  _set_pause:function(e, message){
    upsell.config.pause = true;
    upsell.config.pause_message = message;

    if(e.checked) {
      upsell.config.pause = false;
      upsell.config.pause_message = "";
    }
  },

  _set_invite: function(e){
    if(e.checked) {
      upsell.config.invite = true;
    } else {
      upsell.config.invite = false;
    }
  },

  //gets selected purchase method
  _get_purchase_method: function(type){
    if(!upsell.config.pause){
      jQuery.getJSON(
        "/ajax/upsell/multiple?session="+parent.session+"&action=get_method",
        function(data){
          upsell.config.purchase_type = type;
          upsell._procces_purchase(data.data.upsell_method);
        }
      );
    } else {
      alert(upsell.config.pause_message);
    }
  },

  //initialize purchase
  _procces_purchase: function(data){
    switch (data) {
      case "email":
        jQuery.getJSON(
          "/ajax/upsell/multiple?session="+parent.session+"&action=send_upsell_email&feature_clicked=" + upsell.config.feature + "&purchase_type=" + upsell.config.purchase_type + "&invite=" + upsell.config.invite + "&accept_terms=" + upsell.config.terms,
          function(data){
            upsell._do_purchase_mail(data);
          }
        );
        break;
      case "static":
        upsell._do_purchase_redirect();
        break;
      default:
        alert("No Valid Purchasing method");
        break;
    }
  },


  
  
  //redirect static purchase
  _do_purchase_redirect: function(data){
	  jQuery.getJSON(
      "/ajax/upsell/multiple?session="+parent.session+"&action=get_static_redirect_url&feature_clicked=" + upsell.config.feature + "&purchase_type=" + upsell.config.purchase_type + "&invite=" + upsell.config.invite + "&accept_terms=" + upsell.config.terms,
      function(data){
        window.open(data.data.upsell_static_redirect_url, '_blank');
      }
    );
  },

  //generates purchase mail
  _do_purchase_mail: function(data){
	  jQuery.getJSON(
      "/ajax/upsell/multiple?session="+parent.session+"&action=change_context_permissions&upsell_plan=groupware_premium&feature_clicked=" + upsell.config.feature + "&purchase_type=" + upsell.config.purchase_type,
      function(data){
        upsell._close_dialouge();
        upsell.init("order_confirm");
      }
    );
  },

  //reload and activate feature
  _do_reload: function(data){
    location.reload();
  },
};

// loads required files
upsell._get_required_files();

//registration
register("Feature_Not_Available", upsell.init);


/**
* upsell function in the portal pannel: syncronization for outlook and mobility
*/

var syncupsell = MenuNodes.createSmallButtonContext("syncronisation", "NEU! Synchronisierung");
MenuNodes.createSmallButton(syncupsell,"buttonol", "Windows Outlook", getFullImgSrc("img/mail/email_priolow.gif"), "", function(){
        upsell.init("modules/outlook");
});

/* The pannel object gets the id 42 and gets displayed in the fixed area
 * possible areas are FIXED and DYNAMIC the id controls the order in the areas
 */
addMenuNode(syncupsell.node, MenuNodes.FIXED, 42);

//Following makes the new pannel options dynamic active/inactive
changeDisplay("portal", "syncronisation");
//show the upsell area in the pannel on the first login
showNode("syncronisation");