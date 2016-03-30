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

package com.openexchange.filemanagement.distributed.servlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
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

    private static final long serialVersionUID = 1L;

    private final ServiceLookup services;

    private final ManagedFileManagement fileManagement;

    private final int port;

    public static final String PATH = "distributedFiles";

    public DistributedFileServlet(ServiceLookup services) {
        this.services = services;
        fileManagement = services.getService(ManagedFileManagement.class);
        port = services.getService(ConfigurationService.class).getIntProperty("com.openexchange.filemanagement.distributed.port", 2003);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String id = crop(uri);

        if (!fileManagement.containsLocal(id)) {
            return;
        }

        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            ManagedFile file = fileManagement.getByID(id);
            inStream = new BufferedInputStream(file.getInputStream(), 65536);
            resp.setContentType(file.getContentType());

            outStream = resp.getOutputStream();
            int bytesRead = 0;

            while ((bytesRead = inStream.read()) != -1) {
                outStream.write(bytesRead);
            }
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(DistributedFileServlet.class).error("", e);
        } finally {
            Streams.close(inStream);
            Streams.close(outStream);
        }
    }

    private String crop(String uri) {
        String[] split = uri.split("/");
        return split[split.length-1];
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String id = crop(uri);

        if (!fileManagement.containsLocal(id)) {
            return;
        }

        try {
            fileManagement.getByID(id);
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(DistributedFileServlet.class).error("", e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String id = crop(uri);

        if (!fileManagement.containsLocal(id)) {
            return;
        }

        try {
            fileManagement.removeByID(id);
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(DistributedFileServlet.class).error("", e);
        }
    }
}
