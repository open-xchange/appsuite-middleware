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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.realtime.client.groupchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.realtime.client.ID;
import com.openexchange.realtime.client.RTConnection;
import com.openexchange.realtime.client.RTConnectionFactory;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTConnectionProperties.RTConnectionType;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;
import com.openexchange.realtime.client.room.RTRoom;
import com.openexchange.realtime.client.room.RTRoomFacory;
import com.openexchange.realtime.client.room.chinese.ChineseRoomFactory;

/**
 * {@link RealtimeGroupChatClientCLT}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class RealtimeGroupChatClientCLT {

    public static RTRoom room;

    /**
     * Initializes a new {@link RealtimeGroupChatClientCLT}.
     */
    public RealtimeGroupChatClientCLT() {
        super();
    }

    public static void main(final String[] args) {
        final CommandLineParser parser = new PosixParser();
        String user = "";
        String hostname = "";
        try {
            final CommandLine cmd = parser.parse(toolkitOptions, args);
            if (cmd.hasOption('h')) {
                printHelp();
                System.exit(0);
            }
            if (cmd.hasOption('u')) {
                user = cmd.getOptionValue('u');
            } else {
                printOptionError("user");
                System.exit(1);
            }
            if (cmd.hasOption('f')) {
                hostname = cmd.getOptionValue('f');
            } else {
                printOptionError("hostname");
                System.exit(1);
            }

            UUID uuid = UUID.randomUUID();
            System.out.print("Password: ");
            String password = new BufferedReader(new InputStreamReader(System.in)).readLine();

            RTConnection connection = null;
            try {
                RTConnectionProperties properties = RTConnectionProperties.newBuilder(user, password, "GroupChatClient-" + uuid.toString())
                    .setConnectionType(RTConnectionType.LONG_POLLING)
                    .setHost(hostname)
                    .setSecure(false)
                    .build();
                connection = RTConnectionFactory.getInstance().newConnection(properties);
                RTRoomFacory roomFactory = new ChineseRoomFactory();
                room = roomFactory.newRoom(connection);
                room.join(new ID("synthetic.china://room1"), new RTMessageHandler() {
                    @Override
                    public void onMessage(JSONValue message) {
                        JSONObject array = message.toObject();
                        try {
                            String from = array.getString("from");
                            JSONObject payloads = array.getJSONArray("payloads").getJSONObject(0);
                            String data = payloads.getString("data");
                            System.out.println(from.substring(5, from.length()).split("/")[0] + ": " + data);
                        } catch (JSONException e) {
                            System.err.println("JSON error: " + e.getMessage());
                        }
                    }
                });
                Thread chat = new Thread(new Chat());
                chat.start();
                chat.join();
            } catch (RTException e) {
                throw e;
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (ParseException e) {
            System.err.println("Unable to parse command line: " + e.getMessage());
            printHelp();
            System.exit(2);
        } catch (RTException e) {
            System.err.println("RTException:");
            e.printStackTrace();
            System.exit(3);
        } catch (InterruptedException e) { //
        } catch (IOException e) {
            System.err.println("I/O error:");
            e.printStackTrace();
            System.exit(4);
        }
        System.exit(0);
    }

    private static final Options toolkitOptions;

    static {
        toolkitOptions = new Options();
        toolkitOptions.addOption("h", "help", false, "Prints a help text");

        toolkitOptions.addOption("u", "user", true, "Username");
        toolkitOptions.addOption("f", "hostname", true, "Host on which ox realtime is running");
    }

    private static void printHelp() {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("groupchat", toolkitOptions);
    }

    private static void printOptionError(String option) {
        System.err.println("Option " + option + " must be set.");
    }

    static class Chat implements Runnable {

        private boolean run = true;

        @Override
        public void run() {
            try {
                while (run) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));
                    String message = rd.readLine();
                    if ("/leave".equals(message)) {
                        run = false;
                    } else {
                        room.say(message);
                    }
                }
                room.leave();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } catch (RTException e) {
                System.err.println(e.getMessage());
            }
        }

    }

}
