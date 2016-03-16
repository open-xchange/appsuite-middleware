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
import com.openexchange.group.Group;

/**
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 */
public class GroupFixtureFactory implements FixtureFactory<Group> {
    private final FixtureLoader fixtureLoader;

	public GroupFixtureFactory(FixtureLoader fixtureLoader) {
		super();
		this.fixtureLoader = fixtureLoader;
	}

	@Override
    public Fixtures<Group> createFixture(final String fixtureName, final Map<String, Map<String, String>> entries) {
        return new GroupFixtures(fixtureName, entries, fixtureLoader);
    }

    private class GroupFixtures extends DefaultFixtures<Group> implements Fixtures<Group> {
        private final Map<String, Map<String, String>> entries;

        private final Map<String, Fixture<Group>> groupMap = new HashMap<String, Fixture<Group>>();

        public GroupFixtures(final String fixtureName, final Map<String, Map<String, String>> values, FixtureLoader fixtureLoader) {
            super(Group.class, values, fixtureLoader);
            this.entries = values;
        }

        @Override
        public Fixture<Group> getEntry(final String entryName) throws OXException {
            if (groupMap.containsKey(entryName)) {
                return groupMap.get(entryName);
            }

            final Map<String, String> values = entries.get(entryName);

            if (null == values) {
                throw new FixtureException("Entry with name "+entryName+" not found");
            }

            final Group group = new Group();

            apply(group, values);

            final Fixture<Group> fixture = new Fixture<Group>(group, values.keySet().toArray(new String[values.size()]), values);

            groupMap.put(entryName, fixture);
            return fixture;
        }
    }
}
