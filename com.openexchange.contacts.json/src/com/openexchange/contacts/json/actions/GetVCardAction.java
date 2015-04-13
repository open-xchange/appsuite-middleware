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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.contacts.json.actions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;


/**
 * {@link GetVCardAction} - Gets session user's VCard.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class GetVCardAction extends ContactAction {

    /**
     * Initializes a new {@link GetVCardAction}.
     *
     * @param serviceLookup The OSGi service look-up
     */
    public GetVCardAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(ContactRequest request) throws OXException {
        ServerSession session = request.getSession();
        Contact contact = getContactService().getUser(session, session.getUserId());

        OXContainerConverter converter = new OXContainerConverter(session, session.getContext());
        OutputStream out = null;
        try {
            VersitObject versitObj = converter.convertContact(contact, "3.0");
            ByteArrayOutputStream os = Streams.newByteArrayOutputStream();
            String mimeTextVcard = MimeTypes.MIME_TEXT_VCARD;
            VersitDefinition def = Versit.getDefinition(mimeTextVcard);
            VersitDefinition.Writer w = def.getWriter(os, "UTF-8");
            def.write(w, versitObj);
            w.flush();
            os.flush();

            AJAXRequestData ajaxRequestData = request.getRequest();
            if (ajaxRequestData.setResponseHeader("Content-Type", mimeTextVcard)) {
                // Set HTTP response headers
                {
                    final StringBuilder sb = new StringBuilder(512);
                    sb.append("attachment");
                    DownloadUtility.appendFilenameParameter("vcard.vcf", mimeTextVcard, ajaxRequestData.getUserAgent(), sb);
                    ajaxRequestData.setResponseHeader("Content-Disposition", sb.toString());
                }

                // Write content
                out = ajaxRequestData.optOutputStream();
                if (null != out) {
                    out.write(os.toByteArray());
                    out.flush();
                    Streams.close(out);
                    out = null;

                    // Signal direct response
                    return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
                }
            }

            ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(os.toByteArray());
            fileHolder.setDisposition("attachment");
            fileHolder.setName("vcard.vcf");
            fileHolder.setContentType(mimeTextVcard);
            fileHolder.setDelivery("download");

            ajaxRequestData.setFormat("file");
            return new AJAXRequestResult(fileHolder, "file");
        } catch (ConverterException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            converter.close();
            Streams.close(out);
        }
    }

}
