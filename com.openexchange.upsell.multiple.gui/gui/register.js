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
    //path to plugin
    path: "plugins/com.openexchange.upsell.multiple.gui/",
    //path to files
    invite: false,
    show_features_initial: 2,
    history: new Array(),
    files: {
      jss: {
        fancy: {
          script: "fancybox.js",
          action: "jQuery('a.light_box').fancybox({'titleShow':false});"
        },
        modal: {
          script: "modal.js"
        }
      },
      css: {
        main: "upsell.css"
      }
    },
    features: {
      //feature infostore
      infostore: {
        name: ["modules/infostore","modules/contacts/new/add_attachment", "modules/contacts/new/delete_attachment", "modules/mail/save_to_infostore", "modules/infostore/send_as_attachment", "modules/infostore/send_as_link", "modules/infostore/mail/save_to_infostore", "modules/folders/users"],
        title: _("Enhance your system with &#8222;InfoStore&#8220;"),
        intro: _("Where ever you are your documents are stored securely and<br> can be accessed and worked on, anytime!"),
        list: {
          list_item_1:  _("Teamwork on important files & data"),
          list_item_2:  _("Share or publish docments without sending large email attachments"),
          list_item_3:  _("Ensure actuality through automatic versioning"),
          list_item_4:  _("Distinguish between official and private content through &#8220;Personal&#8221; &amp; &#8220;Public&#8221; folders"),
          list_item_5:  _("Refer to documents via URL-link in projekts and meetings"),
          list_item_6:  _("Access your files anywhere anytime"),
          list_item_7:  _("Data-Security by centralized Server-Back-Up")
        },
        outro: _("Sign up for 90 Days free trial now !"),
        videos: {
          video_1: {
            thumb: "infostore_video.png",
            video: "teaser.swf"
          }
        },
        images: {
          image_1: {
            thumb: "infostore_small.png",
            image: "infostore_big.png"
          }
        },
        buttons: {
          trial: {
            content: _("Sign-Up for trial"),
            action: "upsell._get_purchase_method('trial')"
          },
          buy: {
            content: _("Buy"),
            action: "upsell._get_purchase_method('buy')"
          }
        },
        checkboxes: {
          invite: {
            content: _("Invite all my colleagues"),
            action: "upsell._set_invite(this)"
          }
        }
      },
      //feature calender
      calendar: {
        name: ["sidepanel/tasks.share", "sidepanel/contacts.share", "sidepanel/calendar.share", "modules/portal","modules/calendar", "modules/calendar/freebusy", "modules/calendar/team", "modules/calendar/mini_calender", "modules/calendar/new/add_participants", "modules/calendar/new/remove_participants", "modules/calendar/new/add_attachment", "modules/calendar/new/delete_attachment","modules/tasks","modules/tasks/new/add_participants", "modules/tasks/new/remove_participants", "modules/tasks/new/add_attachment", "modules/tasks/new/delete_attachment", "configuration/mail/accounts/new", "sidepanel/premium"],
        title: _("Enhance your system with &#8222;Teamwork Capabilities&#8220;"),
        intro: _("Make your team successful and cooperate with each other <br>on tasks, shared files and your team-calendar!"),
        list: {
          list_item_1:  _("Team-Calendar"),
          list_item_2:  _("Tasks Delegation"),
          list_item_3:  _("Share Folders"),
          list_item_4:  _("Share Calendars")
        },
        outro: _("Sign up for 90 Days free trial now !"),
        videos: {
          video_1: {
            thumb: "calendar_video.png",
            video: "teaser.swf"
          }
        },
        images: {
          image_1: {
            thumb: "calendar_small.png",
            image: "calendar_big.png"
          }
        },
        buttons: {
          trial: {
            content: _("Sign-Up for trial"),
            action: "upsell._get_purchase_method('trial')"
          },
          buy: {
            content: _("Buy"),
            action: "upsell._get_purchase_method('buy')"
          }
        },
        checkboxes: {
          invite:{
            content: _("Invite all my colleagues"),
            action: "upsell._set_invite(this)"
          }
        }
      },
      //feature mobility
      mobility: {
        name: ["modules/usm/eas", "modules/mobility", "sidepanel/calendar.sync.mobile" , "sidepanel/contacts.sync.mobile"],
        title: _("Enhance your system with &#8222;Business Mobility&#8220;"),
        product_name: _("Mail Push or Mail Professional"),
        intro: _("With your &#8220;SmartPhone&#8221; you access all vital data<br>(mail, calendar, contacts, etc.) effortlessly and manageable on the spot."),
        list: {
          list_item_1:  _("Read and answer your mails."),
          list_item_2:  _("Accept appointment proposals."),
          list_item_3:  _("Organize your meetings."),
          list_item_4:  _("Add new addresses."),
          list_item_5:  _("Supported are: Windows Mobile 6.x, the Apple iPhone and Nokia S60 OS phones"),
          list_item_6:  _("Blackberry & Android phones are supported through additional Software-Clients.")
        },
        outro: _("Sign up for 90 Days free trial now !"),
        videos: {
          video_1: {
            thumb: "mobility_video.png",
            video: "teaser.swf"
          }
        },
        images: {
          image_1: {
            thumb: "mobility_small.png",
            image: "mobility_big.png"
          }
        },
        buttons: {
          trial: {
            content: _("Sign-Up for trial"),
            action: "upsell._get_purchase_method('trial')"
          },
          buy: {
            content: _("Buy"),
            action: "upsell._get_purchase_method('buy')"
          }
        },
        checkboxes: {
          invite:{
            content: _("Invite all my colleagues"),
            action: "upsell._set_invite(this)"
          }
        }
      },
      
      //feature outlook
      outlook: {
        name: ["modules/outlook", "sidepanel/sync.outlook", "modules/outlook.updater"],
        title: _("Enhance your system with &#8222;Outlook Oxtender&#8220;"),
        intro: _("If Outlook&copy; is your preferred choice,<br>use it as your local Groupware Client."),
        list: {
          list_item_1:  _("All your Appointments, Contacts, Mails, etc. get synchronized."),
          list_item_2:  _("Same look & feel locally and access via web-interface if needed."),
          list_item_3:  _("All your private, public and shared folders are accessable")
        },
        outro: _("Sign up for 90 Days free trial now !"),
        videos: {
          video_1: {
            thumb: "outlook_video.png",
            video: "teaser.swf"
          }
        },
        images: {
          image_1: {
            thumb: "outlook_small.png",
            image: "outlook_big.png"
          }
        },
        buttons: {
          trial: {
            content: _("Sign-Up for trial"),
            action: "upsell._get_purchase_method('trial')"
          },
          buy: {
            content: _("Buy"),
            action: "upsell._get_purchase_method('buy')"
          }
        },
        checkboxes: {
          invite:{
            content: _("Invite all my colleagues"),
            action: "upsell._set_invite(this)"
          }
        }
      },
      
      //order confirmation window
      order_confirm: {
        name: ["order_confirm"],
        title: _("Your provider processed your order successfully."),
        intro: _("After you press 'OK', an automatic Re-Login will be initiated, and the new feature is going to be available immediately"),
        buttons: {
          confirm: {
            content: _("Ok"),
            action: "upsell._do_reload()"
          }
        }
      },
      
      //order confirmation window
      error: {
        name: ["error"],
        title: _("Error."),
        intro: _("No Valid Purchasing method"),
        buttons: {
          cancel: {
            content: _("cancel"),
            action: "upsell._close_dialouge()"
          },
          back: {
            content: _("back"),
            action: "upsell._back(1)"
          }
        }
      }
    }
  },
  
  init: function (feature, win) {
    if(!upsell.config.init){
      
      
      
      //registration
      register("Feature_Not_Available", upsell.init);
      
      upsell._get_required_files();
      
      jQuery('.upsell-close').live('click', function() {
        upsell._close_dialouge();
      })
      
      jQuery('#upsell_window .detail_show').live('click', function() {
        
        if(jQuery("#upsell_window .detail").is(':visible')){
          jQuery('#upsell_window .detail').hide();
          jQuery(this).html(_('more'));
        } else {
          jQuery('#upsell_window .detail').show();
          jQuery(this).html(_('close'));      
        };
      });
      return false;
    };
    
    upsell._get_feature(feature);
  },
  
  _get_required_files: function(){
    jQuery.each(upsell.config.files, function(i, val){
      jQuery.each(val, function(ib, valb){
        if(i == "jss") {
          jQuery.getScript(upsell.config.path + i + "/" + valb.script, function(){
            upsell.config.init = true;
            eval(valb.action);
          });
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
  
  _get_feature: function(feature){
    upsell.config.history.push(feature);
    upsell.config.feature_internal = feature;
    jQuery.each(upsell.config.features, function(i, val){
      if(jQuery.inArray(feature, val.name) >= 0){
        upsell.config.feature = i;
        upsell._get_display_type();
        return false;
      }
    });
  },
  
  _get_display_type: function(){
    upsell._prepare_flash_content_for_ie(true);
    jQuery.getJSON(
      "/ajax/upsell/multiple?session="+parent.session+"&action=get_method",
      function(data){
        if(data.data.upsell_method === "direct"){
          jQuery.getJSON(
            "/ajax/upsell/multiple?session="+parent.session+"&action=get_static_redirect_url&feature_clicked=" + upsell.config.feature,
            function(data){
              upsell.config.hide_buttons = true;
              upsell.config.iframe_url = data.data.upsell_static_redirect_url;
              upsell._build_window();
            }
          )
        } else {
          upsell.config.hide_buttons = false;
          upsell.config.iframe_url = "";
          upsell._build_window();
        }
        
      }
    );
  },
  
  
  //builds window and content
  _build_window: function(){
    var data =
      '<div id="upsell_window">' +
        '<div id="headerSection">' +
           '<h2>' +
             upsell._get_title() +
             '<span>' +
               oxProductInfo.product_name +
             '</span>' +
           '</h2>' +
          '<a href="#" class="upsell-close"></a>' +
         '</div>' +
         '<div id="contentSection" class="contentSection">' +
           upsell._get_content() +
         '</div>' +
         '<div id="footerSection">' +
           upsell._get_inputs() +
         '</div>' +
       '</div>';
       
    if(jQuery('#upsell_window').length > 0){
      jQuery('#upsell_window').animate({
        opacity: 0
      }, 500, function(){
        jQuery('#upsell_window').html(jQuery(data).html()).animate({
          opacity: 1
        }, 500);
      });
    } else {
      jQuery.modal(data);
      jQuery('#upsell_window').css('opacity','0').animate({opacity: 1}, 500);
      jQuery('.simplemodal-overlay').css('opacity','0').animate({opacity: .8}, 500);
    }
    
  },
  
  //close current window
  _close_dialouge: function(){
    upsell._prepare_flash_content_for_ie(false);
    jQuery('#upsell_window').animate({opacity: 0}, 500);
    jQuery('.simplemodal-overlay').animate({opacity: 0}, 500, function(){
      jQuery.modal.close();
    });
  },
  
  //back history
  _back: function(steps){
    var history = upsell.config.history.reverse();
    upsell.init(history[steps]);
  },
  
  //builds required buttons from configuration
  _get_inputs: function(){
    
    var button = "";
    
    if(!upsell.config.hide_buttons){
      var feature = upsell.config.features[upsell.config.feature];

      if(feature.buttons){
        jQuery.each(feature.buttons, function(ib, valb){
          if(valb.content != "undefined"){
            button += '<a href="#" id="' + ib + '" class="btn"';
            button += 'onClick="' + valb.action + '"';
            button += '><span>';
            button += valb.content;
            button += '</span></a>';
          }
        });
      };
      if(feature.checkboxes){
        jQuery.each(feature.checkboxes, function(ib, valb){
          if(valb.content != "undefined"){
            button += '<span style="float: left; margin: 10px"><input type="checkbox" name="';
            button += ib;
            button += '"onClick="' + valb.action + '"';
            button += '" value="false">' + valb.content + '</span>';
          }
        });
      }
    }
    return button;
  },
  
  _get_content: function(){
    var feature = upsell.config.features[upsell.config.feature];
    
    var intro = "";
    var list = "";
    var outro = "";
    var videos = "";
    var images = "";
        
    if(feature.intro) {
      intro = '<b style="font-size: 14px">' + feature.intro + '</b><br>';
    };
    
    if(feature.list) {
      var list_count = 0;
      list += '<ul>';
      jQuery.each(
        feature.list, function(i, val){
          if( val == "" ) { return false; };
          list_count += 1;
          if (list_count > upsell.config.show_features_initial) {
            list += '<li class="detail" style="display: none">' + val + '</li>';
          } else {
            list += '<li>' + val + '</li>';
          }
		      
  		  }
	  	);

	  	list += '</ul>';

	  	if (list_count > upsell.config.show_features_initial) {
  	  	list += '<a style="display: block" href="#" class="detail_show">' + _("more") + '</a>';
  	  }
	  };
		
		if(feature.outro) {
      outro = '<b style="font-size: 14px; display: block; padding-top: 20px">' + feature.outro + '</b>';;
    };
		
		if(feature.videos) {
  		jQuery.each(
        feature.videos, function(i, val){
  		    videos +=
		        '<a href="' + upsell.config.path + 'templates/_' + upsell.config.feature + '/' + config.language + '/flash/' + val.video + '" class="light_box swf">' +
		          '<img src="' + upsell.config.path + 'templates/_' + upsell.config.feature + '/' + config.language + '/img/' + val.thumb + '" />' +
		        '</a>';
	  	  }
  		);
		};
		
		if(feature.images) {
  		jQuery.each(
        feature.images, function(i, val){
  		    images +=
		        '<a href="' + upsell.config.path + 'templates/_' + upsell.config.feature + '/' + config.language + '/img/' + val.image + '" class="light_box image">' +
		          '<img src="' + upsell.config.path + 'templates/_' + upsell.config.feature + '/' + config.language + '/img/' + val.thumb + '" />' +
		        '</a>';
	  	  }
  		);
		};
    
    if(upsell.config.iframe_url != "") {
      var content = '<iframe src="'+ upsell.config.iframe_url +'" width="100%" height="100%" style="border: none; margin: 0 0 -5px 0; clear: both;"></iframe>';
    } else {
      var content =
        '<div class="section_left">' +
	        intro +
	        list +
          outro +
        '</div>' +
        '<div class="section_right">' +
          videos +
          images +
        '</div>';
    }
    
    return content;
    
  },
  
  _get_title: function(){
    return upsell.config.features[upsell.config.feature].title;
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
    jQuery.getJSON(
      "/ajax/upsell/multiple?session="+parent.session+"&action=get_method",
      function(data){
        upsell.config.purchase_type = type;
        upsell._procces_purchase(data.data.upsell_method);
      }
    );
  },
  
  //initialize purchase
  _procces_purchase: function(data){
    switch (data) {
      case "email":
        jQuery.getJSON(
          "/ajax/upsell/multiple?session="+parent.session+"&action=send_upsell_email&feature_clicked=" + upsell.config.feature + "&purchase_type=" + upsell.config.purchase_type + "&invite=" + upsell.config.invite,
          function(data){
            upsell._do_purchase_mail(data);
          }
        );
        break;
      case "static":
        upsell._do_purchase_redirect();
        break;
      case "direct":
        jQuery.getJSON(
          "/ajax/upsell/multiple?session="+parent.session+"&action=get_static_redirect_url&feature_clicked=" + upsell.config.feature,
          function(data){
            upsell.config.iframe_url = data.data.upsell_static_redirect_url;
            upsell.init("iframe");
          }
        );
        break;
      default:
        upsell.init("error");
        break;
    }
  },
  
  //redirect static purchase
  _do_purchase_redirect: function(data){
	  jQuery.getJSON(
      "/ajax/upsell/multiple?session="+parent.session+"&action=get_static_redirect_url&feature_clicked=" + upsell.config.feature + "&purchase_type=" + upsell.config.purchase_type + "&invite=" + upsell.config.invite,
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
        upsell.init('order_confirm');
      }
    );
  },
  
  //reload and activate feature
  _do_reload: function(data){
    location.reload();
  },
  _prepare_flash_content_for_ie: function(flag){
   if (flag) {
      jQuery("iframe").each(
        function () {
          var n = jQuery(this), src = n.attr("src") + "";
          if (/^http/.test(src)) {
            n.data("visibility", n.css("visibility")).css("visibility", "hidden");
          }
        }
    	);
    } else {
      jQuery("iframe").each(
        function () {
          var n = jQuery(this), src = n.attr("src") + "";
          if (/^http/.test(src)) {
            n.css("visibility", n.data("visibility"));
          }
        }
      );
    }
   
   
  }
};


upsell.init();
