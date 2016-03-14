
package com.openexchange.mail.filter.json;

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

import org.junit.Assert;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.export.SieveHandlerFactory;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.MailFilterProperties;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.json.ajax.actions.MailFilterAction;
import com.openexchange.mailfilter.json.osgi.Services;

public class MailFilterActionTest extends MailFilterAction {

    public MailFilterActionTest() {
        super();
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Common.prepare(null, null);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetRightPasswordNothing() throws OXException {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String credsPW = "pw2";
        final Credentials creds = new Credentials("", credsPW, 0, 0, null);
        try {
            SieveHandlerFactory.getRightPassword(config, creds);
            Assert.fail("No exception thrown");
        } catch (final OXException e) {
            Assert.assertTrue(MailFilterExceptionCode.NO_VALID_PASSWORDSOURCE.equals(e));
        }
    }

    @Test
    public void testGetRightPasswordSession() throws OXException {
        Common.simConfigurationService.stringProperties.put(
            MailFilterProperties.Values.SIEVE_PASSWORDSRC.property,
            MailFilterProperties.PasswordSource.SESSION.name);
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String credsPW = "pw2";
        final Credentials creds = new Credentials("", credsPW, 0, 0, null);
        final String rightPassword = SieveHandlerFactory.getRightPassword(config, creds);
        Assert.assertEquals("Password should be equal to \"" + credsPW + "\"", credsPW, rightPassword);
    }

    @Test
    public void testGetRightPasswordGlobalNoMasterPW() throws OXException {
        Common.simConfigurationService.stringProperties.put(
            MailFilterProperties.Values.SIEVE_PASSWORDSRC.property,
            MailFilterProperties.PasswordSource.GLOBAL.name);
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String credsPW = "pw2";
        final Credentials creds = new Credentials("", credsPW, 0, 0, null);
        try {
            SieveHandlerFactory.getRightPassword(config, creds);
            Assert.fail("No exception thrown");
        } catch (final OXException e) {
            Assert.assertTrue(MailFilterExceptionCode.NO_MASTERPASSWORD_SET.equals(e));
        }
    }

    @Test
    public void testGetRightPasswordGlobal() throws OXException {
        final String masterPW = "masterPW";
        Common.simConfigurationService.stringProperties.put(
            MailFilterProperties.Values.SIEVE_PASSWORDSRC.property,
            MailFilterProperties.PasswordSource.GLOBAL.name);
        Common.simConfigurationService.stringProperties.put(MailFilterProperties.Values.SIEVE_MASTERPASSWORD.property, masterPW);
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String credsPW = "pw2";
        final Credentials creds = new Credentials("", credsPW, 0, 0, null);
        final String rightPassword = SieveHandlerFactory.getRightPassword(config, creds);
        Assert.assertEquals("Password should be equal to \"" + masterPW + "\"", masterPW, rightPassword);
    }

}
