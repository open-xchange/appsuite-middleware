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

package com.openexchange.ajax.mail.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.webdav.xml.fields.CommonFields;

/**
 * {@link ImportMailRequest} - The request for <code>/ajax/mail?action=import</code>.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ImportMailRequest extends AbstractMailRequest<ImportMailResponse> {

    private final String folder;
    private final InputStream[] rfc822;
    private final int flags;
    private final boolean failOnError;
    private final boolean preserveReceivedDate;
    private boolean strictParsing = true;

    public ImportMailRequest(String folder, int flags, boolean failOnError, InputStream... rfc822) {
        this(folder, flags, failOnError, false, rfc822);
    }

    public ImportMailRequest(String folder, int flags, boolean failOnError, boolean preserveReceivedDate, InputStream... rfc822) {
        super();
        this.preserveReceivedDate = preserveReceivedDate;
        this.folder = folder;
        this.rfc822 = rfc822;
        this.flags = flags;
        this.failOnError = failOnError;
    }

    public ImportMailRequest(String folder, int flags, InputStream... rfc822) {
        this(folder, flags, true, rfc822);
    }

    public ImportMailRequest(String folder, int flags, Charset charset, String... mails) {
        this(folder, flags, true, toStreams(charset, mails));
    }

    public ImportMailRequest setStrictParsing(boolean strictParsing) {
        this.strictParsing = strictParsing;
        return this;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.UPLOAD;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> list = new LinkedList<Parameter>();
        list.add(new URLParameter(Mail.PARAMETER_ACTION, Mail.ACTION_IMPORT));
        list.add(new URLParameter(Mail.PARAMETER_FOLDERID, folder));
        list.add(new URLParameter("force", "true"));
        list.add(new URLParameter("preserveReceivedDate", preserveReceivedDate));
        list.add(new URLParameter("strictParsing", strictParsing));
        if (flags >= 0) {
            list.add(new URLParameter(Mail.PARAMETER_FLAGS, flags));
        }
        int i = 0;
        for (InputStream is : rfc822) {
            list.add(new FileParameter("mail" + (i++), "mail.eml", is, "text/rfc822"));
        }
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<ImportMailResponse> getParser() {
        return new AbstractAJAXParser<ImportMailResponse>(failOnError) {
            @Override
            protected ImportMailResponse createResponse(final Response response) throws JSONException {
                ImportMailResponse retval = new ImportMailResponse(response);
                Object tmp = response.getData();
                if (tmp instanceof JSONObject) {
                    JSONObject json = (JSONObject) response.getData();
                    retval.setIds(new String[][] { parseIds(json) });
                } else if (tmp instanceof JSONArray) {
                    JSONArray json = (JSONArray) response.getData();
                    String[][] ids = new String[json.length()][];
                    for (int i = 0; i < json.length(); i++) {
                        ids[i] = parseIds(json.getJSONObject(i));
                    }
                    retval.setIds(ids);
                }
                return retval;
            }

            private String[] parseIds(JSONObject json) throws JSONException {
                String[] retval = new String[2];
                if (json.has(CommonFields.FOLDER_ID)) {
                    retval[0] = json.getString(CommonFields.FOLDER_ID);
                }
                if (json.has(CommonFields.ID)) {
                    retval[1] = json.getString(CommonFields.ID);
                }
                return retval;
            }
        };
    }

    private static final InputStream[] toStreams(Charset charset, String... mails) {
        InputStream[] retval = new InputStream[mails.length];
        for (int i = 0; i < mails.length; i++) {
            ByteBuffer buffer = charset.encode(mails[i]);
            retval[i] = new ByteArrayInputStream(buffer.array(), buffer.position(), buffer.limit());
        }
        return retval;
    }
}
