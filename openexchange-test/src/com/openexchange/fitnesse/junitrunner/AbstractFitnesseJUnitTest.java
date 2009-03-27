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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.fitnesse.junitrunner;

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import com.neuri.trinidad.JUnitHelper;
import com.neuri.trinidad.TestEngine;
import com.neuri.trinidad.TestRunner;
import com.neuri.trinidad.fitnesserunner.FitNesseRepository;
import com.neuri.trinidad.fitnesserunner.FitTestEngine;
import com.neuri.trinidad.fitnesserunner.SlimTestEngine;

/**
 * 
 * {@link AbstractFitnesseJUnitTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public abstract class AbstractFitnesseJUnitTest extends TestCase {

    protected JUnitHelper helper;

    public AbstractFitnesseJUnitTest(String name) {
        super();
        try {
            helper = new JUnitHelper(new TestRunner(
                initRepository( getWithDefault("fitnesseWikiLocation","/Users/development/workspace/openexchange-test/fitnesse/") ),
                initEngine( getWithDefault("fitnesseEngine","slim") ),
                initOutputPath( getWithDefault("fitnesseOutputPath","/tmp") ) ));

        } catch (IOException e) {
            fail("Instantiation failed: " + e.getMessage());
        }
    }

    protected TestEngine initEngine(String value) {
        if (value.equalsIgnoreCase("slim"))
            return new SlimTestEngine();
        return new FitTestEngine();
    }

    protected String initOutputPath(String value) {
        return new File(value).getAbsolutePath();
    }

    protected FitNesseRepository initRepository(String value) throws IOException {
        return new FitNesseRepository(value);
    }

    protected String getWithDefault(String key, String defaultValue){
        String value = System.getProperty(key);
        if(value == null)
            return defaultValue;
        return value;
    }
}
