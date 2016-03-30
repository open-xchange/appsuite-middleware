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
package com.openexchange.test.fixtures;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.fixtures.transformators.BooleanTransformator;
import com.openexchange.test.fixtures.transformators.DistributionListTransformator;
import com.openexchange.test.fixtures.transformators.FileToByteArrayTransformator;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Markus Wagner <markus.wagner@open-xchange.com>
 * @author Martin Braun <martin.braun@open-xchange.com>
 */
public class ContactFixtureFactory implements FixtureFactory<Contact> {

    private final FixtureLoader fixtureLoader;

	public ContactFixtureFactory(FixtureLoader fixtureLoader) {
		super();
		this.fixtureLoader = fixtureLoader;
	}

	@Override
    public Fixtures<Contact> createFixture(final String fixtureName, final Map<String, Map<String, String>> entries) {
		return new ContactFixtures(fixtureName, entries, fixtureLoader);
    }

    private class ContactFixtures  extends DefaultFixtures<Contact> implements Fixtures<Contact>{
        private final Map<String, Map<String, String>> entries;
        private final Map<String, Fixture<Contact>>  contacts = new HashMap<String,Fixture<Contact>>();

        public ContactFixtures(final String fixtureName, final Map<String, Map<String, String>> entries, FixtureLoader fixtureLoader) {
            super(Contact.class, entries, fixtureLoader);
            this.entries = entries;
            addTransformator(new DistributionListTransformator(fixtureLoader), "distribution_list");
            addTransformator(new BooleanTransformator(), "private_flag");
            addTransformator(new FileToByteArrayTransformator(fixtureLoader), "image1");
        }

        @Override
        public Fixture<Contact> getEntry(final String entryName) throws OXException {
            if (contacts.containsKey(entryName)) {
                return contacts.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }
            defaults(values);
            final Contact contact = new Contact();
            apply(contact,values);

            final Fixture<Contact> fixture = new Fixture<Contact>(contact, values.keySet().toArray(new String[values.size()]), values) {
            	@Override
                public Comparator getComparator(final String field) {
            		if("birthday".equals(field) || "anniversary".equals(field)) {
            			return new DayOnlyDateComparator();
            		}
            		return super.getComparator(field);
            	}
            };

            contacts.put(entryName, fixture);
            return fixture;
        }

        private void defaults(final Map values) {
        	if (false == values.containsKey("display_name")) {
            	final String surName = values.containsKey("sur_name") ? values.get("sur_name").toString() : null;
            	final String givenName = values.containsKey("given_name") ? values.get("given_name").toString() : null;
        		if (null != surName) {
        			values.put("display_name", null == givenName ? surName : String.format("%s, %s", surName, givenName));
        		} else if (null != givenName) {
        			values.put("display_name", givenName);
        		}
        	}
        }
    }
}
