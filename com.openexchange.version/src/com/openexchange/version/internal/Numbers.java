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

package com.openexchange.version.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data object storing the version and build number from the manifest of this bundle.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Numbers {

    private static final String EXPRESSION = "([0-9]+)\\.([0-9]+)\\.([0-9]+)";
    private static final Pattern PATTERN = Pattern.compile(EXPRESSION);

    private final String version;
    private final String buildNumber;
    private final int major;
    private final int minor;
    private final int patch;

    public Numbers(String version, String buildNumber) throws Exception {
        super();
        this.version = version;
        this.buildNumber = buildNumber;
        Matcher matcher = PATTERN.matcher(version);
        if (matcher.find()) {
            try {
                major = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                throw new Exception("Can not parse major out of version \"" + version + "\".", e);
            }
            try {
                minor = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                throw new Exception("Can not parse minor out of version \"" + version + "\".", e);
            }
            try {
                patch = Integer.parseInt(matcher.group(3));
            } catch (NumberFormatException e) {
                throw new Exception("Can not parse patch out of version \"" + version + "\".", e);
            }
        } else {
            throw new Exception("Version pattern does not match on version string \"" + version + "\".");
        }
    }

    public String getVersion() {
        return version;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }
}
