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

package com.openexchange.report.console;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;
import com.openexchange.report.Constants;
import com.openexchange.report.internal.LoginCounterMBean;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public final class LoginCounterTool extends AbstractMBeanCLI<Void> {

    private static final String SYNTAX = "logincounter [-a] -e <endDate> [-h] [-H <jmxHost>] [-l <jmxLogin>] [-p <jmxPort>] [-r <regex>] [--responsetimeout <timeout>] [-s <jmxPassword>] -t <startDate>";
    private static final String FOOTER = "";

    private Date startDate = null;
    private Date endDate = null;
    private String regex = null;

    /**
     * Prevent instantiation.
     */
    private LoginCounterTool() {
        super();
    }

    /**
     * Main method for starting from console.
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        new LoginCounterTool().execute(args);
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("t", "start", "startDate", "Required. Sets the start date for the detecting range. Example: 2009-12-31T00:00:00", true));
        options.addOption(createArgumentOption("e", "end", "endDate", "Required. Sets the end date for the detecting range. Example: 2010-01-1T23:59:59", true));
        options.addOption(createArgumentOption("r", "regex", "regex", "Optional. Limits the counter to login devices that match regex.", false));
        options.addOption(createSwitch("a", "aggregate", "Optional. Aggregates the counts by users. Only the total number of logins without duplicate counts (caused by multiple clients per user) is returned.", false));

    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        boolean error = true;
        try {
            writeNumberOfLogins(mbsc, startDate, endDate, cmd.hasOption('a'), regex);
            error = false;
        } finally {
            if (error) {
                System.exit(1);
            }
        }
        return null;
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.FALSE;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (!cmd.hasOption('t') || !cmd.hasOption('e')) {
            System.out.println("Parameters 'start' and 'end' are required.");
            printHelp();
            System.exit(1);
        }

        // Parse dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String source = unquote(cmd.getOptionValue('t'));
        try {
            startDate = sdf.parse(source);
        } catch (ParseException e) {
            System.out.println("Wrong format for parameter 'start': " + source + ". Expected in format of \"yyyy-MM-ddTHH:mm:ss\" (mind the double quotes)");
            printHelp();
            System.exit(1);
        }
        //asd
        if (null == endDate) {
            source = unquote(cmd.getOptionValue('e'));
            try {
                endDate = sdf.parse(source);
            } catch (ParseException e) {
                System.out.println("Wrong format for parameter 'end': " + source + ". Expected in format of \"yyyy-MM-ddTHH:mm:ss\" (mind the double quotes)");
                printHelp();
                System.exit(1);
            }
        }

        if (cmd.hasOption('r')) {
            regex = cmd.getOptionValue('r');
        }
    }

    @Override
    protected String getFooter() {
        return FOOTER;
    }

    @Override
    protected String getName() {
        return SYNTAX;
    }

    ////////////////////////////////// HELPERS ///////////////////////////////

    private static void writeNumberOfLogins(MBeanServerConnection mbsc, Date startDate, Date endDate, boolean aggregate, String regex) {
        String withRegex = "";
        if (regex != null) {
            withRegex += "\nfor expression\n    '" + regex + "'";
        }

        String andAggregated = "\n";
        if (aggregate) {
            andAggregated += "with total logins aggregated by users\n";
        }

        try {
            LoginCounterMBean loginCounterProxy = loginCounterProxy(mbsc);
            Map<String, Integer> logins = loginCounterProxy.getNumberOfLogins(startDate, endDate, aggregate, regex);

            System.out.println("Number of logins between\n    " + startDate.toString() + "\nand\n    " + endDate.toString() + withRegex + andAggregated);

            for (Entry<String, Integer> clientEntry : logins.entrySet()) {
                String client = clientEntry.getKey();
                if (client.equals(LoginCounterMBean.SUM)) {
                    continue;
                }
                Integer number = clientEntry.getValue();
                System.out.println(client + ": " + number);
            }

            Integer sum = logins.get(LoginCounterMBean.SUM);
            System.out.println("Total: " + sum);
        } catch (Exception e) {
            String errMsg = e.getMessage();
            System.out.println(errMsg != null ? errMsg : "An error occurred.");
        }
    }

    private static String unquote(final String s) {
        if (isEmpty(s) || s.length() <= 1) {
            return s;
        }
        String retval = s;
        char c;
        if ((c = retval.charAt(0)) == '"' || c == '\'') {
            retval = retval.substring(1);
        }
        final int mlen = retval.length() - 1;
        if ((c = retval.charAt(mlen)) == '"' || c == '\'') {
            retval = retval.substring(0, mlen);
        }
        return retval;
    }

    private static LoginCounterMBean loginCounterProxy(MBeanServerConnection mbsc) {
        return MBeanServerInvocationHandler.newProxyInstance(mbsc, Constants.LOGIN_COUNTER_NAME, LoginCounterMBean.class, false);
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    /**
     * High speed test for whitespace! Faster than the java one (from some testing).
     *
     * @return <code>true</code> if the indicated character is whitespace; otherwise <code>false</code>
     */
    private static boolean isWhitespace(final char c) {
        switch (c) {
            case 9: // 'unicode: 0009
            case 10: // 'unicode: 000A'
            case 11: // 'unicode: 000B'
            case 12: // 'unicode: 000C'
            case 13: // 'unicode: 000D'
            case 28: // 'unicode: 001C'
            case 29: // 'unicode: 001D'
            case 30: // 'unicode: 001E'
            case 31: // 'unicode: 001F'
            case ' ': // Space
                // case Character.SPACE_SEPARATOR:
                // case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                return true;
            default:
                return false;
        }
    }
}
