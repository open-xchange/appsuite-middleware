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
package com.openexchange.test.fixtures.transformators;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.resource.Resource;
import com.openexchange.test.fixtures.FixtureLoader;
import com.openexchange.test.fixtures.SimpleCredentials;

/**
 * Transforms strings of the kind "users:user_a,user_b,user_c,groups:test_group_01,resources:beamer"
 * into a list of user-, group- or external participants, where the entries refer to the "users",
 * "groups", "resources" or "contacts" fixture files.
 *
 * @author tfriedrich
 */
public class ParticipantTransformator implements Transformator {

	private final FixtureLoader fixtureLoader;

	public ParticipantTransformator(FixtureLoader fixtureLoader) {
		super();
		this.fixtureLoader = fixtureLoader;
	}

	@Override
    public Object transform(final String value) throws OXException {
		if (null == value || 1 > value.length()) { return null; }
		String fixtureName = "users";
		String fixtureEntry = "";
		final String[] splitted = value.split(",");
		final List<Participant> participants = new ArrayList<Participant>(splitted.length);
		for (int i = 0; i < splitted.length; i++) {
			final int idx = splitted[i].indexOf(':');
			if (0 < idx && splitted[i].length() > idx) {
				fixtureName = splitted[i].substring(0, idx);
				fixtureEntry = splitted[i].substring(idx + 1);
			} else {
				fixtureEntry = splitted[i];
			}
			participants.add(getParticipant(fixtureName, fixtureEntry));
		}
		return participants;
    }

	private Participant getParticipant(final String fixtureName, final String fixtureEntry) throws OXException {
		if ("users".equals(fixtureName)) {
			return getUserParticipant(fixtureName, fixtureEntry);
		} else if ("groups".equals(fixtureName)) {
			return getGroupParticipant(fixtureName, fixtureEntry);
		} else if ("contacts".equals(fixtureName)) {
			return getExternalUserParticipant(fixtureName, fixtureEntry);
		} else if ("resources".equals(fixtureName)) {
			return getResourceParticipant(fixtureName, fixtureEntry);
		} else {
			throw OXException.general("Unable to convert " + fixtureName + ":" + fixtureEntry + " into a participant.");
		}
	}

	private Participant getExternalUserParticipant(final String fixtureName, final String fixtureEntry) throws OXException {
		final Contact contact = fixtureLoader.getFixtures(fixtureName, Contact.class).getEntry(fixtureEntry).getEntry();
		String email = null;
		if (contact.containsEmail1()) {
			email = contact.getEmail1();
		} else if (contact.containsEmail2()) {
			email = contact.getEmail2();
		} else if (contact.containsEmail3()) {
			email = contact.getEmail3();
		}
		if (null == email) { throw OXException.general("External participants must contain an email address"); }
		final ExternalUserParticipant participant = new ExternalUserParticipant(email);
		participant.setDisplayName(contact.getDisplayName());
		participant.setIdentifier(contact.getObjectID());
		return participant;
	}

	private GroupParticipant getGroupParticipant(final String fixtureName, final String fixtureEntry) throws OXException {
		final Group group = fixtureLoader.getFixtures(fixtureName, Group.class).getEntry(fixtureEntry).getEntry();
		final GroupParticipant participant = new GroupParticipant(group.getIdentifier());
		participant.setDisplayName(group.getDisplayName());
		return participant;
	}

	private UserParticipant getUserParticipant(final String fixtureName, final String fixtureEntry) throws OXException {
		final Contact user = fixtureLoader.getFixtures(fixtureName, SimpleCredentials.class)
		    .getEntry(fixtureEntry)
		        .getEntry()
		            .asContact();
		final UserParticipant participant = new UserParticipant(user.getObjectID());
		participant.setDisplayName(user.getDisplayName());
		participant.setEmailAddress(user.getEmail1());
		return participant;
	}

	private ResourceParticipant getResourceParticipant(final String fixtureName, final String fixtureEntry) throws OXException {
		final Resource resource = fixtureLoader.getFixtures(fixtureName, Resource.class).getEntry(fixtureEntry).getEntry();
		final ResourceParticipant participant = new ResourceParticipant(resource.getIdentifier());
		participant.setDisplayName(resource.getDisplayName());
		participant.setEmailAddress(resource.getMail());
		return participant;
	}
}
