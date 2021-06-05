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

package com.openexchange.html.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;

/**
 * {@link WhitelistedSchemes}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class WhitelistedSchemes {

    private static volatile WhitelistedSchemes instance;

    /**
     * Initializes the instance using given service.
     *
     * @param configService The configuration service to use
     */
    public static void initInstance(ConfigurationService configService) {
        String schemesString = configService.getProperty("com.openexchange.html.sanitizer.allowedUrlSchemes", "http, https, ftp, ftps, mailto, tel");
        String[] schemas = Strings.splitByComma(schemesString);
        for (int i = 0; i < schemas.length; i++) {
            schemas[i] = Strings.asciiLowerCase(schemas[i]);
        }
        Set<String> schemes = new LinkedHashSet<String>(Arrays.asList(schemas));
        schemes.add("cid");
        schemes.add("data");
        instance = new WhitelistedSchemes(schemes);
    }

    /**
     * Drops the instance
     */
    public static void dropInstance() {
        instance = null;
    }

    /**
     * Gets the instance
     *
     * @return The instance or <code>null</code> if not initialized
     */
    public static WhitelistedSchemes getInstance() {
        return instance;
    }

    /**
     * Gets the list of white-listed schemes.
     *
     * @return The list of white-listed schemes
     */
    public static List<String> getWhitelistedSchemes() {
        WhitelistedSchemes ws = instance;
        return null == ws ? Collections.<String> emptyList() : ws.whitelistedSchemes;
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    private final List<String> whitelistedSchemes;

    /**
     * Initializes a new {@link WhitelistedSchemes}.
     */
    private WhitelistedSchemes(Collection<String> schemes) {
        super();
        whitelistedSchemes = ImmutableList.copyOf(schemes);
    }

    /**
     * Gets the list of white-listed schemes.
     *
     * @return The white-listed schemes.
     */
    public List<String> getSchemes() {
        return whitelistedSchemes;
    }

}
