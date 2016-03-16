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

import com.openexchange.ajax.kata.Step;
import com.openexchange.ajax.kata.tasks.TaskCreateStep;
import com.openexchange.ajax.kata.tasks.TaskDeleteStep;
import com.openexchange.ajax.kata.tasks.TaskUpdateStep;
import com.openexchange.ajax.kata.tasks.TaskVerificationStep;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.fixtures.Fixture;

/**
 * {@link TaskFixtureTransformer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TaskFixtureTransformer extends AbstractFixtureTransformer<Task> implements FixtureTransformer {

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajax.kata.fixtures.FixtureTransformer#handles(java.lang.Class, java.lang.String,
     * com.openexchange.test.fixtures.Fixture)
     */
    @Override
    public boolean handles(Class class1, String fixtureName, Fixture fixture) {
        return class1 == Task.class;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajax.kata.fixtures.FixtureTransformer#transform(java.lang.Class, java.lang.String,
     * com.openexchange.test.fixtures.Fixture, java.lang.String)
     */
    @Override
    public Step transform(Class class1, String fixtureName, Fixture fixture, String displayName) {
        if (isUpdate(fixtureName)) {
            return assign(fixtureName, new TaskUpdateStep(
                (Task) fixture.getEntry(),
                displayName,
                (String) fixture.getAttribute("expectedError")));
        } else if (isVerification(fixtureName)) {
            return assign(fixtureName, new TaskVerificationStep((Task) fixture.getEntry(), displayName));
        } else if (isDelete(fixtureName)) {
            return assign(fixtureName, new TaskDeleteStep(
                (Task) fixture.getEntry(),
                displayName,
                (String) fixture.getAttribute("expectedError")));
        } else if (isCreate(fixtureName)) {
            TaskCreateStep step = new TaskCreateStep((Task) fixture.getEntry(), displayName, (String) fixture.getAttribute("expectedError"));
            remember(fixtureName, step);
            return step;
        }
        return null;
    }

}
