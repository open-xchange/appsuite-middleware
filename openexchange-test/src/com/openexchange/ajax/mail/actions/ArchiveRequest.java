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

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link ArchiveRequest}
 *
 */
public class ArchiveRequest extends AbstractMailRequest<ArchiveResponse> {

	private final String sourceFolderID;
	private final boolean failOnError = true;
    private final String[] mailIDs;
    private Boolean useDefaultName;
    private Boolean createIfAbsent;

    public ArchiveRequest(String[] mailIDs, String sourceFolderID){
    	this.mailIDs = mailIDs;
    	this.sourceFolderID = sourceFolderID;
    }

    /**
     * Sets the createIfAbsent
     *
     * @param createIfAbsent The createIfAbsent to set
     */
    public void setCreateIfAbsent(boolean createIfAbsent) {
        this.createIfAbsent = Boolean.valueOf(createIfAbsent);
    }

    /**
     * Sets the useDefaultName
     *
     * @param useDefaultName The useDefaultName to set
     */
    public void setUseDefaultName(boolean useDefaultName) {
        this.useDefaultName = Boolean.valueOf(useDefaultName);
    }

	@Override
    public Object getBody() throws JSONException {
	    int length = mailIDs.length;
	    JSONArray jso = new JSONArray(length);
	    for (int i = 0; i < length; i++) {
            jso.put(mailIDs[i]);
        }
        return jso;
	}

	@Override
    public Method getMethod() {
		return Method.PUT;
	}

	@Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
		List<Parameter> list = new LinkedList<Parameter>();

        list.add(new Parameter(Mail.PARAMETER_ACTION, "archive"));
        list.add(new Parameter(Mail.PARAMETER_FOLDERID, sourceFolderID));
        if (null != useDefaultName) {
            list.add(new Parameter("useDefaultName", useDefaultName.toString()));
        }
        if (null != createIfAbsent) {
            list.add(new Parameter("createIfAbsent", createIfAbsent.toString()));
        }

        return list.toArray(new Parameter[list.size()]);
	}

	@Override
    public AbstractAJAXParser<? extends ArchiveResponse> getParser() {
		return new AbstractAJAXParser<ArchiveResponse>(failOnError) {

            @Override
            protected ArchiveResponse createResponse(final Response response) throws JSONException {
                return new ArchiveResponse(response);
            }
        };
	}

}
