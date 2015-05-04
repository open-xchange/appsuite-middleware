
package com.openexchange.push.dovecot.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * The {@link DovecotPushRESTService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
@Path("/http-notify/v1/")
public class DovecotPushRESTService {

    /**
     * Initializes a new {@link DovecotPushRESTService}.
     */
    public DovecotPushRESTService() {
        super();
    }

    /**
     * <pre>
     * PUT /rest/http-notify/v1/notify
     * &lt;JSON-content&gt;
     * </pre>
     *
     * Notifies about passed event.<br>
     */
    @PUT
    @Path("/notify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject notify(JSONObject data) throws OXException {
        if (data == null || data.isEmpty()) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        /*-
         * {
         *   "user":"4@464646669",
         *   "imap-uidvalidity":123412341,
         *   "imap-uid":2345,
         *   "folder":"INBOX",
         *   "event":"MessageNew",
         *   "from":"alice@barfoo.org",
         *   "subject":"Test",
         *   "snippet":"Hey guys\nThis is only a test..."
         * }
         */

        try {

            // TODO:
            // PushUtility.triggerOSGiEvent(MailFolderUtility.prepareFullname(accountId, fullName), session, this.additionalProps, true, true);

            return new JSONObject(2).put("success", true);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
