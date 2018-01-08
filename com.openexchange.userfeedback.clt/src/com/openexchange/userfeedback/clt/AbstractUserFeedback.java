/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

    protected void addGenericOptions(Options options) {
        options.addOption(TYPE_SHORT, TYPE_LONG, true, "The feedback type to delete. Default: 'star-rating-v1'.");
        options.addOption(CONTEXT_GROUP_SHORT, CONTEXT_GROUP_LONG, true, "The context group identifying the global DB where the feedback is stored. Default: 'default'.");
        options.addOption(START_SHORT, START_LONG, true, "Start time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given after this time is deleted. If not set, all feedback up to -e is deleted.");
        options.addOption(END_SHORT, END_LONG, true, "End time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given before this time is deleted. If not set, all feedback since -s is deleted.");
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
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
