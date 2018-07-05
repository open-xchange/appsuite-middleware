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

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.client.ClientConfig;

/**
 *
 * {@link ExportUserFeedback}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class ExportUserFeedback extends AbstractUserFeedback {

    private static final String EXPORT_PATH = "export";

    private static final String COLUMN_DELIMITER_LONG = "delimiter";
    private static final char COLUMN_DELIMITER_DEFAULT = ';';

    private Path path;

    /**
     * Invokes this command-line tool
     *
     * @param args The arguments
     */
    public static void main(String[] args) {
        new ExportUserFeedback().execute(args);
    }

    @Override
    protected void addOptions(Options options) {
        addGenericOptions(options);
        options.addOption(null, COLUMN_DELIMITER_LONG, true, "The column delimiter used. Default: " + COLUMN_DELIMITER_DEFAULT);
        options.addOption(null, ENDPOINT_LONG, true, " URL to an alternative HTTP API endpoint. Example: 'https://192.168.0.1:8443/userfeedback/v1/export/'");
    }

    @Override
    protected void checkOptions(CommandLine cmd) {}

    @Override
    protected String getFooter() {
        return "Exports collected user feedback into a file.";
    }

    @Override
    protected String getName() {
        return "exportuserfeedback -U myUser:myPassword [OPTIONS] output_file";
    }

    @Override
    protected String getHeader() {
        return "exportuserfeedback -U myUser:myPassword [-t type] [-g ctx_grp] [-s time] [-e time] [--delimiter ,] output_file\n" + "exportuserfeedback -s 1487348317 /tmp/feedback.csv";
    }

    @Override
    protected void checkArguments(CommandLine cmd) {
        if (cmd.getArgs().length < 1) {
            System.err.println("Please add a destination file for export.");
            System.exit(1);
            return;
        }
        setFilePath(cmd);

        if (Files.exists(path)) {
            System.err.println("File " + path.toString() + " does already exist! Please choose another location.");
            System.exit(1);
            return;
        }
    }

    private void setFilePath(CommandLine cmd) {
        String pathStr = cmd.getArgs()[0];
        String normalize = FilenameUtils.normalize(pathStr);
        if(normalize == null) {
            throw new IllegalArgumentException("Normalized destination file is null!!");
        }
        String normalizedPath = normalize.replaceFirst("^~", System.getProperty("user.home"));
        Path tmpPath = Paths.get(normalizedPath).normalize();
        path = Paths.get(tmpPath.toUri());
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, Builder builder) throws Exception {
        builder.accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_OCTET_STREAM_TYPE, MediaType.TEXT_PLAIN_TYPE, new MediaType("text", "csv"));
        builder.acceptEncoding("UTF-8");

        InputStream response = builder.get(InputStream.class);
        Files.copy(response, path);

        System.out.println("File successfully written to: " + path.toString());
        return null;
    }

    @Override
    protected WebTarget getEndpoint(CommandLine cmd) {
        String endpoint = cmd.getOptionValue(ENDPOINT_LONG, ENDPOINT_DEFAULT);
        endpoint = addPathIfRequired(endpoint, EXPORT_PATH);
        try {
            URI uri = new URI(endpoint);

            ClientConfig config = new ClientConfig();
            Client client = ClientBuilder.newClient(config);
            WebTarget baseTarget = client.target(uri);

            String contextGroup = cmd.getOptionValue(CONTEXT_GROUP_SHORT, CONTEXT_GROUP_DEFAULT);
            String type = cmd.getOptionValue(TYPE_SHORT, TYPE_DEFAULT);
            WebTarget target = baseTarget.path(contextGroup).path(type);
            target.request(MediaType.APPLICATION_OCTET_STREAM_TYPE).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE);

            if (cmd.hasOption(START_SHORT)) {
                target = target.queryParam("start", cmd.getOptionValue(START_SHORT).concat("000")); // convert seconds to ms
            }
            if (cmd.hasOption(END_SHORT)) {
                target = target.queryParam("end", cmd.getOptionValue(END_SHORT).concat("000")); // convert seconds to ms
            }
            if (cmd.hasOption(COLUMN_DELIMITER_LONG)) {
                target = target.queryParam("delimiter", cmd.getOptionValue(COLUMN_DELIMITER_LONG));
            }
            return target;
        } catch (URISyntaxException e) {
            System.err.print("Unable to return endpoint: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
