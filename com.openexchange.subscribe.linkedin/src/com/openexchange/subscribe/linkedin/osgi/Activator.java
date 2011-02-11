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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.subscribe.linkedin.osgi;

import java.util.ArrayList;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.linkedin.LinkedInService;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.linkedin.LinkedInSubscribeService;

public class Activator implements BundleActivator {

    private static final Log LOG = LogFactory.getLog(Activator.class);

    private BundleContext bundleContext;

    private final Stack<ServiceTracker> trackers = new Stack<ServiceTracker>();

    private ArrayList<ServiceRegistration> services;

    private OAuthServiceMetaData oAuthServiceMetadata;

    private LinkedInService linkedInService;

    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        services = new ArrayList<ServiceRegistration>();
        // react dynamically to the appearance/disappearance of LinkedinService
        trackers.push(new ServiceTracker(context, LinkedInService.class.getName(), new LinkedInServiceRegisterer(context, this)));

        // react dynamically to the appearance/disappearance of LinkedinService
        trackers.push(new ServiceTracker(context, OAuthServiceMetaData.class.getName(), new OAuthServiceMetaDataRegisterer(context, this)));

        for (final ServiceTracker tracker : trackers) {
            tracker.open();
        }
    }

    public void stop(BundleContext context) throws Exception {
        while (!trackers.isEmpty()) {
            trackers.pop().close();
        }
    }

    public void registerServices() {
        if (null != oAuthServiceMetadata && null != linkedInService){
            final LinkedInSubscribeService linkedInSubscribeService = new LinkedInSubscribeService(this);
            final ServiceRegistration serviceRegistration = bundleContext.registerService(
                SubscribeService.class.getName(),
                linkedInSubscribeService,
                null);
            services.add(serviceRegistration);
            LOG.info("LinkedInSubscribeService was started.");
        }
    }

    public void unregisterServices() {
        for (final ServiceRegistration serviceRegistration : services) {
            serviceRegistration.unregister();
        }
    }

    public OAuthServiceMetaData getOAuthServiceMetadata() {
        return oAuthServiceMetadata;
    }

    public void setOAuthServiceMetadata(OAuthServiceMetaData authServiceMetadata) {
        oAuthServiceMetadata = authServiceMetadata;
    }

    public LinkedInService getLinkedInService() {
        return linkedInService;
    }

    public void setLinkedInService(LinkedInService linkedInService) {
        this.linkedInService = linkedInService;
    }

}
