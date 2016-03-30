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
package com.openexchange.config.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Properties;
import junit.framework.TestCase;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ReinitializableConfigProviderService;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ConfigurationImplTest extends TestCase {
    private File configFolder;

    @Override
	public void setUp() {
        configFolder = new File("/tmp/configTest");
        configFolder.mkdirs();
    }

    @Override
	public void tearDown() {
        deleteAll(configFolder);
    }

    private void deleteAll(final File file) {
        if(file.isFile()) {
            file.delete();
        } else {
            for(final File subfile : file.listFiles()) {
                deleteAll(subfile);
            }
            file.delete();
        }
    }

    public void testGetAllBelowDirectory() throws IOException {
        final Properties props = new Properties();
        props.put("prop1", "value1");
        props.put("prop2", "value2");
        dump(props, "subfolder", "expectedProps.properties");

        final Properties props2 = new Properties();
        props2.put("prop3", "value3");
        props2.put("prop4", "value4");
        dump(props2, "subfolder", "otherExpectedProps.properties");

        final Properties props3 = new Properties();
        props3.put("prop5", "value5");
        props3.put("prop6", "value6");
        dump(props3, "subfolder","subsubfolder", "yetMoreExpectedProps.properties");

        final Properties props4 = new Properties();
        props4.put("prop7", "value7");
        props4.put("prop8", "value8");
        dump(props4, "otherFolder","unexpected.properties");


        final ConfigurationService config = getConfiguration();

        final Properties aggregated = config.getPropertiesInFolder("subfolder");

        assertEquals("value1", aggregated.get("prop1"));
        assertEquals("value2", aggregated.get("prop2"));
        assertEquals("value3", aggregated.get("prop3"));
        assertEquals("value4", aggregated.get("prop4"));
        assertEquals("value5", aggregated.get("prop5"));
        assertEquals("value6", aggregated.get("prop6"));
        assertNull(aggregated.get("prop7"));
        assertNull(aggregated.get("prop8"));
    }

    private void dump(final Properties props, final String...path) throws IOException {
        File folder = configFolder;
        for(int i = 0; i < path.length-1; i++) {
            folder = new File(folder, path[i]);
            folder.mkdirs();
        }
        final File propertiesFile = new File(folder, path[path.length-1]);
        if(propertiesFile.exists()) {
            propertiesFile.delete();
        }
        OutputStream out = null;
        try {
            propertiesFile.createNewFile();
            out = new FileOutputStream(propertiesFile);
            props.store(out,"");
        } finally {
            if(out != null) {
                out.close();
            }
        }
    }

    protected ConfigurationService getConfiguration() {
        return new ConfigurationImpl(new String[] { configFolder.getAbsolutePath() }, Collections.<ReinitializableConfigProviderService> emptyList());
    }
}
