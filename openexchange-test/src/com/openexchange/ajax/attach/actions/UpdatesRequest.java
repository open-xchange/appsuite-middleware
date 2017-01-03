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

package com.openexchange.ajax.attach.actions;

import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Attachment;

/**
 * 
 * {@link UpdatesRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class UpdatesRequest extends AbstractAttachmentRequest<UpdatesResponse> {

    private int folderId;
    private int objectId;
    private int moduleId;
    private long timestamp;
    private int[] columns;

    public UpdatesRequest(int folderId, int objectId, int moduleId, int[] columns, long timestamp) {
        super();
        this.folderId = folderId;
        this.objectId = objectId;
        this.moduleId = moduleId;
        this.columns = columns;
        this.timestamp = timestamp;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() throws JSONException {
        return new Parameter[] { 
            new URLParameter(AJAXServlet.PARAMETER_ACTION, Attachment.ACTION_UPDATES),
            new URLParameter(AJAXServlet.PARAMETER_MODULE, moduleId),
            new URLParameter(AJAXServlet.PARAMETER_FOLDERID, folderId),
            new URLParameter(AJAXServlet.PARAMETER_ATTACHEDID, objectId),
            new URLParameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(timestamp)),
            new URLParameter(AJAXServlet.PARAMETER_COLUMNS, columns),
        };
    }

    @Override
    public UpdatesParser getParser() {
        return new UpdatesParser(false);
    }
}
