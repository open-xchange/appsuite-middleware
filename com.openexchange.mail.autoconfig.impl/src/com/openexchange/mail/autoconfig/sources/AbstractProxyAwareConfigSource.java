/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.autoconfig.sources;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.autoconfig.tools.Utils.OX_CONTEXT_ID;
import static com.openexchange.mail.autoconfig.tools.Utils.OX_USER_ID;
import static com.openexchange.rest.client.httpclient.util.HttpContextUtils.addCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import com.openexchange.server.ServiceLookup;

/**
 * Connects to the Mozilla ISPDB. For more information see <a
 * href="https://developer.mozilla.org/en/Thunderbird/Autoconfiguration">https://developer.mozilla.org/en/Thunderbird/Autoconfiguration</a>
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added google-common cache
 */
public abstract class AbstractProxyAwareConfigSource extends AbstractConfigSource {

    /** The OSGi service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractProxyAwareConfigSource}.
     *
     * @param services The service look-up
     */
    protected AbstractProxyAwareConfigSource(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Generated a {@link HttpContext} in which user and context identifiers
     * are set.
     *
     * @param context The context to set with identifier {@link #OX_CONTEXT_ID}
     * @param user The user to set with identifier {@link #OX_USER_ID}
     * @return A {@link HttpContext}
     */
    protected HttpContext httpContextFor(int context, int user) {
        BasicHttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(OX_CONTEXT_ID, I(context));
        httpContext.setAttribute(OX_USER_ID, I(user));
        addCookieStore(httpContext, context, user, getAccountId());
        return httpContext;
    }

    /**
     * Gets the account identifier used in the HTTP context
     *
     * @return The identifier
     */
    protected abstract String getAccountId();
}
