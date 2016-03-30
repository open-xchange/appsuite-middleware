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

package com.openexchange.i18n;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.groupware.Init;
import com.openexchange.server.services.I18nServices;
import com.openexchange.test.I18nTests;

/**
 * {@link TranslatedTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@RunWith(value = Parameterized.class)
public class TranslatedTest {

    private static final Class<?>[] I18N_CLASSES = new Class<?>[] {
        com.openexchange.calendar.itip.HRRStrings.class,
        com.openexchange.calendar.itip.Messages.class,
        com.openexchange.calendar.printing.CalendarPrintingStrings.class,
        com.openexchange.groupware.i18n.FolderStrings.class,
        com.openexchange.groupware.i18n.Groups.class,
        com.openexchange.groupware.i18n.MailStrings.class,
        com.openexchange.groupware.i18n.Notifications.class,
        com.openexchange.messaging.rss.FormStrings.class,
        com.openexchange.messaging.twitter.FormStrings.class,
        com.openexchange.publish.online.infostore.FormStrings.class,
        com.openexchange.publish.microformats.MicroformatStrings.class,
        com.openexchange.publish.microformats.FormStrings.class,
        com.openexchange.subscribe.crawler.internal.FormStrings.class,
        com.openexchange.subscribe.linkedin.FormStrings.class,
        com.openexchange.subscribe.microformats.FormStrings.class,
        com.openexchange.unifiedinbox.NameStrings.class
    };

    private final String className;
    private final Locale locale;
    private final String key;

    public TranslatedTest(String className, Locale locale, String key) {
        super();
        this.className = className;
        this.locale = locale;
        this.key = key;
    }

    @Parameters
    public static Collection<Object[]> data() throws InstantiationException, IllegalAccessException {
        final List<Object[]> retval = new ArrayList<Object[]>();
        for (final Locale locale : I18nTests.LOCALES) {
            for (final Class<?> clazz : I18N_CLASSES) {
                final Object instance = clazz.newInstance();
                for (final Field field : clazz.getFields()) {
                    if (String.class.isAssignableFrom(field.getType())) {
                        final String key = (String) field.get(instance);
                        retval.add(new Object[] { clazz.getName(), locale, key });
                    }
                }
            }
            final List<String> timeZoneIDs = new ArrayList<String>();
            for (String timeZoneID : TimeZone.getAvailableIDs()) {
                timeZoneIDs.add(timeZoneID);
            }
            removeUnwantedZones(timeZoneIDs);
            for (final String timeZoneID : timeZoneIDs) {
                retval.add(new Object[] { TimeZone.class.getName(), locale, timeZoneID.replace('_', ' ') });
            }
        }
        return retval;
    }

    @BeforeClass
    public static void startup() throws Exception {
        Init.injectProperty();
        Init.startAndInjectConfigBundle();
        Init.startAndInjectI18NBundle();
    }

    @AfterClass
    public static void shutdown() {
        Init.dropI18NBundle();
        Init.dropConfigBundle();
        Init.dropProperty();
    }

    @Test
    public void testTranslation() {
        final I18nService i18nService = I18nServices.getInstance().getService(locale);
        assertNotNull("Can't get i18n service for " + locale.toString(), i18nService);
        final boolean isTranslated = i18nService.hasKey(key);
        assertTrue("No translation for key \"" + key + "\" of class " + className + " into language " + locale + ".", isTranslated);
        final String translation = i18nService.getLocalized(key);
        assertNotNull("Translation for key \"" + key + "\" of class " + className + " is null.", translation);
    }

    private static void removeUnwantedZones(List<String> timeZoneIDs) {
        Pattern[] patterns = new Pattern[whiteRegex.length];
        int i = 0;
        for (String regex : whiteRegex) {
            patterns[i++] = Pattern.compile(regex);
        }
        Iterator<String> iter = timeZoneIDs.iterator();
        while (iter.hasNext()) {
            String timeZoneID = iter.next();
            boolean isWhiteListed = false;
            for (Pattern pattern : patterns) {
                isWhiteListed = pattern.matcher(timeZoneID).matches();
                if (isWhiteListed) {
                    break;
                }
            }
            if (!isWhiteListed) {
                iter.remove();
            }
        }
    }

    private static final String[] whiteRegex = {
        "Africa/.*", "America/.*", "Antarctica/.*", "Arctic/.*", "Asia/.*", "Atlantic/.*", "Australia/.*", "Brazil/.*", "Canada/.*",
        "Chile/.*", "Cuba", "Egypt", "Eire", "Europe/.*", "Greenwich", "Hongkong", "Iceland", "Indian/.*", "Iran", "Israel", "Jamaica",
        "Japan", "Kwajalein", "Libya", "Mexico/.*", "Mideast/.*", "Navajo", "Pacific/.*", "Poland", "Portugal", "Singapore", "Turkey",
        "US/.*", "UTC", "Zulu" };

}
