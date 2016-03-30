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
package com.openexchange.admin.rmi;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;

/**
 *
 * @author cutmasta
 */
public abstract class AbstractTest {

    protected  static String TEST_DOMAIN = "example.org";
    protected  static String change_suffix = "-changed";

    protected static String getRMIHostUrl(){
        String host = getRMIHost();

        if(!host.startsWith("rmi://")){
            host = "rmi://"+host;
        }
        if(!host.endsWith("/")){
            host = host+"/";
        }
        return host;
    }

    protected static String getRMIHost() {
        String host = "localhost";

        if (System.getProperty("rmi_test_host") != null) {
            host = System.getProperty("rmi_test_host");
        }

        return host;
    }

    public static Credentials DummyCredentials(){
        return new Credentials("oxadmin","secret");
    }

    // The throwing of the exception is necessary to be able to let methods which override
    // this one throw exceptions. So don't remove this
    public static Context getTestContextObject(final Credentials cred) throws Exception {
        return getTestContextObject(1, 50);
    }

    public static Context getTestContextObject(final long quota_max_in_mb) {
        return getTestContextObject(1, quota_max_in_mb);
    }

    public static Context getTestContextObject(final int context_id, final long quota_max_in_mb) {
        final Context ctx = new Context(context_id);
        final Filestore filestore = new Filestore();
        filestore.setSize(quota_max_in_mb);
        ctx.setFilestoreId(filestore.getId());
        return ctx;
    }

    public static String getChangedEmailAddress(String address, String changed) {
        return address.replaceFirst("@", changed+"@");
    }

    public static Credentials DummyMasterCredentials() {
        String mpw = "secret";
        if(System.getProperty("rmi_test_masterpw")!=null){
            mpw = System.getProperty("rmi_test_masterpw");
        }
        return new Credentials("oxadminmaster",mpw);
    }
}
