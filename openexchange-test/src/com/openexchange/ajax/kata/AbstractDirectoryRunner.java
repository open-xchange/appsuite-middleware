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

package com.openexchange.ajax.kata;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.kata.fixtures.FixtureStepFactory;
import com.openexchange.test.fixtures.FixtureLoader;
import com.openexchange.test.fixtures.ajax.FixtureLoaderFactory;


/**
 * {@link AbstractDirectoryRunner}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class AbstractDirectoryRunner extends AbstractAJAXSession {

    private final String dirName;
    private final Class aClass;

    public AbstractDirectoryRunner(String name, String dirName, Class aClass) {
        super(name);
        this.dirName = dirName;
        this.aClass = aClass;
    }

    public void testRunKata() throws Exception {
        FixtureLoader loader = buildLoader();
        String[] filenames = scanDirectory();

        AJAXClient client = getClient();

        FixtureStepFactory stepFactory = new FixtureStepFactory(loader);

        for(String filename : filenames) {
            List<Step> steps = stepFactory.loadSteps(aClass, filename);
            try {
                for (Step step : steps) {
                    step.perform(client);
                }
            } finally {
                for(Step step: steps) {
                    try {
                        step.cleanUp();
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }
        }
    }

    private String[] scanDirectory() {
        return new File(dirName).list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".kata.yml");
            }
        });
    }

    private FixtureLoader buildLoader() {
        FixtureLoader loader = FixtureLoaderFactory.getLoader(getClient(), null);
        loader.appendToLoadPath(dirName);
        return loader;
    }
}
