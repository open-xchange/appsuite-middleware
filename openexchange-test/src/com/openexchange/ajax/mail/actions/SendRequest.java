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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractUploadParser;
import com.openexchange.ajax.framework.Header;

/**
 * {@link SendRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SendRequest implements AJAXRequest<SendResponse> {

    /**
     * URL of the tasks AJAX interface.
     */
    public static final String MAIL_URL = "/ajax/mail";

    private final String mailStr;

    private final List<InputStream> uploads;

    /*
     * Mail object settings
     */

    private String recipientTo;

    private final boolean failOnError;

    /**
     * Initializes a new {@link SendRequest}
     *
     * @param mailStr The mail string (JSON)
     */
    public SendRequest(final String mailStr) {
        this(mailStr, null);
    }

    public SendRequest(String mailStr, boolean failOnError) {
        this(mailStr, null, failOnError);
    }

    public SendRequest(String mailStr, InputStream upload, boolean failOnError) {
        super();
        this.mailStr = mailStr;
        this.uploads = new LinkedList<InputStream>();
        if (null != upload) {
            this.uploads.add(upload);
        }
        this.failOnError = failOnError;
    }

    /**
     * Initializes a new {@link SendRequest}
     *
     * @param mailStr The mail string (JSON)
     * @param upload The upload input stream
     */
    public SendRequest(final String mailStr, final InputStream upload) {
        this(mailStr, upload, true);
    }

    /**
     * Adds an upload
     *
     * @param upload The upload
     */
    public void addUpload(final InputStream upload) {
        this.uploads.add(upload);
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
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> params = new ArrayList<AJAXRequest.Parameter>(4);
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));
        params.add(new FieldParameter("json_0", mailStr));
        if (null != uploads) {
            final int size = uploads.size();
            for (int i = 0; i < size; i++) {
                final String sNum = Integer.toString(i + 1);
                params.add(new FileParameter("file_" + sNum, "text"+sNum+".txt", uploads.get(i), "text/plain; charset=us-ascii"));
            }
        }
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public String getServletPath() {
        return MAIL_URL;
    }

    @Override
    public SendParser getParser() {
        return new SendParser(failOnError);
    }

    private static final class SendParser extends AbstractUploadParser<SendResponse> {

        public SendParser(final boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected SendResponse createResponse(final Response response) {
            return new SendResponse(response);
        }

    }
}
