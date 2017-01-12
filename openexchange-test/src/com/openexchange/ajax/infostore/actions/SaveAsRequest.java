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

package com.openexchange.ajax.infostore.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.infostore.thirdparty.actions.AbstractFileRequest;

/**
 * 
 * {@link SaveAsRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class SaveAsRequest extends AbstractFileRequest<SaveAsResponse> {

    private final String folderId;
    private final int attached;
    private final int module;
    private final int attachment;
    private Map<String, String> body;

    public SaveAsRequest(final String folderId, final int attached, final int module, final int attachment, final Map<String, String> body) {
        super(false);
        this.folderId = folderId;
        this.attached = attached;
        this.module = module;
        this.attachment = attachment;
        this.body = body;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public JSONObject getBody() throws IOException, JSONException {
        return toJSONArgs(body);
    }

    @Override
    public SaveAsParser getParser() {
        return new SaveAsParser(failOnError);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "saveAs"));
        params.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        params.add(new Parameter(AJAXServlet.PARAMETER_ATTACHEDID, this.attached));
        params.add(new Parameter(AJAXServlet.PARAMETER_MODULE, this.module));
        params.add(new Parameter(AJAXServlet.PARAMETER_ATTACHMENT, this.attachment));

        return params.toArray(new Parameter[params.size()]);
    }

    private JSONObject toJSONArgs(final Map<String, String> modified) throws JSONException {
        final JSONObject obj = new JSONObject();
        for (final String attr : modified.keySet()) {
            obj.put(attr, modified.get(attr));
        }
        return obj;
    }
}
