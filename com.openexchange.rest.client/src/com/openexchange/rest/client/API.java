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

package com.openexchange.rest.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.exception.RESTExceptionCodes;
import com.openexchange.rest.client.session.Session;

/**
 * {@link API}. Defines an Abstract REST API class, which encapsulates a {@link Session} and a {@link RequestAndResponse} object
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class API<S extends Session> {

    protected final S session;

    /**
     * Initializes a new {@link API}.
     * 
     * @throws OXException if the specified session is null.
     */
    public API(final S session) throws OXException {
        if (session == null) {
            throw RESTExceptionCodes.SESSION_NULL.create();
        }
        this.session = session;
    }

    /**
     * Returns the {@link Session} that this API is using.
     * 
     * @return The {@link Session} that this API is using.
     */
    public S getSession() {
        return session;
    }

    /**
     * Holds an {@link HttpUriRequest} and the associated {@link HttpResponse}.
     */
    public static final class RequestAndResponse {

        /** The request */
        public final HttpUriRequest request;

        /** The response */
        public final HttpResponse response;

        protected RequestAndResponse(HttpUriRequest request, HttpResponse response) {
            this.request = request;
            this.response = response;
        }
    }

    /**
     * Get the domain name of the API server
     * 
     * @return The domain name of the API server
     * @throws IllegalStateException if the subclass of <b>this</b> class does not implement and hence shadow/hide this method
     */
    public static String getServer() {
        throw new IllegalStateException("Subclass must implement and hence shadow this method.");
    }

    /**
     * Get the API version
     * 
     * @return The API version
     * @throws IllegalStateException if the subclass of <b>this</b> class does not implement and hence shadow/hide this method
     */
    public static int getVersion() {
        throw new IllegalStateException("Subclass must implement and hence shadow this method.");
    }

    /**
     * Get the user agent
     * 
     * @return The user agent
     * @throws IllegalStateException if the subclass of <b>this</b> class does not implement and hence shadow/hide this method
     */
    public static String getUserAgent() {
        throw new IllegalStateException("Subclass must implement and hence shadow this method.");
    }
}
