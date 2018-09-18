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
package com.openexchange.oidc;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;


/**
 * Contains all potential OpenID exception codes.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.4
 */
public enum OIDCExceptionCode implements OXExceptionCode {
    /**
     * Unable to find a configuration for the given OpenID Backend:  '%1$s'
     */
    MISSING_BACKEND_CONFIGURATION("Unable to find a configuration for the given OpenID Backend: '%1$s'", Category.CATEGORY_CONFIGURATION, 1),
    /**
     * Path contains invalid characters: '%1$s'
     */
    INVALID_BACKEND_PATH("Path contains invalid characters: '%1$s'", Category.CATEGORY_CONFIGURATION, 2),
    /**
     * A provided URI is corrupted: '%1$s', '%2$s'
     */
    CORRUPTED_URI("A provided URI is corrupted: '%1$s', '%2$s'", Category.CATEGORY_CONFIGURATION, 3),
    /**
     * Unable to create the authentication request, please check the provided backend configuration of backend: '%1$s'
     */
    UNABLE_TO_CREATE_AUTHENTICATION_REQUEST("Unable to create the authentication request, please check the provided backend configuration of backend: '%1$s'", Category.CATEGORY_ERROR, 4),
    /**
     * Unable to parse the given JWS algorithm parameter: '%1$s'
     */
    UNABLE_TO_PARSE_JWS_ALGORITHM("Unable to parse the given JWS algorithm parameter: '%1$s'", Category.CATEGORY_CONFIGURATION, 5), 
    /**
     * Users IDToken validation failed because of invalid claims or signature
     */
    IDTOKEN_VALIDATON_FAILED_CONTENT("Users IDToken validation failed because of invalid claims or signature. Reason: '%1$s'", Category.CATEGORY_PERMISSION_DENIED, 6),
    /**
     * Users IDToken validation failed because internal errors: '%1$s'
     */
    IDTOKEN_VALIDATON_FAILED("Users IDToken validation failed because internal errors: '%1$s'", Category.CATEGORY_ERROR, 7), 
    /**
     * Failed to get IDToken from IDP: '%1$s'
     */
    IDTOKEN_GATHERING_ERROR("Failed to get IDToken from IDP: '%1$s'", Category.CATEGORY_ERROR, 8), 
    /**
     * No user information available for the give state.
     */
    INVALID_AUTHENTICATION_STATE_NO_USER("No user information available for the give state.", Category.CATEGORY_ERROR, 9), 
    /**
     * Unable to load user information from IDP after valid authentication: '%1$s'
     */
    UNABLE_TO_LOAD_USERINFO("Unable to load user information from IDP after valid authentication: '%1$s'", Category.CATEGORY_ERROR, 10), 
    /**
     * Unable to send request to the IDP when trying to '%1$s'
     */
    UNABLE_TO_SEND_REQUEST("Unable to send request to the IDP when trying to '%1$s'", Category.CATEGORY_ERROR, 11), 
    /**
     * Unable to parse the IDP response, when trying to '%1$s'
     */
    UNABLE_TO_PARSE_RESPONSE_FROM_IDP("Unable to parse the IDP response, when trying to '%1$s'", Category.CATEGORY_ERROR, 12), 
    /**
     * Unable to parse the following URI: '%1$s'
     */
    UNABLE_TO_PARSE_URI("Unable to parse the following URI: '%1$s'", Category.CATEGORY_CONFIGURATION, 13), 
    /**
     * Received an invalid logout request: '%1$s'
     */
    INVALID_LOGOUT_REQUEST("Received an invalid logout request: '%1$s'", Category.CATEGORY_WARNING, 14), 
    /**
     * Unable to parse the IDToken which was transported with the session.
     */
    UNABLE_TO_PARSE_SESSIONS_IDTOKEN("Unable to parse the IDToken which was transported with the session.", Category.CATEGORY_WARNING, 15), 
    /**
     * Subject claim in IDToken is not valid. Subject: '%1$s'
     */
    BAD_SUBJECT("Subject claim in IDToken is not valid. Subject: '%1$s'", Category.CATEGORY_WARNING, 16), 
    /**
     * Unable to find the needed backend for a given session path: '%1$s'
     */
    UNABLE_TO_FIND_BACKEND_FOR_SESSION("Unable to find the needed backend for a given session path: '%1$s'", Category.CATEGORY_WARNING, 17), 
    /**
     * Unable to refresh the access token from IDP, because of: '%1$s'
     */
    UNABLE_TO_RELOAD_ACCESSTOKEN("Unable to refresh the access token from IDP, because of: '%1$s'", Category.CATEGORY_WARNING, 18), 
    /**
     * Unable to send login redirect, because of: '%1$s'
     */
    UNABLE_TO_SEND_REDIRECT("Unable to send login redirect, because of: '%1$s'", Category.CATEGORY_WARNING, 19), 
    /**
     * "Unable to handle third party login request, because of: '%1$s'"
     */
    INVALID_THIRDPARTY_LOGIN_REQUEST("Unable to handle third party login request, because of: '%1$s'", Category.CATEGORY_ERROR, 20),
    /**
     * "The OpenID-Provider responded with an error: '%1$s'"
     */
    OP_SERVER_ERROR("The OpenID-Provider responded with an error: '%1$s'", Category.CATEGORY_ERROR, 21),
    
    /**
     * "An error occured while accessing an IMap: '%1$s'"
     */
    HAZELCAST_EXCEPTION("An error occured while accessing an IMap: '%1$s'", Category.CATEGORY_ERROR, 22);
    
    private final String message;
    private final String displayMessage;
    private final int detailNumber;
    private final Category category;
    
    private OIDCExceptionCode(final String message, final Category category, final int detailNumber) {
        this(message, null, category, detailNumber);
    }
    
    private OIDCExceptionCode(final String message, final String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.category = category;
        this.detailNumber = detailNumber;
    }
    

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return this.detailNumber;
    }

    @Override
    public Category getCategory() {
        return this.category;
    }

    @Override
    public String getPrefix() {
        return "OIDC";
    }

    @Override
    public String getMessage() {
        return this.message;
    }
    
    public String getDisplaymessage() {
        return this.displayMessage;
    }
    
    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
