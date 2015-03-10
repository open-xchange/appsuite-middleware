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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import javax.mail.internet.InternetAddress;
import org.osgi.service.event.Event;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mobilepush.events.MailPushUtility;
import com.openexchange.mobilepush.events.MobilePushEvent;
import com.openexchange.mobilepush.events.MobilePushEventService;
import com.openexchange.mobilepush.events.MobilePushPublisher;
import com.openexchange.mobilepush.events.osgi.Services;
import com.openexchange.mobilepush.events.storage.ContextUsers;
import com.openexchange.mobilepush.events.storage.MobilePushStorageService;
import com.openexchange.push.PushEventConstants;
import com.openexchange.session.Session;

/**
 * {@link MobilePushMailEventImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobilePushMailEventImpl implements org.osgi.service.event.EventHandler, MobilePushEventService {

    /**
     * INBOX
     */
    private static final String INBOX = "INBOX";

    private final List<MobilePushPublisher> publishers;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobilePushMailEventImpl.class);

    /**
     * Initializes a new {@link MobilePushMailEventImpl}.
     */
    public MobilePushMailEventImpl() {
        super();
        this.publishers = new CopyOnWriteArrayList<MobilePushPublisher>();
    }

    @Override
    public void handleEvent(Event event) {
        /**
         * Check event
         */
        Session session = getSession(event);
        if (session == null) {
            LOG.debug("Unable to handle incomplete event: {}", event);
            return;
        }
        if (!isRemoteEvent(event)) {
            LOG.debug("Unable to handle incomplete event: {}", event);
            return;
        }
        if (!hasFolderProperty(event)) {
            LOG.debug("Unable to handle incomplete event: {}", event);
            return;
        }
        /**
         * Build publish message for push provider
         */
        int userId = session.getUserId();
        int contextId = session.getContextId();
        Map<String, Object> props = handleEvents(event, session);
        notifySubscribers(new MobilePushMailEvent(contextId, userId, props));
    }

    private boolean hasFolderProperty(Event event) {
        if (event != null && event.containsProperty(PushEventConstants.PROPERTY_FOLDER)) {
            String folder = (String) event.getProperty(PushEventConstants.PROPERTY_FOLDER);
            if (false == Strings.isEmpty(folder)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRemoteEvent(Event event) {
        if (event != null && event.containsProperty("OX_EVENT")) {
            Object commonEvent = event.getProperty("OX_EVENT");
            if (commonEvent instanceof CommonEvent) {
                return true;
            }
        }
        return false;
    }

    private Session getSession(Event event) {
        if (event != null && event.containsProperty(PushEventConstants.PROPERTY_SESSION)) {
            if (event.getProperty(PushEventConstants.PROPERTY_SESSION) instanceof Session) {
                Session session = (Session) event.getProperty(PushEventConstants.PROPERTY_SESSION);
                if (session != null) {
                    return session;
                }
            }
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
    private Map<String, Object> handleEvents(Event event, Session session) {
        if (!event.containsProperty(PushEventConstants.PROPERTY_DELETED) ||
            (event.containsProperty(PushEventConstants.PROPERTY_DELETED) && !(boolean) event.getProperty(PushEventConstants.PROPERTY_DELETED))) {
            return getNewMailProperties(event, session);
        } else {
            return getDeleteMailPayload(event);
        }
    }

    private Map<String, Object> getNewMailProperties(Event event, Session session) {
        Map<String, Object> props = new HashMap<String, Object>(9);
        props.put("SYNC_EVENT", "NEW_MAIL");
        props.put("title", "OX Mail");
        props.put("message", "You've received a new mail");
        props.put("msgcnt", "1");

        if (event != null && event.containsProperty("OX_EVENT") && event.containsProperty(PushEventConstants.PROPERTY_IDS)) {
            // check if its a new mail event
            String mailIds = (String) event.getProperty(PushEventConstants.PROPERTY_IDS);
            if (mailIds != null) {
                MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                try {
                    MailService mailService = Services.getService(MailService.class, true);
                    mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
                    mailAccess.connect(false);
                    MailMessage[] mms = fetchMessageInformation(mailAccess, mailIds, event, session);
                    int unread = mailAccess.getUnreadMessagesCount(INBOX);
                    if (mms != null) {
                        //TODO: currently catch only first message; ignore multiple messages
                        if (mms.length == 1) {
                            MailMessage mm = mms[0];
                            if (mm != null) {
                                String subject = mm.getSubject();
                                InternetAddress[] ia = mm.getFrom();
                                String personalFrom = ia[0].getPersonal();
                                String receivedFrom = ia[0].getAddress();
                                String mailId = mm.getMailId();
                                String folder = mm.getFolder();
                                props.put(MailPushUtility.KEY_PATH, folder + "://" + mailId);
                                props.put(MailPushUtility.KEY_SUBJECT, subject == null ? "(no subject)" : subject);
                                props.put(MailPushUtility.KEY_SENDER, personalFrom == null ? receivedFrom : personalFrom);
                                props.put(MailPushUtility.KEY_UNREAD, unread);
                            }
                        }
                    }
                } catch (OXException e) {
                    LOG.error("An unexpected mail exception occured", e);
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
        return props;
    }

    /**
     * Fetches the message information from event mail id properties. If there are no property ids returned <code>null</code> is returned
     *
     * @param event - The event
     * @param session - The session
     * @return an array of mail messages or <code>null</code> if property does not exist or mail id does not exist
     * @throws OXException
     */
    private MailMessage[] fetchMessageInformation(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, String mailIds, Event event, Session session) throws OXException {
        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        return messageStorage.getMessages(INBOX, getMailIds(mailIds),
            new MailField[] { MailField.ID, MailField.FOLDER_ID, MailField.SUBJECT, MailField.FROM });
    }

    private final static Pattern COMMA_SEPERATED_PATTERN = Pattern.compile("\\s*,\\s*");

    private String[] getMailIds(String mailId) {
        if (mailId != null) {
            return COMMA_SEPERATED_PATTERN.split(mailId);
        }
        return null;
    }

    private Map<String, Object> getDeleteMailPayload(Event event) {
        Map<String, Object> props = new HashMap<String, Object>(4);
        props.put("SYNC_EVENT", "MAIL");
        props.put("title", "OX Mail");
        props.put("message", "refresh");
        props.put("msgcnt", "1");
        return props;
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

    @Override
    public void notifyLogin(final List<ContextUsers> contextUsers) throws OXException {
        MobilePushStorageService mnss = Services.getService(MobilePushStorageService.class, true);

        //Currently blocked for seven days (configurable?)
        long timeToWait = 1000 * 60 * 60 * 24 * 7;
        mnss.blockLoginPush(contextUsers, timeToWait);

        Map<String, Object> props = getLoginMessagePayload();
        MobilePushMailEvent loginEvent = new MobilePushMailEvent(contextUsers, props);
        for (MobilePushPublisher publisher : publishers) {
            LOG.debug("Publishing new login event: {}", contextUsers);
            publisher.multiPublish(loginEvent);
        }
    }

    private Map<String, Object> getLoginMessagePayload() {
        Map<String, Object> props = new HashMap<String, Object>(4);
        props.put("SYNC_EVENT", "LOGIN");
        props.put("title", "OX Mail");
        props.put("message", "You've received a new login");
        props.put("msgcnt", "1");
        return props;
    }
}
