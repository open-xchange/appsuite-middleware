define("com.openexchange.halo.json/main", ["osgi", "httpAPI"], function (osgi, httpAPI) {
   var Contact = Packages.com.openexchange.groupware.container.Contact;
   var ContactParser = Packages.com.openexchange.ajax.parser.ContactParser;
   osgi.services(["com.openexchange.halo.ContactHalo"], function (contactHalo) {
      httpAPI.defineModule("halo/contact", {
          services : function (req, session) {
              var retval = [];
              var providers = contactHalo.getProviders(session).iterator();
              while(providers.hasNext()) {
                  retval.push(providers.next()+"");
              }
              return retval;
          },
          investigate : function (req, session) {
              var contact = new Contact();
              var parser = new ContactParser();
              var provider = req.getParameter("provider");
              
              if (req.getData() != null) {
                  parser.parse(contact, req.getData());
                  var obj = req.getData();
                  var userId = obj.optInt("contact_id");
                  if (userId <= 0) {
                      userId = obj.optInt("internal_userid");
                  }
                  if (userId > 0) {
                      contact.setInternalUserId(userId);
                  }
              } else {
                  contact.setEmail1(req.getParameter("email1"));
                  contact.setEmail2(req.getParameter("email2"));
                  contact.setEmail3(req.getParameter("email3"));
                  if (req.isSet("internal_userid")) {
                      contact.setInternalUserId(java.lang.Integer.parseInt(req.getParameter("internal_userid")));
                  }
              }
              
              return contactHalo.investigate(provider, contact, req, session);
          }
      });
   });
});

