



/**
 * These iframes are for the confixx integration within OX
 */
var innode = new ox.Configuration.InnerNode("configuration/com.openexchange.custom.dynamicnet", "Dynamic Net");

var spamfilter_config = new ox.Configuration.LeafNode("configuration/com.openexchange.custom.dynamicnet/spamfilter", "Spam Filter");
var spamfilter_iframe = new ox.Configuration.IFrame( spamfilter_config, "Spam Filter", '/plugins/com.openexchange.custom.dynamicnet/confixx_filter_redirect.html');

var autoresponder_config = new ox.Configuration.LeafNode("configuration/com.openexchange.custom.dynamicnet/autoresponder", "Autoresponder");
var autoresponder_iframe = new ox.Configuration.IFrame( autoresponder_config, "Autoresponder", '/plugins/com.openexchange.custom.dynamicnet/confixx_autoresponder_redirect.html');

var passwd_config = new ox.Configuration.LeafNode("configuration/com.openexchange.custom.dynamicnet/passwd", "Passwort");
var passwd_iframe = new ox.Configuration.IFrame( passwd_config, "Passwort", '/plugins/com.openexchange.custom.dynamicnet/confixx_passwd_redirect.html');




/**
 * Here we will handle our custom logout to the different login GUIs like https://sigma.ibone.ch
 */
 
 
 function getServername() {
          try {
              var xmlhttp = null;
              try {
                  xmlhttp = new XMLHttpRequest();
              } catch (e) {
                  try {
                      xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
                  } catch (e) {
                      xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
                  }
              }
		     // ask server for data we need
              xmlhttp.open("GET", "/ajax/iframe_info?session="+session, false);             
             
              xmlhttp.send("");
              var error;
              if (xmlhttp.status == 200) {
                  var reply = Function("return " + xmlhttp.responseText)();
                  if (!reply.error) {			
			var tmp_data =  reply.data.context_name ; // web999_sigma
			// now we need sigma as server name 
			var ctx_data = tmp_data.split("_");
			return ctx_data[1]; // sigma 
                  } else error = reply.error;
              } else error = xmlhttp.statusText;
          } catch (e) {
              error = e.message;
          }          
          
      }

var redirect_server = getServername();

logout_location = "[protocol]://"+redirect_server+".ibone.ch/loginform_ox.php";
sessionExpired_location = "[protocol]://"+redirect_server+".ibone.ch/loginform_ox.php";
directLink_location = "[protocol]://"+redirect_server+".ibone.ch/loginform_ox.php#m=[module]&f=[folder]&i=[object_id]";

