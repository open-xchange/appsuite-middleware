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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link GetRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetRequest extends AbstractMailRequest<GetResponse> {

    private final String folder;
    private final String id;
    private final View view;
    private final boolean structure;
    private boolean unseen;
    private boolean source;
    private boolean save;
    private boolean failOnError = true;
    private TimeZone timeZone;

    public GetRequest(final String folder, final String id) {
        this(folder, id, null, false, true);
    }

    public GetRequest(String folder, String id, boolean failOnError) {
        this(folder, id, null, false, failOnError);
    }

    public GetRequest(String folder, String id, boolean structure, boolean failOnError) {
        this(folder, id, null, structure, failOnError);
    }

    public GetRequest(String folder, String id, View view) {
        this(folder, id, view, false, true);
    }

    private GetRequest(String folder, String id, View view, boolean structure, boolean failOnError) {
        super();
        this.folder = folder;
        this.id = id;
        this.view = view;
        this.structure = structure;
        this.failOnError = failOnError;
    }

    public GetRequest setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
        return this;
    }

    public GetRequest setUnseen(boolean unseen) {
        this.unseen = unseen;
        return this;
    }

    public GetRequest setSource(boolean source) {
        this.source = source;
        return this;
    }

    public GetRequest setSave(boolean save) {
        this.save = save;
        return this;
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
    public Parameter[] getParameters() {
        final List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter(AJAXServlet.PARAMETER_ACTION, structure ? AJAXServlet.ACTION_GET_STRUCTURE : AJAXServlet.ACTION_GET));
        l.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folder));
        l.add(new Parameter(AJAXServlet.PARAMETER_ID, id));
        if (null != view && !structure) {
            l.add(new Parameter(Mail.PARAMETER_VIEW, view.value));
        }
        if (unseen) {
            l.add(new Parameter(Mail.PARAMETER_UNSEEN, 1));
        }
        if (source) {
            l.add(new Parameter(Mail.PARAMETER_SRC, 1));
        }
        if (source && isSave()) {
            l.add(new Parameter(Mail.PARAMETER_SAVE, 1));
        }
        if (timeZone != null) {
            l.add(new URLParameter(Mail.PARAMETER_TIMEZONE, timeZone.getID()));
        }
        return l.toArray(new Parameter[l.size()]);
    }

    @Override
    public GetParser getParser() {
        return new GetParser(failOnError);
    }

    public boolean isSave() {
        return save;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    private class GetParser extends AbstractAJAXParser<GetResponse> {

        GetParser(final boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected Response getResponse(String body) throws JSONException {
            if (isSave()) {
                return null;
            }
            return super.getResponse(body);
        }

        @Override
        protected GetResponse createResponse(final Response response) {
            return new GetResponse(response);
        }
    }

    public enum View {
        RAW("raw"),
        TEXT("text"),
        HTML("html");
        String value;
        View(String value) {
            this.value = value;
        }
    }
}
