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

package com.openexchange.pubsub;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.microformats.MicroformatSubscribeService;
import com.openexchange.templating.OXTemplate;

/**
 * {@link BasicContactTemplateTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class BasicContactTemplateTest extends AbstractContactTemplateTest {

    /**
     * Initializes a new {@link BasicContactTemplateTest}.
     */
    public BasicContactTemplateTest() {
        super();
    }

    /**
     * Initializes a new {@link BasicContactTemplateTest}.
     * @param name
     */
    public BasicContactTemplateTest(String name) {
        super(name);
    }

    protected abstract OXTemplate getTemplate() throws Exception;

    /**
     * Tests a single export. Compares every single field.
     * Is rather helpful telling you which field failed.
     *
     * @throws Exception
     */
    public void testSingle() throws Exception {
        SubscriptionSource source = getSubscriptionSource();
        MicroformatSubscribeService service = getSubscribeService();
        introduceToEachOther(service, source);

        StringWriter writer = new StringWriter();

        Contact expected = generateContact("");
        List<Contact> expecteds = Arrays.asList(expected);

        OXTemplate templ = getTemplate();
        Map<String, Object> variables = getVariables();
        variables.put("contacts", expecteds);
        templ.process(variables, writer);

        String htmlData = writer.toString();

        Collection<Contact> actuals = service.getContent(new StringReader(htmlData));

        assertEquals("Should return one contact", 1, actuals.size());

        Contact actual = actuals.iterator().next();

        for (int field : Contact.ALL_COLUMNS) {
            assertEquals("Comparing field #" + field, expected.get(field), actual.get(field));
        }
    }

    /**
     * Tests several exports. Tries to find them in the result set using
     * {@link #equals(Object)}. If that does not match, have a look at {@link #testSingle()},
     * which will tell you which fields did not match.
     *
     * @throws Exception
     */
    public void testSeveral() throws Exception {
        SubscriptionSource source = getSubscriptionSource();
        MicroformatSubscribeService service = getSubscribeService();
        introduceToEachOther(service, source);

        StringWriter writer = new StringWriter();

        List<Contact> expecteds = getContacts();
        OXTemplate templ = getTemplate();
        Map<String, Object> variables = getVariables();
        variables.put("contacts", expecteds);
        templ.process(variables, writer);

        String htmlData = writer.toString();

        Collection<Contact> actuals = service.getContent(new StringReader(htmlData));

        assertEquals("Should return the same amount of contacts as were inserted", expecteds.size(), actuals.size());

        for (Contact expected : expecteds) {
            assertTrue("Should contain the following contact: " + expected, actuals.contains(expected));
        }
    }

}
