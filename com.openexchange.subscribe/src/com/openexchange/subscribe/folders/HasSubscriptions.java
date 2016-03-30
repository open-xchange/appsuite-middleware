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

package com.openexchange.subscribe.folders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
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

    private static final Set<String> ID_BLACKLIST = new HashSet<String>(){{
        add(String.valueOf(FolderObject.SYSTEM_GLOBAL_FOLDER_ID));
        add(String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID));
        add(String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
        add(String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID));
        add(String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID));
        add(String.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID));
        add(String.valueOf(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID));
        add(String.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID));
        add(String.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID));
    }};


    @Override
    public int getColumnID() {
        return 3020;
    }

    @Override
    public String getColumnName() {
        return "com.openexchange.subscribe.subscriptionFlag";
    }

    @Override
    public Object getValue(final FolderObject folder, final ServerSession session) {
        return getValues(Arrays.asList(folder), session).get(0);
     }

     @Override
    public List<Object> getValues(final List<FolderObject> folder, final ServerSession session) {
         UserPermissionBits permissionBits = session.getUserPermissionBits();
         if (null == permissionBits || !permissionBits.isPublication()) {
             return allFalse(folder.size());
         }
         final List<String> folderIdsToQuery = new ArrayList<String>(folder.size());
         final List<String> folderIds= new ArrayList<String>(folder.size());

         final Map<String, Boolean> hasSubscriptions = new HashMap<String, Boolean>();
         for (final FolderObject f : folder) {
             String fn = f.getFullName();
             if (fn == null) {
                 fn = String.valueOf(f.getObjectID());
             }
             folderIds.add(fn);
             if (f.getModule() != FolderObject.MAIL && ! ID_BLACKLIST.contains(fn)) {
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
         } catch (final OXException e) {
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
