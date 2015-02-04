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

package com.openexchange.oauth.twitter;

import java.util.Collection;
import java.util.Collections;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;
import org.slf4j.Logger;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.oauth.API;
import com.openexchange.oauth.AbstractScribeAwareOAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link OAuthServiceMetaDataTwitterImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OAuthServiceMetaDataTwitterImpl extends AbstractScribeAwareOAuthServiceMetaData {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OAuthServiceMetaDataTwitterImpl.class);

    /**
     * Initializes a new {@link OAuthServiceMetaDataTwitterImpl}.
     */
    public OAuthServiceMetaDataTwitterImpl(ServiceLookup services) {
        super(services, "com.openexchange.oauth.twitter", "Twitter");
    }

    @Override
    public String getDisplayName() {
        return "Twitter";
    }

    @Override
    public String getId() {
        return "com.openexchange.oauth.twitter";
    }

    @Override
    public API getAPI() {
        return API.TWITTER;
    }

    @Override
    public boolean registerTokenBasedDeferrer() {
        return true;
    }

    @Override
    public String modifyCallbackURL(final String callbackUrl, final String currentHost, final Session session) {
        if (null == callbackUrl) {
            return super.modifyCallbackURL(callbackUrl, currentHost, session);
        }

        final DeferringURLService deferrer = services.getService(DeferringURLService.class);
        if (null != deferrer && deferrer.isDeferrerURLAvailable(session.getUserId(), session.getContextId())) {
            final String retval = deferrer.getDeferredURL(callbackUrl, session.getUserId(), session.getContextId());
            LOGGER.debug("Initializing Twitter OAuth account for user {} in context {} with call-back URL: {}", session.getUserId(), session.getContextId(), retval);
            return retval;
        }

        final String retval = deferredURLUsing(callbackUrl, new StringBuilder(extractProtocol(callbackUrl)).append("://").append(currentHost).append('/').toString());
        LOGGER.debug("Initializing Twitter OAuth account for user {} in context {} with call-back URL: {}", session.getUserId(), session.getContextId(), retval);
        return retval;
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return TwitterApi.class;
    }

    @Override
    protected String getPropertyId() {
        return "twitter";
    }

    @Override
    protected Collection<OAuthPropertyID> getExtraPropertyNames() {
        return Collections.emptyList();
    }

}
