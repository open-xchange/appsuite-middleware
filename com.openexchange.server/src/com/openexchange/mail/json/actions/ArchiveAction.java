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

package com.openexchange.mail.json.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ArchiveAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public final class ArchiveAction extends AbstractArchiveMailAction {

    /**
     * Initializes a new {@link ArchiveAction}.
     *
     * @param services
     */
    public ArchiveAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult performArchive(final MailRequest req) throws OXException {
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderId = req.getParameter(AJAXServlet.PARAMETER_FOLDERID);
            boolean useDefaultName = AJAXRequestDataTools.parseBoolParameter("useDefaultName", req.getRequest(), true);
            boolean createIfAbsent = AJAXRequestDataTools.parseBoolParameter("createIfAbsent", req.getRequest(), true);
            JSONArray jArray = ((JSONArray) req.getRequest().getData());
            if (null == jArray) {
                throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
            }
            int length = jArray.length();

            if (folderId == null) {

                ArrayList<String[]> paraList = new ArrayList<String[]>(length);
                for (int i = 0; i < length; i++) {
                    JSONObject jObject = jArray.getJSONObject(i);
                    String folder = jObject.getString(AJAXServlet.PARAMETER_FOLDERID);
                    String id = jObject.getString(AJAXServlet.PARAMETER_ID);
                    paraList.add(new String[] { folder, id });
                }
                /*
                 * Get mail interface
                 */
                final MailServletInterface mailInterface = getMailInterface(req);

                List<ArchiveDataWrapper> retval = mailInterface.archiveMultipleMail(paraList, session, useDefaultName, createIfAbsent);
                if (retval == null) {
                    return new AJAXRequestResult(Boolean.TRUE, "native");
                } else {
                    JSONArray json = new JSONArray();
                    for (ArchiveDataWrapper obj : retval) {
                        JSONObject tmp = new JSONObject();
                        tmp.put("id", obj.getId());
                        tmp.put("created", obj.isCreated());
                        json.put(tmp);
                    }
                    return new AJAXRequestResult(json, "json");
                }

            } else {
                ArrayList<String> ids = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    ids.add(jArray.getString(i));
                }
                /*
                 * Get mail interface
                 */
                final MailServletInterface mailInterface = getMailInterface(req);
                List<ArchiveDataWrapper> retval = mailInterface.archiveMail(folderId, ids, session, useDefaultName, createIfAbsent);
                if (retval == null) {
                    return new AJAXRequestResult(Boolean.TRUE, "native");
                } else {
                    JSONArray json = new JSONArray();
                    for (ArchiveDataWrapper obj : retval) {
                        JSONObject tmp = new JSONObject();
                        tmp.put("id", obj.getId());
                        tmp.put("created", obj.isCreated());
                        json.put(tmp);
                    }
                    return new AJAXRequestResult(json, "json");
                }
            }
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
