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

package com.openexchange.ajax.folder.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.infostore.thirdparty.actions.AbstractFileRequest;

/**
 * 
 * {@link DetachRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class DetachRequest extends AbstractFileRequest<DetachResponse> {

    private final String id;
    private final Date timestamp;
    private final int[] versions;

    public DetachRequest(String id, Date timestamp) {
        this(id, timestamp, null);
    }

    public DetachRequest(String id, Date timestamp, final int[] versions) {
        super(false);
        this.id = id;
        this.timestamp = timestamp;
        this.versions = versions;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public JSONArray getBody() throws IOException, JSONException {
        if (this.versions != null) {
            final StringBuffer data = new StringBuffer("[");

            if (versions.length > 0) {
                for (final int id : versions) {
                    data.append(id);
                    data.append(',');
                }
                data.deleteCharAt(data.length() - 1);
            }

            data.append(']');
            return new JSONArray(data.toString());
        }
        return null;
    }

    @Override
    public DetachParser getParser() {
        return new DetachParser(false);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "detach"));
        if (this.id != null) {
            params.add(new Parameter("id", id));
        }
        if (this.timestamp != null) {
            params.add(new Parameter("timestamp", timestamp.getTime()));
        }
        return params.toArray(new Parameter[params.size()]);
    }
}
