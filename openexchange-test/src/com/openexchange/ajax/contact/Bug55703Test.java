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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.user.actions.UpdateRequest;
import com.openexchange.ajax.user.actions.UpdateResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug55703Test}
 *
 * "oxadmin" account information can be altered by users
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug55703Test extends AbstractManagedContactTest {

    /**
     * Initializes a new {@link Bug55703Test}.
     *
     * @param name The test name
     */
    public Bug55703Test() {
        super();
    }

    @Test
    public void testUpdateOXAdmin() throws Exception {
        /*
         * get current contact for the admin (assume oxadmin always has contact identifier '1')
         */
        Contact originalContact = cotm.getAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, 1);
        /*
         * try and update the contact through the 'user' module
         */
        Contact modifiedContact = new Contact();
        modifiedContact.setParentFolderID(originalContact.getParentFolderID());
        modifiedContact.setObjectID(originalContact.getObjectID());
        modifiedContact.setInternalUserId(originalContact.getInternalUserId());
        modifiedContact.setNote(UUID.randomUUID().toString());
        modifiedContact.setLastModified(originalContact.getLastModified());
        UpdateResponse updateResponse = getClient().execute(new UpdateRequest(modifiedContact, null, false));
        assertTrue("No errors when updating the admin contact",  updateResponse.hasError());
        assertEquals("Unexpected error code", "CON-0176", updateResponse.getException().getErrorCode());
        /*
         * check that the admin contact was not modified
         */
        Contact reloadedContact = cotm.getAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, 1);
        assertEquals("Note was modified", originalContact.getNote(), reloadedContact.getNote());
    }

}
