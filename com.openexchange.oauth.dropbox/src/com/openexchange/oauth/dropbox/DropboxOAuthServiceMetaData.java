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

package com.openexchange.oauth.dropbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.scribe.builder.api.Api;
import org.scribe.model.Verb;
import com.openexchange.oauth.API;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.impl.AbstractExtendedScribeAwareOAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DropboxOAuthServiceMetaData}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxOAuthServiceMetaData extends AbstractExtendedScribeAwareOAuthServiceMetaData {

    private static final String IDENTITY_URL = "https://api.dropboxapi.com/2/users/get_current_account";
    private static final String IDENTITY_FIELD_NAME = "account_id";

    /**
     * Initializes a new {@link DropboxOAuthServiceMetaData}.
     */
    public DropboxOAuthServiceMetaData(ServiceLookup serviceLookup) {
        super(serviceLookup, KnownApi.DROPBOX, DropboxOAuthScope.values());
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return DropboxApi2.class;
    }

    @Override
    public API getAPI() {
        return KnownApi.DROPBOX;
    }

    @Override
    protected String getPropertyId() {
        return "dropbox";
    }

    @Override
    public boolean needsRequestToken() {
        return false;
    }

    @Override
    protected Collection<OAuthPropertyID> getExtraPropertyNames() {
        Collection<OAuthPropertyID> propertyNames = new ArrayList<OAuthPropertyID>(2);
        Collections.addAll(propertyNames, OAuthPropertyID.redirectUrl, OAuthPropertyID.productName);
        return propertyNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.impl.OAuthIdentityAware#getIdentityMethod()
     */
    @Override
    public Verb getIdentityHTTPMethod() {
        return Verb.POST;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.impl.OAuthIdentityAware#getIdentityURL()
     */
    @Override
    public String getIdentityURL(String accessToken) {
        return IDENTITY_URL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.impl.OAuthIdentityAware#getIdentityPattern()
     */
    @Override
    public String getIdentityFieldName() {
        return IDENTITY_FIELD_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.impl.AbstractScribeAwareOAuthServiceMetaData#getContentType()
     */
    @Override
    public String getContentType() {
        // Empty content-type otherwise the getUserIdentity call will fail.
        // 
        // The scribe library it tries to append a body when the request verb is
        // set to POST. The Dropbox API is picky and if the 'Content-Type' is
        // set to 'application/json', it then tries to interpret the body that is send
        // with the request. However, when getting the user's identity via the getIdentityURL link
        // no body is required... and none is sent... hence the fail on Dropbox's side.
        return EMPTY_CONTENT_TYPE;
    }
}
