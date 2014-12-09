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

package com.openexchange.mobilepush.events.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.mail.service.MailService;
import com.openexchange.mobilepush.events.MobilePushEventService;
import com.openexchange.mobilepush.events.mail.impl.MobilePushMailEventImpl;
import com.openexchange.mobilepush.events.storage.MobilePushStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.PushEventConstants;

/**
 * {@link MobilePushEventsActivator}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobilePushEventsActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobilePushEventsActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { MobilePushStorageService.class, MailService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: {}", context.getBundle().getSymbolicName());
        Services.set(this);
        final MobilePushMailEventImpl service = new MobilePushMailEventImpl();

        //register event handler to listen on push events
        registerService(MobilePushEventService.class, service);
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, new String[] {
            PushEventConstants.TOPIC,
            PushEventConstants.PROPERTY_IMMEDIATELY,
            PushEventConstants.PROPERTY_IDS,
            PushEventConstants.PROPERTY_DELETED,
            PushEventConstants.PROPERTY_SESSION,
        });

        registerService(EventHandler.class, service, serviceProperties);
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: {}", context.getBundle().getSymbolicName());
        Services.set(null);
    }

}
