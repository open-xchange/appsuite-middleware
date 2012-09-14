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
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;
import com.openexchange.realtime.presence.subscribe.database.SubscriptionsSQL;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link SubscriptionServiceImpl}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class SubscriptionServiceImpl implements PresenceSubscriptionService {

    private ServiceLookup services;

    public SubscriptionServiceImpl(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public void subscribe(Presence subscription, ServerSession session) throws OXException {
        SubscriptionParticipant from = createParticipant(subscription.getFrom());
        SubscriptionParticipant to = createParticipant(subscription.getTo());

        Subscription sub = new Subscription(from, to, Subscription.State.pending);

        SubscriptionsSQL storage = new SubscriptionsSQL(services.getService(DatabaseService.class));
        storage.store(sub, session);

        MessageDispatcher messageDispatcher = services.getService(MessageDispatcher.class);
        messageDispatcher.send(subscription, session);
    }

    @Override
    public void approve(ID id, boolean subscribed, ServerSession session) throws OXException {
        SubscriptionParticipant from = createParticipant(id);
        SubscriptionParticipant to = new SubscriptionParticipant(session.getUserId(), session.getContextId());

        Subscription sub = new Subscription(from, to, subscribed ? Subscription.State.subscribed : Subscription.State.unsubscribed);

        SubscriptionsSQL storage = new SubscriptionsSQL(services.getService(DatabaseService.class));
        storage.store(sub, session);
    }

    @Override
    public List<ID> getSubscribers(ServerSession session) throws OXException {
        SubscriptionsSQL storage = new SubscriptionsSQL(services.getService(DatabaseService.class));

        SubscriptionParticipant to = new SubscriptionParticipant(session.getUserId(), session.getContextId());
        List<Subscription> subscriptions = storage.getFrom(to, session);
        List<ID> subscribers = new ArrayList<ID>();
        for (Subscription subscription : subscriptions) {
            if (subscription.getState() == Subscription.State.subscribed) {
                ID id = createID(subscription.getFrom());
                subscribers.add(id);
            }
        }
        return subscribers;
    }

    @Override
    public List<ID> getSubscriptions(ServerSession session) throws OXException {
        SubscriptionsSQL storage = new SubscriptionsSQL(services.getService(DatabaseService.class));

        SubscriptionParticipant from = new SubscriptionParticipant(session.getUserId(), session.getContextId());
        List<Subscription> subscriptions = storage.getTo(from, session);
        List<ID> subscribers = new ArrayList<ID>();
        for (Subscription subscription : subscriptions) {
            if (subscription.getState() == Subscription.State.subscribed) {
                ID id = createID(subscription.getTo());
                subscribers.add(id);
            }
        }
        return subscribers;
    }

    @Override
    public List<Presence> getPendingRequests(ServerSession session) throws OXException {
        SubscriptionsSQL storage = new SubscriptionsSQL(services.getService(DatabaseService.class));
        List<Subscription> pendings = storage.getPendingFor(
            new SubscriptionParticipant(session.getUserId(), session.getContextId()),
            session);
        List<Presence> presences = new ArrayList<Presence>();
        for (Subscription pending : pendings) {
            Presence presence = new Presence();
            presence.setFrom(createID(pending.getFrom()));
            presence.setTo(createID(pending.getTo()));
            presence.setState(Presence.Type.subscribe);
            presences.add(presence);
        }
        return presences;
    }

    @Override
    public void pushPendingRequests(ServerSession session) throws OXException {
        List<Presence> pendingRequests = getPendingRequests(session);
        MessageDispatcher messageDispatcher = services.getService(MessageDispatcher.class);
        for (Presence presence : pendingRequests) {
            messageDispatcher.send(presence, session);
        }
    }

    private SubscriptionParticipant createParticipant(ID id) {
        id = id.toGeneralForm();
        ContextService contextService = services.getService(ContextService.class);
        UserService userService = services.getService(UserService.class);

        try {
            int contextId = contextService.getContextId(id.getContext());
            int userId = userService.getUserId(id.getUser(), contextService.getContext(contextId));

            return new SubscriptionParticipant(userId, contextId);
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
        UserService userService = services.getService(UserService.class);

        Context context = contextService.getContext(participant.getCid());
        User user = userService.getUser(participant.getUserId(), context);

        return new ID(null, user.getLoginInfo(), context.getName(), null);
    }

}
