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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.admin.reseller.rmi.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.admin.reseller.rmi.AbstractOXResellerTest;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.rmi.factory.ResellerAdminFactory;

/**
 * {@link Bug795}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class Bug795 extends AbstractOXResellerTest {

    private static final String ERROR_MESSAGE = "Unable to delete %1$s, still owns Subadmin(s)";

    /**
     * Initializes a new {@link Bug795}.
     */
    public Bug795() {
        super();
    }

    @Test
    public void testDeleteSubadminThatStillOwnsSubsubAdmins() throws Exception {
        ResellerAdmin adm = ResellerAdminFactory.createResellerAdmin("reseller-mwb-795");
        adm.setRestrictions(new Restriction[] { CanCreateSubAdmin() });

        ResellerAdmin admin = getResellerManager().create(adm);
        ResellerAdmin subAdmin = getResellerManager().create(admin, ResellerAdminFactory.createResellerAdmin("subreseller-mwb-795"));

        try {
            getResellerManager().delete(admin);
            fail("The parent admin should not be deletable when there are subadmins bound to the parent.");
        } catch (Exception e) {
            // Expected
            assertTrue("Unexpected error", e instanceof OXResellerException);
            String expected = String.format(ERROR_MESSAGE, admin.getId());
            String actual = String.format(OXResellerException.Code.UNABLE_TO_DELETE_OWNS_SUBADMINS.getText(), admin.getId());
            assertEquals("Wrong message", expected, actual);
        }

        getResellerManager().delete(subAdmin);
        getResellerManager().delete(admin);
    }
}
