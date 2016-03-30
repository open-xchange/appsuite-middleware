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
            } catch (final Exception exc) {
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
