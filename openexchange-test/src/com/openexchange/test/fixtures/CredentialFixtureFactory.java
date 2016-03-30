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

import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CredentialFixtureFactory implements FixtureFactory<SimpleCredentials>{

	private final TestUserConfigFactory userConfigFactory;
	private final ContactFinder contactFinder;
	private final FixtureLoader fixtureLoader;

    public CredentialFixtureFactory(TestUserConfigFactory userConfigFactory, ContactFinder contactFinder, FixtureLoader fixtureLoader) {
		super();
		this.userConfigFactory = userConfigFactory;
		this.contactFinder = contactFinder;
		this.fixtureLoader = fixtureLoader;
	}

	@Override
    public Fixtures<SimpleCredentials> createFixture(final String fixtureName, final Map<String, Map<String, String>> entries) {
        return new CredentialFixtures(fixtureName, entries, userConfigFactory, contactFinder, fixtureLoader);
    }

    private class CredentialFixtures extends DefaultFixtures<SimpleCredentials> implements Fixtures<SimpleCredentials> {
        private final Map<String, Map<String, String>> entries;
        private final Map<String, Fixture<SimpleCredentials>> credentialMap = new HashMap<String, Fixture<SimpleCredentials>>();
		private final TestUserConfigFactory userConfigFactory;
		private final ContactFinder contactFinder;

        public CredentialFixtures(final String fixtureName, final Map<String, Map<String, String>> values, TestUserConfigFactory userConfigFactory, ContactFinder contactFinder, FixtureLoader fixtureLoader) {
            super(SimpleCredentials.class, values, fixtureLoader);
            this.entries = values;
            this.userConfigFactory = userConfigFactory;
            this.contactFinder = contactFinder;
        }

        @Override
        public Fixture<SimpleCredentials> getEntry(final String entryName) throws OXException {
            if(credentialMap.containsKey(entryName)) {
                return credentialMap.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }
            final SimpleCredentials credentials = new SimpleCredentials(userConfigFactory, contactFinder);
            apply(credentials,values);
            final Fixture<SimpleCredentials> fixture = new Fixture<SimpleCredentials>(credentials, values.keySet().toArray(new String[values.size()]), values);
            credentialMap.put(entryName, fixture);
            return fixture;
        }
    }
}
