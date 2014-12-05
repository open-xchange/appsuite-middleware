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
import org.osgi.service.event.Event;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.mobilepush.events.MobilePushEvent;
import com.openexchange.mobilepush.events.MobilePushEventService;
import com.openexchange.mobilepush.events.MobilePushPublisher;
import com.openexchange.mobilepush.events.osgi.Services;
import com.openexchange.mobilepush.events.storage.ContextUsers;
import com.openexchange.mobilepush.events.storage.MobilePushStorageService;

/**
 * {@link MobilePushMailEventImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobilePushMailEventImpl implements org.osgi.service.event.EventHandler, MobilePushEventService {

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
        if(false == checkEvent(event)) {
            LOG.debug("Unable to handle incomplete event: {}", event);
            return;
        }
        CommonEvent commonEvent = (CommonEvent) event.getProperty("OX_EVENT");
        Map<String, String> map = new HashMap<String, String>();
        map.put("SYNC_EVENT", "NEW_MAIL");
        map.put("title", "OX Mail");
        map.put("message", "You've received a new mail");
        map.put("msgcnt", "1");
        int userId = commonEvent.getUserId();
        int contextId = commonEvent.getContextId();
        notifySubscribers(new MobilePushMailEvent(contextId, userId, map));
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
    public void registerPushPublisher(MobilePushPublisher publisher) {
        if(publishers.add(publisher)) {
            LOG.debug("Added successfully the push provider {}", publisher);
        }
    }

    @Override
    public void unregisterPushPublisher(MobilePushPublisher publisher) {
        if(publishers.remove(publisher)) {
            LOG.debug("Removed successfully the provider {}", publisher);
        }
    }

    @Override
    public void notifySubscribers(MobilePushEvent event) {
        if(event != null) {
            for (MobilePushPublisher publisher : publishers) {
                LOG.debug("Publishing: {}", event);
                publisher.publish(event);
            }
        }
    }

    @Override
    public void notifyLogin(final List<ContextUsers> contextUsers) throws OXException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("SYNC_EVENT", "LOGIN");
        map.put("title", "OX Mail");
        map.put("message", "You've received a new login");
        map.put("msgcnt", "1");

        MobilePushMailEvent loginEvent = new MobilePushMailEvent(contextUsers, map);

        MobilePushStorageService mnss = Services.getService(MobilePushStorageService.class);

        //Currently blocked for seven days (configurable?)
        long timeToWait = 1000 * 60 * 60 * 24 * 7;
        mnss.blockLoginPush(contextUsers, timeToWait);

        for (MobilePushPublisher publisher : publishers) {
            LOG.debug("Publishing new login event: {}", contextUsers);
            publisher.multiPublish(loginEvent);
        }
    }
}
