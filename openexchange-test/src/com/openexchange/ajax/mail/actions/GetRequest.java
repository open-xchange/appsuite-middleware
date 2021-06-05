/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.mail.FullnameArgument;

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
    private boolean estimateLength;
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
        this.folder = new FullnameArgument(folder).getPreparedName();
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

    public GetRequest setEstimateLength(boolean estimateLength) {
        this.estimateLength = estimateLength;
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
        if (estimateLength) {
            l.add(new URLParameter("estimate_length", "true"));
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
