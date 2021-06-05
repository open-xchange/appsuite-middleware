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

package com.openexchange.security.manager.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;

/**
 * {@link PolicyFileParser} Parses a OSGi security policies for the {@link ConditionalPermissionAdmin} service.
 *
 * <br>
 * The policy syntax used is defined in the OSGIi spec: "50.2.5 Typical Example"
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.3
 */
public class PolicyFileParser {

    /*
     * ^(?!(?=#)(?=\/\/))(\b(DENY)|(ALLOW)\b).*?} (".*?")
     */
    private static final Pattern PATTERN = Pattern.compile("^(?!(?=#)(?=\\/\\/))(\\b(DENY)|(ALLOW)\\b).*?} (\".*?\")", Pattern.DOTALL | Pattern.MULTILINE);

    private final File file;

    /**
     * Initializes a new {@link PolicyFileParser}.
     *
     * @param file The file to parse the policies from. Each policy from the given file must follow the syntax defined in the OSGi spec (50.2.5 Typical Example).
     */
    public PolicyFileParser(File file) {
        this.file = file;
    }

    /**
     * Returns a list of all defined policies.
     *
     * @return A list of polices
     * @throws IOException
     */
    public List<String> readPolicies() throws IOException {
        String data = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        List<String> policies = new ArrayList<String>();
        Matcher matcher = PATTERN.matcher(data);
        while (matcher.find()) {
            String encodedPolicy = matcher.group().trim();
            policies.add(encodedPolicy);
        }

        return policies;
    }
}
