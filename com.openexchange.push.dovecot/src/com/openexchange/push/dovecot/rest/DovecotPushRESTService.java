
package com.openexchange.push.dovecot.rest;

import static com.openexchange.push.dovecot.locking.AbstractDovecotPushClusterLock.cancelFutureSafe;
import static com.openexchange.push.dovecot.locking.AbstractDovecotPushClusterLock.getOtherMembers;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.pns.DefaultPushNotification;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.PushNotificationField;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.push.Container;
import com.openexchange.push.PushEventConstants;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.push.dovecot.osgi.Services;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSession;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionRemoteLookUp;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * The {@link DovecotPushRESTService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
@Path("/preliminary/http-notify/v1/")
@RoleAllowed(Role.BASIC_AUTHENTICATED)
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

                    PushNotificationService pushNotificationService = Services.optService(PushNotificationService.class);
                    if (null != pushNotificationService) {
                        sendViaNotificationService(userId, contextId, uid, folder, data, pushNotificationService);
                    }

                    SessiondService sessiondService = Services.getService(SessiondService.class);
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

                    if (null == session) {
                        HazelcastInstance hzInstance = Services.optService(HazelcastInstance.class);
                        ObfuscatorService obfuscatorService = Services.optService(ObfuscatorService.class);
                        if (null != hzInstance && null != obfuscatorService) {
                            Cluster cluster = hzInstance.getCluster();

                            // Get local member
                            Member localMember = cluster.getLocalMember();

                            // Determine other cluster members
                            Set<Member> otherMembers = getOtherMembers(cluster.getMembers(), localMember);

                            if (!otherMembers.isEmpty()) {
                                IExecutorService executor = hzInstance.getExecutorService("default");
                                Map<Member, Future<PortableSession>> futureMap = executor.submitToMembers(new PortableSessionRemoteLookUp(userId, contextId), otherMembers);
                                for (Iterator<Entry<Member, Future<PortableSession>>> it = futureMap.entrySet().iterator(); null == session && it.hasNext();) {
                                    Future<PortableSession> future = it.next().getValue();
                                    // Check Future's return value
                                    int retryCount = 3;
                                    while (retryCount-- > 0) {
                                        try {
                                            PortableSession portableSession = future.get();
                                            retryCount = 0;
                                            if (null != portableSession) {
                                                portableSession.setPassword(obfuscatorService.unobfuscate(portableSession.getPassword()));
                                                session = portableSession;
                                            }
                                        } catch (InterruptedException e) {
                                            // Interrupted - Keep interrupted state
                                            Thread.currentThread().interrupt();
                                        } catch (CancellationException e) {
                                            // Canceled
                                            retryCount = 0;
                                        } catch (ExecutionException e) {
                                            Throwable cause = e.getCause();

                                            // Check for Hazelcast timeout
                                            if (!(cause instanceof com.hazelcast.core.OperationTimeoutException)) {
                                                if (cause instanceof RuntimeException) {
                                                    throw ((RuntimeException) cause);
                                                }
                                                if (cause instanceof Error) {
                                                    throw (Error) cause;
                                                }
                                                throw new IllegalStateException("Not unchecked", cause);
                                            }

                                            // Timeout while awaiting remote result
                                            if (retryCount <= 0) {
                                                // No further retry
                                                cancelFutureSafe(future);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (null == session) {
                        LOGGER.warn("Could not look-up an appropriate session for user {} in context {}. Hence cannot push 'new-message' event.", userId, contextId);
                    } else {
                        Map<String, Object> props = new LinkedHashMap<String, Object>(4);
                        props.put(PushEventConstants.PROPERTY_NO_FORWARD, Boolean.TRUE); // Do not redistribute through com.openexchange.pns.impl.event.PushEventHandler!
                        setEventProperties(uid, folder, data.optString("from", null), data.optString("subject", null), data.optInt("unread", -1), props);
                        PushUtility.triggerOSGiEvent(MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, "INBOX"), session, props, true, true);
                        LOGGER.info("Successfully parsed & triggered 'new-message' event for user {} in context {}", userId, contextId);
                    }
                }
            }

            return new JSONObject(2).put("success", true);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void sendViaNotificationService(int userId, int contextId, long uid, String folder, JSONObject data, PushNotificationService pushNotificationService) throws OXException {
        Map<String, Object> messageData = new LinkedHashMap<>(6);
        messageData.put(PushNotificationField.FOLDER.getId(), MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, folder));
        messageData.put(PushNotificationField.ID.getId(), Long.toString(uid));
        {
            String from = data.optString("from", null);
            if (null != from) {
                try {
                    messageData.put(PushNotificationField.MAIL_SENDER.getId(), QuotedInternetAddress.parseHeader(from, true)[0].toUnicodeString());
                } catch (AddressException e) {
                    messageData.put(PushNotificationField.MAIL_SENDER.getId(),  MimeMessageUtility.decodeEnvelopeSubject(from));
                }
            }
        }
        {
            String subject = data.optString("subject", null);
            if (null != subject) {
                messageData.put(PushNotificationField.MAIL_SUBJECT.getId(), MimeMessageUtility.decodeEnvelopeSubject(subject));
            }
        }
        {
            int unread = data.optInt("unread", -1);
            if (unread >= 0) {
                messageData.put(PushNotificationField.MAIL_UNREAD.getId(), Integer.valueOf(unread));
            }
        }

        DefaultPushNotification notification = DefaultPushNotification.builder()
            .contextId(contextId)
            .userId(userId)
            .topic(KnownTopic.MAIL_NEW.getName())
            .messageData(messageData)
            .build();
        pushNotificationService.handle(notification);
    }

    private void setEventProperties(long uid, String fullName, String from, String subject, int unread, Map<String, Object> props) {
        props.put(PushEventConstants.PROPERTY_IDS, Long.toString(uid));

        try {
            Container<MailMessage> container = new Container<MailMessage>();
            container.add(asMessage(uid, fullName, from, subject, unread));
            props.put(PushEventConstants.PROPERTY_CONTAINER, container);
        } catch (MessagingException e) {
            LOGGER.warn("Could not fetch message info.", e);
        }
    }

    private MailMessage asMessage(long uid, String fullName, String from, String subject, int unread) throws MessagingException {
        MailMessage mailMessage = new IDMailMessage(Long.toString(uid), fullName);
        mailMessage.addFrom(QuotedInternetAddress.parseHeader(from, true));
        mailMessage.setSubject(null == subject ? null : MimeMessageUtility.decodeEnvelopeSubject(subject));
        if (unread >= 0) {
            mailMessage.setUnreadMessages(unread);
        }
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

    private Session generateSessionFor(int userId, int contextId) {
        try {
            PushListenerService pushListenerService = services.getService(PushListenerService.class);
            return pushListenerService.generateSessionFor(new PushUser(userId, contextId));
        } catch (OXException e) {
            LOGGER.debug("Unable to generate a session", e);
            return null;
        } catch (RuntimeException e) {
            LOGGER.warn("Unable to generate a session", e);
            return null;
        }
    }

}
