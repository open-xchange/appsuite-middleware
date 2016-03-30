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

import java.io.IOException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.ListRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 *
 */
public final class Bug12716Test extends AbstractAJAXSession {

    private AJAXClient client;

    private int folderId;

    private Contact contact;

    /**
     * Default constructor.
     * @param name test name
     */
    public Bug12716Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateContactFolder();
        contact = insertContact();
    }

    @Override
    protected void tearDown() throws Exception {
        deleteContact();
        super.tearDown();
    }

    public void testListProblem() throws OXException, IOException,
        SAXException, JSONException {
        final int[] columns = new int[] {
            20, 1, 5, 2, 3, 4, 20, 100, 101,
            500, 501, 502, 503, 504, 505, 506, 507, 508, 509,
            510, 511, 512, 513, 514, 515, 516, 517, 518, 519,
            520, 521, 522, 523,      525, 526, 527, 528, 529,
            530, 531, 532, 533, 534, 535, 536, 537, 538, 539,
            540, 541, 542, 543, 544, 545, 546, 547, 548, 549,
            550, 551, 552, 553, 554, 555, 556, 557, 558, 559,
            560, 561, 562, 563, 564, 565, 566, 567, 568, 569,
            570, 571, 572, 573, 574, 575, 576, 577, 578, 579,
            580, 581, 582, 583, 584, 585, 586, 587, 588, 589,
            590, 591, 592, 594, 595, 596, 597, 598, 599,
            104, 601, 602, 605, 102, 524, 606};
        final ListRequest request = new ListRequest(
            ListIDs.l(new int[] { folderId, contact.getObjectID() }),
            columns, false);
        final CommonListResponse response = client.execute(request);
        if (response.hasError()) {
            fail(response.getException().toString());
        }
    }

    private Contact insertContact() throws OXException, IOException,
        SAXException, JSONException {
        final Contact contact = new Contact();
        contact.setParentFolderID(folderId);
        contact.setDisplayName("Test for bug 12716");
        final InsertRequest request = new InsertRequest(contact);
        final InsertResponse response = client.execute(request);
        contact.setObjectID(response.getId());
        contact.setLastModified(response.getTimestamp());
        return contact;
    }

    private void deleteContact() throws OXException, IOException,
        SAXException, JSONException {
        client.execute(new DeleteRequest(contact));
    }
}
