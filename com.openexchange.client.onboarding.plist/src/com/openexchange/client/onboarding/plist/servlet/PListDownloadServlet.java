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

package com.openexchange.client.onboarding.plist.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.client.onboarding.download.DownloadLinkProvider;
import com.openexchange.client.onboarding.download.DownloadParameters;
import com.openexchange.client.onboarding.plist.OnboardingPlistProvider;
import com.openexchange.client.onboarding.plist.PListSigner;
import com.openexchange.client.onboarding.plist.osgi.Services;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.plist.PListDict;
import com.openexchange.plist.PListWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.webdav.WebDavServlet;

/**
 * {@link PListDownloadServlet}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class PListDownloadServlet extends WebDavServlet {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -175037413514512006L;

    public static final String SERVLET_PATH = "plist";
    private static final Logger LOG = LoggerFactory.getLogger(PListDownloadServlet.class);

    private final ServiceLookup lookup;

    /**
     * Initializes a new {@link PListDownloadServlet}.
     */
    public PListDownloadServlet(ServiceLookup lookup) {
        super();
        this.lookup = lookup;

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        InputStream filestream = null;
        ThresholdFileHolder fileHolder = null;
        try {
            int fileSize = -1;

            String fileName = req.getPathInfo();
            if (fileName == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("text/plain");
                resp.getWriter().println("No file name was given.");
                return;
            }

            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1, fileName.length());
            }

            DownloadLinkProvider downloadLinkProvider = lookup.getService(DownloadLinkProvider.class);
            DownloadParameters parameters;
            try {
                parameters = downloadLinkProvider.getParameter(req.getPathInfo());
            } catch (OXException e) {
                LOG.error("", e);
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String scenarioId = parameters.getScenarioId();
            Device device = Device.deviceFor(parameters.getDeviceId());
            int userId = parameters.getUserId();
            int contextId = parameters.getContextId();

            try {
                if (false == downloadLinkProvider.validateChallenge(userId, contextId, scenarioId, device.getId(), parameters.getChallenge())) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            } catch (OXException e) {
                LOG.error("", e);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            Scenario scenario = null;
            PListDict plist = null;
            try {
                OnboardingService onboardingService = lookup.getService(OnboardingService.class);
                scenario = onboardingService.getScenario(scenarioId, device, userId, contextId);
                String hostName = determineHostName(req, userId, contextId);

                for (OnboardingProvider provider : scenario.getProviders(userId, contextId)) {
                    if (provider instanceof OnboardingPlistProvider) {
                        plist = ((OnboardingPlistProvider) provider).getPlist(plist, scenario, hostName, userId, contextId);
                    }
                }
            } catch (OXException e) {
                LOG.error("", e);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (plist == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            fileHolder = new ThresholdFileHolder();
            fileHolder.setDisposition("attachment");
            fileHolder.setName(scenario.getId() + ".mobileconfig");
            fileHolder.setContentType("application/x-apple-aspen-config");// Or application/x-plist ?
            fileHolder.setDelivery("download");
            new PListWriter().write(plist, fileHolder.asOutputStream());

            // Sign it
            try {
                fileHolder = sign(fileHolder, userId, contextId);
                filestream = fileHolder.getClosingStream();
            } catch (OXException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                LOG.error(e.getMessage());
                return;
            }
            fileSize = (int) fileHolder.getLength();
            if (filestream == null || fileSize == 0) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/octet-stream");
            resp.setContentLength(fileSize);
            resp.setHeader("Content-disposition", "attachment; filename=\"" + scenarioId + ".mobileconfig\"");
            Tools.removeCachingHeader(resp);

            OutputStream out = resp.getOutputStream();
            byte[] buf = new byte[4096];
            for (int read; (read = filestream.read(buf)) > 0;) {
                out.write(buf, 0, read);
            }
            out.flush();
            filestream.close();
        } finally {
            Streams.close(filestream, fileHolder);
        }
    }

    private String determineHostName(final HttpServletRequest req, int userId, int contextId) {
        String hostName = null;

        {
            HostnameService hostnameService = Services.optService(HostnameService.class);
            if (null != hostnameService) {
                hostName = hostnameService.getHostname(userId, contextId);
            }
        }

        // Get from request
        if (Strings.isEmpty(hostName)) {
            hostName = req.getServerName();
        }

        // Get from java
        if (Strings.isEmpty(hostName)) {
            try {
                hostName = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                // ignore
            }
        }

        // Fall back to localhost as last resort
        if (Strings.isEmpty(hostName)) {
            hostName = "localhost";
        }

        return hostName;
    }

    private ThresholdFileHolder sign(ThresholdFileHolder fileHolder, int userId, int contextId) throws OXException, IOException {
        PListSigner signer = lookup.getService(PListSigner.class);
        IFileHolder signed = signer.signPList(fileHolder, userId, contextId);

        if (signed instanceof ThresholdFileHolder) {
            return (ThresholdFileHolder) signed;
        }

        ThresholdFileHolder tfh = new ThresholdFileHolder(signed);
        signed.close();
        return tfh;
    }
}
