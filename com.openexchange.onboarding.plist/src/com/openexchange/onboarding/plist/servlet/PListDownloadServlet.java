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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

package com.openexchange.onboarding.plist.servlet;

import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.helper.BrowserDetector;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.onboarding.DefaultOnboardingRequest;
import com.openexchange.onboarding.Device;
import com.openexchange.onboarding.OnboardingAction;
import com.openexchange.onboarding.OnboardingProvider;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.Scenario;
import com.openexchange.onboarding.plist.OnboardingPlistProvider;
import com.openexchange.onboarding.plist.PListSigner;
import com.openexchange.onboarding.service.OnboardingService;
import com.openexchange.onboarding.sms.SMSLinkProvider;
import com.openexchange.plist.PListDict;
import com.openexchange.plist.PListWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.encoding.Helper;
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

    private ServiceLookup lookup;

    /**
     * Initializes a new {@link PListDownloadServlet}.
     */
    public PListDownloadServlet(ServiceLookup lookup) {
        super();
        this.lookup = lookup;

    }

    @SuppressWarnings("resource")
    @Override
    protected void doGet(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

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

            SMSLinkProvider smsLinkProvider = lookup.getService(SMSLinkProvider.class);
            String[] arguments = smsLinkProvider.getParameter(req.getPathInfo());
            String scenarioId = arguments[3];
            Device device = Device.deviceFor(arguments[2]);
            int userId;
            int contextId;

            try {
                userId = Integer.valueOf(arguments[0]);
                contextId = Integer.valueOf(arguments[1]);
            } catch (NumberFormatException ex) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            try {
                if (!smsLinkProvider.validateChallenge(userId, contextId, scenarioId, device.getId(), arguments[4])) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            } catch (OXException e1) {
                LOG.error(e1.getMessage());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            Scenario scenario = null;
            PListDict plist = null;
            OnboardingService onboardingService = lookup.getService(OnboardingService.class);
            try {
                scenario = onboardingService.getScenario(scenarioId, device, userId, contextId);
                List<OnboardingProvider> list = scenario.getProviders(userId, contextId);
                for (OnboardingProvider provider : list) {
                    if (provider instanceof OnboardingPlistProvider) {
                        HostData data = new HostData() {
                            
                            @Override
                            public boolean isSecure() {
                                return req.isSecure();
                            }
                            @Override
                            public String getRoute() {
                                return null;
                            }
                            @Override
                            public int getPort() {
                                return req.getServerPort();
                            }
                            @Override
                            public String getHost() {
                                return req.getServerName();
                            }
                            @Override
                            public String getHTTPSession() {
                                return null;
                            }
                            @Override
                            public String getDispatcherPrefix() {
                                return null;
                            }
                        };
                        OnboardingRequest onboardingReq = new DefaultOnboardingRequest(scenario, OnboardingAction.SMS, device, data, null);
                        plist = ((OnboardingPlistProvider) provider).getPlist(userId, contextId, scenario, onboardingReq);
                        if (plist != null) {
                            break;
                        }
                    }
                }
            } catch (OXException e) {
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

            BrowserDetector detector = BrowserDetector.detectorFor(req.getHeader(Tools.HEADER_AGENT));
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/octet-stream");
            resp.setContentLength(fileSize);
            resp.setHeader("Content-disposition", "attachment; filename=\"" + Helper.escape(Helper.encodeFilename(fileName, "UTF-8", detector.isMSIE())) + "\"");
            Tools.removeCachingHeader(resp);

            OutputStream out = resp.getOutputStream();
            byte[] buf = new byte[4096];
            int length = -1;
            while ((length = filestream.read(buf)) != -1) {
                out.write(buf, 0, length);
            }

            out.flush();
            filestream.close();
        } finally {
            if (filestream != null) {
                filestream.close();
            }
            if (fileHolder != null) {
                fileHolder.close();
            }
        }
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
