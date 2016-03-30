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

package com.openexchange.ajax.request;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Folder;
import com.openexchange.exception.OXException;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FolderRequest} - Folder request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderRequest {

    private final ServerSession session;

    private final JSONWriter pw;

    private static final Folder FOLDER_SERVLET = new Folder();

    public FolderRequest(final ServerSession session, final JSONWriter pw) {
        super();
        this.session = session;
        this.pw = pw;
    }

    public void action(final String action, final JSONObject jsonObject) throws JSONException, OXException {
        if (action.equalsIgnoreCase(AJAXServlet.ACTION_ROOT)) {
            FOLDER_SERVLET.actionGetRoot(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
            FOLDER_SERVLET.actionGetSubfolders(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_PATH)) {
            FOLDER_SERVLET.actionGetPath(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
            FOLDER_SERVLET.actionGetUpdatedFolders(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
            FOLDER_SERVLET.actionGetFolder(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
            FOLDER_SERVLET.actionPutUpdateFolder(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
            FOLDER_SERVLET.actionPutInsertFolder(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
            FOLDER_SERVLET.actionPutDeleteFolder(session, pw, jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_CLEAR)) {
            FOLDER_SERVLET.actionPutClearFolder(session, pw, jsonObject);
        } else {
            throw OXFolderExceptionCode.UNKNOWN_ACTION.create(action);
        }
    }

}
