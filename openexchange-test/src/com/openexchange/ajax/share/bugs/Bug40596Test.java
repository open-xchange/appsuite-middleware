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

package com.openexchange.ajax.share.bugs;

import static org.junit.Assert.assertTrue;
import java.util.Collections;
import org.junit.Test;
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
import com.openexchange.test.tryagain.TryAgain;

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
    public Bug40596Test() {
        super();
    }

    @Test
    @TryAgain
    public void testBug40596() throws Exception {
        File file = insertFile(getClient().getValues().getPrivateInfostoreFolder());
        remember(file);
        OCLGuestPermission guestPermission = createNamedGuestPermission();
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
