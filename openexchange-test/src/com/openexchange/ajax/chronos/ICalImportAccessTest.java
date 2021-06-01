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

package com.openexchange.ajax.chronos;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.rmi.Naming;
import org.junit.Test;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.ajax.chronos.manager.ICalImportExportManager;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.AJAXConfig.Property;

/**
 * {@link ICalImportAccessTest} This test is a substitution for the test Bug8681forICAL
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ICalImportAccessTest extends AbstractImportExportTest {

    private com.openexchange.admin.rmi.dataobjects.User user;
    private Credentials credentials;
    private OXUserInterface iface;
    private Context ctx;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.user = new com.openexchange.admin.rmi.dataobjects.User(getApiClient().getUserId().intValue());
        this.credentials = new Credentials(admin.getUser(), admin.getPassword());
        this.iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMIHOST) + ":1099/" + OXUserInterface.RMI_NAME);
        this.ctx = new Context();
        this.ctx.setId(I(testUser.getContextId()));
        setModuleAccess(false);
    }

    @Test
    public void testImportCalendarAccess() throws Exception {
        String errorResponse = getImportResponse(ICalImportExportManager.SINGLE_IMPORT_ICS);
        assertNotNull(errorResponse);
        assertTrue("Wrong error:" + errorResponse, errorResponse.contains("CAL-4045"));
    }

    private void setModuleAccess(boolean hasAccess) throws Exception {
        UserModuleAccess access = iface.getModuleAccess(ctx, user, credentials);
        access.setTasks(hasAccess);
        access.setCalendar(hasAccess);
        changeModuleAccess(access);
    }

    private void changeModuleAccess(UserModuleAccess access) throws Exception {
        iface.changeModuleAccess(ctx, user, access, credentials);
    }

}
