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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider;

import java.io.IOException;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import com.openexchange.exception.OXException;

/**
 * {@link OAuthProviderService} - The OAuth provider service in addition to <a href="http://oauth.googlecode.com/">Google's OAuth Java
 * library</a>.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface OAuthProviderService {

    /**
     * The OAuth validator.
     */
    public static final OAuthValidator VALIDATOR = new SimpleOAuthValidator();

    /**
     * The identifier of the default provider.
     */
    public static final int DEFAULT_PROVIDER = 0;

    /*
     * Property names
     */

    /**
     * Property name <code>"authorized"</code>
     */
    public static final String PROP_AUTHORIZED = "authorized";

    /**
     * Property name <code>"user"</code>
     */
    public static final String PROP_USER = "user";

    /**
     * Property name <code>"context"</code>
     */
    public static final String PROP_CONTEXT = "context";

    /**
     * Property name <code>"description"</code>
     */
    public static final String PROP_DESCRIPTION = "description";

    /**
     * Property name <code>"providerId"</code>
     */
    public static final String PROP_PROVIDER_ID = "providerId";

    /**
     * Property name <code>"name"</code>
     */
    public static final String PROP_NAME = "name";

    /**
     * Property name <code>"id"</code>
     */
    public static final String PROP_ID = "id";

    /*
     * Methods
     */

    /**
     * Loads consumers from database
     * 
     * @throws OXException If loading consumers fails
     */
    public void loadConsumers() throws OXException;

    /**
     * Gets the consumer for specified OAuth request message.
     * 
     * @param requestMessage The request message
     * @return The associated consumer
     * @throws IOException If an I/O error occurs
     * @throws OAuthProblemException If an OAuth problem occurs
     */
    public OAuthConsumer getConsumer(final OAuthMessage requestMessage) throws IOException, OAuthProblemException;

    /**
     * Get the access token and token secret for the given oauth_token.
     * 
     * @param requestMessage The OAuth message providing oauth_token
     */
    public OAuthAccessor getAccessor(final OAuthMessage requestMessage) throws IOException, OAuthProblemException;

    /**
     * Marks specified access token as authorized.
     * 
     * @throws OXException If token cannot be marked as authorized
     */
    public void markAsAuthorized(final OAuthAccessor accessor, final int userId, final int contextId) throws OXException;

    /**
     * Generate a fresh request token and secret for a consumer.
     * 
     * @param accessor The user-associated <tt>OAuthAccessor</tt> instance
     * @throws OXException If generation fails
     */
    public void generateRequestToken(final OAuthAccessor accessor, final int userId, final int contextId) throws OXException;

    /**
     * Generate an access token for a consumer.
     * 
     * @param accessor The user-associated <tt>OAuthAccessor</tt> instance
     * @throws OXException If generation fails
     */
    public void generateAccessToken(final OAuthAccessor accessor, final int userId, final int contextId) throws OXException;

}
