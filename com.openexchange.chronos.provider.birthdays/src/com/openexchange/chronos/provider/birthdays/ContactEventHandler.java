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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.chronos.provider.birthdays;

import static com.openexchange.chronos.provider.birthdays.BirthdaysCalendarProvider.PROVIDER_ID;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.osgi.Tools.requireService;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.chronos.common.UpdateResultImpl;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarEventNotificationService;
import com.openexchange.context.ContextService;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ContactEventHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.0
 */
public class ContactEventHandler implements EventHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactEventHandler.class);

    /** The event topic used when a new contact was created */
    private static final String TOPIC_CREATE = "com/openexchange/groupware/contact/insert";

    /** The event topic used when a contact was updated */
    private static final String TOPIC_UPDATE = "com/openexchange/groupware/contact/update";

    /** The event topic used when a contact was deleted */
    private static final String TOPIC_DELETE = "com/openexchange/groupware/contact/delete";

    /** The handled event topics */
    public static final String[] TOPICS = { TOPIC_CREATE, TOPIC_UPDATE, TOPIC_DELETE };

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ContactEventHandler}.
     *
     * @param services A service lookup reference
     */
    public ContactEventHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void handleEvent(Event event) {
        if (null == event || null == event.getTopic() || event.containsProperty(CommonEvent.REMOTE_MARKER)) {
            LOG.debug("Skipping invalid event: {}", event);
            return;
        }
        CommonEvent commonEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);
        if (null == commonEvent) {
            LOG.info("Unable to handle event \"{}\" due to missing common event data, skipping.", event.getTopic());
            return;
        }
        Map<Integer, Set<Integer>> affectedUsersWithFolder = commonEvent.getAffectedUsersWithFolder();
        if (null == affectedUsersWithFolder || affectedUsersWithFolder.isEmpty()) {
            LOG.info("Unable to handle event \"{}\" due to incomplete information about affected users, skipping.", event.getTopic());
            return;
        }
        handle(event.getTopic(), commonEvent, affectedUsersWithFolder.keySet());
    }

    private void handle(String topic, CommonEvent event, Set<Integer> affectedUsers) {
        try {
            switch (topic) {
                case TOPIC_CREATE:
                    handleCreate(event.getContextId(), affectedUsers, (Contact) event.getActionObj());
                    break;
                case TOPIC_UPDATE:
                    handleUpdate(event.getContextId(), affectedUsers, (Contact) event.getOldObj(), (Contact) event.getActionObj());
                    break;
                case TOPIC_DELETE:
                    handleDelete(event.getContextId(), affectedUsers, (Contact) event.getActionObj());
                    break;
                default:
                    LOG.info("Skipping event handling for unexpected topic \"{}\".", topic);
                    break;
            }
        } catch (Exception e) {
            LOG.warn("Error handling event \"{}\": {}", topic, e.getMessage(), e);
        }
    }

    private void handleCreate(int contextId, Collection<Integer> affectedUserIds, Contact newContact) throws OXException {
        if (null != newContact.getBirthday()) {
            processNewBirthday(contextId, affectedUserIds, newContact);
        }
    }

    private void handleUpdate(int contextId, Collection<Integer> affectedUserIds, Contact originalContact, Contact updatedContact) throws OXException {
        if (null == originalContact.getBirthday()) {
            if (null != updatedContact.getBirthday()) {
                processNewBirthday(contextId, affectedUserIds, updatedContact);
            }
            return;
        } else if (null != updatedContact.getBirthday()) {
            if (false == originalContact.getBirthday().equals(updatedContact.getBirthday()) || containsChangesForEmailAlarms(originalContact, updatedContact)) {
                processChangedBirthday(contextId, affectedUserIds, updatedContact);
            }
            return;
        } else {
            processRemovedBirthday(contextId, affectedUserIds, originalContact);
        }
    }

    /**
     * Checks if the updated contact contains any relevant changes for the EMail alarm
     *
     * @param originalContact The original {@link Contact}
     * @param updatedContact The updated {@link Contact}
     * @return true if the updated {@link Contact} contains any relevant changes
     */
    private boolean containsChangesForEmailAlarms(Contact originalContact, Contact updatedContact) {
        return  equals(originalContact.getGivenName(), updatedContact.getGivenName()) == false ||
                equals(originalContact.getSurName(), updatedContact.getSurName()) == false ||
                equals(originalContact.getDisplayName(), updatedContact.getDisplayName()) == false ||
                equals(originalContact.getDepartment(), updatedContact.getDepartment()) == false ||
                originalContact.getParentFolderID() != updatedContact.getParentFolderID() ||
                equals(originalContact.getEmail1(), updatedContact.getEmail1()) == false;

    }

    private static boolean equals(String s1, String s2) {
        if (null == s1) {
            return null == s2;
        }
        if (null == s2) {
            return false;
        }
        return s1.equals(s2);
    }

    public void handleDelete(int contextId, Set<Integer> affectedUserIds, Contact deletedContact) throws OXException {
        if (null == deletedContact.getBirthday()) {
            return;
        }
        processRemovedBirthday(contextId, affectedUserIds, deletedContact);
    }

    private void processNewBirthday(int contextId, Collection<Integer> affectedUserIds, Contact contact) throws OXException {
        Context context = services.getService(ContextService.class).getContext(contextId);
        for (CalendarAccount account : getBirthdaysCalendarAccounts(context, affectedUserIds)) {
            com.openexchange.chronos.Event event = loadEvent(contact, context, account);
            insertDefaultAlarms(context, account, contact);
            notifyHandlers(context, account, event, contact);
        }
    }

    private com.openexchange.chronos.Event loadEvent(Contact contact, Context context, CalendarAccount account) throws OXException {
        com.openexchange.chronos.Event event = getEventConverter(account).getSeriesMaster(contact);
        return getAlarmHelper(context, account).applyAlarms(event);
    }

    private void processChangedBirthday(int contextId, Collection<Integer> affectedUserIds, Contact contact) throws OXException {
        Context context = services.getService(ContextService.class).getContext(contextId);
        for (CalendarAccount account : getBirthdaysCalendarAccounts(context, affectedUserIds)) {
            com.openexchange.chronos.Event event = loadEvent(contact, context, account);
            recreateAlarms(context, account, contact);
            notifyHandlers(context, account, event, contact);
        }
    }

    private void recreateAlarms(Context context, CalendarAccount account, Contact contact) throws OXException {
        deleteAlarms(context, account, contact);
        insertDefaultAlarms(context, account, contact);
    }

    private void processRemovedBirthday(int contextId, Collection<Integer> affectedUserIds, Contact contact) throws OXException {
        Context context = services.getService(ContextService.class).getContext(contextId);
        for (CalendarAccount account : getBirthdaysCalendarAccounts(context, affectedUserIds)) {
            com.openexchange.chronos.Event event = loadEvent(contact, context, account);
            deleteAlarms(context, account, contact);
            notifyHandlers(context, account, event, contact);
        }
    }

    private void deleteAlarms(Context context, CalendarAccount account, Contact contact) throws OXException {
        String eventId = getEventConverter(account).getEventId(contact);
        getAlarmHelper(context, account).deleteAlarms(eventId);
    }

    private void notifyHandlers(Context context, CalendarAccount account, com.openexchange.chronos.Event original, Contact contact) throws OXException {
        com.openexchange.chronos.Event updated = loadEvent(contact, context, account);
        CalendarEvent event = getEvent(context, account, original, updated);
        services.getService(CalendarEventNotificationService.class).notifyHandlers(event);
    }

    private CalendarEvent getEvent(Context context, CalendarAccount account, com.openexchange.chronos.Event original, com.openexchange.chronos.Event updated) throws OXException {
        return new BirthdayCalendarEvent(   context.getContextId(),
                                            account.getAccountId(),
                                            account.getUserId(),
                                            Collections.singletonMap(account.getUserId(), Collections.singletonList(BasicCalendarAccess.FOLDER_ID)),
                                            Collections.emptyList(),
                                            Collections.singletonList(new UpdateResultImpl(original, updated)),
                                            Collections.emptyList(),
                                            null);
    }

    private void insertDefaultAlarms(Context context, CalendarAccount account, Contact contact) throws OXException {
        AlarmHelper alarmHelper = getAlarmHelper(context, account);
        if (alarmHelper.hasDefaultAlarms()) {
            alarmHelper.insertDefaultAlarms(getEventConverter(account).getSeriesMaster(contact));
        }
    }

    private AlarmHelper getAlarmHelper(Context context, CalendarAccount account) {
        return new AlarmHelper(services, context, account);
    }

    private EventConverter getEventConverter(CalendarAccount account) {
        return new EventConverter(services, Locale.US, account.getUserId());
    }

    private List<CalendarAccount> getBirthdaysCalendarAccounts(Context context, Collection<Integer> affectedUserIds) throws OXException {
        AdministrativeCalendarAccountService accountService = requireService(AdministrativeCalendarAccountService.class, services);
        return accountService.getAccounts(context.getContextId(), I2i(affectedUserIds), PROVIDER_ID);
    }

}
