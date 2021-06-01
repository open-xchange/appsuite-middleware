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

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Bug46654Test}
 *
 * list of contacts not displayed in address book
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class Bug46654Test extends AbstractManagedContactTest {

    /**
     * Initializes a new {@link Bug46654Test}.
     *
     * @param name The test name
     */
    public Bug46654Test() {
        super();
    }

    @Test
    public void testSortUnnamedList() throws Exception {
        /*
         * generate test contact on server (and two more to make sorting kick in)
         */
        Contact list = new Contact();
        list.setParentFolderID(folderID);
        list.setMarkAsDistributionlist(true);
        list.setDistributionList(new DistributionListEntryObject[] { new DistributionListEntryObject("Otto", "otto@exmample.com", DistributionListEntryObject.INDEPENDENT), new DistributionListEntryObject("Horst", "horst@exmample.com", DistributionListEntryObject.INDEPENDENT)
        });
        list = cotm.newAction(list);
        cotm.newAction(generateContact(UUIDs.getUnformattedStringFromRandom()));
        cotm.newAction(generateContact(UUIDs.getUnformattedStringFromRandom()));
        /*
         * get all contacts, sorted by column 607
         */
        int[] columns = { 1, 20, 101, 607 };
        AllRequest allRequest = new AllRequest(folderID, columns, Contact.SPECIAL_SORTING, Order.ASCENDING, null);
        CommonAllResponse allResponse = getClient().execute(allRequest);
        assertFalse(allResponse.hasError());
    }

}
