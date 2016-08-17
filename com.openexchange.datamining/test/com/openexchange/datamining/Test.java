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

package com.openexchange.datamining;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import junit.framework.TestCase;


/**
 * {@link Test}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Test extends TestCase {

    PrintStream out;

    @Override
    public void setUp(){
        String printString = new String();
        out = new PrintStream(new ByteArrayOutputStream());
        System.setOut(out);
    }

    public void shutDown(){

    }

    public static void testWithoutParameters(){
        String[] array = {};
        Datamining.main(array);
    }

    public static void testWithOnlyVerboseParameter(){
        String[] array = {"-v"};
        Datamining.main(array);
    }

    public static void testWitOnlyHelpParameter(){
        String[] array = {"-h"};
        Datamining.main(array);
    }

    public static void testWithParametersForVM(){
        String[] array = {"-n", "172.16.119.135", "-u", "openexchange", "-p", "db_password", "-v", "--reportfilePath", "/Users/karstenwill/Desktop/"};
        Datamining.main(array);
    }

    public static void testWithParametersForQADB(){
        String[] array = {"-hostname", "10.20.30.214", "-dbName", "configdb", "-dbUser", "openexchange", "-dbPassword", "secret", "-v"};
        Datamining.main(array);
    }

    public static void testWithParametersForSteffensMaster(){
        String[] array = {"-hostname", "10.20.31.104", "-dbName", "configdb", "-dbUser", "openexchange", "-dbPassword", "secret", "-v"};
        Datamining.main(array);
    }

    public static void testWithParametersForSteffensSlave(){
        String[] array = {"-hostname", "10.20.31.103", "-dbName", "configdb", "-dbUser", "openexchange", "-dbPassword", "secret", "-v"};
        Datamining.main(array);
    }

    public static void testWithIncorrectParameters(){
        String[] array = {"-hostname", "172.16.119.128", "-dbUser", "openexchange", "-dbPassword", "db_password", "-v", "-dbPort", "9999"};
        Datamining.main(array);
    }

    public static void testWithMissingParameter(){
        String[] array = { "-dbUser", "openexchange", "-dbPassword", "db_password", "-v"};
        Datamining.main(array);
    }

    public static void testParameterWithoutValue(){
        String[] array = {"-hostname", "-dbUser", "openexchange", "-dbPassword", "db_password", "-v", "-dbPort", "9999"};
        Datamining.main(array);
    }

    public static void testThatFilepathStillWorksEvenIfNoOtherParametersAreGiven(){
        String[] array = {"-v","--reportfilePath", "/Users/karstenwill/Desktop/"};
        Datamining.main(array);
        boolean exists = (new File("/Users/karstenwill/Desktop/"+Datamining.getFilename()).exists());
        assertTrue("report file is not there", exists);
    }

    public static void testThatHelpIsReallyPrinted(){
    }

}
