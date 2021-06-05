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

package com.openexchange.servlet;

import javax.servlet.Filter;
import javax.servlet.Servlet;


/**
 * {@link Constants} - Common {@link Servlet} constants.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public interface Constants {

    /**
     * Additional property used when registering {@link Filter} services to specify the paths that a {@link Filter} should be applied to.
     * <p>
     * This property may consist of path expressions including wildcards. The path property should be provided as:
     * <ol>
     *   <li>A single String for a single path</li>
     *   <li>An array of Strings</li>
     *   <li>A Collection of of Objects that provides the path via invocation of <cod>toString()</code></li>
     * </ol>
     * if the filter.path property is missing/null the filter will be used for every incoming request.
     * </p>
     * <p>
     * The form of a path must be one of:
     * <ol>
     *   <li><strong>*</strong>: This filter will be applied to all request</li>
     *   <li>The path starts with <strong>/</strong> and ends with the <strong>/*</strong> wildcard but doesn't equal <strong>/*</strong> e.g.
     *   <strong>/a/b/*</strong>: This filter will be used for requests to all URLs starting with <strong>/a/b</strong> e.g
     *   <strong>/a/b/c</strong>, <strong>/a/b/c/d</strong> and so on</li>
     * <li>The path starts with <strong>/</strong> but doesn't end with the <strong>/*</strong> wildcard: This filter will only be used for
     * requests that match this path exactly</li>
     * </ol>
     * </p>
     */
    public static final String FILTER_PATHS = "filter.paths";

    /**
     * The name of the HTTP session attribute containing the authenticated status of the Open-Xchange application.
     * <p>
     * An HTTP session is marked as authenticated by Open-Xchange application if this attribute is not <code>null</code> and equals to
     * {@link Boolean#TRUE}.
     */
    public static final String HTTP_SESSION_ATTR_AUTHENTICATED = "ox.authenticated";

    /**
     * The name of the HTTP session attribute containing the rate-limited status.
     * <p>
     * An HTTP session is marked as rate-limited by Open-Xchange application if this attribute is not <code>null</code> and equals to
     * {@link Boolean#TRUE}.
     */
    public static final String HTTP_SESSION_ATTR_RATE_LIMITED = "ox.rate-limited";

}
