/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        if (null == value || 1 > value.length()) {
            return null;
        }
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

    @SuppressWarnings("deprecation")
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
        if (null == email) {
            throw OXException.general("External participants must contain an email address");
        }
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
        final Contact user = fixtureLoader.getFixtures(fixtureName, SimpleCredentials.class).getEntry(fixtureEntry).getEntry().asContact();
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
