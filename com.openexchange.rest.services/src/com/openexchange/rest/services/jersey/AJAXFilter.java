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

package com.openexchange.rest.services.jersey;

import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Path;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

/**
 * {@link AJAXFilter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
@Provider
@Priority(Priorities.USER)
@PreMatching
public class AJAXFilter implements ContainerRequestFilter {

    /** The placeholder to be used in a JAXRS {@link Path path annotation}, which will re-routed to for incoming requests starting with the configured Dispatcher prefix */
    public static final String AJAX_JAXRS_PLACEHOLDER_PREFIX = "ajaxplaceholder/";

    private final String prefix;

    /**
     * Initializes a new {@link AJAXFilter}.
     */
    public AJAXFilter(String prefix) {
        super();
        this.prefix = prefix.startsWith("/") ? prefix.substring(1) : prefix;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo();
        String path = uriInfo.getPath(); // Path is returned w/o starting '/' character
        if (path.startsWith(prefix)) {
            UriBuilder builder = uriInfo.getRequestUriBuilder().replacePath(AJAX_JAXRS_PLACEHOLDER_PREFIX + path.substring(prefix.length()));
            requestContext.setRequestUri(builder.build());
        }
    }

}
