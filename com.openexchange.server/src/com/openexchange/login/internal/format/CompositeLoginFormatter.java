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

package com.openexchange.login.internal.format;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import com.openexchange.java.Strings;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;

/**
 * {@link CompositeLoginFormatter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CompositeLoginFormatter implements LoginFormatter {

    public static void main(String[] args) {
        CompositeLoginFormatter cp = new CompositeLoginFormatter("$u $c $s $agent $client ende", null);

        System.out.println(cp.loginFormatters);
    }

    private static List<LoginFormatter> parseFormat(final String format) {
        if (Strings.isEmpty(format)) {
            return null;
        }

        final Logger logger = org.slf4j.LoggerFactory.getLogger(CompositeLoginFormatter.class);
        try {
            final String frm = format.trim();
            final Pattern pattern = Pattern.compile("\\$[a-zA-Z]+");
            final Matcher m = pattern.matcher(frm);
            final List<LoginFormatter> formatters = new LinkedList<LoginFormatter>();
            int off = 0;
            while (m.find()) {
                final String id = m.group().substring(1);
                final TokenFormatter tokenFormatter = TokenFormatter.tokenFormatterFor(id);
                if (null == tokenFormatter) {
                    logger.warn("Invalid login format: \"{}\". Using default format.", format);
                    return null;
                }
                final String ws = frm.substring(off, m.start());
                if (ws.length() > 0) {
                    formatters.add(new StaticLoginFormatter(ws));
                }
                formatters.add(tokenFormatter);
                off = m.end();
            }
            if (off < frm.length()) {
                final String ws = frm.substring(off);
                if (ws.length() > 0) {
                    formatters.add(new StaticLoginFormatter(ws));
                }
            }
            return formatters;
        } catch (final Exception e) {
            logger.warn("Parsing format string failed. Using default format.", e);
            return null;
        }
    }

    // -------------------------------------------------------------------------------------- //

    private final List<LoginFormatter> loginFormatters;
    private final List<LoginFormatter> logoutFormatters;

    /**
     * Initializes a new {@link CompositeLoginFormatter}.
     */
    public CompositeLoginFormatter(final String loginFormat, final String logoutFormat) {
        this(parseFormat(loginFormat), parseFormat(logoutFormat));
    }

    /**
     * Initializes a new {@link CompositeLoginFormatter}.
     */
    public CompositeLoginFormatter(final List<LoginFormatter> loginFormatters, final List<LoginFormatter> logoutFormatters) {
        super();
        this.loginFormatters = null == loginFormatters || loginFormatters.isEmpty() ? null : loginFormatters;
        this.logoutFormatters = null == logoutFormatters || logoutFormatters.isEmpty() ? null : logoutFormatters;
    }

    /**
     * Gets the login formatters.
     *
     * @return The login formatters
     */
    public List<LoginFormatter> getLoginFormatters() {
        return loginFormatters;
    }

    /**
     * Gets the logout formatters.
     *
     * @return The logout formatters
     */
    public List<LoginFormatter> getLogoutFormatters() {
        return logoutFormatters;
    }

    @Override
    public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder logBuilder) {
        final List<LoginFormatter> loginFormatters = this.loginFormatters;
        if (null == loginFormatters) {
            DefaultLoginFormatter.getInstance().formatLogin(request, result, logBuilder);
        } else {
            for (final LoginFormatter formatter : loginFormatters) {
                formatter.formatLogin(request, result, logBuilder);
            }
        }
    }

    @Override
    public void formatLogout(final LoginResult result, final StringBuilder logBuilder) {
        final List<LoginFormatter> logoutFormatters = this.logoutFormatters;
        if (null == logoutFormatters) {
            DefaultLoginFormatter.getInstance().formatLogout(result, logBuilder);
        } else {
            for (final LoginFormatter formatter : logoutFormatters) {
                formatter.formatLogout(result, logBuilder);
            }
        }
    }

}
