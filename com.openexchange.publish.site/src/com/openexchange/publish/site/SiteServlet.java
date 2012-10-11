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

package com.openexchange.publish.site;

import static com.openexchange.publish.site.SitePublicationService.SECRET;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.publish.Publication;
import com.openexchange.publish.tools.PublicationSession;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderLoader;
import com.openexchange.tools.oxfolder.OXFolderLoader.IdAndName;

/**
 * {@link SiteServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SiteServlet extends HttpServlet {

    private static final String MODULE = "module";

    private static final String SITE = "site";

    private static final String CONTEXTID = "ctx";

    private static final String USE_WHITELISTING_PROPERTY_NAME = "com.openexchange.publish.microformats.usesWhitelisting";

    protected static final Pattern SPLIT = Pattern.compile("/");

    protected static ContextService contexts = null;

    public static void setContextService(final ContextService service) {
        contexts = service;
    }

    private static volatile IDBasedFileAccessFactory files;

    public static void setFileAccess(final IDBasedFileAccessFactory fileAccess) {
        files = fileAccess;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            final Map<String, Object> args = getPublicationArguments(req);

            final SitePublicationService publisher = SitePublicationService.getInstance();

            final Context ctx = contexts.getContext(Integer.parseInt((String) args.get(CONTEXTID)));
            final Publication publication = publisher.getPublication(ctx, (String) args.get(SECRET));
            if (publication == null || !publication.isEnabled()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().println("Don't know site " + args.get(SITE));

                return;
            }

            final List<String> subpath = (List<String>) args.get(SITE);

            if (subpath.isEmpty()) {
                subpath.add("index.html");
            }

            final IDBasedFileAccess fileAccess = files.createAccess(new PublicationSession(publication));
            final File file = resolve(ctx, fileAccess, subpath, publication.getEntityId());

            if (file == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (file.getFileName() != null && !file.getFileName().equals("")) {
                sendFile(file, resp, fileAccess);
            } else {
                sendDescription(file, resp);
            }

        } catch (final Throwable t) {
            resp.getWriter().println("An exception occurred: ");
            t.printStackTrace(resp.getWriter());
        }
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            final Map<String, Object> args = getPublicationArguments(req);

            final SitePublicationService publisher = SitePublicationService.getInstance();

            final Context ctx = contexts.getContext(Integer.parseInt((String) args.get(CONTEXTID)));
            final Publication publication = publisher.getPublication(ctx, (String) args.get(SECRET));
            if (publication == null || !publication.isEnabled()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().println("Don't know site " + args.get(SITE));

                return;
            }

            final List<String> subpath = (List<String>) args.get(SITE);

            if (subpath.isEmpty()) {
                subpath.add("index.html");
            }

            final IDBasedFileAccess fileAccess = files.createAccess(new PublicationSession(publication));
            final File file = resolve(ctx, fileAccess, subpath, publication.getEntityId());

            if (file == null) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (file.getFileName() == null || "".equals(file.getFileName())) {
                updateFile(file, req, fileAccess);
                resp.getWriter().write("{data: true}");
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

        } catch (final Throwable t) {
            resp.getWriter().println("An exception occurred: ");
            t.printStackTrace(resp.getWriter());
        }
    }

    // TODO: Whitelisting

    private void updateFile(final File file, final HttpServletRequest req, final IDBasedFileAccess fileAccess) throws IOException, OXException {
        BufferedReader r = null;
        final File f = new DefaultFile();
        f.setId(file.getId());
        f.setFolderId(file.getFolderId());
        try {
            final ServletInputStream inputStream = req.getInputStream();
            r = new BufferedReader(new InputStreamReader(inputStream));
            final StringBuilder b = new StringBuilder();
            String l = null;
            while((l = r.readLine()) != null) {
                b.append(l).append('\n');
            }
            f.setDescription(b.toString());

            fileAccess.saveFileMetadata(f, file.getSequenceNumber());
        } finally {
            if (r != null) {
                r.close();
            }
        }

    }

    private void sendDescription(final File file, final HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().print(file.getDescription());
    }

    private void sendFile(final File file, final HttpServletResponse resp, final IDBasedFileAccess files) throws OXException, IOException {
        resp.setContentType(file.getFileMIMEType());
        resp.setContentLength((int) file.getFileSize());

        InputStream document = null;

        try {
            document = new BufferedInputStream(files.getDocument(file.getId(), file.getVersion()));
            final ServletOutputStream outputStream = resp.getOutputStream();
            int d = 0;
            while ((d = document.read()) != -1) {
                outputStream.write(d);
            }
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    private File resolve(final Context ctx, final IDBasedFileAccess files, final List<String> subpath, final String entityId) throws Exception {
        if (entityId == null) {
            return null;
        }
        final String currentElement = subpath.get(0);

        final boolean needFile = subpath.size() == 1;

        if (needFile) {
            return getFileWithName(files, entityId, currentElement);
        } else {
            return resolve(ctx, files, subpath.subList(1, subpath.size()), getFolderWithName(ctx, entityId, currentElement));
        }
    }

    private String getFolderWithName(final Context ctx, final String entityId, final String currentElement) throws Exception {
        final List<IdAndName> subfolderIdAndNames = OXFolderLoader.getSubfolderIdAndNames(Integer.parseInt(entityId), ctx, null);
        for (final IdAndName idAndName : subfolderIdAndNames) {
            if (idAndName.getName().equals(currentElement)) {
                return Integer.valueOf(idAndName.getFolderId()).toString();
            }
        }
        return null;
    }

    private File getFileWithName(final IDBasedFileAccess files, final String entityId, final String filename) throws OXException {
        final TimedResult<File> documents = files.getDocuments(entityId);
        final SearchIterator<File> results = documents.results();
        try {
            while (results.hasNext()) {
                final File next = results.next();
                final String name = next.getFileName();
                final String title = next.getTitle();
                if ((name != null && name.equals(filename)) || (title != null && title.equals(filename))) {
                    return next;
                }
            }
        } finally {
            results.close();
        }
        return null;
    }

    private Map<String, Object> getPublicationArguments(final HttpServletRequest req) throws UnsupportedEncodingException {
        final String[] path = SPLIT.split(req.getPathInfo(), 0);
        final List<String> normalized = new ArrayList<String>(path.length);
        for (int i = 0; i < path.length; i++) {
            if (!path[i].equals("")) {
                normalized.add(path[i]);
            }
        }

        final Map<String, Object> args = new HashMap<String, Object>();
        args.put(CONTEXTID, normalized.get(0));
        args.put(SECRET, normalized.get(1));
        args.put(SITE, normalized.subList(2, normalized.size()));
        return args;
    }

    protected boolean checkProtected(final Publication publication, final Map<String, Object> args, final HttpServletResponse resp) throws IOException {
        final Map<String, Object> configuration = publication.getConfiguration();
        final String secret = (String) configuration.get(SECRET);
        if (!secret.equals(args.get(SECRET))) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().println("Cannot find the publication site.");
            return false;
        }
        return true;
    }

    protected Collection<String> decode(final List<String> subList, final HttpServletRequest req) throws UnsupportedEncodingException {
        final String encoding = req.getCharacterEncoding() == null ? "UTF-8" : req.getCharacterEncoding();
        final List<String> decoded = new ArrayList<String>();
        for (final String component : subList) {
            final String decodedComponent = decode(component, encoding);
            decoded.add(decodedComponent);
        }
        return decoded;
    }

    private String decode(final String string, final String encoding) throws UnsupportedEncodingException {
        final String[] chunks = string.split("\\+");
        final StringBuilder decoded = new StringBuilder(string.length());
        final boolean endsWithPlus = string.endsWith("+");
        for (int i = 0; i < chunks.length; i++) {
            final String chunk = chunks[i];
            decoded.append(URLDecoder.decode(chunk, encoding));
            if (i != chunks.length - 1 || endsWithPlus) {
                decoded.append('+');
            }
        }

        return decoded.toString();
    }

}
