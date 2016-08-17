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

package com.openexchange.report.internal;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.RuntimeOperationsException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ReportingMBean}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ReportingMBean implements DynamicMBean {

    private static final String COM_OPENEXCHANGE_MAIL_ADMIN_MAIL_LOGIN_ENABLED = "com.openexchange.mail.adminMailLoginEnabled";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReportingMBean.class);

    private final String[] totalNames = { "contexts", "users", "guests", "links" };

    private CompositeType totalRow;

    private TabularType totalType;

    private final String[] macsNames = { "mac", "count", "nradmin", "nrdisabled" };

    private CompositeType macsRow;

    private TabularType macsType;

    private final String[] configurationNames = { "key", "value" };

    private CompositeType configurationRow;

    private TabularType configurationType;

    private final String[] moduleAccessCombinationNames = { "module access combination", "users", "inactive" };

    private final String[] detailNames = { "identifier", "admin permission", "users", "age", "created", "module access combinations" };

    private CompositeType detailRow;

    private TabularType moduleAccessCombinationsType;

    private TabularType detailType;

    private final MBeanInfo mbeanInfo;

    private CompositeType moduleAccessPermission;

    /**
     * Initializes a new {@link ReportingMBean}.
     */
    public ReportingMBean() {
        super();
        mbeanInfo = buildMBeanInfo();
    }

    @Override
    public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException {
        if (attribute == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"), "Cannot call getAttributeInfo with null attribute name");
        }
        final ConfigurationService configurationService;
        try {
            configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class, true);
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        }
        if ("Total".equals(attribute)) {
            return generateTotalTabular();
        } else if ("Macs".equals(attribute)) {
            return generateMacsTabular();
        } else if ("Detail".equals(attribute)) {
            return generateDetailTabular();
        } else if ("Configuration".equals(attribute)) {
            return generateConfigurationTabular(configurationService);
        }
        throw new AttributeNotFoundException("Cannot find " + attribute + " attribute ");
    }

    private TabularData generateConfigurationTabular(ConfigurationService configurationService) throws MBeanException {
        final TabularData configuration = new TabularDataSupport(configurationType);
        try {
            boolean adminMailLoginEnabled = configurationService.getBoolProperty(COM_OPENEXCHANGE_MAIL_ADMIN_MAIL_LOGIN_ENABLED, false);
            final CompositeDataSupport value = new CompositeDataSupport(configurationRow, configurationNames, new Object[] { COM_OPENEXCHANGE_MAIL_ADMIN_MAIL_LOGIN_ENABLED, B(adminMailLoginEnabled) });
            configuration.put(value);
        } catch (final OpenDataException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe);
        } catch (final RuntimeException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe);
        } catch (final Throwable t) {
            LOG.error("", t);
            final Exception wrapMe = new Exception(t.getMessage());
            throw new MBeanException(wrapMe);
        }
        return configuration;
    }

    private final Map<Integer, ReportContext> loadContextData() throws MBeanException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        try {
            final Map<String, Integer> schemaMap = Tools.getAllSchemata(LOG);
            final Map<Integer, ReportContext> allctx = new HashMap<Integer, ReportContext>();
            for (final Entry<String, Integer> schemaEntry : schemaMap.entrySet()) {
                String schema = schemaEntry.getKey();
                int readPool = schemaEntry.getValue().intValue();

                final Connection connection;
                try {
                    connection = dbService.get(readPool, schema);
                } catch (final OXException e) {
                    LOG.error("", e);
                    throw new MBeanException(e, "Couldn't get connection to schema " + schema + " in pool " + readPool + ".");
                }
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = connection.prepareStatement("SELECT c.cid,c.creating_date,a.user,u.permissions FROM prg_contacts c JOIN user_setting_admin a ON c.cid=a.cid AND c.userid=a.user JOIN user_configuration u ON c.cid=u.cid AND u.user=a.user");
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        final ReportContext rc = new ReportContext();
                        rc.setId(I(rs.getInt(1)));
                        rc.setAge(L((System.currentTimeMillis() - rs.getLong(2)) / Constants.MILLI_DAY));
                        rc.setCreated(new Date(rs.getLong(2)));
                        rc.setAdminId(I(rs.getInt(3)));
                        rc.setAdminPermission(I(rs.getInt(4)));
                        allctx.put(rc.getId(), rc);
                    }
                    rs.close();
                    stmt.close();

                    stmt = connection.prepareStatement("SELECT c.cid,COUNT(c.permissions),c.permissions,COUNT(IF(u.mailEnabled=0,1,null)) FROM user_configuration AS c JOIN user AS u ON u.cid=c.cid AND u.id=c.user WHERE u.guestCreatedBy=0 GROUP BY permissions,cid ORDER BY cid;");
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        final ReportContext rc = allctx.get(I(rs.getInt(1)));
                        final int numusr = rs.getInt(2);
                        final int perm = rs.getInt(3);
                        final int inaccnt = rs.getInt(4);
                        if (null != rc) {
                            Map<Integer, Integer> accCombs = rc.getAccessCombinations();
                            Map<Integer, Integer> inactive = rc.getInactiveByCombination();
                            if (null == accCombs) {
                                accCombs = new HashMap<Integer, Integer>();
                            }
                            if (null == inactive) {
                                inactive = new HashMap<Integer, Integer>();
                            }
                            accCombs.put(I(perm), I(numusr));
                            inactive.put(I(perm), I(inaccnt));
                            final Integer nusr = rc.getNumUsers();
                            if (null != nusr) {
                                rc.setNumUsers(I(nusr.intValue() + numusr));
                            } else {
                                rc.setNumUsers(I(numusr));
                            }
                            rc.setInactiveByCombination(inactive);
                            rc.setAccessCombinations(accCombs);
                        }
                    }
                } catch (final SQLException e) {
                    LOG.error("", e);
                    throw new MBeanException(e, e.getMessage());
                } finally {
                    DBUtils.closeSQLStuff(rs, stmt);
                    dbService.back(readPool, connection);
                }
            }
            return allctx;
        } catch (final MBeanException e) {
            LOG.error("", e);
            throw e;
        } catch (OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (SQLException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        }
    }

    private TabularDataSupport generateTotalTabular() throws MBeanException {
        final TabularDataSupport total = new TabularDataSupport(totalType);

        try {
            int userCount = 0;
            int guestCount = 0;
            int linkCount = 0;

            List<Integer> allContextIds = Tools.getAllContextIds();

            List<Integer> contextsToProcess = Collections.synchronizedList(new ArrayList<>(allContextIds));
            while (!contextsToProcess.isEmpty()) {
                Integer firstRemainingContext = contextsToProcess.get(0);
                List<Integer> contextInSameSchema = Tools.getContextInSameSchema(firstRemainingContext.intValue());
                userCount = userCount + Tools.getNumberOfUsers(contextInSameSchema);
                guestCount = guestCount + Tools.getNumberOfGuests(contextInSameSchema);
                linkCount = linkCount + Tools.getNumberOfLinks(contextInSameSchema);

                contextsToProcess.removeAll(contextInSameSchema);
            }
            int nrctx = allContextIds.size();
            int nruser = userCount;
            int nrguests = guestCount;
            int nrlinks = linkCount;

            final CompositeDataSupport value = new CompositeDataSupport(totalRow, totalNames, new Object[] { I(nrctx), I(nruser), I(nrguests), I(nrlinks) });
            total.put(value);
            return total;
        } catch (OpenDataException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        }
    }

    private TabularDataSupport generateMacsTabular() throws MBeanException {
        final TabularDataSupport total = new TabularDataSupport(macsType);
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        try {
            final Map<String, Integer> schemaMap = Tools.getAllSchemata(LOG);
            HashMap<Integer, Integer> macMap = new HashMap<Integer, Integer>();
            HashMap<Integer, Integer> admMap = new HashMap<Integer, Integer>();
            HashMap<Integer, Integer> disabledMap = new HashMap<Integer, Integer>();
            for (final Entry<String, Integer> schemaEntry : schemaMap.entrySet()) {
                int readPool = schemaEntry.getValue().intValue();
                String schema = schemaEntry.getKey();

                final Connection connection;
                try {
                    connection = dbService.get(readPool, schema);
                } catch (final OXException e) {
                    LOG.error("", e);
                    throw new MBeanException(e, "Couldn't get connection to schema " + schema + " in pool " + readPool + ".");
                }
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = connection.prepareStatement("SELECT c.permissions,COUNT(c.permissions) AS count,COUNT(IF(c.user=2,1,null)) AS nradm,COUNT(IF(u.mailEnabled=0,1,null)) AS nrdisabled FROM user_configuration AS c JOIN user AS u ON u.cid=c.cid AND u.id=c.user WHERE u.guestCreatedBy=0 GROUP BY c.permissions");
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        Integer mac = rs.getInt(1);
                        Integer count = rs.getInt(2);
                        Integer nradm = rs.getInt(3);
                        Integer nrdisabled = rs.getInt(4);
                        if (macMap.containsKey(mac)) {
                            macMap.put(mac, macMap.get(mac) + count);
                        } else {
                            macMap.put(mac, count);
                        }
                        if (admMap.containsKey(mac)) {
                            admMap.put(mac, admMap.get(mac) + nradm);
                        } else {
                            admMap.put(mac, nradm);
                        }
                        if (disabledMap.containsKey(mac)) {
                            disabledMap.put(mac, disabledMap.get(mac) + nrdisabled);
                        } else {
                            disabledMap.put(mac, nrdisabled);
                        }
                    }
                    rs.close();
                    stmt.close();
                } catch (final SQLException e) {
                    LOG.error("", e);
                    throw new MBeanException(e, e.getMessage());
                } finally {
                    DBUtils.closeSQLStuff(rs, stmt);
                    dbService.back(readPool, connection);
                }
            }
            for (final Integer key : macMap.keySet()) {
                final CompositeDataSupport value = new CompositeDataSupport(macsRow, macsNames, new Object[] { key, macMap.get(key), admMap.get(key), disabledMap.get(key) });
                total.put(value);
            }
            return total;
        } catch (final MBeanException e) {
            LOG.error("", e);
            throw e;
        } catch (OpenDataException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (SQLException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        }
    }

    private TabularDataSupport generateDetailTabular() throws MBeanException {
        final TabularDataSupport detail = new TabularDataSupport(detailType);
        try {
            /**
             * FIXME:
             * Caldav/Carddav only available via ConfigCascade this might need to be added
             * Former version also did send login_mappings, but report client did not use it
             */
            final Map<Integer, ReportContext> ret = loadContextData();
            for (final ReportContext c : ret.values().toArray(new ReportContext[ret.size()])) {
                final TabularDataSupport moduleAccessCombinations = new TabularDataSupport(moduleAccessCombinationsType);
                final Map<Integer, Integer> accessCombinations = c.getAccessCombinations();
                final Map<Integer, Integer> inacByCombi = c.getInactiveByCombination();
                if (null != accessCombinations) {
                    for (final Entry<Integer, Integer> e : accessCombinations.entrySet()) {
                        int inac = 0;
                        if (null != inacByCombi) {
                            final Integer inAc = inacByCombi.get(e.getKey());
                            inac = inAc == null ? 0 : inAc.intValue();
                        }
                        moduleAccessCombinations.put(new CompositeDataSupport(moduleAccessPermission, moduleAccessCombinationNames, new Object[] { e.getKey(), e.getValue(), I(inac) }));
                    }
                }
                final CompositeDataSupport value = new CompositeDataSupport(detailRow, detailNames, new Object[] { c.getId(), c.getAdminPermission(), c.getNumUsers(), c.getAge(), c.getCreated(), moduleAccessCombinations });
                detail.put(value);
            }
        } catch (final OpenDataException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe);
        } catch (final RuntimeException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe);
        } catch (final Throwable t) {
            LOG.error("", t);
            final Exception wrapMe = new Exception(t.getMessage());
            throw new MBeanException(wrapMe);
        }
        return detail;
    }

    @Override
    public AttributeList getAttributes(final String[] attributes) {
        if (attributes == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("attributes can not be null"), "Cannot call getAttributes with null attribute names");
        }
        final AttributeList resultList = new AttributeList();
        if (attributes.length == 0) {
            return resultList;
        }
        for (int i = 0; i < attributes.length; i++) {
            try {
                final Object value = getAttribute(attributes[i]);
                resultList.add(new Attribute(attributes[i], value));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return (resultList);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) {
        throw new RuntimeOperationsException(new UnsupportedOperationException("invoke is not supported"), "The method invoke is not supported.");
    }

    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException {
        throw new AttributeNotFoundException("No attribute can be set in this MBean");
    }

    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        return new AttributeList();
    }

    private final MBeanInfo buildMBeanInfo() {
        try {
            final String[] totalDescriptions = { "Number of contexts", "Number of users", "Number of guests", "Number of links" };
            final OpenType[] totalTypes = { SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER };
            totalRow = new CompositeType("Total row", "A total row", totalNames, totalDescriptions, totalTypes);
            totalType = new TabularType("Total", "Total view", totalRow, totalNames);

            final String[] macsDescriptions = { "Access combination", "Count", "Admins", "Disabled" };
            final OpenType[] macsTypes = { SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER };
            macsRow = new CompositeType("Macs row", "A macs row", macsNames, macsDescriptions, macsTypes);
            macsType = new TabularType("Macs", "Macs view", macsRow, macsNames);

            final String[] moduleAccessCombinationDescriptions = { "Integer value of the module access combination", "number of users configured with this module access combination", "inactive subset of useres configured with this module access combination" };
            final OpenType[] moduleAccessCombinationTypes = { SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER };
            moduleAccessPermission = new CompositeType("Module access permission", "A module access combination and the number of users having it", moduleAccessCombinationNames, moduleAccessCombinationDescriptions, moduleAccessCombinationTypes);
            moduleAccessCombinationsType = new TabularType("Module access permission combinations", "The different access combinations used in this context", moduleAccessPermission, new String[] { "module access combination" });

            final String[] detailDescriptions = { "Context identifier", "Context admin permission", "Number of users", "Context age in days", "Date and time of context creation", "Module access permission combinations" };
            final OpenType[] detailTypes = { SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.LONG, SimpleType.DATE, moduleAccessCombinationsType };
            detailRow = new CompositeType("Detail row", "A detail row", detailNames, detailDescriptions, detailTypes);
            detailType = new TabularType("Detail", "Detail view", detailRow, new String[] { "identifier" });

            final String[] configurationDescriptions = { "Property key", "Property value" };
            final OpenType[] configurationTypes = { SimpleType.STRING, SimpleType.BOOLEAN };
            configurationRow = new CompositeType("Configuration row", "A configuration row", configurationNames, configurationDescriptions, configurationTypes);
            configurationType = new TabularType("Configuration", "Configuration view", configurationRow, configurationNames);

            final OpenMBeanAttributeInfo totalAttribute = new OpenMBeanAttributeInfoSupport("Total", "Total contexts and users.", totalType, true, false, false);
            final OpenMBeanAttributeInfo macsAttribute = new OpenMBeanAttributeInfoSupport("Macs", "List of macs and their count.", macsType, true, false, false);
            final OpenMBeanAttributeInfo detailAttribute = new OpenMBeanAttributeInfoSupport("Detail", "Detailed report about contexts and users", detailType, true, false, false);
            final OpenMBeanAttributeInfo configurationAttribute = new OpenMBeanAttributeInfoSupport("Configuration", "Configuration report", detailType, true, false, false);
            return new OpenMBeanInfoSupport(this.getClass().getName(), "Context, user and server reporting.", new OpenMBeanAttributeInfo[] { totalAttribute, macsAttribute, detailAttribute, configurationAttribute }, null, null, null);
        } catch (final OpenDataException e) {
            LOG.error("", e);
        }
        return null;
    }
}
