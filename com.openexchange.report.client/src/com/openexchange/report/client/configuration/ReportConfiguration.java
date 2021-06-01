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
            } catch (FileNotFoundException e) {
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
}
