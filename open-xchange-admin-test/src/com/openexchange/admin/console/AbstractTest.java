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

package com.openexchange.admin.console;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractTest {

    protected  static String TEST_DOMAIN = "example.org";
    protected  static String CHANGE_SUFFIX = "_changed";

    protected static String OPTION_CONTEXT_ADMIN_USER = "--adminuser=oxadmin";
    protected static String OPTION_CONTEXT_ADMIN_PWD = "--adminpass=secret";
    protected static String OPTION_SUPER_ADMIN_USER = "--adminuser=oxadminmaster";
    protected static String OPTION_SUPER_ADMIN_PWD = "--adminpass=secret";
    protected static String OPTION_USER_PASSWORD = "--password=foo-user-pass";

    protected static String VALID_CHAR_TESTUSER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    protected static String VALID_CHAR_TESTRESOURCE = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _-+.%$@";
    protected static String VALID_CHAR_TESTGROUP = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _-+.%$@";


    protected int returnCode;
    protected ByteArrayOutputStream errBytes;
    protected ByteArrayOutputStream outBytes;
    protected PrintStream errConsole;
    protected PrintStream outConsole;

    @Before
    public void grabStreams() {
        errBytes = new ByteArrayOutputStream();
        outBytes = new ByteArrayOutputStream();

        errConsole = System.err;
        outConsole = System.out;

        System.setErr(new PrintStream(errBytes));
        System.setOut(new PrintStream(outBytes));
    }

    /**
     * Reset the buffers, so that the next test doesn't use the output of this one!
     * Should be called after checking result of test via an assert method!
     */
    protected void resetBuffers(){
        errBytes.reset();
        outBytes.reset();
    }

    @After
    public void tearDown() {
        if(errConsole == null) {
            return;
        }
        System.setErr(errConsole);
        System.setOut(outConsole);

        System.out.println("OUT-->" + outBytes.toString());
        System.out.println("ERR-->" + errBytes.toString());
        System.out.println("Returncode-->" + this.returnCode);

    }

    /**
     *
     * @return String[] with only oxadmin and password option set and a "--foouknownoption=bar" option
     */
    public static String[] getUnknownOptionData(){
        final String[] tmp = {OPTION_CONTEXT_ADMIN_USER, OPTION_CONTEXT_ADMIN_PWD,"--foouknownoption=bar"};

        return  tmp;
    }

    /**
     *
     * @return String[] with only oxadmin and password option and NO other option set.
     */
    public static String[] getMissingOptionData(){
        final String[] tmp = {OPTION_CONTEXT_ADMIN_USER, OPTION_CONTEXT_ADMIN_PWD};

        return  tmp;
    }

    /**
     *
     * @return String[] with only wrong oxadmin and wrong password option
     */
    public static String[] getWrongCredentialsOptionData(){
        final String[] tmp = {OPTION_CONTEXT_ADMIN_USER+"_xyzf00bar", OPTION_CONTEXT_ADMIN_PWD+"_xyzfoobar"};

        return  tmp;
    }

    /**
     *
     * @return String[] with only wrong oxadminmaster and wrong password option
     */
    public static String[] getWrongMasterCredentialsOptionData(){
        final String[] tmp = {OPTION_SUPER_ADMIN_USER+"_xyzf00bar", OPTION_SUPER_ADMIN_PWD+"_xyzfoobar"};

        return  tmp;
    }

    /**
     *
     * @return String[] with only  oxadminmaster and  password option ,usefull for list tools!
     */
    public static String[] getMasterCredentialsOptionData(){
        final String[] tmp = {OPTION_SUPER_ADMIN_USER, OPTION_SUPER_ADMIN_PWD};

        return  tmp;
    }

    /**
     *
     * @return String[] with only oxadmin and password option and "--csv" option set.
     */
    public static String[] getCSVOptionData(){
        final String[] tmp = {OPTION_CONTEXT_ADMIN_USER, OPTION_CONTEXT_ADMIN_PWD,"--csv"};

        return  tmp;
    }

    /**
     *
     * @return String[] with only oxadmin and password option and "--csv" option set.
     */
    public static String[] getCSVMasterOptionData(){
        final String[] tmp = {OPTION_SUPER_ADMIN_USER, OPTION_SUPER_ADMIN_PWD,"--csv"};

        return  tmp;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    protected static String getRMIHost() {
        String host = "localhost";

        // if (System.getProperty("rmi_test_host") != null) {
        // host = System.getProperty("rmi_test_host");
        // }
        if (System.getProperty("host") != null) {
            host = System.getProperty("host");
        }

        return host;
    }
}
