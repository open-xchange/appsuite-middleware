
package com.openexchange.push.dovecot.rest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.push.Container;
import com.openexchange.push.PushEventConstants;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * The {@link DovecotPushRESTService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
@Path("/http-notify/v1/")
public class DovecotPushRESTService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DovecotPushRESTService.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DovecotPushRESTService}.
     */
    public DovecotPushRESTService(ServiceLookup services) {
        super();
        this.services = services;
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
            if ("messageNew".equals(data.optString("event", null))) {
                int[] userAndContext = parseUserAndContext(data.optString("user", null));
                if (null != userAndContext) {
                    int contextId = userAndContext[1];
                    int userId = userAndContext[0];
                    String folder = data.getString("folder");
                    long uid = data.getLong("imap-uid");

                    SessiondService sessiondService = services.getService(SessiondService.class);
                    Session session = sessiondService.findFirstMatchingSessionForUser(userId, contextId, new SessionMatcher() {

                        @Override
                        public Set<Flag> flags() {
                            return SessionMatcher.NO_FLAGS;
                        }

                        @Override
                        public boolean accepts(Session session) {
                            return true;
                        }}
                    );
                    if (null == session) {
                        session = generateSessionFor(userId, contextId);
                    }

                    Map<String, Object> props = new LinkedHashMap<String, Object>(4);
                    setEventProperties(uid, folder, data.optString("from", null), data.optString("subject", null), props);
                    PushUtility.triggerOSGiEvent(MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, "INBOX"), session, props, true, true);
                }
            }

            return new JSONObject(2).put("success", true);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void setEventProperties(long uid, String fullName, String from, String subject, Map<String, Object> props) {
        props.put(PushEventConstants.PROPERTY_IDS, Long.toString(uid));

        try {
            Container<MailMessage> container = new Container<MailMessage>();
            container.add(asMessage(uid, fullName, from, subject));
            props.put(PushEventConstants.PROPERTY_CONTAINER, container);
        } catch (MessagingException e) {
            LOGGER.warn("Could not fetch message info.", e);
        }
    }

    private MailMessage asMessage(long uid, String fullName, String from, String subject) throws MessagingException {
        MailMessage mailMessage = new IDMailMessage(Long.toString(uid), fullName);
        mailMessage.addFrom(QuotedInternetAddress.parseHeader(from, true));
        mailMessage.setSubject(subject);
        return mailMessage;
    }

    private int[] parseUserAndContext(String userAndContext) {
        if (Strings.isEmpty(userAndContext)) {
            LOGGER.error("Missing user and context identifiers");
            return null;
        }
        int pos = userAndContext.indexOf('@');
        if (pos <= 0) {
            LOGGER.error("Could not parse user and context identifiers from \"{}\"", userAndContext);
            return null;
        }
        try {
            return new int[] { Integer.parseInt(userAndContext.substring(0, pos)), Integer.parseInt(userAndContext.substring(pos + 1)) };
        } catch (NumberFormatException e) {
            LOGGER.error("Could not parse user and context identifiers from \"{}\"", userAndContext, e);
            return null;
        }
    }

    private Session generateSessionFor(int userId, int contextId) throws OXException {
        PushListenerService pushListenerService = services.getService(PushListenerService.class);
        return pushListenerService.generateSessionFor(new PushUser(userId, contextId));
    }

}
