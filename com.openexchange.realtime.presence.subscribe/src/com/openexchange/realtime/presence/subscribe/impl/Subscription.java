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

import java.util.UUID;
import com.openexchange.realtime.packet.Presence;

/**
 * {@link Subscription} - Represents a Subscription from user1 to user2. User1 is the 'from' {@link SubscriptionParticipant} and user2 is
 * the 'to' {@link SubscriptionParticipant}. This {@link Subscription} can have various states during it's liftime e.g. First it's 'pending'
 * until user2 approves the request of user1 and turns the {@link Subscription} to SUBSCRIBED.
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Subscription {

    private SubscriptionParticipant from;

    private SubscriptionParticipant to;

    private String request;

    private Presence.Type state;
    
    private UUID uuid;

    /**
     * Initializes a new {@link Subscription}.
     * 
     * @param from The user requesting the {@link Subscription}
     * @param to The recipient of the {@link Subscription}
     * @param state The current state of the {@link Subscription}
     */
    public Subscription(SubscriptionParticipant from, SubscriptionParticipant to, Presence.Type state) {
        this.from = from;
        this.to = to;
        this.state = state;
        this.uuid = UUID.randomUUID();
    }
    
    /**
     * Initializes a new {@link Subscription}.
     * 
     * @param from The user requesting the {@link Subscription}
     * @param to The recipient of the {@link Subscription}
     * @param state The current state of the {@link Subscription}
     * @param uuid The uuid of the {@link Subscription}
     */
    public Subscription(SubscriptionParticipant from, SubscriptionParticipant to, Presence.Type state, UUID uuid) {
        this.from = from;
        this.to = to;
        this.state = state;
        this.uuid = uuid;
    }

    public SubscriptionParticipant getFrom() {
        return from;
    }

    public void setFrom(SubscriptionParticipant from) {
        this.from = from;
    }

    public SubscriptionParticipant getTo() {
        return to;
    }

    public void setTo(SubscriptionParticipant to) {
        this.to = to;
    }

    public Presence.Type getState() {
        return state;
    }

    public void setState(Presence.Type state) {
        this.state = state;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

}
