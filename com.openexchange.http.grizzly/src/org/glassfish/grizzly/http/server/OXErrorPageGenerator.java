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

package org.glassfish.grizzly.http.server;


/**
 * {@link OXErrorPageGenerator} - The {@link ErrorPageGenerator} for creating Open-Xchange error pages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class OXErrorPageGenerator implements ErrorPageGenerator {

    private static final OXErrorPageGenerator instance = new OXErrorPageGenerator();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static OXErrorPageGenerator getInstance() {
        return instance;
    }

    // ----------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link OXErrorPageGenerator}.
     */
    private OXErrorPageGenerator() {
        super();
    }

    @Override
    public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
        return com.openexchange.tools.servlet.http.Tools.getErrorPage(status, reasonPhrase, description);
    }

}
