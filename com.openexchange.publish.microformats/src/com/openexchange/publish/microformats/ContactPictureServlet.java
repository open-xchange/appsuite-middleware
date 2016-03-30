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

package com.openexchange.publish.microformats;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.html.HtmlService;
import com.openexchange.java.Strings;
import com.openexchange.publish.Publication;
import com.openexchange.publish.tools.PublicationSession;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link ContactPictureServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactPictureServlet extends OnlinePublicationServlet {

    private static final long serialVersionUID = -2401315328817932765L;

    private static final String CONTEXTID = "contextId";
    private static final String SITE = "site";
    private static final String CONTACT_ID = "contactId";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactPictureServlet.class);

    private static OXMFPublicationService contactPublisher = null;

    public static void setContactPublisher(final OXMFPublicationService service) {
        contactPublisher = service;
    }

    private static volatile ContactService contacts;

    public static void setContactService(final ContactService service) {
        contacts = service;
    }

    private static FileResponseRenderer fileResponseRenderer = null;

    public static void setFileResponseRenderer(final FileResponseRenderer renderer) {
    	fileResponseRenderer = renderer;
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final Map<String, String> args = getPublicationArguments(req);
        try {
            final Context ctx = contexts.getContext(Integer.parseInt(args.get(CONTEXTID)));
            final Publication publication = contactPublisher.getPublication(ctx, args.get(SITE));
            if (publication == null || !publication.isEnabled()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                final PrintWriter writer = resp.getWriter();
                final HtmlService htmlService = MicroformatServlet.htmlService;
                writer.println("Unknown site " + (null == htmlService ? "" : htmlService.encodeForHTML(args.get(SITE))));
                writer.flush();
                return;
            }
            if (!checkProtected(publication, args, resp)) {
                return;
            }

            if (!checkPublicationPermission(publication, resp)) {
                return;
            }

            final int folderId = Integer.parseInt(publication.getEntityId());
            final int contactId = Integer.parseInt(args.get(CONTACT_ID));

            final Contact contact = loadContact(publication, folderId, contactId);

            writeImage(contact, req, resp, new PublicationSession(publication));

        } catch (final Throwable t) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            t.printStackTrace(resp.getWriter());
            LOG.error("", t);
        }

    }

    private void writeImage(final Contact contact, final HttpServletRequest req, final HttpServletResponse resp, final Session session) throws IOException, OXException {

        final AJAXRequestData request = AJAXRequestDataTools.getInstance().parseRequest(req, false, false, ServerSessionAdapter.valueOf(session), "/publications/", resp);

        final ByteArrayFileHolder holder = new ByteArrayFileHolder(contact.getImage1());
        holder.setContentType(contact.getImageContentType());
        final AJAXRequestResult result = new AJAXRequestResult(holder, "file");


        fileResponseRenderer.write(request, result, req, resp);

    }

    private Contact loadContact(final Publication publication, final int folderId, final int contactId) throws OXException {
    	return contacts.getContact(new PublicationSession(publication), Integer.toString(folderId), Integer.toString(contactId),
    			new ContactField[] { ContactField.IMAGE1, ContactField.IMAGE_LAST_MODIFIED } );
    }

    private Map<String, String> getPublicationArguments(final HttpServletRequest req) throws UnsupportedEncodingException {
        // URL format is: /publications/contactPictures/[cid]/[siteName]/[contactID]/[displayName]?secret=[secret]

        final String[] path = SPLIT.split(req.getPathInfo(), 0);
        final List<String> normalized = new ArrayList<String>(path.length);
        for (int i = 0; i < path.length; i++) {
            if (!path[i].equals("")) {
                normalized.add(path[i]);
            }
        }

        final String site = Strings.join(HelperClass.decode(normalized.subList(1, normalized.size()-2), req,SPLIT2), "/");
        final Map<String, String> args = new HashMap<String, String>();
        args.put(CONTEXTID, normalized.get(0));
        args.put(SITE, site);
        args.put(SECRET, req.getParameter(SECRET));
        args.put(CONTACT_ID, normalized.get(normalized.size()-2));
        return args;
    }

}
