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

package com.openexchange.report.appsuite.management;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.config.ConfigurationService;
import com.openexchange.report.appsuite.internal.Services;
import com.openexchange.report.appsuite.serialization.Report;
import com.openexchange.version.VersionService;


/**
 * The {@link JMXReport} is a wrapper around a {@link Report} that can be transmitted via JMXs MXBean conventions. It contains
 * nearly the same attributes as the report, but opts to serialize the report data in JSON format, adding into it the uuid, reportType, start and stop timestamps and the
 * OX versioning information.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public class JMXReport {
    private final String uuid;
    private final int pendingTasks;
    private final int tasks;
    private final long startTime;
    private final long stopTime;
    private final String type;
    private String data;
    private boolean isNeedsComposition;
    private String storageFolderPath;

    /**
     * Creates a JMX friendly version of the given report
     */
    public JMXReport(Report report) throws Exception {
        this.uuid = report.getUUID();
        this.pendingTasks = report.getNumberOfPendingTasks();
        this.tasks = report.getNumberOfTasks();
        this.startTime = report.getStartTime();
        this.stopTime = report.getStopTime();
        this.isNeedsComposition = report.isNeedsComposition();
        this.storageFolderPath = report.getStorageFolderPath();

        this.type = report.getType();

        try {
            Map<String, Map<String, Object>> lData = report.getData();
            
            boolean adminMailLoginEnabled = Services.getService(ConfigurationService.class).getBoolProperty("com.openexchange.mail.adminMailLoginEnabled", false);
            Map<String, Object> configs = lData.get("configs");
            if (configs == null) {
                configs = new HashMap<>();
            }
            configs.put("com.openexchange.mail.adminMailLoginEnabled", Boolean.toString(adminMailLoginEnabled));
            lData.put("configs", configs);
            
            JSONObject jsonData = (JSONObject) JSONCoercion.coerceToJSON(lData);
            jsonData.put("uuid", uuid);
            jsonData.put("reportType", type);
            jsonData.put("timestamps", new JSONObject().put("start", startTime).put("stop", stopTime));
            VersionService versionService = Services.getService(VersionService.class);
            jsonData.put("version", new JSONObject().put("version", versionService.getVersionString()).put("buildDate", versionService.getBuildDate()));
            jsonData.put("needsComposition", isNeedsComposition);
            jsonData.put("storageFolderPath", storageFolderPath);
            jsonData.put(Report.OPERATINGSYSTEM, new JSONObject().put(Report.OPERATINGSYSTEM_NAME, report.getOperatingSystemName()).put(Report.OPERATINGSYSTEM_VERSION, report.getOperatingSystemVersion()).put(Report.OPERATINGSYSTEM_DISTRIBUTION, report.getDistribution()));
            jsonData.put(Report.DATABASE_VERSION, report.getDatabaseVersion());
            jsonData.put(Report.JAVA, new JSONObject().put(Report.JAVA_VERSION, report.getJavaVersion()).put(Report.JAVA_VENDOR, report.getJavaVendor()));
            jsonData.put(Report.INSTALLED_OX_PACKAGES, new JSONArray(report.getInstalledOXPackages()));
            jsonData.put(Report.CONFIGURED_3RD_PARTY_APIS, new JSONObject().put(Report.APIS_OAUTH, new JSONArray(report.getConfiguredThirdPartyAPIsOAuth())).put(Report.APIS_OTHERS, new JSONArray(report.getConfiguredThirdPartyAPIsOthers())));

            this.data = jsonData.toString();
        } catch (JSONException e) {
            throw new Exception(e.getMessage());
        }
    }


    public String getUuid() {
        return uuid;
    }


    public int getPendingTasks() {
        return pendingTasks;
    }


    public int getTasks() {
        return tasks;
    }


    public long getStartTime() {
        return startTime;
    }


    public long getStopTime() {
        return stopTime;
    }


    public String getType() {
        return type;
    }


    public String getData() {
        return data;
    }
    
    
    public boolean isNeedsComposition() {
        return isNeedsComposition;
    }

    
    public void setNeedsComposition(boolean isNeedsComposition) {
        this.isNeedsComposition = isNeedsComposition;
    }

    
    public String getStorageFolderPath() {
        return storageFolderPath;
    }

    
    public void setStorageFolderPath(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
    }


    /**
     * A utility method for wrapping an array of reports
     */
    public static JMXReport[] wrap(Report[] reports) throws Exception {
        if (null == reports) {
            return null;
        }
        JMXReport[] wrapped = new JMXReport[reports.length];
        for (int i = 0; i < reports.length; i++) {
            Report report = reports[i];
            wrapped[i] = null == report ? null : new JMXReport(report);
        }

        return wrapped;
    }
}
