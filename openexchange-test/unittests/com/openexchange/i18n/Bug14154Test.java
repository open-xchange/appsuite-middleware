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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.i18n;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.server.services.I18nServices;
import junit.framework.TestCase;

/**
 * {@link Bug14154Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug14154Test extends TestCase {

    private static final Pattern pattern = Pattern.compile("(#.*?#)");

    private final Locale locale;

    public Bug14154Test(String name, Locale locale) {
        super(name);
        this.locale = locale;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Init.injectProperty();
        Init.startAndInjectConfigBundle();
        Init.startAndInjectI18NBundle();
    }

    @Override
    protected void tearDown() throws Exception {
        Init.dropI18NBundle();
        Init.dropConfigBundle();
        Init.dropProperty();
        super.tearDown();
    }

    public void testContainingPattern() {
        final I18nService i18nService = I18nServices.getInstance().getService(locale);
        assertNotNull("Can't get i18n service for " + locale.toString(), i18nService);
        final String translation = i18nService.getLocalized(MailStrings.FORWARD_PREFIX);
        assertNotNull("Mail forwarding template is not translated.", translation);
        Matcher matcher = pattern.matcher(MailStrings.FORWARD_PREFIX);
        while (matcher.find()) {
            String replacer = matcher.group();
            assertTrue("Translation does not contain the pattern " + replacer + ".", translation.indexOf(replacer) >= 0);
        }
    }
}
