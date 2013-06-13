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

package com.openexchange.report.client.transport;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import javax.management.openmbean.CompositeData;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.report.client.configuration.ReportConfiguration;
import com.openexchange.report.client.container.ClientLoginCount;
import com.openexchange.report.client.container.ContextDetail;
import com.openexchange.report.client.container.ContextModuleAccessCombination;
import com.openexchange.report.client.container.MacDetail;
import com.openexchange.report.client.container.Total;
import com.openexchange.tools.encoding.Base64;

public class TransportHandler {

    private static final String REPORT_SERVER_URL = "activation.open-xchange.com";

    private static final String REPORT_SERVER_CLIENT_AUTHENTICATION_STRING = "rhadsIsAgTicOpyodNainPacloykAuWyribZydkarbEncherc4";

    private static final String POST_CLIENT_AUTHENTICATION_STRING_KEY = "clientauthenticationstring";

    private static final String POST_LICENSE_KEYS_KEY = "license_keys";

    private static final String POST_METADATA_KEY = "client_information";

    private static final String URL_ENCODING = "UTF-8";

    public TransportHandler() {
    }

    public void sendReport(final List<Total> totals, final List<MacDetail> macDetails, final List<ContextDetail> contextDetails, final String[] versions, final ClientLoginCount clc, final ClientLoginCount clcYear, final boolean savereport) throws IOException, JSONException {
        final JSONObject metadata = buildJSONObject(totals, macDetails, contextDetails, versions, clc, clcYear);

        send(metadata, savereport);
    }

    private void send(JSONObject metadata, boolean savereport) throws IOException {
        final ReportConfiguration reportConfiguration = new ReportConfiguration();

        final StringBuffer report = new StringBuffer();
        report.append(POST_CLIENT_AUTHENTICATION_STRING_KEY);
        report.append("=");
        report.append(URLEncoder.encode(REPORT_SERVER_CLIENT_AUTHENTICATION_STRING, URL_ENCODING));
        report.append("&");
        report.append(POST_LICENSE_KEYS_KEY);
        report.append("=");
        report.append(URLEncoder.encode(reportConfiguration.getLicenseKeys(), URL_ENCODING));
        report.append("&");
        report.append(POST_METADATA_KEY);
        report.append("=");
        report.append(URLEncoder.encode(metadata.toString(), URL_ENCODING));

        if ("true".equals(reportConfiguration.getUseProxy().trim())) {
            System.setProperty("https.proxyHost", reportConfiguration.getProxyAddress().trim());
            System.setProperty("https.proxyPort", reportConfiguration.getProxyPort().trim());
        }

        if (savereport) {
            File tmpfile = File.createTempFile("oxreport", ".json", new File("/tmp"));
            System.out.println("Saving report to " + tmpfile.getAbsolutePath());
            DataOutputStream tfo = new DataOutputStream(new FileOutputStream(tmpfile));
            tfo.writeBytes(report.toString());
            tfo.close();
        } else {
            final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://" + REPORT_SERVER_URL + "/").openConnection();
            httpsURLConnection.setUseCaches(false);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if ("true".equals(reportConfiguration.getUseProxy().trim()) && "true".equals(reportConfiguration.getProxyAuthRequired().trim())) {
                final String proxyAutorizationProperty = "Basic " + Base64.encode((reportConfiguration.getProxyUsername().trim() + ":" + reportConfiguration.getProxyPassword().trim()).getBytes());

                Authenticator.setDefault(new ProxyAuthenticator(
                    reportConfiguration.getProxyUsername().trim(),
                    reportConfiguration.getProxyPassword().trim()));

                httpsURLConnection.setRequestProperty("Proxy-Authorization", proxyAutorizationProperty);
            }

            final DataOutputStream stream = new DataOutputStream(httpsURLConnection.getOutputStream());
            stream.writeBytes(report.toString());
            stream.flush();
            stream.close();

            if (httpsURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new MalformedURLException("Problem contacting report server: " + httpsURLConnection.getResponseCode());
            }
            final BufferedReader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
            String buffer = "";
            while ((buffer = in.readLine()) != null) {
                System.out.println(new StringBuilder().append(REPORT_SERVER_URL).append(" said: ").append(buffer).toString());
            }
            in.close();
        }        
    }

