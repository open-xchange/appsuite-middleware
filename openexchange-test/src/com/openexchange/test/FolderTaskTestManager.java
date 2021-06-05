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

package com.openexchange.test;

import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.ConcurrentLinkedList;

/**
 * {@link FolderTaskTestManager}
 *
 * @author <a href="mailto:jan-oliver.huhn@open-xchange.com">Jan-Oliver Huhn</a>
 */
public class FolderTaskTestManager extends FolderTestManager {

    private final AJAXClient client2;

    private List<FolderObject> createdItems2;

    public FolderTaskTestManager(final AJAXClient client, final AJAXClient client2) {
        super(client);
        this.client2 = client2;
        createdItems2 = new ConcurrentLinkedList<FolderObject>();
    }

    @Override
    public void cleanUp(){
        super.cleanUp();
        deleteFolder(client2, createdItems2);
        createdItems2 = new ConcurrentLinkedList<FolderObject>();
        this.setClient(getClient());
    }

    /**
     * Method to clean up folders from multi-clients after a find task test was invoked
     */
    public void deleteFolder(final AJAXClient client, List<FolderObject> list){
        this.setClient(client);
        final Vector<FolderObject> deleteMe = new Vector<FolderObject>(list);
        try {
            for (final FolderObject folder : deleteMe) {
                folder.setLastModified(new Date(Long.MAX_VALUE));
                deleteFolderOnServer(folder, Boolean.TRUE);
                if (getLastResponse().hasError()) {
                    org.slf4j.LoggerFactory.getLogger(FolderTestManager.class).warn("Unable to delete the folder with id {} in folder {} with name '{}': {}", I(folder.getObjectID()), I(folder.getParentFolderID()), folder.getFolderName(), getLastResponse().getException().getMessage());
                }
            }
        } catch (Exception e){
            doExceptionHandling(e, "clean-up");
        }
    }

    public void rememberFolderFromClientA(final FolderObject folderObject){
        this.getCreatedItems().add(folderObject);
    }

    public void rememberFolderFromClientB(final FolderObject folderObject){
        createdItems2.add(folderObject);
    }

}
