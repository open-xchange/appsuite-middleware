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

package com.openexchange.cli;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;

/**
 *
 * {@link AbstractRestCLI}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 * @param <R>
 */
public abstract class AbstractRestCLI<R> extends AbstractAdministrativeCLI<R, Builder> {

    protected static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    protected static final String USER_LONG = "api-user";
    protected static final String USER_SHORT = "U";

    /**
     * Initializes a new {@link AbstractRestCLI}.
     */
    protected AbstractRestCLI() {
        super();
    }

    /**
     * Executes the command-line tool.
     *
     * @param args The arguments
     * @return The return value
     */
    @Override
    public R execute(final String[] args) {
        final Options options = newOptions();
        boolean error = true;
        try {
            // Option for help
            options.addOption("h", "help", false, "Prints a help text");
            if (requiresAdministrativePermission()) {
                Option user = new Option(USER_SHORT, USER_LONG, true, "Username and password to use for API authentication (user:password).");
                user.setRequired(true);
                options.addOption(user);
            }

            // Add other options
            addOptions(options);

            // Check if help output is requested
            if (showHelp(args)) {
                printHelp(options);
                System.exit(0);
                return null;
            }

            // Initialize command-line parser & parse arguments
            final CommandLineParser parser = new PosixParser();
            final CommandLine cmd = parser.parse(options, args);

            checkArguments(cmd);

            // Check other mandatory options
            checkOptions(cmd, options);

            WebTarget endpoint = getEndpoint(cmd);
            if (null == endpoint) {
                return null;
            }

            Builder request = endpoint.request();
            if (requiresAdministrativePermission()) {
                String authString = cmd.getOptionValue(USER_SHORT);
                String authorizationHeaderValue = "Basic " + Base64.encodeBase64String(authString.getBytes(Charsets.UTF_8));
                request.header(AUTHORIZATION_HEADER_NAME, authorizationHeaderValue);
            }
            R retval = invoke(options, cmd, request);
            error = false;
            return retval;
        } catch (final ParseException e) {
            System.err.println("Unable to parse command line: " + e.getMessage());
            printHelp(options);
        } catch (final MalformedURLException e) {
            System.err.println("URL to connect to server is invalid: " + e.getMessage());
        } catch (final IOException e) {
            System.err.println("Unable to communicate with the server: " + e.getMessage());
        } catch (final javax.ws.rs.NotAuthorizedException e) {
            System.err.println("Authorization not possible. Please check the provided credentials.");
        } catch (final javax.ws.rs.ProcessingException e) {
            System.err.println("Unable to reach provided endpoint: " + e.getMessage());
        } catch (final javax.ws.rs.InternalServerErrorException e) {
            System.err.println("An error occurred on endpoint side. Please check the server logs.");
        } catch (final javax.ws.rs.BadRequestException e) {
            System.err.println(printClientException(e, "The provided request parameters seem to be invalid. Please check them and additionally the server logs for further information."));
        } catch (final javax.ws.rs.NotFoundException e) {
            System.err.println(printClientException(e, "The requested resource cannot be found. Please check the provided parameters and additionally the server logs for further information."));
        } catch (final RuntimeException e) {
            String message = e.getMessage();
            String clazzName = e.getClass().getName();
            System.err.println("A runtime error occurred: " + (null == message ? clazzName : new StringBuilder(clazzName).append(": ").append(message).toString()));
        } catch (final Error e) {
            String message = e.getMessage();
            String clazzName = e.getClass().getName();
            System.err.println("A JVM problem occurred: " + (null == message ? clazzName : new StringBuilder(clazzName).append(": ").append(message).toString()));
        } catch (final Throwable t) {
            String message = t.getMessage();
            String clazzName = t.getClass().getName();
            System.err.println("A JVM problem occurred: " + (null == message ? clazzName : new StringBuilder(clazzName).append(": ").append(message).toString()));
        } finally {
            if (error) {
                System.exit(1);
            }
        }
        return null;
    }

    private String printClientException(ClientErrorException exception, String defaultMessage) {
        String result = defaultMessage;
        InputStream response = (InputStream) exception.getResponse().getEntity();
        if (response != null) {
            try {
                String parsedResponse = new String(IOUtils.toCharArray(new AsciiReader(response)));
                JSONObject errorJson = new JSONObject(parsedResponse);
                String errorMessage = (String) errorJson.get("error_desc");
                if (errorJson.hasAndNotNull("error_id")) {
                    StringBuilder sb = new StringBuilder(errorMessage);
                    result = sb.append(" Server log exception ID: ").append(errorJson.getString("error_id")).toString();
                } else {
                    result = Strings.isEmpty(errorMessage) ? defaultMessage : errorMessage;
                }
            } catch (IOException | JSONException e) {
                //do nothing
            }
        }

        return result;
    }

    private boolean showHelp(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h") || args[i].equals("--help")) {
                return true;
            }
        }
        return false;
    }

    protected abstract void checkArguments(CommandLine cmd);

    /**
     * Adds this command-line tool's options.
     * <p>
     * Note following options are reserved:
     * <ul>
     * <li>-h / --help
     * <li>-t / --host
     * <li>-p / --port
     * <li>-l / --login
     * <li>-s / --password
     * <li>-A / --adminuser
     * <li>-P / --adminpass
     * </ul>
     *
     * @param options The options
     */
    @Override
    protected abstract void addOptions(Options options);

    protected abstract WebTarget getEndpoint(CommandLine cmd);
}
