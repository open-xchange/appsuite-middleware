
package com.openexchange.mail.filter.json;

import org.junit.After;
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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.export.SieveHandlerFactory;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.json.ajax.actions.MailFilterAction;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.mailfilter.properties.MailFilterProperty;
import com.openexchange.mailfilter.properties.PasswordSource;

public class MailFilterActionTest extends MailFilterAction {

    public MailFilterActionTest() {
        super();
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        Common.prepare(null, null);
    }

    @After
    @Test
    public void testGetRightPasswordNothing() {
        LeanConfigurationService config = Services.getService(LeanConfigurationService.class);
        String credsPW = "pw2";
        Credentials creds = new Credentials("", credsPW, 0, 0, null);
        try {
            SieveHandlerFactory.getRightPassword(config, creds);
            Assert.fail("No exception thrown");
        } catch (OXException e) {
            Assert.assertTrue(MailFilterExceptionCode.NO_VALID_PASSWORDSOURCE.equals(e));
        }
    }

    @Test
    public void testGetRightPasswordSession() throws OXException {
        Common.simMailFilterConfigurationService.delegateConfigurationService.stringProperties.put(MailFilterProperty.passwordSource.getFQPropertyName(), PasswordSource.SESSION.name);
        LeanConfigurationService config = Services.getService(LeanConfigurationService.class);
        String credsPW = "pw2";
        Credentials creds = new Credentials("", credsPW, 0, 0, null);
        String rightPassword = SieveHandlerFactory.getRightPassword(config, creds);
        Assert.assertEquals("Password should be equal to \"" + credsPW + "\"", credsPW, rightPassword);
    }

    @Test
    public void testGetRightPasswordGlobalNoMasterPW() {
        Common.simMailFilterConfigurationService.delegateConfigurationService.stringProperties.put(MailFilterProperty.passwordSource.getFQPropertyName(), PasswordSource.GLOBAL.name);
        LeanConfigurationService config = Services.getService(LeanConfigurationService.class);
        String credsPW = "pw2";
        Credentials creds = new Credentials("", credsPW, 0, 0, null);
        try {
            SieveHandlerFactory.getRightPassword(config, creds);
            Assert.fail("No exception thrown");
        } catch (OXException e) {
            Assert.assertTrue(MailFilterExceptionCode.NO_MASTERPASSWORD_SET.equals(e));
        }
    }

    @Test
    public void testGetRightPasswordGlobal() throws OXException {
        String masterPW = "masterPW";
        Common.simMailFilterConfigurationService.delegateConfigurationService.stringProperties.put(MailFilterProperty.passwordSource.getFQPropertyName(), PasswordSource.GLOBAL.name);
        Common.simMailFilterConfigurationService.delegateConfigurationService.stringProperties.put(MailFilterProperty.masterPassword.getFQPropertyName(), masterPW);
        LeanConfigurationService config = Services.getService(LeanConfigurationService.class);
        String credsPW = "pw2";
        Credentials creds = new Credentials("", credsPW, 0, 0, null);
        String rightPassword = SieveHandlerFactory.getRightPassword(config, creds);
        Assert.assertEquals("Password should be equal to \"" + masterPW + "\"", masterPW, rightPassword);
    }

}
