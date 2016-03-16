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

package com.openexchange.subscribe.linkedin.osgi;

import com.openexchange.context.ContextService;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.linkedin.LinkedInService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.linkedin.LinkedInSubscribeService;
import com.openexchange.subscribe.linkedin.groupware.LinkedInSubscriptionsOAuthAccountDeleteListener;

public class Activator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    private OAuthServiceMetaData oAuthServiceMetadata;

    private LinkedInService linkedInService;

    private ContextService contextService;

    @Override
    public void startBundle() throws Exception {

        // react dynamically to the appearance/disappearance of LinkedinService
        track(LinkedInService.class, new LinkedInServiceRegisterer(context, this));

        // react dynamically to the appearance/disappearance of OAuthServiceMetadata
        track(OAuthServiceMetaData.class, new OAuthServiceMetaDataRegisterer(context, this));

        // react dynamically to the appearance/disappearance of ContextService
        track(ContextService.class, new ContextServiceRegisterer(context, this));

        openTrackers();
    }

    @Override
    public void stopBundle() throws Exception {
        closeTrackers();
    }

    public void registerServices() {
        if (null != oAuthServiceMetadata && null != linkedInService && null != contextService){
            final LinkedInSubscribeService linkedInSubscribeService = new LinkedInSubscribeService(this);
            registerService(SubscribeService.class, linkedInSubscribeService);

            try {
                registerService(OAuthAccountDeleteListener.class, new LinkedInSubscriptionsOAuthAccountDeleteListener(linkedInSubscribeService, contextService));
            } catch (final Throwable t) {
                LOG.error("", t);
            }

            LOG.info("LinkedInSubscribeService was started.");
        }
    }

    @Override
    public void unregisterServices() {
        super.unregisterServices();
    }

    public OAuthServiceMetaData getOAuthServiceMetadata() {
        return oAuthServiceMetadata;
    }

    public void setOAuthServiceMetadata(final OAuthServiceMetaData authServiceMetadata) {
        oAuthServiceMetadata = authServiceMetadata;
    }

    public LinkedInService getLinkedInService() {
        return linkedInService;
    }

    public void setLinkedInService(final LinkedInService linkedInService) {
        this.linkedInService = linkedInService;
    }

    public void setContextService(final ContextService contexts) {
        this.contextService = contexts;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        // Nothing to do
        return null;
    }

}
