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

package com.openexchange.ajax.contact.action;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.container.Contact;

/**
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class GetRequest extends AbstractContactRequest<GetResponse> {

    private final int folderId;

    private final int objectId;

    private final TimeZone timeZone;

    private final boolean failOnError;

    public GetRequest(final int folderId, final int objectId, TimeZone timeZone, boolean failOnError) {
        super();
        this.folderId = folderId;
        this.objectId = objectId;
        this.timeZone = timeZone;
        this.failOnError = failOnError;
    }

    public GetRequest(final int folderId, final int objectId, TimeZone timeZone) {
        this(folderId, objectId, timeZone, true);
    }

    public GetRequest(final int folderId, final InsertResponse insert, TimeZone timeZone) {
        this(folderId, insert.getId(), timeZone, true);
    }

    public GetRequest(Contact contact, TimeZone timeZone) {
        this(contact.getParentFolderID(), contact.getObjectID(), timeZone, true);
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
        final List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(folderId)));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ID, String.valueOf(objectId)));
        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    @Override
    public GetParser getParser() {
        return new GetParser(failOnError, timeZone);
    }
}
