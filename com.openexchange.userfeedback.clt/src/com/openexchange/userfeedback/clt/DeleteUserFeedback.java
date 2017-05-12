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

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONObject;
import com.openexchange.cli.AbstractRestCLI;

/**
 * {@link DeleteUserFeedback}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.4
 */
public class DeleteUserFeedback extends AbstractRestCLI<Void> {

    private static final String END_LONG = "end-time";
    private static final String END_SHORT = "e";

    private static final String START_LONG = "start-time";
    private static final String START_SHORT = "s";

    private static final String CONTEXT_GROUP_LONG = "context-group";
    private static final String CONTEXT_GROUP_SHORT = "g";
    private static final String CONTEXT_GROUP_DEFAULT = "default";

    private static final String TYPE_LONG = "type";
    private static final String TYPE_SHORT = "t";
    private static final String TYPE_DEFAULT = "star-rating-v1";

    private static final String ENDPOINT_LONG = "api-root";
    private static final String ENDPOINT_DEFAULT = "http://localhost:8009/userfeedback/v1/";

    /**
     * @param args
     */
    public static void main(String[] args) {
        new DeleteUserFeedback().execute(args);
    }

    @Override
    protected void checkArguments(CommandLine cmd) {
        // nothing to do
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(TYPE_SHORT, TYPE_LONG, true, "The feedback type to delete. Default: 'star-rating-v1'.");
        options.addOption(CONTEXT_GROUP_SHORT, CONTEXT_GROUP_LONG, true, "The context group identifying the global DB where the feedback is stored. Default: 'default'.");
        options.addOption(START_SHORT, START_LONG, true, "Start time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given after this time is deleted. If not set, all feedback up to -e is deleted.");
        options.addOption(END_SHORT, END_LONG, true, "End time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given before this time is deleted. If not set, all feedback since -s is deleted.");
        options.addOption(null, ENDPOINT_LONG, true, " URL to an alternative HTTP API endpoint. Example: 'https://192.168.0.1:8443/userfeedback/v1'");
    }

    @Override
    protected WebTarget getEndpoint(CommandLine cmd) {
        String endpoint = cmd.getOptionValue(ENDPOINT_LONG, ENDPOINT_DEFAULT);
        try {
            URI uri = new URI(endpoint);

            ClientConfig config = new ClientConfig();
            Client client = ClientBuilder.newClient(config);
            WebTarget baseTarget = client.target(uri);

            String contextGroup = cmd.getOptionValue(CONTEXT_GROUP_SHORT, CONTEXT_GROUP_DEFAULT);
            String type = cmd.getOptionValue(TYPE_SHORT, TYPE_DEFAULT);
            WebTarget target = baseTarget.path(contextGroup).path(type);

            if (cmd.hasOption(START_SHORT)) {
                target = target.queryParam("start", cmd.getOptionValue(START_SHORT).concat("000")); // convert seconds to ms
            }
            if (cmd.hasOption(END_SHORT)) {
                target = target.queryParam("end", cmd.getOptionValue(END_SHORT).concat("000")); // convert seconds to ms
            }
            return target;
        } catch (URISyntaxException e) {
            System.err.print("Unable to return endpoint: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, Builder context) throws Exception {
        context.accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_OCTET_STREAM_TYPE, MediaType.TEXT_PLAIN_TYPE);
        String response = context.delete(String.class);
        JSONObject entity = new JSONObject(response);

        if (entity.getBoolean("successful")) {
            String result = "Feedback data deleted for type: " + entity.getString("type") + ", context group: " + entity.getString("contextGroup") + (entity.has("start") ? (", start time: " + entity.getLong("start") / 1000) : "") + (entity.has("end") ? (", end time: " + entity.getLong("end") / 1000) : "");
            System.out.println(result);
        }
        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        // nothing to do
    }

    @Override
    protected String getFooter() {
        return "Delete user feedback.";
    }

    @Override
    protected String getName() {
        return "deleteuserfeedback [OPTIONS]";
    }

    @Override
    protected String getHeader() {
        return "deleteuserfeedback [-t type] [-g ctx_grp] [-s time] [-e time]\n" + "deleteuserfeedback -s 1487348317";
    }
}
