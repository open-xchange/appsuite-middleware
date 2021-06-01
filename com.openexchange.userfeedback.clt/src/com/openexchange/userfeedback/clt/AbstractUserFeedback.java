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

package com.openexchange.userfeedback.clt;

import org.apache.commons.cli.Options;
import com.openexchange.cli.AbstractRestCLI;
import com.openexchange.java.Strings;

/**
 * {@link AbstractUserFeedback}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class AbstractUserFeedback extends AbstractRestCLI<Void> {

    protected static final String END_LONG = "end-time";
    protected static final String END_SHORT = "e";

    protected static final String START_LONG = "start-time";
    protected static final String START_SHORT = "s";


    protected static final String CONTEXT_GROUP_LONG = "context-group";
    protected static final String CONTEXT_GROUP_SHORT = "g";
    protected static final String CONTEXT_GROUP_DEFAULT = "default";

    protected static final String TYPE_LONG = "type";
    protected static final String TYPE_SHORT = "t";
    protected static final String TYPE_DEFAULT = "star-rating-v1";

    protected static final String ENDPOINT_LONG = "api-root";
    protected static final String ENDPOINT_DEFAULT = "http://localhost:8009/userfeedback/v1/";

    /**
     * Adds generic options to the given {@link Options}
     *
     * @param options The {@link Options} object
     */
    protected void addGenericOptions(Options options) {
        options.addOption(TYPE_SHORT, TYPE_LONG, true, "The feedback type. Default: 'star-rating-v1'. Alternative value: 'nps-v1'.");
        options.addOption(CONTEXT_GROUP_SHORT, CONTEXT_GROUP_LONG, true, "The context group identifying the global DB where the feedback is stored. Default: 'default'.");
        options.addOption(START_SHORT, START_LONG, true, "Start time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given after this time is considered. If not set, all feedback up to -e is considered.");
        options.addOption(END_SHORT, END_LONG, true, "End time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given before this time is considered. If not set, all feedback since -s is considered.");
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    /**
     * Adds desired path to the given endpoint if not yet available.
     *
     * @param endpoint The customizable endpoint
     * @param path The path to suffix
     * @return the enhanced path
     */
    protected String addPathIfRequired(String endpoint, String path) {
        if (Strings.isEmpty(path)) {
            return endpoint;
        }

        StringBuilder builder = new StringBuilder(endpoint);

        if (endpoint.endsWith("/")) {
            if (endpoint.endsWith(path + "/")) {
                return endpoint;
            }
            builder.append(path + "/");
            return builder.toString();
        }
        if (endpoint.endsWith(path)) {
            builder.append("/");
            return builder.toString();
        }
        builder.append("/" + path + "/");
        return builder.toString();
    }
}
