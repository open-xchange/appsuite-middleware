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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.OverridingUserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.impl.DBPoolingException;
import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.BeforeClass;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Bug8681forVCard extends AbstractVCardTest {

	
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(Bug8681forVCard.class);
	}

    @BeforeClass

    public static void initialize() throws Exception {
        AbstractVCardTest.initialize();
        ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId("defaultcontext"));
    }

    @Test public void checkVCard() throws AbstractOXException, UnsupportedEncodingException, SQLException, OXException{
		//creating folder before changing permissions...
		folderId = createTestFolder(FolderObject.CONTACT, sessObj, ctx, "vcard7719Folder");
		
		UserConfigurationStorage original = UserConfigurationStorage.getInstance();
        OverridingUserConfigurationStorage override = new OverridingUserConfigurationStorage(original) {
            public UserConfiguration getOverride(int userId, int[] groups, Context ctx) throws UserConfigurationException {
                UserConfiguration orig = delegate.getUserConfiguration(userId, ctx);
                UserConfiguration copy = (UserConfiguration) orig.clone();
                copy.setContact(false);
                return copy;
            }
        };
        override.override();
        try {
			//uploading
			final List <String> folders = Arrays.asList( Integer.toString(folderId) );
	
			try{
				imp.canImport(sessObj, Format.VCARD, folders, null);
				fail("Could import Contacts without permission on Contact module!");
			} catch(ImportExportException e) {
				assertEquals(Category.PERMISSION, e.getCategory());
				assertEquals("I_E-0607" , e.getErrorCode() );
			}
		} finally {
			//undo changes
			override.takeBack();
		}
	}
    @Test public void testDummy(){}
}
