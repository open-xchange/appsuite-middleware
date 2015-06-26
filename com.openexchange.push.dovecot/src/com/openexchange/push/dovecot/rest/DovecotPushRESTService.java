
package com.openexchange.push.dovecot.rest;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.OXRESTService;
import com.openexchange.rest.services.annotations.PUT;
import com.openexchange.rest.services.annotations.ROOT;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * The {@link DovecotPushRESTService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
@ROOT("/http-notify/v1/")
public class DovecotPushRESTService extends OXRESTService<Void> {

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
    @PUT("/notify")
    public Object notifyMethod() throws OXException {
        Object data = request.getData();
        if (!(data instanceof JSONObject)) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        JSONObject jData = (JSONObject) data;
        if (jData.isEmpty()) {
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
