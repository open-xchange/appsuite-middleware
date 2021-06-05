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

package com.openexchange.ajax.contact;

import static org.junit.Assert.fail;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.ListRequest;
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

    private int folderId;

    private Contact contact;

    /**
     * Default constructor.
     * 
     * @param name test name
     */
    public Bug12716Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderId = getClient().getValues().getPrivateContactFolder();
        contact = insertContact();
    }

    @Test
    public void testListProblem() throws OXException, IOException, JSONException {
        final int[] columns = new int[] { 20, 1, 5, 2, 3, 4, 20, 100, 101, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 515, 516, 517, 518, 519, 520, 521, 522, 523, 525, 526, 527, 528, 529, 530, 531, 532, 533, 534, 535, 536, 537, 538, 539, 540, 541, 542, 543, 544, 545, 546, 547, 548, 549, 550, 551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575, 576, 577, 578, 579, 580, 581, 582, 583, 584, 585, 586, 587, 588, 589, 590, 591, 592, 594, 595, 596, 597, 598, 599, 104, 601, 602, 605, 102, 524, 606 };
        final ListRequest request = new ListRequest(ListIDs.l(new int[] { folderId, contact.getObjectID() }), columns, false);
        final CommonListResponse response = getClient().execute(request);
        if (response.hasError()) {
            fail(response.getException().toString());
        }
    }

    private Contact insertContact() throws OXException, IOException, JSONException {
        final Contact contact = new Contact();
        contact.setParentFolderID(folderId);
        contact.setDisplayName("Test for bug 12716");
        final InsertRequest request = new InsertRequest(contact);
        final InsertResponse response = getClient().execute(request);
        contact.setObjectID(response.getId());
        contact.setLastModified(response.getTimestamp());
        return contact;
    }

}
