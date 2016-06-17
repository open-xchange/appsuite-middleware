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
	public Bug46654Test(String name) {
		super(name);
	}

    public void testSortUnnamedList() throws Exception {
        /*
         * generate test contact on server (and two more to make sorting kick in)
         */
        Contact list = new Contact();
        list.setParentFolderID(folderID);
        list.setMarkAsDistributionlist(true);
        list.setDistributionList(new DistributionListEntryObject[] {
            new DistributionListEntryObject("Otto", "otto@exmample.com", DistributionListEntryObject.INDEPENDENT),
            new DistributionListEntryObject("Horst", "horst@exmample.com", DistributionListEntryObject.INDEPENDENT)
        });
        list = manager.newAction(list);
        manager.newAction(generateContact(UUIDs.getUnformattedStringFromRandom()));
        manager.newAction(generateContact(UUIDs.getUnformattedStringFromRandom()));
        /*
         * get all contacts, sorted by column 607
         */
        int[] columns = { 1,20,101,607 };
        AllRequest allRequest = new AllRequest(folderID, columns, Contact.SPECIAL_SORTING, Order.ASCENDING, null);
        CommonAllResponse allResponse = getClient().execute(allRequest);
        assertFalse(allResponse.hasError());
	}

}
