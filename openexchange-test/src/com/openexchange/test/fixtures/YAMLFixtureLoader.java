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
package com.openexchange.test.fixtures;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jvyaml.YAML;
import com.openexchange.exception.OXException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 * @author Markus Wagner <markus.wagner@open-xchange.com>
 */
public class YAMLFixtureLoader implements FixtureLoader {

    private final List<File> loadPath = new ArrayList<File>();
    private final Map<String, Map<String, Map<String, String>>> fixtureDefinitions = new HashMap<String, Map<String, Map<String, String>>>();
    private final Map<Class<?>, FixtureFactory<?>> factories = new HashMap<Class<?>, FixtureFactory<?>>();
    private final Map<String, Fixtures<?>> fixturesCache = new HashMap<String, Fixtures<?>>();
    private final Map<String, Class<?>> fixturesClasses = new HashMap<String, Class<?>>();

    @Override
    public void appendToLoadPath(final String... paths) {
        for (final String path : paths) {
            loadPath.add(new File(path));
        }
    }

    @Override
    public void load(final String... fixtureNames) throws OXException {
        for (final String fixtureName : fixtureNames) {
            parse(locateFile(fixtureName), fixtureName );
        }
    }

    @Override
    public <T> Fixtures<T> getFixtures(final String fixtureName, final Class<T> aClass) throws OXException {
        if (fixturesCache.containsKey(fixtureName) && fixturesClasses.get(fixtureName).equals(aClass)) {
            return (Fixtures<T>) fixturesCache.get(fixtureName);
        }
        if (null == fixtureDefinitions.get(fixtureName)) {
            load(fixtureName);
        }
        final Fixtures<T> fixtures = getFixtureFactory(aClass).createFixture(fixtureName, fixtureDefinitions.get(fixtureName));
        fixturesCache.put(fixtureName, fixtures);
        fixturesClasses.put(fixtureName, aClass);
        return fixtures;
    }

    public <T> void addFixtureFactory(final FixtureFactory<T> factory, final Class<T> c) {
        factories.put(c, factory);
    }

    private File locateFile(final String fixtureName) throws OXException {
        for (final File path : loadPath) {
            File fixtureFile = new File(path, fixtureName).getAbsoluteFile();
            if( fixtureFile.exists()) {
                return fixtureFile;
            }
            fixtureFile = new File(path, fixtureName+".yml").getAbsoluteFile();
            if (fixtureFile.exists()) {
                return fixtureFile;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (File path : loadPath) {
            sb.append('"');
            sb.append(path.getAbsolutePath());
            sb.append('"');
            sb.append(',');
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        throw new FixtureException("Can't find fixture " + fixtureName + " in path: " +sb.toString());
    }

    private void parse(final File file, final String fixtureName) throws OXException {
    	InputStreamReader reader = null;
        try {
        	reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF8"));
            fixtureDefinitions.put(fixtureName, (Map<String, Map<String, String>>)YAML.load(reader));
        } catch (FileNotFoundException e) {
            throw new FixtureException(e);
		} finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //IGNORE
                }
            }
        }
    }

    private <T> FixtureFactory<T> getFixtureFactory(final Class<T> aClass) throws OXException {
        if (!factories.containsKey(aClass)) {
            throw new FixtureException("Can't load fixtures of type: "+aClass);
        }
        return (FixtureFactory<T>) factories.get(aClass);
    }

}
