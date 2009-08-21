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

package com.openexchange.ajax.folder.actions;

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.CommonDeleteParser;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link ClearRequest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ClearRequest extends AbstractFolderRequest<CommonDeleteResponse> {

    private final String[] folderIds;

    public ClearRequest(final String[] folderIds) {
        super();
        this.folderIds = folderIds;
    }

    public ClearRequest(final String folderId) {
        this(new String[] { folderId });
    }

    public ClearRequest(final int[] folderIds) {
        super();
        this.folderIds = i2s(folderIds);
    }

    public ClearRequest(final int folderId, final Date lastModified) {
        this(new int[] { folderId });
    }

    public ClearRequest(final FolderObject... folder) {
        super();
        folderIds = new String[folder.length];
        for (int i = 0; i < folder.length; i++) {
            if (folder[i].containsObjectID()) { // task, appointment or contact folder
                folderIds[i] = Integer.valueOf(folder[i].getObjectID()).toString();
            } else { // mail folder
                folderIds[i] = folder[i].getFullName();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object getBody() throws JSONException {
        final JSONArray array = new JSONArray();
        for (final String folderId : folderIds) {
            array.put(folderId);
        }
        return array;
    }

    /**
     * {@inheritDoc}
     */
    public Method getMethod() {
        return Method.PUT;
    }

    /**
     * {@inheritDoc}
     */
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_CLEAR) };
    }

    /**
     * {@inheritDoc}
     */
    public CommonDeleteParser getParser() {
        return new CommonDeleteParser(true);
    }

    private String[] i2s(final int[] intArr) {
        final String[] strArr = new String[intArr.length];
        for (int i = 0; i < intArr.length; i++) {
            strArr[i] = Integer.valueOf(intArr[i]).toString();
        }
        return strArr;
    }
}
