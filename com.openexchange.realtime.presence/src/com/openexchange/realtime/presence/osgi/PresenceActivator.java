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

package com.openexchange.realtime.presence.osgi;

import org.apache.commons.logging.Log;
import org.osgi.framework.ServiceReference;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.presence.publish.PresencePublisher;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;

public class PresenceActivator extends HousekeepingActivator {

    private final static Log LOG = com.openexchange.log.Log.loggerFor(PresenceActivator.class);

    private PresencePublisher presencePublisher;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { MessageDispatcher.class, ResourceDirectory.class, PresenceSubscriptionService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: " + getClass().getCanonicalName());
        Services.setServiceLookup(this);
        ResourceDirectory resourceDirectory = getService(ResourceDirectory.class);
        if (resourceDirectory == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ResourceDirectory.class.getName());
        }
        presencePublisher = new PresencePublisher(resourceDirectory);

        track(ResourceDirectory.class, new SimpleRegistryListener<ResourceDirectory>() {

            @Override
            public void added(final ServiceReference<ResourceDirectory> ref, final ResourceDirectory resourceDirectory) {
                presencePublisher.setResourceDirectory(resourceDirectory);
            }

            @Override
            public void removed(final ServiceReference<ResourceDirectory> ref, final ResourceDirectory resourceDirectory) {
                presencePublisher.setResourceDirectory(null);
            }
        });
        openTrackers();
    }

    @Override
    public void stopBundle() throws Exception {
        LOG.info("Stopping bundle: " + getClass().getCanonicalName());
        super.stopBundle();
        Services.setServiceLookup(null);
    }

}
