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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
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

    @Test
    public void testWrongMemberReference() {
        /*
         * try and create a distribution list, using a wrong entry id reference and no e-mail address
         */
        Contact distributionList = super.generateContact("List");
        List<DistributionListEntryObject> members = new ArrayList<DistributionListEntryObject>();
        DistributionListEntryObject member = new DistributionListEntryObject();
        member.setEntryID(98967896);
        members.add(member);
        distributionList.setDistributionList(members.toArray(new DistributionListEntryObject[0]));
        cotm.newAction(distributionList);
        /*
         * check for excpetion
         */
        assertFalse("contact has an object ID", 0 < distributionList.getObjectID());
        OXException lastException = cotm.getLastResponse().getException();
        assertNotNull("no exception thrown", lastException);
        assertEquals("unexpected error code in exception", "CON-0177", lastException.getErrorCode());
    }

    @Test
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
        cotm.newAction(distributionList);
        /*
         * check for excpetion
         */
        assertFalse("contact has an object ID", 0 < distributionList.getObjectID());
        OXException lastException = cotm.getLastResponse().getException();
        assertNotNull("no exception thrown", lastException);
        assertEquals("unexpected error code in exception", "CON-0177", lastException.getErrorCode());
    }

    @Test
    public void testNoObjectIDReference() {
        /*
         * try and create a distribution list, using a specific mail-field, but no entry id
         */
        Contact distributionList = super.generateContact("List");
        List<DistributionListEntryObject> members = new ArrayList<DistributionListEntryObject>();
        DistributionListEntryObject member = new DistributionListEntryObject();
        member.setEmailfield(1);
        members.add(member);
        distributionList.setDistributionList(members.toArray(new DistributionListEntryObject[0]));
        cotm.newAction(distributionList);
        /*
         * check for excpetion
         */
        assertFalse("contact has an object ID", 0 < distributionList.getObjectID());
        OXException lastException = cotm.getLastResponse().getException();
        assertNotNull("no exception thrown", lastException);
        assertEquals("unexpected error code in exception", "CON-0178", lastException.getErrorCode());
    }

}