    private JSONObject buildJSONObject(final List<Total> totals, final List<MacDetail> macDetails, final List<ContextDetail> contextDetails, final String[] versions, final ClientLoginCount clc, final ClientLoginCount clcYear) throws JSONException {
        final JSONObject retval = new JSONObject();

        final JSONObject total = new JSONObject();
        final JSONObject macdetail = new JSONObject();
        final JSONObject detail = new JSONObject();
        final JSONObject version = new JSONObject();
        final JSONObject clientlogincount = new JSONObject();
        final JSONObject clientlogincountyear = new JSONObject();

        final boolean wantsdetails = (null != contextDetails);

        if (wantsdetails) {
            total.put("report-format", "long");
        } else {
            total.put("report-format", "short");
        }

        for (final Total tmp : totals) {
            total.put("contexts", tmp.getContexts());
            total.put("users", tmp.getUsers());
        }

        for (final MacDetail tmp : macDetails) {
            final JSONObject macDetailObjectJSON = new JSONObject();
            macDetailObjectJSON.put("id", tmp.getId());
            macDetailObjectJSON.put("count", tmp.getCount());
            macDetailObjectJSON.put("adm", tmp.getNrAdm());
            macDetailObjectJSON.put("disabled", tmp.getNrDisabled());
            macdetail.put(tmp.getId(), macDetailObjectJSON);
        }

        if (wantsdetails) {
            for (final ContextDetail tmp : contextDetails) {
                final JSONObject contextDetailObjectJSON = new JSONObject();
                contextDetailObjectJSON.put("id", tmp.getId());
                contextDetailObjectJSON.put("age", tmp.getAge());
                contextDetailObjectJSON.put("created", tmp.getCreated());
                contextDetailObjectJSON.put("adminmac", tmp.getAdminmac());

                final JSONObject moduleAccessCombinations = new JSONObject();
                for (final ContextModuleAccessCombination moduleAccessCombination : tmp.getModuleAccessCombinations()) {
                    final JSONObject moduleAccessCombinationJSON = new JSONObject();
                    moduleAccessCombinationJSON.put("mac", moduleAccessCombination.getUserAccessCombination());
                    moduleAccessCombinationJSON.put("users", moduleAccessCombination.getUserCount());
                    moduleAccessCombinationJSON.put("inactive", moduleAccessCombination.getInactiveCount());
                    moduleAccessCombinations.put(moduleAccessCombination.getUserAccessCombination(), moduleAccessCombinationJSON);
                }
                contextDetailObjectJSON.put("macs", moduleAccessCombinations);
                detail.put(tmp.getId(), contextDetailObjectJSON);
            }
        }

        version.put("version", versions[0]);
        version.put("buildDate", versions[1]);

        clientlogincount.put("usm-eas", clc.getUsmeas());
        clientlogincount.put("olox2", clc.getOlox2());
        clientlogincount.put("mobileapp", clc.getMobileapp());
        clientlogincount.put("carddav", clc.getCarddav());
        clientlogincount.put("caldav", clc.getCarddav());

        clientlogincountyear.put("usm-eas", clcYear.getUsmeas());
        clientlogincountyear.put("olox2", clcYear.getOlox2());
        clientlogincountyear.put("mobileapp", clcYear.getMobileapp());
        clientlogincountyear.put("carddav", clcYear.getCarddav());
        clientlogincountyear.put("caldav", clcYear.getCarddav());

        retval.put("total", total);
        retval.put("macdetail", macdetail);
        if (wantsdetails) {
            retval.put("detail", detail);
        }
        retval.put("version", version);
        retval.put("clientlogincount", clientlogincount);
        retval.put("clientlogincountyear", clientlogincountyear);

        return retval;
    }

    public void sendASReport(CompositeData report, boolean savereport) throws IOException, JSONException {
        send(new JSONObject((String)report.get("data")), savereport);
    }

}
