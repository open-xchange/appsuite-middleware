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

package com.openexchange.report.appsuite.serialization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.report.appsuite.serialization.osgi.StringParserServiceRegistry;
import com.openexchange.tools.strings.StringParser;

/**
 * A {@link Report} contains the analysis of a context ( in a {@link ContextReport}), a User ( in a {@link UserReport} ) or the system ( in
 * a regular {@link Report} ). It also keeps track of runtime statistics (when was it started, when was it done, how many tasks must be
 * performed, how many are still open).
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public class Report implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Report.class);
    private static final long serialVersionUID = 6998213011280390705L;

    //--------------------Report output sections and identifiers--------------------

    public static final String MACDETAIL = "macdetail";

    public static final String MACDETAIL_QUOTA = "macdetail-quota";

    public static final String QUOTA = "quota";

    public static final String CAPABILITIES = "capabilities";

    public static final String CAPABILITY_LIST = "capabilityList";

    public static final String DISABLED = "disabled";

    public static final String TOTAL = "total";

    public static final String CAPABILITY_SETS = "capabilitySets";

    public static final String MAILADMIN = "mailadmin";

    public static final String USER_LOGINS = "user-logins";

    public static final String ADMIN = "admin";

    public static final String MACDETAIL_LISTS = "macdetail-lists";

    public static final String LINKS = "links";

    public static final String GUESTS = "guests";

    public static final String CONTEXTS = "contexts";

    public static final String CONTEXT_USERS_MAX = "Context-users-max";

    public static final String CONTEXT_USERS_MIN = "Context-users-min";

    public static final String CONTEXT_USERS_AVG = "Context-users-avg";

    public static final String CLIENT_LOGINS = "client-list";

    public static final String CONTEXTS_DISABLED = "contexts-disabled";

    public static final String USERS = "users";

    public static final String USERS_DISABLED = "users-disabled";

    public static final String DRIVE_OVERALL = "drive-overall";

    public static final String DRIVE_USER = "drive-user";

    public static final String DRIVE_TOTAL = "drive-total";

    public static final String TIMEFRAME = "timeframe";

    public static final String TIMEFRAME_START = "start";

    public static final String TIMEFRAME_END = "end";

    public static final String ERRORS = "errors";

    public static final String IGNORED = "ignored";

    //--------------------Report attributes--------------------

    private final String uuid;

    private final String type;

    private final Map<String, Map<String, Object>> namespaces = new HashMap<String, Map<String, Object>>();

    private final long startTime;

    private long stopTime;

    private int numberOfTasks;

    private int pendingTasks;

    private LinkedHashMap<String, LinkedHashMap<String, Object>> tenantMap;

    private ReportConfigs reportConfig;

    private boolean isSingleDeployment = true;

    private Long defaultTimeframeStart;

    private Long defaultTimeframeEnd;

    private String storageFolderPath;

    private boolean needsComposition;

    /**
     * Initializes a new {@link Report}.
     *
     * @param uuid The uuid of this report run
     * @param type The type of this report run to determine which analyzers and cumulators are asked for contributions
     * @param startTime When this report was started in milliseconds.
     */
    public Report(String uuid, String type, long startTime) {
        this.uuid = uuid;
        this.type = type;
        this.startTime = startTime;
        this.tenantMap = new LinkedHashMap<>();
        this.tenantMap.put("deployment", new LinkedHashMap<String, Object>());
    }

    /**
     * Save a value in the report
     * 
     * @param ns a namespace, to keep the data of different analyzers and cumulators separate from one another
     * @param key the name to save the value under
     * @param value the value. The value must be serializable, and a List (composed of these legal values) or Map (again, composed of these legal values) or a primitive Java Type or a String
     * @return this report, so that multiple set commands can be chained
     */
    public Report set(String ns, String key, Serializable value) {
        checkValue(value);
        getNamespace(ns).put(key, value);

        return this;
    }

    /**
     * Initializes a new {@link Report}. With all fields needed for either timeframe considered only "default" report or
     * "oxcs-etended" report-type with additional parameters. If no timeframe is given, the last year from today is used
     * as timeframe.
     * 
     * @param uuid
     * @param reportType
     * @param startTime
     * @param startDate
     * @param endDate
     * @param isCustomTimerange
     * @param isShowSingleTenant
     * @param singleTenantId
     * @param isIgnoreAdmin
     * @param isShowDriveMetrics
     * @param isShowMailMetrics
     */
    public Report(String uuid, String reportType, long startTime, Date startDate, Date endDate, Boolean isCustomTimerange, Boolean isShowSingleTenant, Long singleTenantId, Boolean isIgnoreAdmin, Boolean isShowDriveMetrics, Boolean isShowMailMetrics) {
        this.uuid = uuid;
        this.type = reportType;
        this.startTime = startTime;
        this.tenantMap = new LinkedHashMap<>();
        this.tenantMap.put("deployment", new LinkedHashMap<String, Object>());
        if (isCustomTimerange) {
            this.defaultTimeframeStart = startDate.getTime();
            this.defaultTimeframeEnd = endDate.getTime();
        } else {
            Calendar cal = Calendar.getInstance();
            Date ed = cal.getTime();
            cal.add(Calendar.YEAR, -1);
            Date sd = cal.getTime();
            this.defaultTimeframeStart = sd.getTime();
            this.defaultTimeframeEnd = ed.getTime();
        }
    }

    public Report(String uuid, long startTime, ReportConfigs reportConfig) {
        this.uuid = uuid;
        this.type = reportConfig.getType();
        this.startTime = startTime;
        this.tenantMap = new LinkedHashMap<>();
        this.tenantMap.put("deployment", new LinkedHashMap<String, Object>());
        this.reportConfig = reportConfig;
        Calendar cal = Calendar.getInstance();
        Date ed = cal.getTime();
        cal.add(Calendar.YEAR, -1);
        Date sd = cal.getTime();
        this.defaultTimeframeStart = sd.getTime();
        this.defaultTimeframeEnd = ed.getTime();
        this.needsComposition = false;
    }

    private static Class[] allowedTypes = new Class[] { Integer.class, Long.class, Float.class, Short.class, Double.class, Byte.class, Boolean.class, String.class };

    public void addError(OXException exception) {
        HashMap<String, String> errors = get(Report.ERRORS, Report.IGNORED, new HashMap<String, String>(), HashMap.class);
        errors.put(exception.getExceptionId(), exception.getLogMessage());
        set(Report.ERRORS, Report.IGNORED, errors);
    }

    private void checkValue(Object value) {

        if (value == null) {
            throw new NullPointerException("value may not be null");
        }

        if (!(value instanceof Serializable)) {
            throw new IllegalArgumentException("Illegal type! Use only serializable types! " + value.getClass() + ": " + value);
        }
        if (value instanceof Map) {
            Map map = (Map) value;
            for (Object oEntry : map.entrySet()) {
                Map.Entry entry = (Map.Entry) oEntry;
                checkValue(entry.getKey());
                checkValue(entry.getValue());
            }
            return;
        } else if (value instanceof Collection) {
            Collection col = (Collection) value;
            for (Object o : col) {
                checkValue(o);
            }
            return;
        } else {
            for (Class candidate : allowedTypes) {
                if (candidate.isInstance(value)) {
                    return;
                }
            }
        }

        throw new IllegalArgumentException("Illegal type! Use only native java types! Was " + value.getClass());

    }

    /**
     * Retrieve a value and try to turn it into an Object of the given class
     * 
     * @param ns The namespace, as it was used in {@link #set(String, String, Serializable)}
     * @param key The key
     * @param klass The klass to try and turn the value into
     * @return The value or null, if the value wasn't previously set.
     */
    public <T> T get(String ns, String key, Class<T> klass) {
        return get(ns, key, null, klass);
    }

    /**
     * Retrieve a value and try to turn it into an Object of the given class
     * 
     * @param ns The namespace, as used by {@link #set(String, String, Serializable)}
     * @param key The key
     * @param defaultValue The value to return, if no value was set in this report
     * @param klass The klass to try and turn the value into
     * @return
     */
    public <T> T get(String ns, String key, T defaultValue, Class<T> klass) {
        Object value = getNamespace(ns).get(key);
        if (value == null) {
            return defaultValue;
        }

        if (klass.isAssignableFrom(klass)) {
            return (T) value;
        }

        if (klass == String.class) {
            return (T) value.toString();
        }
        return StringParserServiceRegistry.getServiceRegistry().getService(StringParser.class).parse(value.toString(), klass);
    }

    /**
     * Remove a value from the report
     */
    public void remove(String ns, String key) {
        Map<String, Object> namespace = getNamespace(ns);
        namespace.remove(key);
        if (namespace.isEmpty()) {
            namespaces.remove(ns);
        }
    }

    /**
     * Remove all values in the given namespace
     */
    public void clearNamespace(String ns) {
        namespaces.remove(ns);
    }

    /**
     * Retrieve the Namespace -> ( Key -> Value ) mappings. Note that this is the internal Object, so
     * whatever you do with it, it will also change the state in the report. Though it's better to use {@link #set(String, String, Serializable)}, {@link #get(String, String, Class)}, {@link #remove(String, String)} and
     * {@link #clearNamespace(String)} for modifying this Report
     */
    public Map<String, Map<String, Object>> getData() {
        return namespaces;
    }

    public String getUUID() {
        return uuid;
    }

    public String getType() {
        if (type == null) {
            return "default";
        }
        return type;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    /**
     * Retrieve a namespace. Useful for e.g. iterating of all keys in a namespace. Note this is also the internal Object, so modifying it
     * will have side-effects in the state of the report
     */
    public Map<String, Object> getNamespace(String ns) {
        Map<String, Object> candidate = namespaces.get(ns);
        if (candidate == null) {
            candidate = new HashMap<String, Object>();
            namespaces.put(ns, candidate);
        }
        return candidate;
    }

    /**
     * A report tracks the number of tasks that had to be, or still have to be, looked at for the report to complete. This is usually the number of contexts
     * that have to be analyzed
     */
    public void setNumberOfTasks(int numberOfTasks) {
        this.numberOfTasks = numberOfTasks;
        this.pendingTasks = numberOfTasks;
    }

    public void setTaskState(int numberOfTasks, int pendingTasks) {
        this.numberOfTasks = numberOfTasks;
        this.pendingTasks = pendingTasks;
    }

    /**
     * Retrieve the number of tasks that remain to be done. A report is complete when this number reaches 0.
     */
    public int getNumberOfPendingTasks() {
        return pendingTasks;
    }

    /**
     * A report tracks the number of tasks that had to be, or still have to be, looked at for the report to complete. This is usually the
     * number of contexts that have to be analyzed
     */
    public int getNumberOfTasks() {
        return numberOfTasks;
    }

    @Override
    public String toString() {
        return "Report [UUID=" + uuid + ", type=" + type + ", tasks=" + numberOfTasks + ", tasksToDo=" + pendingTasks + "]";
    }

    public boolean isSingleDeployment() {
        return isSingleDeployment;
    }

    public LinkedHashMap<String, LinkedHashMap<String, Object>> getTenantMap() {
        return tenantMap;
    }
    
    public ReportConfigs getReportConfig() {
        return reportConfig;
    }

    public void setReportConfig(ReportConfigs reportConfig) {
        this.reportConfig = reportConfig;
    }

    public Long getConsideredTimeframeStart() {
        Long timeframeStart = defaultTimeframeStart;
        if (this.reportConfig.isConfigTimerange()) {
            timeframeStart = this.reportConfig.getConsideredTimeframeStart();
        }
        return timeframeStart;
    }

    public Long getConsideredTimeframeEnd() {
        Long timeframeEnd = defaultTimeframeEnd;
        if (this.reportConfig.isConfigTimerange()) {
            timeframeEnd = this.reportConfig.getConsideredTimeframeEnd();
        }
        return timeframeEnd;
    }

    public boolean isShowSingleTenant() {
        return this.reportConfig.isShowSingleTenant();
    }

    public Long getSingleTenantId() {
        return this.reportConfig.getSingleTenantId();
    }

    public boolean isAdminIgnore() {
        return this.reportConfig.isAdminIgnore();
    }

    public boolean isShowDriveMetrics() {
        return this.reportConfig.isShowDriveMetrics();
    }

    public boolean isShowMailMetrics() {
        return this.reportConfig.isShowMailMetrics();
    }

    public String getStorageFolderPath() {
        return storageFolderPath;
    }

    public void setStorageFolderPath(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
    }

    public boolean isNeedsComposition() {
        return needsComposition;
    }

    public void setNeedsComposition(boolean needsComposition) {
        this.needsComposition = needsComposition;
    }

    // Attention, if you add values, also correct the composeReportFromStoredPats(...) method
    public static enum JsonObjectType {
        MAP, ARRAY
    };

    /**
     * Gather all report parts and merge them into a report-file, with a ".report" ending, inside the
     * reports folder-path. The result is stored in the same folder . The "*.part" files are deleted in the process
     * after the successful creation of the result.
     * <br><br>
     * The <code>rootAttribute</code> will be the first key of the map, the <code>contentContainer</code>
     * the first value. Every other value will be either an entry in a list or a key/value pair, depending
     * on the given <code>contentContainerType</code> ({@link JsonObjectType}). The <code>defaultIndentation</code> will add
     * two whitespace per count to each line.
     * <br><br>
     * The result will be stored inside this reports <code>storageFolderPath</code> in a file which name
     * is <code>uuid</code>.report.
     * <br><br>
     * Every .part file will be deleted afterwards.
     * 
     * @param contentContainer, the first level container above the content, that is gathered from the .part files
     * @param contentContainerType, the type of the first level container, either Map or Array
     * @param rootAttribute, the highest level container, contentContainer will be this containers first entry
     * @param defaultIndentation, the number of whitespace before each entry * 2
     */
    public void composeReportFromStoredParts(String contentContainer, JsonObjectType contentContainerType, String rootAttribute, int defaultIndentation) {
        // Check for any existing parts inside the reports folder and load filenames into a list
        File partsFolder = new File(storageFolderPath);
        int indentationLevel = defaultIndentation;
        LinkedList<File> parts = new LinkedList<>((Arrays.asList(partsFolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".part");
            }
        }))));
        if (parts.size() == 0) {
            return;

        }
        if (contentContainer != null && contentContainer.length() > 0 && contentContainerType == null) {
            return;
        }
        File reportContent = new File(storageFolderPath + "/" + uuid + ".report");
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(reportContent), "UTF-8")) {

            // create a new line with the rootAttribute and container attribute and open brackets depending on the given JsonObjectType
            if (rootAttribute != null && rootAttribute.length() > 0) {
                osw.write(getIndentation(indentationLevel++) + "\"" + rootAttribute + "\"" + " : {\n");
            }
            if (contentContainer != null && contentContainer.length() > 0) {
                osw.write(getIndentation(indentationLevel++) + "\"" + contentContainer + "\"" + " : " + (contentContainerType == JsonObjectType.MAP ? "{" : "[") + "\n");
            }
            // Paste the stored files content
            for (ListIterator<File> fileIterator = parts.listIterator(); fileIterator.hasNext();) {
                File file = fileIterator.next();
                // Dont touch parts of other reports
                if (file.getName().contains(uuid)) {
                    appendReportParts(osw, file, fileIterator.hasNext(), getIndentation(indentationLevel));
                }
            }
            // Append the closing attributes
            if (contentContainer != null && contentContainer.length() > 0) {
                osw.write(getIndentation(--indentationLevel) + (contentContainerType == JsonObjectType.MAP ? "}" : "]") + "\n");
            }
            if (rootAttribute != null && rootAttribute.length() > 0) {
                osw.write(getIndentation(--indentationLevel) + "},\n");
            }
        } catch (FileNotFoundException e) {
            LOG.error("Unable to create the .report file");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            LOG.error("Used encode is not supported.");
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("Unable to write into .report file.");
            e.printStackTrace();
        }
        for (File file : parts) {
            if (file.getName().contains(this.uuid)) {
                file.delete();
            }
        }
    }

    public void appendReportParts(OutputStreamWriter osw, File appendingFile, boolean hasNext, String indentation) {
        // load every part-file of the report 
        try (BufferedReader br = new BufferedReader(new FileReader(appendingFile))) {
            String line = br.readLine();
            while (line != null) {
                osw.write(indentation + line + ((line = br.readLine()) == null && hasNext ? "," : "") + "\n");
            }
        } catch (FileNotFoundException e) {
            LOG.error("Unable to load file: " + appendingFile.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("Unable to write into file: " + appendingFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public static void printStoredReportContentToConsole(String storageFolderPath, String uuid) {

        try (FileInputStream is = new FileInputStream(storageFolderPath + "/" + uuid + ".report"); Scanner sc = new Scanner(is, "UTF-8")) {
            while (sc.hasNext()) {
                System.out.println(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            LOG.error("Unable to load file: " + storageFolderPath + "/" + uuid + ".report");
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("Unable to load and write report data to console.");
            e.printStackTrace();
        }
    }

    public String getIndentation(int level) {
        String result = "";
        for (int i = 0; i < level; i++) {
            result += "  ";
        }
        return result;
    }
}
