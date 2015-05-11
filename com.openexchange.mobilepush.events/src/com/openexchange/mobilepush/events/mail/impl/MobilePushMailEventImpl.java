/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.mobilepush.events.mail.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.mail.internet.InternetAddress;
import org.osgi.service.event.Event;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mobilepush.events.MailPushUtility;
import com.openexchange.mobilepush.events.MobilePushEvent;
import com.openexchange.mobilepush.events.MobilePushEventService;
import com.openexchange.mobilepush.events.MobilePushPublisher;
import com.openexchange.mobilepush.events.osgi.Services;
import com.openexchange.push.Container;
import com.openexchange.push.PushEventConstants;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link MobilePushMailEventImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MobilePushMailEventImpl implements org.osgi.service.event.EventHandler, MobilePushEventService {

    /** The logger constant for this class */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobilePushMailEventImpl.class);

    private static final String OX_EVENT = "OX_EVENT";
    private static final String INBOX = "INBOX";

    // --------------------------------------------------------------------------------------------------------------------------------

    private final List<MobilePushPublisher> publishers;

    /**
     * Initializes a new {@link MobilePushMailEventImpl}.
     */
    public MobilePushMailEventImpl() {
        super();
        this.publishers = new CopyOnWriteArrayList<MobilePushPublisher>();
    }

    @Override
    public void handleEvent(Event event) {
        if (null != event && PushEventConstants.TOPIC.equals(event.getTopic())) {
            ThreadPoolService threadPool = ThreadPools.getThreadPool();
            if (null == threadPool) {
                try {
                    doHandleEvent(event);
                } catch (Exception e) {
                    LOG.warn("Failed handling event {}", event, e);
                }
            } else {
                threadPool.submit(new MobilePushMailEventTask(event, this));
            }
        }
    }

    /**
     * Handles specified push event.
     *
     * @param event The push event
     */
    protected void doHandleEvent(Event event) {
        // Check event...
        Session session = getSession(event);
        if (session != null && markedForRemoteDistribution(event) && hasFolderProperty(event)) {
            // Build message for push provider
            List<Map<String, Object>> events = handleEvents(event, session);
            if (events != null) {
                int userId = session.getUserId();
                int contextId = session.getContextId();
                for (Map<String, Object> props : events) {
                    notifySubscribers(new MobilePushMailEvent(contextId, userId, props));
                }
            }
        } else {
            LOG.debug("Unable to handle incomplete event: {}", event);
        }
    }

    private boolean hasFolderProperty(Event event) {
        String folder = (String) event.getProperty(PushEventConstants.PROPERTY_FOLDER);
        if (false == Strings.isEmpty(folder)) {
            return true;
        }
        return false;
    }

    private boolean markedForRemoteDistribution(Event event) {
        Object commonEvent = event.getProperty(OX_EVENT);
        if (commonEvent instanceof CommonEvent) {
            return true;
        }
        return false;
    }

    private Session getSession(Event event) {
        Object obj = event.getProperty(PushEventConstants.PROPERTY_SESSION);
        if (obj instanceof Session) {
            Session session = (Session) obj;
            return session;
        }
        return null;
    }

    /**
     * Handles new mail or delete mail events
     *
     * @param event - The event to analyze
     * @param session - The session
     * @param props - A map
     */
    private List<Map<String, Object>> handleEvents(Event event, Session session) {
        Boolean isDeleted = (Boolean) event.getProperty(PushEventConstants.PROPERTY_DELETED);
        return null != isDeleted && isDeleted.booleanValue() ? getDeleteMailPayload() : getNewMailProperties(event, session);
    }

    private List<Map<String, Object>> getNewMailProperties(Event event, Session session) {
        List<Map<String, Object>> props = new ArrayList<Map<String, Object>>(3);

        if (event.containsProperty(OX_EVENT)) {
            int accountId = MailAccount.DEFAULT_ID;

            if (event.containsProperty(PushEventConstants.PROPERTY_CONTAINER)) {
                @SuppressWarnings("unchecked")
                Container<MailMessage> messageInfos = (Container<MailMessage>) event.getProperty(PushEventConstants.PROPERTY_CONTAINER);
                for (MailMessage mm : messageInfos) {
                    addNewMailProperties(mm, accountId, -1, props);
                }
            } else if (event.containsProperty(PushEventConstants.PROPERTY_IDS)) {
                // Check if its a new mail event
                String mailIds = (String) event.getProperty(PushEventConstants.PROPERTY_IDS);
                if (mailIds != null) {
                    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                    try {
                        mailAccess = Services.getService(MailService.class, true).getMailAccess(session, accountId);
                        mailAccess.connect(true);

                        MailMessage[] mms = fetchMessageInformation(mailAccess, mailIds);
                        int unread = mailAccess.getUnreadMessagesCount(INBOX);
                        if (mms != null) {
                            for (MailMessage mm : mms) {
                                addNewMailProperties(mm, accountId, unread, props);
                            }
                        }
                    } catch (OXException e) {
                        LOG.error("Failed to retrieve mail information.", e);
                    } finally {
                        if (mailAccess != null) {
                            try {
                                mailAccess.close();
                            } catch (Exception e) {
                                //Ignore
                            }
                        }
                    }
                }
            }
        }
        return props;
    }

    private void addNewMailProperties(MailMessage mm, int accountId, int unread, List<Map<String, Object>> props) {
        String subject = mm.getSubject();
        String[] senderInfo = getSenderInfo(mm);

        Map<String, Object> map = new HashMap<String, Object>(6);
        map.put(MailPushUtility.KEY_CID, generateCidFor(mm, accountId));
        map.put(MailPushUtility.KEY_SUBJECT, Strings.isEmpty(subject) ? "(no subject)" : subject);
        map.put(MailPushUtility.KEY_SENDER, Strings.isEmpty(senderInfo[0]) ? senderInfo[1] : senderInfo[0]);
        map.put(MailPushUtility.KEY_UNREAD, Integer.valueOf(unread < 0 ? mm.getUnreadMessages() : unread));
        props.add(map);
    }

    private String[] getSenderInfo(MailMessage m) {
        InternetAddress[] fromAddresses = m.getFrom();
        if (null == fromAddresses || fromAddresses.length <= 0) {
            return new String[] { null, "(no sender)" };
        }

        InternetAddress addr = fromAddresses[0];
        return new String[] { addr.getPersonal(), addr.getAddress() };
    }

    private String generateCidFor(MailMessage m, int accountId) {
        // E.g. "default0/INBOX:3412"
        StringBuilder sb = new StringBuilder(32);
        sb.append(MailFolder.DEFAULT_FOLDER_ID).append(accountId);
        sb.append('/').append(m.getFolder());
        sb.append(':').append(m.getMailId());
        return sb.toString();
    }

    /**
     * Fetches the message information from event mail id properties. If there are no property ids returned <code>null</code> is returned
     *
     * @return an array of mail messages or <code>null</code> if property does not exist or mail id does not exist
     * @throws OXException
     */
    private MailMessage[] fetchMessageInformation(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, String mailIds) throws OXException {
        MailField[] fields = new MailField[] { MailField.ID, MailField.FOLDER_ID, MailField.SUBJECT, MailField.FROM, MailField.FLAGS };
        return mailAccess.getMessageStorage().getMessages(INBOX, getMailIds(mailIds), fields);
    }

    private String[] getMailIds(String mailId) {
        return mailId == null ? null : Strings.splitByComma(mailId);
    }

    private List<Map<String, Object>> getDeleteMailPayload() {
        Map<String, Object> map = new HashMap<String, Object>(4);
        map.put("SYNC_EVENT", "MAIL");
        map.put("message", "refresh");
        return Collections.singletonList(map);
    }

    @Override
    public void registerPushPublisher(MobilePushPublisher publisher) {
        if (publishers.add(publisher)) {
            LOG.debug("Added successfully the push provider {}", publisher);
        }
    }

    @Override
    public void unregisterPushPublisher(MobilePushPublisher publisher) {
        if (publishers.remove(publisher)) {
            LOG.debug("Removed successfully the provider {}", publisher);
        }
    }

    @Override
    public void notifySubscribers(MobilePushEvent event) {
        if (event != null) {
            for (MobilePushPublisher publisher : publishers) {
                LOG.debug("Publishing event {} to publisher {}", event, publisher.getClass().getName());
                publisher.publish(event);
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private static class MobilePushMailEventTask extends AbstractTask<Void> {

        private final Event event;
        private final MobilePushMailEventImpl eventHandler;

        MobilePushMailEventTask(Event event, MobilePushMailEventImpl eventHandler) {
            super();
            this.event = event;
            this.eventHandler = eventHandler;
        }

        @Override
        public Void call() throws Exception {
            try {
                eventHandler.doHandleEvent(event);
            } catch (Exception e) {
                LOG.warn("Failed handling event {}", event, e);
            }
            return null;
        }
    }
}
