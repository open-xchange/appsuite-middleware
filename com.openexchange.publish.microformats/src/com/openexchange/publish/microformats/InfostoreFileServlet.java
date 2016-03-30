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
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.infostore.FileMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.html.HtmlService;
import com.openexchange.java.Strings;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationErrorMessage;
import com.openexchange.publish.tools.PublicationSession;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link InfostoreFileServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class InfostoreFileServlet extends OnlinePublicationServlet {

    private static final long serialVersionUID = -7725853828283398968L;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(InfostoreFileServlet.class);

    private static final String CONTEXTID = "contextId";
    private static final String SITE = "site";
    private static final String INFOSTORE_ID = "infoId";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreFileServlet.class);

    private static OXMFPublicationService infostorePublisher = null;


    public static void setInfostorePublisher(final OXMFPublicationService service) {
        infostorePublisher = service;
    }

    private static volatile IDBasedFileAccessFactory fileFactory;

    public static void setFileFactory(final IDBasedFileAccessFactory service) {
        fileFactory = service;
    }

    private static volatile FileResponseRenderer fileResponseRenderer;

    public static void setFileResponseRenderer(final FileResponseRenderer renderer) {
        fileResponseRenderer = renderer;
    }

    // -------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link InfostoreFileServlet}.
     */
    public InfostoreFileServlet() {
        super();
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final Map<String, String> args = getPublicationArguments(req);
        boolean startedWriting = false;
        try {
            final Context ctx = contexts.getContext(Integer.parseInt(args.get(CONTEXTID)));
            final Publication publication = infostorePublisher.getPublication(ctx, args.get(SITE));
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

            final int infoId = Integer.parseInt(args.get(INFOSTORE_ID));

            final DocumentMetadata metadata = loadMetadata(publication, infoId);

            final InputStream fileData = loadFile(publication, infoId);

            startedWriting = true;
            writeFile(new PublicationSession(publication), metadata, fileData, req, resp);

        } catch (OXException e) {
            if (PublicationErrorMessage.NOT_FOUND_EXCEPTION.equals(e)) {
                // Signal 404 - Not found
                String queryString = req.getQueryString();
                LOGGER.debug("No such file for request: {}{}", req.getRequestURI(), null == queryString ? "" : "?" + queryString);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Signal internal server error
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            if(!startedWriting) {
                e.printStackTrace(resp.getWriter());
            }
            LOG.error("", e);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            if(!startedWriting) {
                e.printStackTrace(resp.getWriter());
            }
            LOG.error("", e);
        }

    }

    protected DocumentMetadata loadMetadata(final Publication publication, final int infoId) throws OXException {
        try {
            IDBasedFileAccessFactory factory = fileFactory;
            if (null == factory) {
                throw ServiceExceptionCode.absentService(IDBasedFileAccessFactory.class);
            }
            if (publication == null) {
                throw PublicationErrorMessage.NOT_FOUND_EXCEPTION.create();
            }
            Session session = new PublicationSession(publication);
            IDBasedFileAccess fileAccess = fileFactory.createAccess(session);
            String id = publication.getEntityId() + '/' + infoId;
            File metadata = fileAccess.getFileMetadata(id, FileStorageFileAccess.CURRENT_VERSION);
            if (null == metadata || null != publication.getEntityId() && false == publication.getEntityId().equals(metadata.getFolderId())) {
                throw PublicationErrorMessage.NOT_FOUND_EXCEPTION.create();
            }
            return FileMetadata.getMetadata(metadata);
        } catch (final OXException e) {
            if (InfostoreExceptionCodes.NOT_EXIST.equals(e) || FileStorageExceptionCodes.FILE_NOT_FOUND.equals(e)) {
                throw PublicationErrorMessage.NOT_FOUND_EXCEPTION.create(e, new Object[0]);
            }
            throw e;
        }
    }

    private void writeFile(final Session session, final DocumentMetadata metadata, final InputStream fileData, final HttpServletRequest req, final HttpServletResponse resp) throws IOException, OXException {
        final FileResponseRenderer renderer = fileResponseRenderer;
        if (null == renderer) {
            throw new IOException("Missing " + FileResponseRenderer.class.getName());
        }
        final ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        final AJAXRequestData request = AJAXRequestDataTools.getInstance().parseRequest(req, false, false, serverSession, "/publications/infostore", resp);
        request.setModule("files");
        request.setAction("document");
        request.setSession(serverSession);
        final AJAXRequestResult result = new AJAXRequestResult(new FileHolder(fileData, metadata.getFileSize(), metadata.getFileMIMEType(), metadata.getFileName()), "file");
        // Set ETag
        final String eTag = FileStorageUtility.getETagFor(Integer.toString(metadata.getId()), Integer.toString(metadata.getVersion()), metadata.getLastModified());
        result.setExpires(0);
        if (eTag != null) {
            result.setHeader("ETag", eTag);
        }
        // Trigger renderer
        renderer.write(request, result, req, resp);
    }

    private InputStream loadFile(final Publication publication, final int infoId) throws OXException {
        final IDBasedFileAccess fileAccess = fileFactory.createAccess(new PublicationSession(publication));
        return fileAccess.getDocument(String.valueOf(infoId), FileStorageFileAccess.CURRENT_VERSION);
    }

    private Map<String, String> getPublicationArguments(final HttpServletRequest req) throws UnsupportedEncodingException {
        // URL format is: /publications/files/[cid]/[siteName]/[infostoreID]/[version]?secret=[secret]

        final String[] path = SPLIT.split(req.getPathInfo(), 0);
        final List<String> normalized = new ArrayList<String>(path.length);
        for (int i = 0; i < path.length; i++) {
            if (!path[i].equals("")) {
                normalized.add(path[i]);
            }
        }

        final String site = Strings.join(HelperClass.decode(normalized.subList(1, normalized.size()-2), req, SPLIT2), "/");
        final Map<String, String> args = new HashMap<String, String>(6);
        args.put(CONTEXTID, normalized.get(0));
        args.put(SITE, site);
        args.put(SECRET, req.getParameter(SECRET));
        args.put(INFOSTORE_ID, normalized.get(normalized.size()-2));
        return args;
    }

}
