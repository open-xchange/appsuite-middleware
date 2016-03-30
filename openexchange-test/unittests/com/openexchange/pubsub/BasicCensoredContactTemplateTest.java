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
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.microformats.MicroformatSubscribeService;
import com.openexchange.templating.OXTemplate;

/**
 * {@link BasicCensoredContactTemplateTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class BasicCensoredContactTemplateTest extends BasicContactTemplateTest {

    public static final int[] CENSORED_COLUMNS = new int[] {
            Contact.EMAIL1, Contact.EMAIL2, Contact.EMAIL3, Contact.BIRTHDAY, Contact.STATE_BUSINESS, Contact.STATE_HOME, Contact.STATE_OTHER,
            Contact.POSTAL_CODE_BUSINESS, Contact.POSTAL_CODE_HOME, Contact.POSTAL_CODE_OTHER, Contact.CITY_BUSINESS, Contact.CITY_HOME,
            Contact.CITY_OTHER, Contact.STREET_BUSINESS, Contact.STREET_HOME, Contact.STREET_OTHER, Contact.COUNTRY_BUSINESS,
            Contact.COUNTRY_HOME, Contact.COUNTRY_OTHER };

    /**
     * Initializes a new {@link BasicCensoredContactTemplateTest}.
     */
    public BasicCensoredContactTemplateTest() {
        super();
    }

    /**
     * Initializes a new {@link BasicCensoredContactTemplateTest}.
     * @param name
     */
    public BasicCensoredContactTemplateTest(String name) {
        super(name);
    }

    /**
     * Override this to contain only the elements that are not censored.
     */
    @Override
    public Contact generateContact(String identifier) {
        Contact contact = new Contact();
        contact.setGivenName("givenname" + identifier);
        contact.setSurName("surname" + identifier);
        contact.setMiddleName("middlename" + identifier);
//        contact.setDisplayName("displayname" + identifier);
        contact.setPosition("position" + identifier);
        contact.setTitle("title" + identifier);
        contact.setCompany("company" + identifier);
        return contact;
    }

    public void testShouldNotPublishCensoredFields() throws Exception {
        SubscriptionSource source = getSubscriptionSource();
        MicroformatSubscribeService service = getSubscribeService();
        introduceToEachOther(service, source);

        StringWriter writer = new StringWriter();

        Contact expected = generateContact("");
        expected.setEmail1("this-will-need-to-be-censored-1@open-xchange.invalid");
        expected.setEmail2("this-will-need-to-be-censored-2@open-xchange.invalid");
        expected.setEmail3("this-will-need-to-be-censored-3@open-xchange.invalid");
        expected.setBirthday(new Date());

        expected.setStreetBusiness("street_business");
        expected.setPostalCodeBusiness("postal_code_business");
        expected.setCityBusiness("city_business");
        expected.setCountryBusiness("country_business");

        expected.setStreetHome("street_home");
        expected.setPostalCodeHome("postal_code_home");
        expected.setCityHome("city_home");
        expected.setCountryHome("country_home");

        expected.setStreetOther("street_other");
        expected.setPostalCodeOther("postal_code_other");
        expected.setCityOther("city_other");
        expected.setCountryOther("country_other");

        List<Contact> expecteds = Arrays.asList(expected);

        OXTemplate templ = getTemplate();
        Map<String, Object> variables = getVariables();
        variables.put("contacts", expecteds);
        templ.process(variables, writer);

        String htmlData = writer.toString();

        Collection<Contact> actuals = service.getContent(new StringReader(htmlData));

        assertEquals("Should return one contact", 1, actuals.size());

        Contact actual = actuals.iterator().next();

        for (int field : CENSORED_COLUMNS) {
            assertNull("This field needs to be censored: #" + field, actual.get(field));
        }
    }

}
