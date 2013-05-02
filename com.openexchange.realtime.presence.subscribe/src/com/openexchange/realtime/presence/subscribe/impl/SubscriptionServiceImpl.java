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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.presence.subscribe.impl;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;
import com.openexchange.realtime.presence.subscribe.database.SubscriptionsSQL;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.realtime.util.IdLookup;
import com.openexchange.realtime.util.IdLookup.UserAndContext;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

/**
 * {@link SubscriptionServiceImpl}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class SubscriptionServiceImpl implements PresenceSubscriptionService {

    private final ServiceLookup services;

    public SubscriptionServiceImpl(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public void subscribe(Presence subscription, String message) throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        if (databaseService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(DatabaseService.class.getName());
        }
        MessageDispatcher messageDispatcher = services.getService(MessageDispatcher.class);
        if(messageDispatcher == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(MessageDispatcher.class.getName());
        }
        ResourceDirectory resourceDirectory = services.getService(ResourceDirectory.class);
        if(resourceDirectory == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ResourceDirectory.class.getName());
        }
        
        SubscriptionParticipant from = createParticipant(subscription.getFrom().toGeneralForm());
        SubscriptionParticipant to = createParticipant(subscription.getTo().toGeneralForm());

        Subscription sub = new Subscription(from, to, Presence.Type.PENDING);
        sub.setRequest(message);
        SubscriptionsSQL storage = new SubscriptionsSQL(databaseService);
        storage.store(sub);
        // Lookup should be done with the general id, as protocol and resource don't matter 
        IDMap<Resource> idMap = resourceDirectory.get(subscription.getTo().toGeneralForm());
        messageDispatcher.send(subscription, idMap);
    }

    @Override
    public void approve(Presence approval) throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        if(databaseService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(DatabaseService.class.getName());
        }
        MessageDispatcher messageDispatcher = services.getService(MessageDispatcher.class);
        if(messageDispatcher == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(MessageDispatcher.class.getName());
        }
        ResourceDirectory resourceDirectory = services.getService(ResourceDirectory.class);
        if(resourceDirectory == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ResourceDirectory.class.getName());
        }
        
        /*
         * The subscriber (from), is the one who ask for approval to subscribe to the Presence of becomes the recipient of this approval.
         * The contact that approved the subscription Request is the to of this Subscription request.
         */
        SubscriptionParticipant from = createParticipant(approval.getTo());
        UserAndContext approvingUser = IdLookup.getUserAndContextIDs(approval.getFrom());
        SubscriptionParticipant to = new SubscriptionParticipant(approvingUser.getUserId(), approvingUser.getContextId());
        // TODO: Check for valid types
        Subscription sub = new Subscription(from, to, approval.getType());
        SubscriptionsSQL storage = new SubscriptionsSQL(databaseService);
        storage.store(sub);
        // Lookup should be done with the general id, as protocol and resource don't matter 
        IDMap<Resource> idMap = resourceDirectory.get(approval.getTo().toGeneralForm());
        messageDispatcher.send(approval, idMap);
    }

    @Override
    public List<ID> getSubscribers(ID id) throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        if(databaseService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(DatabaseService.class.getName());
        }
        SubscriptionsSQL storage = new SubscriptionsSQL(databaseService);
        UserAndContext userAndContextIds = IdLookup.getUserAndContextIDs(id);
        SubscriptionParticipant to = new SubscriptionParticipant(userAndContextIds.getUserId(), userAndContextIds.getContextId());
        List<Subscription> subscriptions = storage.getFrom(to);
        List<ID> subscribers = new ArrayList<ID>();
        for (Subscription subscription : subscriptions) {
            if (subscription.getState() == Presence.Type.SUBSCRIBED) {
                ID subscriberID = createID(subscription.getFrom());
                subscribers.add(subscriberID);
            }
        }
        return subscribers;
    }

    @Override
    public List<ID> getSubscriptions(ID id) throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        if(databaseService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(DatabaseService.class.getName());
        }
        
        SubscriptionsSQL storage = new SubscriptionsSQL(databaseService);
        UserAndContext userAndContextIds = IdLookup.getUserAndContextIDs(id);
        SubscriptionParticipant from = new SubscriptionParticipant(userAndContextIds.getUserId(), userAndContextIds.getContextId());
        List<Subscription> subscriptions = storage.getTo(from);
        List<ID> subscribers = new ArrayList<ID>();
        for (Subscription subscription : subscriptions) {
            if (subscription.getState() == Presence.Type.SUBSCRIBED) {
                ID subscribedID = createID(subscription.getTo());
                subscribers.add(subscribedID);
            }
        }
        return subscribers;
    }

    @Override
    public List<Presence> getPendingRequests(ID id) throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        if(databaseService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(DatabaseService.class.getName());
        }
        
        SubscriptionsSQL storage = new SubscriptionsSQL(databaseService);
        /*
         * Create Subscriptionparticipant by getting userid and contextID from realtimeID
         */
        UserAndContext userAndContextIDs = IdLookup.getUserAndContextIDs(id);
        List<Subscription> pendings = storage.getPendingFor(
            new SubscriptionParticipant(userAndContextIDs.getUserId(), userAndContextIDs.getContextId()));
        List<Presence> presences = new ArrayList<Presence>();
        for (Subscription pending : pendings) {
            Presence presence = new Presence();
            presence.setFrom(createID(pending.getFrom()));
            presence.setTo(createID(pending.getTo()));
            presence.setType(Presence.Type.SUBSCRIBE);
            presence.setMessage(pending.getRequest());
            presences.add(presence);
        }
        return presences;
    }

    @Override
    public void pushPendingRequests(ID id) throws OXException {
        MessageDispatcher messageDispatcher = services.getService(MessageDispatcher.class);
        if(messageDispatcher == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(MessageDispatcher.class.getName());
        }
        ResourceDirectory resourceDirectory = services.getService(ResourceDirectory.class);
        if(resourceDirectory == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ResourceDirectory.class.getName());
        }
        
        List<Presence> pendingRequests = getPendingRequests(id);
        for (Presence presence : pendingRequests) {
            // Lookup should be done with the general id, as protocol and resource don't matter
            IDMap<Resource> idMap = resourceDirectory.get(presence.getTo().toGeneralForm());
            messageDispatcher.send(presence, idMap);
        }
    }

    private SubscriptionParticipant createParticipant(ID id) {
        id = id.toGeneralForm();
        try {
            UserAndContext userAndContextIDs = IdLookup.getUserAndContextIDs(id);
            return new SubscriptionParticipant(userAndContextIDs.getUserId(), userAndContextIDs.getContextId());
        } catch (OXException e) {
            return new SubscriptionParticipant(id.toString());
        }
    }

    private ID createID(SubscriptionParticipant participant) throws OXException {
        String id = participant.getId();
        if (id != null) {
            return new ID(id);
        }

        ContextService contextService = services.getService(ContextService.class);
        if(contextService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ContextService.class.getName());
        }
        UserService userService = services.getService(UserService.class);
        if(userService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(UserService.class.getName());
        }
        
        Context context = contextService.getContext(participant.getCid());
        User user = userService.getUser(participant.getUserId(), context);

        return new ID(null, null, user.getLoginInfo(), context.getName(), null);
    }

}
