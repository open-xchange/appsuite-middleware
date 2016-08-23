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

package com.openexchange.file.storage.boxcom;

import java.io.UnsupportedEncodingException;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.restclientv2.exceptions.BoxRestException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.boxcom.access.BoxOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link BoxClosure}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public abstract class BoxClosure<R> {

    /**
     * Initializes a new {@link BoxClosure}.
     */
    public BoxClosure() {
        super();
    }

    /**
     * Performs the actual operation
     *
     * @param boxAccess The Box.com access to use
     * @return The return value
     * @throws OXException If an Open-Xchange error occurred
     * @throws BoxRestException If a REST error occurred
     * @throws BoxServerException If a server error occurred
     * @throws AuthFatalFailureException If an authentication error occurred
     * @throws UnsupportedEncodingException If an encoding problem occurred
     */
    protected abstract R doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException;

    /**
     * Performs this closure's operation.
     *
     * @param resourceAccess The associated resource access or <code>null</code>
     * @param boxAccess The Box.com access to use
     * @param session The associated session
     * @return The return value
     * @throws OXException If operation fails
     */
    public R perform(AbstractBoxResourceAccess resourceAccess, BoxOAuthAccess boxAccess, Session session) throws OXException {
        return null == resourceAccess ? innerPerform(false, null, boxAccess, session) : innerPerform(true, resourceAccess, boxAccess, session);
    }

    /** Status code (401) indicating that the request requires HTTP authentication. */
    protected static final int SC_UNAUTHORIZED = 401;

    /** Status code (409) indicating that the request could not be completed due to a conflict with the current state of the resource. */
    protected static final int SC_CONFLICT = 409;

    private R innerPerform(boolean handleAuthError, AbstractBoxResourceAccess resourceAccess, BoxOAuthAccess boxAccess, Session session) throws OXException {
        try {
            return doPerform(boxAccess);
        } catch (BoxRestException e) {
            throw AbstractBoxResourceAccess.handleRestError(e);
        } catch (BoxServerException e) {
            if (handleAuthError && SC_UNAUTHORIZED == e.getStatusCode()) {
                BoxOAuthAccess newBoxAccess = resourceAccess.handleAuthError(e, session);
                return innerPerform(false, resourceAccess, newBoxAccess, session);
            }
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", Integer.valueOf(e.getStatusCode()) + " " + e.getMessage());
        } catch (AuthFatalFailureException e) {
            if (!handleAuthError) {
                throw FileStorageExceptionCodes.AUTHENTICATION_FAILED.create(e, resourceAccess.account.getId(), BoxConstants.ID, e.getMessage());
            }
            BoxOAuthAccess newBoxAccess = resourceAccess.handleAuthError(e, session);
            return innerPerform(false, resourceAccess, newBoxAccess, session);
        } catch (final UnsupportedEncodingException | RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
