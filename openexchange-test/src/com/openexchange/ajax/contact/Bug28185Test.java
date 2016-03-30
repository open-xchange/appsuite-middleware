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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link Bug28185Test}
 *
 * Creating a distribution list via ContactService.createContact() shows no error message if some fields are missing
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug28185Test extends AbstractManagedContactTest {

    public Bug28185Test(String name) {
        super(name);
    }

	@Override
	public void setUp() throws Exception {
	    super.setUp();
	}

    public void testWrongMemberReference() throws OXException {
        /*
         * try and create a distribution list, using a wrong entry id reference and no e-mail address
         */
        Contact distributionList = super.generateContact("List");
        List<DistributionListEntryObject> members = new ArrayList<DistributionListEntryObject>();
        DistributionListEntryObject member = new DistributionListEntryObject();
        member.setEntryID(98967896);
        members.add(member);
        distributionList.setDistributionList(members.toArray(new DistributionListEntryObject[0]));
        manager.newAction(distributionList);
        /*
         * check for excpetion
         */
        assertFalse("contact has an object ID", 0 < distributionList.getObjectID());
        OXException lastException = manager.getLastResponse().getException();
        assertNotNull("no exception thrown", lastException);
        assertEquals("unexpected error code in exception", "CON-0177", lastException.getErrorCode());
    }

    public void testEmptyEmailAddress() throws OXException {
        /*
         * try and create a distribution list, using an empty e-mail address
         */
        Contact distributionList = super.generateContact("List");
        List<DistributionListEntryObject> members = new ArrayList<DistributionListEntryObject>();
        DistributionListEntryObject member = new DistributionListEntryObject();
        member.setEmailaddress("", false);
        members.add(member);
        distributionList.setDistributionList(members.toArray(new DistributionListEntryObject[0]));
        manager.newAction(distributionList);
        /*
         * check for excpetion
         */
        assertFalse("contact has an object ID", 0 < distributionList.getObjectID());
        OXException lastException = manager.getLastResponse().getException();
        assertNotNull("no exception thrown", lastException);
        assertEquals("unexpected error code in exception", "CON-0177", lastException.getErrorCode());
    }

    public void testNoObjectIDReference() throws OXException {
        /*
         * try and create a distribution list, using a specific mail-field, but no entry id
         */
        Contact distributionList = super.generateContact("List");
        List<DistributionListEntryObject> members = new ArrayList<DistributionListEntryObject>();
        DistributionListEntryObject member = new DistributionListEntryObject();
        member.setEmailfield(1);
        members.add(member);
        distributionList.setDistributionList(members.toArray(new DistributionListEntryObject[0]));
        manager.newAction(distributionList);
        /*
         * check for excpetion
         */
        assertFalse("contact has an object ID", 0 < distributionList.getObjectID());
        OXException lastException = manager.getLastResponse().getException();
        assertNotNull("no exception thrown", lastException);
        assertEquals("unexpected error code in exception", "CON-0178", lastException.getErrorCode());
    }

}
