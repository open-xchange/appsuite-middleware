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

package com.openexchange.ajax.participant;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.meterware.httpunit.WebConversation;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.user.actions.SearchRequest;
import com.openexchange.ajax.user.actions.SearchResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.ContactSearchObject;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ParticipantTools {

    private static final int[] COLUMNS = new int[] { Contact.INTERNAL_USERID };

    /**
     * Prevent instantiation
     */
    private ParticipantTools() {
        super();
    }

    public static List<Participant> getParticipants(
        final WebConversation conversation, final String hostName,
        final String sessionId) throws Exception {
        final Contact[] userContacts = ContactTest.searchContact(
            conversation, "*", FolderObject.SYSTEM_LDAP_FOLDER_ID, COLUMNS, AbstractAJAXTest.PROTOCOL
            + hostName, sessionId);
        final List<Participant> participants = new ArrayList<Participant>();
        for (final Contact userContact : userContacts) {
            final UserParticipant user = new UserParticipant(userContact
                .getInternalUserId());
            participants.add(user);
        }
        return participants;
    }

    public static List<Participant> getParticipants(final AJAXClient client) throws OXException, IOException, SAXException, JSONException {
        final ContactSearchObject search = new ContactSearchObject();
        search.setPattern("*");
        search.setFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        final SearchRequest request = new SearchRequest(search, SearchRequest.DEFAULT_COLUMNS);
        final SearchResponse response = client.execute(request);
        final List<Participant> participants = new ArrayList<Participant>();
        for (final User user : response.getUser()) {
            participants.add(new UserParticipant(user.getId()));
        }
        return participants;
    }

    public static List<Participant> createParticipants(final int... userIds) {
        final List<Participant> participants = new ArrayList<Participant>();
        for (final int userId : userIds) {
            participants.add(new UserParticipant(userId));
        }
        return participants;
    }

    public static List<Participant> getParticipants(
        final WebConversation conversation, final String hostName,
        final String sessionId, final int count, final boolean noCreator,
        final int creatorId) throws Exception {
        List<Participant> participants = getParticipants(conversation, hostName,
            sessionId);
        if (noCreator) {
            removeParticipant(participants, creatorId);
        }
        participants = extractByRandom(participants, count);
        return participants;
    }

    public static Participant getSomeParticipant(AJAXClient client) throws OXException, IOException, SAXException, JSONException {
        return getParticipants(client, 1, client.getValues().getUserId()).get(0);
    }

    public static List<Participant> getParticipants(final AJAXClient client, final int count, final int creatorId) throws OXException, IOException, SAXException, JSONException {
        List<Participant> participants = getParticipants(client);
        if (-1 != creatorId) {
            removeParticipant(participants, creatorId);
        }
        participants = extractByRandom(participants, count);
        return participants;
    }

    public static void removeParticipant(final List<Participant> participants,
        final int creatorId) {
        final Iterator<Participant> iter = participants.iterator();
        while (iter.hasNext()) {
            if (iter.next().getIdentifier() == creatorId) {
                iter.remove();
            }
        }
    }

    public static void assertParticipants(Participant[] participants, int... userIds) {
        for (int userId : userIds) {
            boolean contained = false;
            for (Participant participant : participants) {
                if (Participant.USER == participant.getType() && participant.getIdentifier() == userId) {
                    contained = true;
                    break;
                }
            }
            assertTrue("Participant with identifier " + userId + " is missing.", contained);
        }
    }

    private static final Random rand = new Random(System.currentTimeMillis());

    public static List<Participant> extractByRandom(final List<Participant> participants, final int count) {
        final List<Participant> retval = new ArrayList<Participant>();
        do {
            final Participant participant = participants.get(rand.nextInt(participants.size()));
            if (!retval.contains(participant)) {
                retval.add(participant);
            }
        } while (retval.size() < count && retval.size() < participants.size());
        return retval;
    }
}
