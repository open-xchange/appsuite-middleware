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

package com.openexchange.mobilenotifier.events.mail.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.service.event.Event;
import com.openexchange.event.CommonEvent;
import com.openexchange.mobilenotifier.events.MobileNotifyEvent;
import com.openexchange.mobilenotifier.events.MobileNotifyEventService;
import com.openexchange.mobilenotifier.events.MobileNotifyPublisher;
import com.openexchange.mobilenotifier.events.storage.ContextUsers;

/**
 * {@link MobileNotifyMailEventImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobileNotifyMailEventImpl implements org.osgi.service.event.EventHandler, MobileNotifyEventService {

    private final List<MobileNotifyPublisher> publishers;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobileNotifyMailEventImpl.class);
    /**
     * Initializes a new {@link MobileNotifyMailEventImpl}.
     */
    public MobileNotifyMailEventImpl() {
        super();
        this.publishers = new CopyOnWriteArrayList<MobileNotifyPublisher>();
    }

    @Override
    public void handleEvent(Event event) {
        if(false == checkEvent(event)) {
            LOG.debug("Unable to handle incomplete event: {}", event);
            return;
        }
        CommonEvent commonEvent = (CommonEvent) event.getProperty("OX_EVENT");
        Map<String, String> map = new HashMap<String, String>();
        map.put("SYNC_EVENT", "NEW_MAIL");

        notifySubscribers(new MobileNotifyMailEvent(commonEvent.getSession(), map));
    }

    private boolean checkEvent(Event event) {
        if(event != null && event.containsProperty("OX_EVENT") && event.containsProperty("com.openexchange.push.folder")) {
            CommonEvent ce =  (CommonEvent) event.getProperty("OX_EVENT");
            if(ce.getSession() != null && ce.getAction() == CommonEvent.INSERT) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void registerPushPublisher(MobileNotifyPublisher publisher) {
        if(publishers.add(publisher)) {
            LOG.debug("Added successfully the push provider {}", publisher);
        }
    }

    @Override
    public void unregisterPushPublisher(MobileNotifyPublisher publisher) {
        if(publishers.remove(publisher)) {
            LOG.debug("Removed successfully the provider {}", publisher);
        }
    }

    @Override
    public void notifySubscribers(MobileNotifyEvent event) {
        if(event != null) {
            for (MobileNotifyPublisher publisher : publishers) {
                LOG.debug("Publishing: {}", event);
                publisher.publish(event);
            }
        }
    }

    @Override
    public void notifyLogin(List<ContextUsers> contextUsers) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("SYNC_EVENT", "LOGIN");

        MobileNotifyMailEvent loginEvent = new MobileNotifyMailEvent(contextUsers, map);
        for (MobileNotifyPublisher publisher : publishers) {
            LOG.debug("Publishing new login event: {}", contextUsers);
            publisher.publishNewLogin(loginEvent);
        }
    }
}
