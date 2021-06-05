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

package com.openexchange.subscribe.folders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link HasSubscriptions}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HasSubscriptions implements AdditionalFolderField {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HasSubscriptions.class);

    private static final Set<String> ID_BLACKLIST = ImmutableSet.of(
        /*String.valueOf(FolderObject.SYSTEM_GLOBAL_FOLDER_ID),*/ // finally dropped
        String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID),
        String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID),
        String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID),
        String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID),
        String.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID),
        String.valueOf(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID),
        String.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID),
        String.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID));


    @Override
    public int getColumnID() {
        return 3020;
    }

    @Override
    public String getColumnName() {
        return "com.openexchange.subscribe.subscriptionFlag";
    }

    @Override
    public Object getValue(final Folder folder, final ServerSession session) {
        return getValues(Arrays.asList(folder), session).get(0);
     }

     @Override
     public List<Object> getValues(final List<Folder> folder, final ServerSession session) {
         UserPermissionBits permissionBits = session.getUserPermissionBits();
         if (null == permissionBits || !permissionBits.isPublication()) {
             return allFalse(folder.size());
         }
         final List<String> folderIdsToQuery = new ArrayList<String>(folder.size());
         final List<String> folderIds= new ArrayList<String>(folder.size());

         final Map<String, Boolean> hasSubscriptions = new HashMap<String, Boolean>();
         for (final Folder f : folder) {
             String fn = f.getID();
             folderIds.add(fn);
             ContentType contentType = f.getContentType();
             if (null != contentType && FolderObject.MAIL != contentType.getModule() && !ID_BLACKLIST.contains(fn)) {
                 folderIdsToQuery.add(fn);
             } else {
                 hasSubscriptions.put(fn, Boolean.FALSE);
             }
         }
         if (folderIdsToQuery.isEmpty()) {
             return allFalse(folder.size());
         }
         try {
             hasSubscriptions.putAll(AbstractSubscribeService.STORAGE.get().hasSubscriptions(session.getContext(), folderIdsToQuery));
             final List<Object> retval = new ArrayList<Object>(folder.size());
             for(final String fn : folderIds) {
                 if (hasSubscriptions.get(fn).booleanValue()) {
                     retval.add(Boolean.TRUE);
                 } else {
                     retval.add(Boolean.FALSE);
                 }
             }
             return retval;
         } catch (OXException e) {
             LOG.error("", e);
         }
         return allFalse(folder.size());
     }

     private List<Object> allFalse(final int size) {
         final List<Object> retval = new ArrayList<Object>(size);
         for(int i = 0; i < size; i++) {
             retval.add(Boolean.FALSE);
         }
         return retval;
     }

     @Override
    public Object renderJSON(AJAXRequestData requestData, final Object value) {
         return value;
     }

}
