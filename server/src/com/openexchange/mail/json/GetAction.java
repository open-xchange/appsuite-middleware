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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.json;

import java.util.Locale;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;


/**
 * {@link GetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetAction}.
     * @param services
     */
    public GetAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        final AJAXRequestData request = req.getRequest();
        /*
         * Read in parameters
         */
        final String folderPath = request.checkParameter(Mail.PARAMETER_FOLDERID);
        // final String uid = paramContainer.checkStringParam(PARAMETER_ID);
        String tmp = request.getParameter(Mail.PARAMETER_SHOW_SRC);
        final boolean showMessageSource = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
        tmp = request.getParameter(Mail.PARAMETER_EDIT_DRAFT);
        final boolean editDraft = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
        tmp = request.getParameter(Mail.PARAMETER_SHOW_HEADER);
        final boolean showMessageHeaders = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
        tmp = request.getParameter(Mail.PARAMETER_SAVE);
        final boolean saveToDisk = (tmp != null && tmp.length() > 0 && Integer.parseInt(tmp) > 0);
        tmp = request.getParameter(Mail.PARAMETER_VIEW);
        final String view = null == tmp ? null : tmp.toLowerCase(Locale.ENGLISH);
        tmp = request.getParameter(Mail.PARAMETER_UNSEEN);
        final boolean unseen = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = request.getParameter("token");
        final boolean token = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = request.getParameter("ttlMillis");
        int ttlMillis;
        try {
            ttlMillis = (tmp == null ? -1 : Integer.parseInt(tmp.trim()));
        } catch (final NumberFormatException e) {
            ttlMillis = -1;
        }
        tmp = null;
        
        
        
        
        // TODO Auto-generated method stub
        return null;
    }

}
