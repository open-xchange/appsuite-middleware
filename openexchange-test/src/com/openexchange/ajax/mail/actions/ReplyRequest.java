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
import org.json.JSONException;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.java.Strings;

/**
 * {@link ReplyRequest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ReplyRequest extends AbstractMailRequest<ReplyResponse> {

    public enum ViewOption {
        TEXT("text"), HTML("html");

        private String str;

        ViewOption(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return this.str;
        }

        public static ViewOption getBy(String str) {
            for (ViewOption option : values()) {
                if (str.equals(option.toString())) {
                    return option;
                }
            }
            return null;
        }
    }

    protected boolean failOnError;

    protected ViewOption view;

    protected String folderID;

    protected String mailID;

    protected String csid;

    public ReplyRequest(){

    }

    public ReplyRequest(String[] folderAndID){
        this(folderAndID[0], folderAndID[1]);
    }

    public ReplyRequest(String folderID, String mailID){
        this.folderID = folderID;
        this.mailID = mailID;
    }

    public ViewOption getView() {
        return view;
    }


    public void setView(ViewOption view) {
        this.view = view;
    }


    public String getFolderID() {
        return folderID;
    }


    public void setFolderID(String folderID) {
        this.folderID = folderID;
    }

    public String getCsid() {
        return csid;
    }


    public void setCsid(String csid) {
        this.csid = csid;
    }

    public String getMailID() {
        return mailID;
    }


    public void setMailID(String mailID) {
        this.mailID = mailID;
    }

    public boolean isFailOnError() {
        return failOnError;
    }


    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() throws JSONException {
        return null;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    public String getAction(){
        return Mail.ACTION_REPLY;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        List<Parameter> list = new LinkedList<Parameter>();

        list.add( new Parameter(Mail.PARAMETER_ACTION, getAction()) );
        list.add( new Parameter(Mail.PARAMETER_FOLDERID, folderID) );
        list.add( new Parameter(Mail.PARAMETER_ID, mailID) );
        if (getView() != null) {
            list.add( new Parameter(Mail.PARAMETER_VIEW, getView().toString()) );
        }

        if(!Strings.isEmpty(csid)) {
            list.add(new Parameter(Mail.PARAMETER_CSID, csid));
        }

        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends ReplyResponse> getParser() {
        return new AbstractAJAXParser<ReplyResponse>(failOnError) {
            @Override
            protected ReplyResponse createResponse(final Response response) throws JSONException {
                return new ReplyResponse(response);
            }
        };
    }

}
