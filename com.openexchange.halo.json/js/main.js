define("com.openexchange.halo.json/main", ["osgi", "httpAPI"], function (osgi, httpAPI) {
   var Contact = Packages.com.openexchange.groupware.container.Contact;
   var ContactParser = Packages.com.openexchange.ajax.parser.ContactParser;
   console.log("loaded...");
   osgi.services(["com.openexchange.halo.ContactHalo"], function (contactHalo) {
      console.log("Services discovered");
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
              } else {
                  contact.setEmail1(req.getParameter("email1"));
              }
              
              return contactHalo.investigate(provider, contact, req, session);
          }
      });
   });
});

