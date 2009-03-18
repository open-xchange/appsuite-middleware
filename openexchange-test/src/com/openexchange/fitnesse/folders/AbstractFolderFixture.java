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

package com.openexchange.fitnesse.folders;

import java.util.Map;
import com.openexchange.ajax.kata.Step;
import com.openexchange.fitnesse.AbstractStepFixture;
import com.openexchange.fitnesse.exceptions.FitnesseException;
import com.openexchange.fitnesse.wrappers.FixtureDataWrapper;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.fixtures.Fixture;
import com.openexchange.test.fixtures.FixtureException;
import com.openexchange.test.fixtures.Fixtures;
import com.openexchange.test.fixtures.FolderFixtureFactory;


/**
 * {@link AbstractFolderFixture}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class AbstractFolderFixture extends AbstractStepFixture {

    /* (non-Javadoc)
     * @see com.openexchange.fitnesse.AbstractStepFixture#createStep(com.openexchange.fitnesse.wrappers.FixtureDataWrapper)
     */
    @Override
    protected Step createStep(FixtureDataWrapper data) throws Exception {
        String fixtureName = data.getFixtureName();
        FolderObject folder = createFolder(fixtureName, data);
        
        Step step = createStep(folder, fixtureName, data.getExpectedError());
        return step;
    }
    
    /**
     * @param fixtureName
     * @param data
     * @throws FitnesseException 
     */
    protected FolderObject createFolder(String fixtureName, FixtureDataWrapper data) throws FixtureException, FitnesseException {
        FolderFixtureFactory factory = new FolderFixtureFactory(null);
        Map<String, Map<String, String>> fixtureMap = data.asFixtureMap("folder");
        
        String permissionRef = removePermissions(fixtureMap.get("folder"));
        
        Fixtures<FolderObject> fixtures = factory.createFixture(data.getFixtureName(), fixtureMap);
        Fixture<FolderObject> fixture = fixtures.getEntry("folder");
        FolderObject folder = fixture.getEntry();
        if(!folder.containsFolderName()) {
            folder.setFolderName(data.getFixtureName());
        }
        
        if(permissionRef != null) {
            PermissionDefinition permissions = environment.getPermissions(permissionRef);
            if(permissions == null) {
                throw new FitnesseException("Can't find permission set: "+permissions);
            }
            folder.setPermissions(permissions.getPermissions());
        }
        return (FolderObject) addFolder(folder, data, 0);
    }

    /**
     * @param map
     */
    private String removePermissions(Map<String, String> map) {
        return map.remove("permissions");
    }

    protected abstract Step createStep(FolderObject folder, String fixtureName, String expectedError) throws Exception;

}
