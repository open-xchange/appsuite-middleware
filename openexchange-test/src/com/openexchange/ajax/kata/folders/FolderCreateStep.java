/*    OPEN-XCHANGE legal information
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

package com.openexchange.ajax.kata.folders;

import java.util.Date;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.kata.AbstractStep;
import com.openexchange.ajax.kata.IdentitySource;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.FolderTestManager;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class FolderCreateStep extends AbstractStep implements IdentitySource<FolderObject>{

    private final FolderObject entry;
    private boolean inserted;
    private FolderTestManager manager;

    public FolderCreateStep(FolderObject entry, String name, String expectedError) {
        super(name, expectedError);
        this.entry = entry;
    }

    @Override
    public void cleanUp() throws Exception {
        if( inserted ){
            entry.setLastModified(new Date(Long.MAX_VALUE));
            manager.deleteFolderOnServer(entry);
            inserted = false;
        }
    }

    @Override
    public void perform(AJAXClient myClient) throws Exception {
        this.client = myClient;
        this.manager = new FolderTestManager(myClient);

        InsertRequest insertRequest = new InsertRequest(EnumAPI.OX_OLD, entry, false);
        CommonInsertResponse insertResponse = execute(insertRequest);
        insertResponse.fillObject(entry);
        inserted = !insertResponse.hasError();
        checkError(insertResponse);
    }

    @Override
    public void assumeIdentity(FolderObject folder) {
        folder.setObjectID( entry.getObjectID() );
        folder.setParentFolderID( entry.getParentFolderID());
        folder.setLastModified( entry.getLastModified());
        folder.setPermissions(entry.getPermissions());
    }

    @Override
    public void rememberIdentityValues(FolderObject folder) {
        folder.setLastModified( entry.getLastModified());
    }

    @Override
    public void forgetIdentity(FolderObject myEntry) {
        inserted = false;
    }

    @Override
    public Class<FolderObject> getType() {
        return FolderObject.class;
    }

}
