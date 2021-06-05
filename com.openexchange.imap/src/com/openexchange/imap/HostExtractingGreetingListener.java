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

package com.openexchange.imap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.sun.mail.imap.GreetingListener;

/**
 * {@link HostExtractingGreetingListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class HostExtractingGreetingListener implements GreetingListener {

    private final Pattern pattern;

    /**
     * Initializes a new {@link HostExtractingGreetingListener}.
     */
    public HostExtractingGreetingListener(Pattern pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    public void onGreetingProcessed(String greeting, String host, int port) {
        if (Strings.isNotEmpty(greeting)) {
            Matcher matcher = pattern.matcher(greeting);
            if (matcher.find()) {
                int groupCount = matcher.groupCount();
                String endpoint = groupCount <= 0 ? matcher.group() : matcher.group(1);
                if (Strings.isNotEmpty(endpoint)) {
                    LogProperties.put(LogProperties.Name.MAIL_ENDPOINT, endpoint);
                }
            }
        }
    }

    @Override
    public String toString() {
        return pattern.toString();
    }

}
