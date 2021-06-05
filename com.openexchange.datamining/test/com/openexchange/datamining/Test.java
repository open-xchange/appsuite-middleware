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

package com.openexchange.datamining;

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import org.junit.Before;


/**
 * {@link Test}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Test {
    PrintStream out;

    @Before
    public void setUp(){
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
