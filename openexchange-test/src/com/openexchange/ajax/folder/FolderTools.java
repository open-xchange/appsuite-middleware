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

package com.openexchange.ajax.folder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FolderTools {

    /**
     * Prevent instantiation
     */
    private FolderTools() {
        super();
    }

    /**
     * @deprecated the generic type of the request now deals with that.
     */
    @Deprecated
    public static ListResponse list(final AJAXClient client,
        final ListRequest request) throws OXException, IOException,
        SAXException, JSONException {
        return Executor.execute(client, request);
    }

    public static List<FolderObject> getSubFolders(AJAXClient client, String parent, boolean ignoreMailFolder) throws OXException, IOException, SAXException, JSONException, OXException {
        final ListRequest request = new ListRequest(EnumAPI.OX_OLD, parent, ignoreMailFolder);
        final ListResponse response = client.execute(request);
        final List<FolderObject> retval = new ArrayList<FolderObject>();
        final Iterator<FolderObject> iter = response.getFolder();
        while (iter.hasNext()) {
            retval.add(iter.next());
        }
        return retval;
    }

    public static List<FolderObject> convert(Iterator<FolderObject> iter) {
        List<FolderObject> retval = new ArrayList<FolderObject>();
        while (iter.hasNext()) {
            retval.add(iter.next());
        }
        return retval;
    }

    public static void shareFolder(AJAXClient client, API api, int folderId, int userId, int fp, int opr, int opw, int opd) throws OXException, IOException, SAXException, JSONException, OXException, OXException {
        GetRequest getQ = new GetRequest(api, folderId);
        GetResponse getR = client.execute(getQ);
        FolderObject origFolder = getR.getFolder();
        FolderObject changed = new FolderObject();
        changed.setObjectID(folderId);
        changed.setLastModified(getR.getTimestamp());
        List<OCLPermission> permissions = new ArrayList<OCLPermission>();
        for (OCLPermission permission : origFolder.getPermissions()) {
            if (permission.getEntity() != userId) {
                permissions.add(permission);
            }
        }
        OCLPermission addedPerm = new OCLPermission();
        addedPerm.setEntity(userId);
        addedPerm.setAllPermission(fp, opr, opw, opd);
        permissions.add(addedPerm);
        changed.setPermissions(permissions);
        UpdateRequest updQ = new UpdateRequest(api, changed);
        client.execute(updQ);
    }

    public static void unshareFolder(AJAXClient client, API api, int folderId, int userId) throws OXException, IOException, SAXException, JSONException, OXException, OXException {
        GetRequest getQ = new GetRequest(api, folderId);
        GetResponse getR = client.execute(getQ);
        FolderObject origFolder = getR.getFolder();
        List<OCLPermission> permissions = new ArrayList<OCLPermission>();
        permissions.addAll(origFolder.getPermissions());
        Iterator<OCLPermission> iter = permissions.iterator();
        while (iter.hasNext()) {
            if (iter.next().getEntity() == userId) {
                iter.remove();
            }
        }
        FolderObject changed = new FolderObject();
        changed.setObjectID(folderId);
        changed.setLastModified(getR.getTimestamp());
        changed.setPermissions(permissions);
        UpdateRequest updQ = new UpdateRequest(api, changed);
        client.execute(updQ);
    }
}
