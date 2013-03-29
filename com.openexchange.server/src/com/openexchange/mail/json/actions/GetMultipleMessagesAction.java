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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.server.ServiceLookup;

/**
 * {@link GetMultipleMessagesAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "zip_messages", description = "Get multiple mails as a ZIP file.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", description = "The folder identifier."),
    @Parameter(name = "id", description = "A comma-separated list of Object IDs of the requested mails.")
}, responseDescription = "The raw byte data of the ZIP file.")
public final class GetMultipleMessagesAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetMultipleMessagesAction}.
     *
     * @param services
     */
    public GetMultipleMessagesAction(final ServiceLookup services) {
        super(services);
    }

    /**
     * Split pattern for CSV.
     */
    private static final Pattern SPLIT = Pattern.compile(" *, *");

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException, JSONException {
        try {
            // final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            final String[] ids;
            {
                final String parameterId = AJAXServlet.PARAMETER_ID;
                final String sIds = req.getParameter(parameterId);
                if (null == sIds) {
                    final JSONArray jArray = (JSONArray) req.getRequest().getData();
                    final int length = jArray.length();
                    ids = new String[length];
                    for (int i = 0; i < length; i++) {
                        ids[i] = jArray.getJSONObject(i).getString(parameterId);
                    }
                } else {
                    ids = SPLIT.split(sIds, 0);
                }
            }
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            ManagedFile mf = null;
            try {
                mf = mailInterface.getMessages(folderPath, ids);
                /*
                 * Set Content-Type and Content-Disposition header
                 */
                final String fileName = "mails.zip";
                /*
                 * We are supposed to offer attachment for download. Therefore enforce application/octet-stream and attachment disposition.
                 */
                final ThresholdFileHolder fileHolder = new ThresholdFileHolder();
                /*
                 * Write from content's input stream to response output stream
                 */
                {
                    final InputStream zipInputStream = mf.getInputStream();
                    try {
                        fileHolder.write(zipInputStream);
                    } finally {
                        Streams.close(zipInputStream);
                    }
                }
                /*
                 * Parameterize file holder
                 */
                req.getRequest().setFormat("file");
                fileHolder.setName(fileName);
                fileHolder.setContentType("application/octet-stream");
                return new AJAXRequestResult(fileHolder, "file");
            } finally {
                if (null != mf) {
                    mf.delete();
                    mf = null;
                }
            }
        } catch (final OXException e) {
            if (e.getCause() instanceof IOException) {
                final IOException ioe = (IOException) e.getCause();
                if ("com.sun.mail.util.MessageRemovedIOException".equals(ioe.getClass().getName())) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(ioe);
                }
            }
            throw e;
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
