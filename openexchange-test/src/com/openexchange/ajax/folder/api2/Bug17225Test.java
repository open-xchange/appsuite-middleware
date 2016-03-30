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

package com.openexchange.ajax.folder.api2;

import java.util.Iterator;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug17225Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug17225Test extends AbstractAJAXSession {

	private AJAXClient client;
	private AJAXClient client2;
	private FolderObject folder;
	private int userId1;

	public Bug17225Test(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		client = getClient();
		userId1 = client.getValues().getUserId();
		client2 = new AJAXClient(User.User2);
		int folderId = client.getValues().getPrivateAppointmentFolder();
		GetResponse getR = client.execute(new GetRequest(EnumAPI.OUTLOOK, folderId));
		FolderObject oldFolder = getR.getFolder();
		folder = new FolderObject();
		folder.setObjectID(oldFolder.getObjectID());
		folder.setLastModified(getR.getTimestamp());
		folder.setPermissionsAsArray(new OCLPermission[] {
				Create.ocl(userId1, false, true,
						OCLPermission.ADMIN_PERMISSION,
						OCLPermission.ADMIN_PERMISSION,
						OCLPermission.ADMIN_PERMISSION,
						OCLPermission.ADMIN_PERMISSION),
				Create.ocl(client2.getValues().getUserId(), false, false,
						OCLPermission.CREATE_OBJECTS_IN_FOLDER,
						OCLPermission.READ_ALL_OBJECTS,
						OCLPermission.WRITE_ALL_OBJECTS,
						OCLPermission.DELETE_ALL_OBJECTS) });
		InsertResponse updateR = client.execute(new UpdateRequest(EnumAPI.OUTLOOK, folder));
		folder.setLastModified(updateR.getTimestamp());
//		client.execute(new GetRequest(EnumAPI.OUTLOOK, folderId));
	}

	@Override
	protected void tearDown() throws Exception {
		folder.setPermissionsAsArray(new OCLPermission[] { Create.ocl(userId1,
				false, true, OCLPermission.ADMIN_PERMISSION,
				OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
				OCLPermission.ADMIN_PERMISSION) });
		client.execute(new UpdateRequest(EnumAPI.OUTLOOK, folder));
		super.tearDown();
	}

	public void testSharedType() throws Throwable {
		ListResponse response = client2.execute(new ListRequest(EnumAPI.OUTLOOK, FolderObject.SHARED_PREFIX + userId1, new int[] { 1, 20, 2, 3, 300, 301, 302, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316 }, false));
		Iterator<FolderObject> iter = response.getFolder();
		while (iter.hasNext()) {
			FolderObject testFolder = iter.next();
			if (testFolder.getObjectID() == folder.getObjectID()) {
				assertEquals("Shared folder is sent with type private.", FolderObject.SHARED, testFolder.getType());
			}
		}
	}
}
