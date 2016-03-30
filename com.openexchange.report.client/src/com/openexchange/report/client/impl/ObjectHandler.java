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

package com.openexchange.report.client.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import com.openexchange.report.Constants;
import com.openexchange.report.client.container.ClientLoginCount;
import com.openexchange.report.client.container.ContextDetail;
import com.openexchange.report.client.container.ContextModuleAccessCombination;
import com.openexchange.report.client.container.MacDetail;
import com.openexchange.report.client.container.Total;
import com.openexchange.report.internal.LoginCounterMBean;

public class ObjectHandler {

    protected static List<Total> getTotalObjects(final MBeanServerConnection mbsc) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        final TabularDataSupport data = (TabularDataSupport) mbsc.getAttribute(new ObjectName(
            "com.openexchange.reporting",
            "name",
            "Reporting"), "Total");

        final List<Total> retval = new ArrayList<Total>();
        for (final Object tmp : data.keySet()) {
            retval.add(new Total(((List<Object>) tmp).get(0).toString(), ((List<Object>) tmp).get(1).toString(), ((List<Object>) tmp).get(2).toString(), ((List<Object>) tmp).get(3).toString()));
        }

        return (retval);
    }

    protected static Map<String, String> getServerConfiguration(final MBeanServerConnection mbsc) throws IOException, InstanceNotFoundException, ReflectionException, MBeanException, MalformedObjectNameException, AttributeNotFoundException {
        final TabularData data = (TabularData) mbsc.getAttribute(new ObjectName(
            "com.openexchange.reporting",
            "name",
            "Reporting"), "Configuration");

        Map<String, String> configuration = new HashMap<>();
        for (final Object tmp : data.values()) {
            final CompositeDataSupport config = (CompositeDataSupport) tmp;
            Object key = config.get("key");
            Object value = config.get("value");
            configuration.put(key.toString(), ((Boolean)value).toString());
        }
        return configuration;
    }

    public static ClientLoginCount getClientLoginCount(final MBeanServerConnection mbsc) throws IOException, InstanceNotFoundException, ReflectionException, MBeanException, MalformedObjectNameException, AttributeNotFoundException, InvalidAttributeValueException {
        return getClientLoginCount(mbsc, false);
    }

    public static ClientLoginCount getClientLoginCount(final MBeanServerConnection mbsc, final boolean forYear) throws MBeanException {
        ClientLoginCount retval = new ClientLoginCount();
        LoginCounterMBean lcProxy = loginCounterProxy(mbsc);

        Calendar cal = Calendar.getInstance();
        Date startDate = null;
        Date endDate = null;
        if (forYear) {
            endDate = cal.getTime();
            cal.add(Calendar.YEAR, -1);
            startDate = cal.getTime();
        } else {
            endDate = cal.getTime();
            cal.add(Calendar.DATE, -30);
            startDate = cal.getTime();
        }

        Map<String, Integer> usmEasResult = lcProxy.getNumberOfLogins(startDate, endDate, true, "USM-EAS");
        Integer usmEas = usmEasResult.get(LoginCounterMBean.SUM);
        retval.setUsmeas(usmEas.toString());

        Map<String, Integer> olox2Result = lcProxy.getNumberOfLogins(startDate, endDate, true, "OpenXchange.HTTPClient.OXAddIn");
        Integer olox2 = olox2Result.get(LoginCounterMBean.SUM);
        retval.setOlox2(olox2.toString());

        Map<String, Integer> mobileAppResult = lcProxy.getNumberOfLogins(startDate, endDate, true, "com.openexchange.mobileapp");
        Integer mobileApp = mobileAppResult.get(LoginCounterMBean.SUM);
        retval.setMobileapp(mobileApp.toString());

        Map<String, Integer> cardDavResults = lcProxy.getNumberOfLogins(startDate, endDate, true, "CARDDAV");
        Integer cardDav = cardDavResults.get(LoginCounterMBean.SUM);
        retval.setCarddav(cardDav.toString());

        Map<String, Integer> calDavResults = lcProxy.getNumberOfLogins(startDate, endDate, true, "CALDAV");
        Integer calDav = calDavResults.get(LoginCounterMBean.SUM);
        retval.setCaldav(calDav.toString());

        return retval;
    }

    private static LoginCounterMBean loginCounterProxy(MBeanServerConnection mbsc) {
        return MBeanServerInvocationHandler.newProxyInstance(mbsc, Constants.LOGIN_COUNTER_NAME, LoginCounterMBean.class, false);
    }

    protected static List<MacDetail> getMacObjects(final MBeanServerConnection mbsc) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        final TabularDataSupport data = (TabularDataSupport) mbsc.getAttribute(new ObjectName(
            "com.openexchange.reporting",
            "name",
            "Reporting"), "Macs");

        final List<MacDetail> retval = new ArrayList<MacDetail>();
        for (final Object tmp : data.values()) {
            final CompositeDataSupport context = (CompositeDataSupport) tmp;

            final MacDetail macDetail = new MacDetail();
            macDetail.setId(context.get("mac").toString());
            macDetail.setCount(context.get("count").toString());
            macDetail.setNrAdm(context.get("nradmin").toString());
            macDetail.setNrDisabled(context.get("nrdisabled").toString());
            retval.add(macDetail);
        }

        return (retval);
    }

    protected static List<ContextDetail> getDetailObjects(final MBeanServerConnection mbsc) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
        final TabularDataSupport data = (TabularDataSupport) mbsc.getAttribute(new ObjectName(
            "com.openexchange.reporting",
            "name",
            "Reporting"), "Detail");

        final List<ContextDetail> retval = new ArrayList<ContextDetail>();
        for (final Object tmp : data.values()) {
            final CompositeDataSupport context = (CompositeDataSupport) tmp;
            final TabularDataSupport moduleAccessCombinations = (TabularDataSupport) context.get("module access combinations");

            final ContextDetail contextDetail = new ContextDetail();
            contextDetail.setId(context.get("identifier").toString());
            contextDetail.setAge(context.get("age").toString());
            contextDetail.setCreated(context.get("created").toString());
            contextDetail.setAdminmac(context.get("admin permission").toString());
            for (final Object tmp2 : moduleAccessCombinations.values()) {
                final CompositeDataSupport moduleAccessCombination = (CompositeDataSupport) tmp2;

                contextDetail.addModuleAccessCombination(new ContextModuleAccessCombination(moduleAccessCombination.get(
                    "module access combination").toString(), moduleAccessCombination.get("users").toString(), moduleAccessCombination.get(
                    "inactive").toString()));
            }
            retval.add(contextDetail);
        }

        return retval;
    }

    protected static List<List<Object>> createTotalList(final List<Total> totals) {
        final List<List<Object>> retval = new ArrayList<List<Object>>();
        retval.add(Arrays.asList((Object) "contexts", "users", "guests", "links"));

        for (final Total tmp : totals) {
            retval.add(Arrays.asList((Object) tmp.getContexts(), tmp.getUsers(), tmp.getGuests(), tmp.getLinks()));
        }

        return retval;
    }

    protected static List<List<Object>> createDetailList(final List<ContextDetail> contextDetails) {
        final List<List<Object>> retval = new ArrayList<List<Object>>();
        retval.add(Arrays.asList((Object) "id", "age", "created", "admin permission", "module access combination", "users", "inactive"));

        final TreeSet<Integer> sorted = new TreeSet<Integer>(new Comparator<Integer>() {

            @Override
            public int compare(final Integer o1, final Integer o2) {
                return o1.compareTo(o2);
            }
        });

        final HashMap<String, List<List<Object>>> sortDetails = new HashMap<String, List<List<Object>>>();

        for (final ContextDetail tmp : contextDetails) {
            for (final ContextModuleAccessCombination moduleAccessCombination : tmp.getModuleAccessCombinations()) {

                sorted.add(new Integer(tmp.getId()));

                List<List<Object>> tmpList;
                if (!sortDetails.containsKey(tmp.getId())) {
                    tmpList = new ArrayList<List<Object>>();
                } else {
                    tmpList = sortDetails.get(tmp.getId());
                }
                tmpList.add(Arrays.asList(
                    (Object) new Integer(tmp.getId()),
                    tmp.getAge(),
                    tmp.getCreated(),
                    tmp.getAdminmac(),
                    moduleAccessCombination.getUserAccessCombination(),
                    moduleAccessCombination.getUserCount(),
                    moduleAccessCombination.getInactiveCount()));
                sortDetails.put(tmp.getId(), tmpList);
            }
        }

        for (final Integer tmp : sorted) {
            for (final List<Object> tmpList : sortDetails.get(String.valueOf(tmp))) {
                retval.add(tmpList);
            }
        }

        return retval;
    }

    protected static List<List<Object>> createConfigurationList(final Map<String, String> serverConfiguration) {
        final List<List<Object>> retval = new ArrayList<List<Object>>();
        retval.add(Arrays.asList((Object) "key", "value"));

        for (final Entry<String, String> tmp : serverConfiguration.entrySet()) {
            retval.add(Arrays.asList((Object) tmp.getKey(), tmp.getValue()));
        }
        return retval;
    }

    protected static List<List<Object>> createMacList(final List<MacDetail> macDetails) {
        final List<List<Object>> retval = new ArrayList<List<Object>>();
        retval.add(Arrays.asList((Object) "mac", "count", "adm", "disabled"));

        for (final MacDetail tmp : macDetails) {
            retval.add(Arrays.asList((Object) tmp.getId(), tmp.getCount(), tmp.getNrAdm(), tmp.getNrDisabled()));
        }
        return retval;
    }

    protected static List<List<Object>> createVersionList(final String[] versions) {
        final List<List<Object>> retval = new ArrayList<List<Object>>();
        retval.add(Arrays.asList((Object) "version", versions[0]));
        retval.add(Arrays.asList((Object) "build date", versions[1]));
        return retval;
    }

    protected static List<List<Object>> createLogincountList(final ClientLoginCount lcount) {
        final List<List<Object>> retval = new ArrayList<List<Object>>();
        retval.add(Arrays.asList((Object) "usmeas", "olox2", "mobileapp", "carddav", "caldav"));
        retval.add(Arrays.asList(
            (Object) lcount.getUsmeas(),
            lcount.getOlox2(),
            lcount.getMobileapp(),
            lcount.getCarddav(),
            lcount.getCaldav()));
        return retval;
    }

    protected static List<List<Object>> createLogincountListYear(final ClientLoginCount lcount) {
        final List<List<Object>> retval = new ArrayList<List<Object>>();
        retval.add(Arrays.asList((Object) "usmeasyear", "olox2year", "mobileappyear", "carddavyear", "caldavyear"));
        retval.add(Arrays.asList(
            (Object) lcount.getUsmeas(),
            lcount.getOlox2(),
            lcount.getMobileapp(),
            lcount.getCarddav(),
            lcount.getCaldav()));
        return retval;
    }

}
