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

package com.openexchange.test.report;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.test.report.internal.formatter.HtmlTestReportFormatter;
import com.openexchange.test.report.internal.formatter.PlainTextTestReportFormatter;
import com.openexchange.test.report.internal.writer.ConsoleTestReportWriter;
import com.openexchange.test.report.internal.writer.FileTestReportWriter;

/**
 * {@link FormatterFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Factory {

    private Map<String, Class<?>> registeredFormatters = new HashMap<String, Class<?>>();

    private Map<String, Class<?>> registeredWriters = new HashMap<String, Class<?>>();

    private static final Factory INSTANCE = new Factory();

    // Register writers and formatters
    static {
        Factory.getInstance().registerFormatter("plain", PlainTextTestReportFormatter.class);
        Factory.getInstance().registerFormatter("html", HtmlTestReportFormatter.class);

        Factory.getInstance().registerWriter("console", ConsoleTestReportWriter.class);
        Factory.getInstance().registerWriter("file", FileTestReportWriter.class);
    }

    /**
     * Get the instance
     * 
     * @return The instance
     */
    public static Factory getInstance() {
        return INSTANCE;
    }

    public void registerFormatter(String id, Class<?> clazz) {
        registeredFormatters.put(id, clazz);
    }

    public void registerWriter(String id, Class<?> clazz) {
        registeredWriters.put(id, clazz);
    }

    public TestReportWriter createWriter(String writerId, String formatterId, String path, String name) throws Exception {
        Class<?> formatterClazz = registeredFormatters.get(formatterId);
        Constructor<?> formatterConstructor = formatterClazz.getDeclaredConstructor();
        TestReportFormatter formatter = (TestReportFormatter) formatterConstructor.newInstance();

        Class<?> writerClazz = registeredWriters.get(writerId);
        Constructor<?> writerConstructor = writerClazz.getDeclaredConstructor(new Class[] { TestReportFormatter.class, String.class, String.class });
        TestReportWriter writer = (TestReportWriter) writerConstructor.newInstance(formatter, name, path);

        return writer;
    }
}
