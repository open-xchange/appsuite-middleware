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

package com.openexchange.control.console;

import com.openexchange.control.consoleold.AbstractJMXHandler;
import com.openexchange.control.consoleold.ConsoleException;
import com.openexchange.control.consoleold.internal.ValueObject;
import com.openexchange.control.consoleold.internal.ValuePairObject;
import com.openexchange.control.consoleold.internal.ValueParser;

/**
 * {@link AbstractConsoleHandler} - Abstract super class for console handlers.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractConsoleHandler extends AbstractJMXHandler {

    /**
     * The default parameter for host, port, login, and password: <code>&quot;-h&quot;</code>, <code>&quot;-p&quot;</code>,
     * <code>&quot;-l&quot;</code>, and <code>&quot;-pw&quot;</code>.
     */
    static final String[] DEFAULT_PARAMETER = { "-h", "-p", "-l", "-pw" };

    protected ValueParser valueParser;

    protected final void init(final String args[]) throws ConsoleException {
        init(args, false);
    }

    protected final void init(final String args[], final boolean noArgs) throws ConsoleException {
        String jmxHost = DEFAULT_HOST;
        int jmxPort = DEFAULT_PORT;
        String jmxLogin = null;
        String jmxPassword = null;

        if (!noArgs && args.length == 0) {
            showHelp();
            exit();
        } else {
            try {
                valueParser = new ValueParser(args, getParameter());
                final ValueObject[] valueObjects = valueParser.getValueObjects();
                for (ValueObject valueObject : valueObjects) {
                    if (valueObject.getValue().equals("-help") || valueObject.getValue().equals("--help")) {
                        showHelp();
                        exit(0);
                    }
                }

                final ValuePairObject[] valuePairObjects = valueParser.getValuePairObjects();
                for (ValuePairObject valuePairObject : valuePairObjects) {
                    if ("-h".equals(valuePairObject.getName())) {
                        jmxHost = valuePairObject.getValue();
                    } else if ("-p".equals(valuePairObject.getName())) {
                        jmxPort = Integer.parseInt(valuePairObject.getValue());
                    } else if ("-l".equals(valuePairObject.getName())) {
                        jmxLogin = valuePairObject.getValue();
                    } else if ("-pw".equals(valuePairObject.getName())) {
                        jmxPassword = valuePairObject.getValue();
                    }
                }

                initJMX(jmxHost, jmxPort, jmxLogin, jmxPassword);
            } catch (Exception exc) {
                throw new ConsoleException(exc);
            }
        }
    }

    protected abstract void showHelp();

    protected abstract void exit();

    protected void exit(int code) {
        System.exit(code);
    }

    protected abstract String[] getParameter();

    protected ValueParser getParser() {
        return valueParser;
    }
}
