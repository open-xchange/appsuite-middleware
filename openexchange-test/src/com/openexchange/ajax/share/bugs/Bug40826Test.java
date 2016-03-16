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

package com.openexchange.ajax.share.bugs;

import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug40826Test}
 *
 * As an appsuite user, can create a guest share with WRiTE permissions in calendar/contacts module
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug40826Test extends ShareTest {

    /**
     * Initializes a new {@link Bug40826Test}.
     *
     * @param name The test name
     */
    public Bug40826Test(String name) {
        super(name);
    }

    public void testShareCalendarToAuthor() throws Exception {
        testShareToAuthor(FolderObject.CALENDAR);
    }

    public void testShareTasksToAuthor() throws Exception {
        testShareToAuthor(FolderObject.TASK);
    }

    public void testShareContactsToAuthor() throws Exception {
        testShareToAuthor(FolderObject.CONTACT);
    }

    private void testShareToAuthor(int module) throws Exception {
        OCLGuestPermission guestPermission = createNamedAuthorPermission(randomUID() + "@example.com", "Test Guest");
        /*
         * try and create folder shared to guest user
         */
        FolderObject folder = Create.createPrivateFolder(randomUID(), module, client.getValues().getUserId(), guestPermission);
        folder.setParentFolderID(getDefaultFolder(module));
        InsertRequest insertRequest = new InsertRequest(randomFolderAPI(), folder, client.getValues().getTimeZone());
        insertRequest.setFailOnError(false);
        InsertResponse insertResponse = client.execute(insertRequest);
        assertNotNull(insertResponse);
        assertTrue("No error in response", insertResponse.hasError());
        assertNotNull("No error in response", insertResponse.getException());
        assertEquals("Unexpected error", "FLD-1039", insertResponse.getException().getErrorCode());
    }

}
