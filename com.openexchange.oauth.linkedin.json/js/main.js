define("com.openexchange.oauth.linkedin.json/main", ["osgi", "httpAPI"], function (osgi, httpAPI) {


    osgi.services(["com.openexchange.oauth.linkedin.LinkedInService", "com.openexchange.oauth.OAuthService"], function (li, oauth) {

        var utils = {
            getAccount: function (req, session) {
                var accounts = oauth.getAccounts("com.openexchange.socialplugin.linkedin", session.getPassword(), session.getUserId(), session.getContextId());
                if (accounts.isEmpty()) {
                    return null;
                }
                return accounts.get(0);
            }
        }
      
        httpAPI.defineModule("integrations/linkedin/portal", {
            inbox: function (req, session) {
                var account = utils.getAccount(req, session);
                if (!account) {
                    throw new Packages.com.openexchange.exception.OXException();
                }
                var messages = li.getMessageInbox(session.getPassword(), session.getUserId(), session.getContextId(), account.getId());
                return new Packages.com.openexchange.ajax.requesthandler.AJAXRequestResult(messages, "json");
            },
            updates: function (req, session) {
                var account = utils.getAccount(req, session);
                if (!account) {
                    throw new Packages.com.openexchange.exception.OXException();
                }
                var messages = li.getNetworkUpdates(session.getPassword(), session.getUserId(), session.getContextId(), account.getId());
                return new Packages.com.openexchange.ajax.requesthandler.AJAXRequestResult(messages, "json");
            },
            fullProfile: function (req, session) {
                var account = utils.getAccount(req, session);
                if (!account) {
                    throw new Packages.com.openexchange.exception.OXException();
                }
                var id = req.getParameter("id");
                var profileData = li.getFullProfileById(id, session.getPassword(), session.getUserId(), session.getContextId(), account.getId());
                return new Packages.com.openexchange.ajax.requesthandler.AJAXRequestResult(profileData, "json");
            }
        });
    });
});

