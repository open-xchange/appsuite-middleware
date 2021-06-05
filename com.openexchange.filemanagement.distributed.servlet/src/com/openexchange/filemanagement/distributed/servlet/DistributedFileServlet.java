/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.filemanagement.distributed.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.DistributedFileUtils;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DistributedFileServlet}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class DistributedFileServlet extends HttpServlet {

    private static final long serialVersionUID = -3293278521646131568L;

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DistributedFileServlet.class);
    }

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DistributedFileServlet}.
     *
     * @param services The service look-up
     */
    public DistributedFileServlet(ServiceLookup services) {
        super();
        this.services = services;
    }

    private Optional<ManagedFileManagement> getFileManagement(HttpServletResponse resp) throws IOException {
        try {
            return Optional.of(services.getServiceSafe(ManagedFileManagement.class));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing service");
            return Optional.empty();
        }
    }

    private Optional<String> extractId(String uri, HttpServletResponse resp) throws IOException {
        int pos = uri.lastIndexOf('/');
        if (pos < 0) {
            return Optional.of(uri);
        }

        String extractedId = uri.substring(pos + 1);
        return decodeId(extractedId, resp);
    }

    private Optional<String> decodeId(String encodedId, HttpServletResponse resp) throws IOException {
        try {
            return Optional.of(services.getServiceSafe(DistributedFileUtils.class).decodeId(encodedId));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing service");
            return Optional.empty();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> optionalId = extractId(req.getRequestURI(), resp);
        if (!optionalId.isPresent()) {
            return;
        }
        String id = optionalId.get();

        Optional<ManagedFileManagement> optionalFileManagement = getFileManagement(resp);
        if (!optionalFileManagement.isPresent()) {
            return;
        }

        ManagedFileManagement fileManagement = optionalFileManagement.get();
        if (!fileManagement.containsLocal(id)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No such file");
            return;
        }

        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            ManagedFile file = fileManagement.getByID(id);
            inStream = file.getInputStream();
            resp.setContentType(file.getContentType());

            outStream = resp.getOutputStream();
            int buflen = 65536;
            byte[] buf = new byte[buflen];
            for (int read; (read = inStream.read(buf, 0, buflen)) > 0;) {
                outStream.write(buf, 0, read);
            }
            outStream.flush();
        } catch (OXException e) {
            LoggerHolder.LOG.error("", e);
        } finally {
            Streams.close(inStream);
            Streams.close(outStream);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> optionalId = extractId(req.getRequestURI(), resp);
        if (!optionalId.isPresent()) {
            return;
        }
        String id = optionalId.get();

        Optional<ManagedFileManagement> optionalFileManagement = getFileManagement(resp);
        if (!optionalFileManagement.isPresent()) {
            return;
        }

        ManagedFileManagement fileManagement = optionalFileManagement.get();
        if (!fileManagement.containsLocal(id)) {
            return;
        }

        try {
            fileManagement.getByID(id);
        } catch (OXException e) {
            LoggerHolder.LOG.error("", e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> optionalId = extractId(req.getRequestURI(), resp);
        if (!optionalId.isPresent()) {
            return;
        }
        String id = optionalId.get();

        Optional<ManagedFileManagement> optionalFileManagement = getFileManagement(resp);
        if (!optionalFileManagement.isPresent()) {
            return;
        }

        ManagedFileManagement fileManagement = optionalFileManagement.get();
        if (!fileManagement.containsLocal(id)) {
            return;
        }

        try {
            fileManagement.removeByID(id);
        } catch (OXException e) {
            LoggerHolder.LOG.error("", e);
        }
    }
}
