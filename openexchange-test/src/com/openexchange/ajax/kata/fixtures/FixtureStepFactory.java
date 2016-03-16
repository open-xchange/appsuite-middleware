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

package com.openexchange.ajax.kata.fixtures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.kata.Step;
import com.openexchange.exception.OXException;
import com.openexchange.test.fixtures.Fixture;
import com.openexchange.test.fixtures.FixtureLoader;
import com.openexchange.test.fixtures.Fixtures;

/**
 * {@link FixtureStepFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FixtureStepFactory {

    private final FixtureLoader loader;

    public FixtureStepFactory(FixtureLoader loader) {
        this.loader = loader;
    }

    public <T> List<Step> loadSteps(Class<T> aClass, String... fixtureNames) throws OXException {
        List<Step> steps = new ArrayList<Step>();
        for (int i = 0; i < fixtureNames.length; i++) {
            String fixtureName = fixtureNames[i];
            Fixtures<T> fixtures = loader.getFixtures(fixtureName, aClass);
            addFixtures(aClass, fixtures, steps, fixtureName);
        }
        return steps;
    }

    private <T> void addFixtures(Class<T> aClass, Fixtures<T> fixtures, List<Step> steps,  String filename) throws OXException {
        List<FixtureTransformer> transformer = getAllTransformers();

        List<Entry<T>> entries = new ArrayList<Entry<T>>();
        for (String name : fixtures.getEntryNames()) {
            Fixture<T> entry = fixtures.getEntry(name);
            entries.add(new Entry<T>(name, entry));
        }
        Collections.sort(entries);

        for(Entry<T> entry : entries) {
            for(FixtureTransformer trans : transformer) {
                if(trans.handles(aClass, entry.name, entry.fixture)) {
                    steps.add( trans.transform(aClass, entry.name, entry.fixture, filename+": Step "+entry.fixture.getAttribute("step")) );
                }
            }
        }

        for (FixtureTransformer trans : transformer) {
            trans.resolveAll();
        }
    }

    private List<FixtureTransformer> getAllTransformers() {
        return new ArrayList<FixtureTransformer>(){{
            add(new AppointmentFixtureTransformer());
			add(new TaskFixtureTransformer());
            add(new ContactFixtureTransformer());
            add(new FolderFixtureTransformer());
        }};
    }

    private static final class Entry<T> implements Comparable<Entry<T>>{
        public String name;
        public Fixture<T> fixture;

        public Entry(String name, Fixture<T> fixture) {
            this.name = name;
            this.fixture = fixture;
        }

        @Override
        public int compareTo(Entry<T> o) {
            Long myStep = fixture.getAttribute("step") != null ? (Long)fixture.getAttribute("step") : Long.valueOf(0);
            Long otherStep = o.fixture.getAttribute("step") != null ? (Long)o.fixture.getAttribute("step") : Long.valueOf(0);
            return new Long(myStep.longValue() - otherStep.longValue()).intValue();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
