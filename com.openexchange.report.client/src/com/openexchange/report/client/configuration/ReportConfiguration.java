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

package com.openexchange.report.client.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.openexchange.java.Streams;

public class ReportConfiguration {

    private final Properties properties;

    public ReportConfiguration() throws IOException {
        properties = new Properties();
        loadProperties(System.getProperties().getProperty("openexchange.propdir"), "reportclient.properties");
        loadProperties(System.getProperties().getProperty("openexchange.propdir"), "licensekeys.properties");
    }

    public void loadProperties(String propDir, String propFile) throws IOException {
        InputStream in;
        {
            if (null == propDir) {
                throw new IOException("Missing property \"" + propDir + "\"");
            }
            final File licensePropFile = new File(propDir, propFile);
            if (!licensePropFile.exists() || !licensePropFile.isFile()) {
                throw new IOException(new StringBuilder("Property file \"").append(propDir).append("/").append(propFile).append("\" couldn't be found").toString());
            }
            try {
                in = new FileInputStream(licensePropFile);
            } catch (final FileNotFoundException e) {
                throw new IOException(new StringBuilder("Property file \"").append(propDir).append("/").append(propFile).append("\" couldn't be found").toString());
            }
        }
        try {
            properties.load(in);
        } finally {
            Streams.close(in);
        }
    }

    public String getLicenseKeys() {
        StringBuilder retval = new StringBuilder();

        for (int currentFetchPosition = 1; properties.getProperty("com.openexchange.licensekey." + currentFetchPosition) != null; currentFetchPosition++) {
            if (retval.length() > 0) {
                retval.append(",");
            }
            retval.append(properties.getProperty("com.openexchange.licensekey." + currentFetchPosition));
        }

        return retval.toString();
    }

    public String getUseProxy() {
        if (null != properties.getProperty("com.openexchange.report.client.proxy.useproxy")) {
            return (properties.getProperty("com.openexchange.report.client.proxy.useproxy"));
        } else {
            return "";
        }
    }

    public String getProxyAddress() {
        if (null != properties.getProperty("com.openexchange.report.client.proxy.address")) {
            return properties.getProperty("com.openexchange.report.client.proxy.address");
        }
        return "";
    }

    public String getProxyPort() {
        if (null != properties.getProperty("com.openexchange.report.client.proxy.port")) {
            return properties.getProperty("com.openexchange.report.client.proxy.port");
        }
        return "";
    }

    public String getProxyAuthRequired() {
        if (null != properties.getProperty("com.openexchange.report.client.proxy.authrequired")) {
            return properties.getProperty("com.openexchange.report.client.proxy.authrequired");
        }
        return "";
    }

    public String getProxyUsername() {
        if (null != properties.getProperty("com.openexchange.report.client.proxy.username")) {
            return properties.getProperty("com.openexchange.report.client.proxy.username");
        }
        return "";
    }

    public String getProxyPassword() {
        if (null != properties.getProperty("com.openexchange.report.client.proxy.password")) {
            return properties.getProperty("com.openexchange.report.client.proxy.password");
        }
        return "";
    }
    
    public String getReportStorage() {
        if (null != properties.getProperty("com.openexchange.report.client.fileStorage")) {
            return properties.getProperty("com.openexchange.report.client.fileStorage");
        }
        return "";
    }
    
    public String getMaxChunkSize() {
        if (null != properties.getProperty("com.openexchange.report.client.maxChunkSize")) {
            return properties.getProperty("com.openexchange.report.client.maxChunkSize");
        }
        return "";
    }
    
    public String getMaxThreadPoolSize() {
        if (null != properties.getProperty("com.openexchange.report.client.maxThreadPoolSize")) {
            return properties.getProperty("com.openexchange.report.client.maxThreadPoolSize");
        }
        return "";
    }
    
    public String getThreadPriority() {
        if (null != properties.getProperty("com.openexchange.report.client.ThreadPriority")) {
            return properties.getProperty("com.openexchange.report.client.ThreadPriority");
        }
        return "";
    }
}
