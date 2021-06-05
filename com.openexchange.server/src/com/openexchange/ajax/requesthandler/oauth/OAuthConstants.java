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

package com.openexchange.ajax.requesthandler.oauth;


/**
 * {@link OAuthConstants}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthConstants {

    /**
     * Prevent instantiation.
     */
    private OAuthConstants() {
        super();
    }

    public static final String BEARER_SCHEME = "Bearer";

    public static final String PARAM_OAUTH_ACCESS = "com.openexchange.oauth.access";

    /**
     * The Servlet alias sub-prefix for OAuth accesses:<br>
     * <pre>
     * [prefix] + "oauth/modules/" + [module]
     * </pre>
     * Example<br>
     * <code>"/ajax/<b>oauth/modules/</b>contacts"</code>
     * 
     * @deprecated Modules can now be accessed via the normal path
     */
    @Deprecated
    public static final String OAUTH_SERVLET_SUBPREFIX = "oauth/modules/";

}
