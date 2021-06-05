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
            parse(locateFile(fixtureName), fixtureName);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Fixtures<T> getFixtures(final String fixtureName, final Class<T> aClass) throws OXException {
        if (fixturesCache.containsKey(fixtureName) && fixturesClasses.get(fixtureName).equals(aClass)) {
            return (Fixtures<T>) fixturesCache.get(fixtureName);
        }
        if (null == fixtureDefinitions.get(fixtureName)) {
            load(fixtureName);
        }
        final Fixtures<T> fixtures = getFixtureFactory(aClass).createFixture(fixtureDefinitions.get(fixtureName));
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
            if (fixtureFile.exists()) {
                return fixtureFile;
            }
            fixtureFile = new File(path, fixtureName + ".yml").getAbsoluteFile();
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
        throw new FixtureException("Can't find fixture " + fixtureName + " in path: " + sb.toString());
    }

    private void parse(final File file, final String fixtureName) throws OXException {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF8"));
            fixtureDefinitions.put(fixtureName, (Map<String, Map<String, String>>) YAML.load(reader));
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
            throw new FixtureException("Can't load fixtures of type: " + aClass);
        }
        return (FixtureFactory<T>) factories.get(aClass);
    }

}
