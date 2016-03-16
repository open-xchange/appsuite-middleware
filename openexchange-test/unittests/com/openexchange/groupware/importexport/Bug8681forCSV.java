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

package com.openexchange.groupware.importexport;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.importexport.importers.TestCSVContactImporter;
import com.openexchange.groupware.userconfiguration.MutableUserConfiguration;
import com.openexchange.groupware.userconfiguration.OverridingUserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import junit.framework.JUnit4TestAdapter;

public class Bug8681forCSV extends AbstractContactTest {

    // workaround for JUnit 3 runner
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(Bug8681forCSV.class);
    }

    @BeforeClass
    public static void initialize() throws Exception {
        AbstractContactTest.initialize();
        ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId("defaultcontext"));
    }

    @Test
    public void testOurCSV() throws Exception {
        imp = new TestCSVContactImporter();
        folderId = createTestFolder(FolderObject.CONTACT, sessObj, ctx, "bug8681 for csv");

        final UserConfigurationStorage original = UserConfigurationStorage.getInstance();
        final OverridingUserConfigurationStorage override = new OverridingUserConfigurationStorage(original) {

            @Override
            public UserConfiguration getOverride(final int userId, final int[] groups, final Context ctx) throws OXException {
                final UserConfiguration orig = delegate.getUserConfiguration(userId, ctx);
                final MutableUserConfiguration copy = orig.getMutable();
                copy.setContact(false);
                return copy;
            }
        };
        override.override();
        try {
            final String csv = "Given name,Email 1\nPrinz, tobias.prinz@open-xchange.com\nLaguna, francisco.laguna@open-xchange.com";
            imp.canImport(sessObj, Format.CSV, _folders(), null);
            imp.importData(sessObj, Format.CSV, new ByteArrayInputStream(csv.getBytes()), _folders(), null);
            fail("Could write contact without rights to use Contact module");
        } catch (final OXException e) {
            assertTrue(e.similarTo(ImportExportExceptionCodes.CONTACTS_DISABLED.create()));
        } finally {
            override.takeBack();
            deleteTestFolder(folderId);
        }
    }

    @Test
    public void testOutlookCSV() throws Exception {
        imp = new TestCSVContactImporter();
        folderId = createTestFolder(FolderObject.CONTACT, sessObj, ctx, "bug8681 for Outlook CSV");

        final UserConfigurationStorage original = UserConfigurationStorage.getInstance();
        final OverridingUserConfigurationStorage override = new OverridingUserConfigurationStorage(original) {

            @Override
            public UserConfiguration getOverride(final int userId, final int[] groups, final Context ctx) throws OXException {
                final UserConfiguration orig = delegate.getUserConfiguration(userId, ctx);
                final MutableUserConfiguration copy = orig.getMutable();
                copy.setContact(false);
                return copy;
            }
        };
        override.override();

        try {
            final String csv = "Given name,Email 1\nPrinz, tobias.prinz@open-xchange.com\nLaguna, francisco.laguna@open-xchange.com";
            imp.canImport(sessObj, Format.OUTLOOK_CSV, _folders(), null);
            imp.importData(sessObj, Format.OUTLOOK_CSV, new ByteArrayInputStream(csv.getBytes()), _folders(), null);
            fail("Could write contact without rights to use Contact module");
        } catch (final OXException e) {
            assertTrue(e.similarTo(ImportExportExceptionCodes.CONTACTS_DISABLED));
        } finally {
            override.takeBack();
            deleteTestFolder(folderId);
        }
    }

}
