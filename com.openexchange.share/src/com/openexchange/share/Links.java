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

package com.openexchange.share;

import java.net.URI;
import java.net.URISyntaxException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Strings;

/**
 * {@link Links} - Utility class for generating links.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class Links {

    /**
     * The <code>path</code> to set on an internal link
     */
    public static final String PATH = "/appsuite/ui";

    /**
     * The <code>app</code> segment to add to the fragment of an internal link
     */
    public static final String FRAGMENT_APP = "!&app=io.ox/";

    /**
     * Initializes a new {@link Links}.
     */
    private Links() {
        super();
    }

    /**
     * Generates a share link for an internal user with a concrete target to jump to.
     *
     * @param module The module identifier
     * @param folder The folder identifier
     * @param item The optional item identifier or <code>null</code>
     * @param hostData The host data
     * @return The internal share link
     */
    public static String generateInternalLink(String module, String folder, String item, HostData hostData) {
        try {
            StringBuilder fragment = new StringBuilder(64).append(FRAGMENT_APP).append(module).append("&folder=").append(folder);
            if (Strings.isNotEmpty(item)) {
                fragment.append("&id=").append(item);
            }

            return new URI(hostData.isSecure() ? "https" : "http", null, hostData.getHost(), -1, PATH, null, fragment.toString()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Building URI failed", e);
        }
    }

}
