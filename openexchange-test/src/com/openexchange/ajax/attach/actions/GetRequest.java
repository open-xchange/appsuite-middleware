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

package com.openexchange.ajax.attach.actions;

import static com.openexchange.ajax.framework.AJAXRequest.Method.GET;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.attach.AttachmentTools;
import com.openexchange.groupware.container.CommonObject;

/**
 * {@link GetRequest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class GetRequest extends AbstractAttachmentRequest<GetResponse> {

    private final int folderId;
    private final int attached;
    private final int module;
    private final int id;

    public GetRequest(CommonObject obj, int id) {
        this(obj.getParentFolderID(), obj.getObjectID(), AttachmentTools.determineModule(obj), id);
    }

    public GetRequest(int folderId, int attached, int module, int id) {
        super();
        this.folderId = folderId;
        this.attached = attached;
        this.module = module;
        this.id = id;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return GET;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET), new URLParameter(AJAXServlet.PARAMETER_FOLDERID, folderId), new URLParameter(AJAXServlet.PARAMETER_ATTACHEDID, attached), new URLParameter(AJAXServlet.PARAMETER_MODULE, module), new URLParameter(AJAXServlet.PARAMETER_ID, id)
        };
    }

    @Override
    public GetParser getParser() {
        return new GetParser(false);
    }
}
