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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.groupware.container.CommonObject;

/**
 * {@link GetDocumentRequest}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetDocumentRequest extends AbstractAttachmentRequest<GetDocumentResponse> {
    
    private final int attached;
    
    private final int moduleID;
    
    private final int folderID;
    
    private final boolean failOnError;

    private final String contentType;
    
    private final int attachmentID;
    
    private final int off;

    private final int len;

    /**
     * Initializes a new {@link GetDocumentRequest}.
     */
    public GetDocumentRequest(CommonObject co, int mid, String ct, int fid, int aid, int off, int len, boolean foe) {
        super();
        attached = co.getObjectID();
        moduleID = mid;
        folderID = fid;
        failOnError = foe;
        contentType = ct;
        attachmentID = aid;
        this.off = off;
        this.len = len;
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.ajax.framework.AJAXRequest#getMethod()
     */
    @Override
    public Method getMethod() {
        return Method.GET;
    }

    /* (non-Javadoc)
     * @see com.openexchange.ajax.framework.AJAXRequest#getParameters()
     */
    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DOCUMENT));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_MODULE, moduleID));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_CONTENT_TYPE, contentType));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_FOLDERID, folderID));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ID, attachmentID));
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ATTACHEDID, attached));
        parameters.add(new URLParameter("off", off));
        parameters.add(new URLParameter("len", len));
        
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    /* (non-Javadoc)
     * @see com.openexchange.ajax.framework.AJAXRequest#getParser()
     */
    @Override
    public AbstractAJAXParser<? extends GetDocumentResponse> getParser() {
        return new GetDocumentParser(failOnError);
    }

    /* (non-Javadoc)
     * @see com.openexchange.ajax.framework.AJAXRequest#getBody()
     */
    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }
}
