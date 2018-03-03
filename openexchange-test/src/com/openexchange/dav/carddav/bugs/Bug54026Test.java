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

package com.openexchange.dav.carddav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import net.sourceforge.cardme.vcard.types.OrgType;

/**
 * {@link Bug54026Test}
 *
 * If changes are made on global address book on eM Client, corresponding changes do not update on Web UI and eM Client.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug54026Test extends CardDAVTest {

    /**
     * Initializes a new {@link Bug54026Test}.
     */
    public Bug54026Test() {
        super();
    }

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.EM_CLIENT_FOR_APP_SUITE;
    }

    @Test
    public void testUpdateOwnUserContact() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        String gabCollection = String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        SyncToken syncToken = new SyncToken(fetchSyncToken(gabCollection));
        /*
         * identify uid of own user contact
         */
        GetRequest userGetRequest = new GetRequest(getClient().getValues().getUserId(), getClient().getValues().getTimeZone());
        Contact contact = getClient().execute(userGetRequest).getContact();
        assertNotNull(contact);
        /*
         * get vcard resource
         */
        String href = "/carddav/" + gabCollection + "/" + contact.getUid() + ".vcf";
        VCardResource card = getVCardResource(href);
        assertNotNull(card);
        /*
         * update vcard resource on client
         */
        String updatedCompany = randomUID();
        card.getVCard().setOrg(new OrgType(updatedCompany));
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCardUpdate(card.getUID(), card.toString(), gabCollection, card.getETag()));
        /*
         * verify updated contact on server
         */
        Contact updatedContact = getClient().execute(userGetRequest).getContact();
        assertNotNull(updatedContact);
        assertEquals("conmpany wrong", updatedCompany, updatedContact.getCompany());
        /*
         * verify updated contact on client
         */

        Map<String, String> eTags = syncCollection(syncToken, "/carddav/" + gabCollection + "/").getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = addressbookMultiget(gabCollection, eTags.keySet());
        card = assertContains(contact.getUid(), addressData);
        assertEquals("ORG wrong", updatedCompany, card.getVCard().getOrg().getOrgName());
    }

}
