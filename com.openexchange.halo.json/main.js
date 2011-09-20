define("com.openexchange.halo.json/main", ["osgi", "httpAPI"], function (osgi, httpAPI) {
   var Contact = Packages.com.openexchange.groupware.container.Contact;
   var ContactParser = Packages.com.openexchange.ajax.parser.ContactParser;

   osgi.services(["com.openexchange.halo.ContactHalo"], function (contactHalo) {
      
      httpAPI.defineModule("halo/contact", {
          services : function (req, session) {
              return contactHalo.getProviders(session);
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