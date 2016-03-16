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

package com.openexchange.ajax.share.bugs;

import java.util.Collections;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreResponse;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageGuestObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * {@link Bug40596Test}
 *
 * [Desktop/Win7] Share file with read/write/delete rights: Not possible to delete file
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class Bug40596Test extends ShareTest {

    /**
     * Initializes a new {@link Bug40596Test}.
     *
     * @param name The test name
     */
    public Bug40596Test(String name) {
        super(name);
    }

    public void testBug40596() throws Exception {
        File file = insertFile(client.getValues().getPrivateInfostoreFolder());
        remember(file);
        OCLGuestPermission guestPermission = createNamedGuestPermission("testbug40596@example.com", "Bug 40596", "secret");
        DefaultFileStorageGuestObjectPermission objectPermission = (DefaultFileStorageGuestObjectPermission) asObjectPermission(guestPermission);
        objectPermission.setPermissions(FileStorageGuestObjectPermission.DELETE);
        file.setObjectPermissions(Collections.<FileStorageObjectPermission> singletonList(objectPermission));
        UpdateInfostoreRequest updateInfostoreRequest = new UpdateInfostoreRequest(file, new Field[] { Field.OBJECT_PERMISSIONS }, file.getLastModified());
        updateInfostoreRequest.setNotifyPermissionEntities(Transport.MAIL);
        updateInfostoreRequest.setFailOnError(false);
        UpdateInfostoreResponse updateInfostoreResponse = getClient().execute(updateInfostoreRequest);
        assertTrue(updateInfostoreResponse.hasError());
        OXException e = updateInfostoreResponse.getException();
        assertTrue(InfostoreExceptionCodes.VALIDATION_FAILED_INAPPLICABLE_PERMISSIONS.equals(e));
    }

}
