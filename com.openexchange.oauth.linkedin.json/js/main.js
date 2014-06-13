define("com.openexchange.oauth.linkedin.json/main", ["osgi", "httpAPI"], function (osgi, httpAPI) {


    osgi.services(["com.openexchange.oauth.linkedin.LinkedInService", "com.openexchange.oauth.OAuthService"], function (li, oauth) {

        var utils = {
            getAccount: function (req, session) {
                var accounts = oauth.getAccounts("com.openexchange.socialplugin.linkedin", session, session.getUserId(), session.getContextId());
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
                    // Formerly: throw new Packages.com.openexchange.exception.OXException();
                	return;
                }
                var messages = li.getMessageInbox(session, session.getUserId(), session.getContextId(), account.getId());
                return new Packages.com.openexchange.ajax.requesthandler.AJAXRequestResult(messages, "json");
            },
            updates: function (req, session) {
                var account = utils.getAccount(req, session);
                if (!account) {
                     // Formerly: throw new Packages.com.openexchange.exception.OXException();
                     return;
                }
                var messages = li.getNetworkUpdates(session, session.getUserId(), session.getContextId(), account.getId());
                return new Packages.com.openexchange.ajax.requesthandler.AJAXRequestResult(messages, "json");
            },
            fullProfile: function (req, session) {
                var account = utils.getAccount(req, session);
                if (!account) {
                     // Formerly: throw new Packages.com.openexchange.exception.OXException();
                     return;
                }
                var id = req.getParameter("id");
                var profileData = li.getFullProfileById(id, session, session.getUserId(), session.getContextId(), account.getId());
                return new Packages.com.openexchange.ajax.requesthandler.AJAXRequestResult(profileData, "json");
            }
        });
    });
});

