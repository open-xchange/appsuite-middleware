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

package com.openexchange.ajax.kata.fixtures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.kata.Step;
import com.openexchange.ajax.kata.appointments.AppointmentIdentitySource;
import com.openexchange.ajax.kata.appointments.CreateAppointmentStep;
import com.openexchange.ajax.kata.appointments.NeedExistingStep;
import com.openexchange.ajax.kata.appointments.UpdateAppointmentStep;
import com.openexchange.ajax.kata.appointments.VerificationStep;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.test.fixtures.Fixture;

/**
 * {@link AppointmentFixtureTransformer}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AppointmentFixtureTransformer implements FixtureTransformer {

    private Map<String, AppointmentIdentitySource> memory = new HashMap<String, AppointmentIdentitySource>();

    private List<PendingResolve> pending = new ArrayList<PendingResolve>();

    public boolean handles(Class aClass, String fixtureName, Fixture fixture) {
        return aClass == AppointmentObject.class;
    }

    public Step transform(Class aClass, String fixtureName, Fixture fixture, String displayName) {
        if (isCreate(fixtureName)) {
            return remember(fixtureName, new CreateAppointmentStep(
                (AppointmentObject) fixture.getEntry(),
                displayName,
                (String) fixture.getAttribute("expectedError")));
        } else if (isUpdate(fixtureName)) {
            return assign(fixtureName, new UpdateAppointmentStep(
                (AppointmentObject) fixture.getEntry(),
                displayName,
                (String) fixture.getAttribute("expectedError")));
        } else if (isVerfication(fixtureName)) {
            return assign(fixtureName, new VerificationStep((AppointmentObject) fixture.getEntry(), displayName));
        }
        return null;
    }

    public void resolveAll() {
        for (PendingResolve pendingResolve : new ArrayList<PendingResolve>(pending)) {
            pending.remove(pendingResolve);
            resolve(pendingResolve.fixtureName, pendingResolve.needExisting);
        }
    }

    private Step assign(String fixtureName, NeedExistingStep needExisting) {
        if (!resolve(fixtureName, needExisting)) {
            pending.add(new PendingResolve(fixtureName, needExisting));
        }
        return needExisting;
    }

    private boolean resolve(String fixtureName, NeedExistingStep needExisting) {
        fixtureName = idSourceName(fixtureName);
        if (memory.containsKey(fixtureName)) {
            needExisting.setIdentitySource(memory.get(fixtureName));
            return true;
        }
        return false;
    }

    private Step remember(String fixtureName, CreateAppointmentStep createAppointmentStep) {
        memory.put(idSourceName(fixtureName), createAppointmentStep);
        return createAppointmentStep;
    }

    private boolean isVerfication(String fixtureName) {

        return postfix(fixtureName).contains("verify");
    }

    private boolean isUpdate(String fixtureName) {
        if (isVerfication(fixtureName)) {
            return false;
        }
        return postfix(fixtureName).contains("update");
    }

    private String postfix(String fixtureName) {
        int index = fixtureName.lastIndexOf('_');
        if (index == -1) {
            return fixtureName;
        }
        return fixtureName.substring(index);
    }

    private boolean isCreate(String fixtureName) {
        return !isVerfication(fixtureName) && !isUpdate(fixtureName);
    }

    private String idSourceName(String fixtureName) {
        int index = fixtureName.lastIndexOf('_');
        if (index == -1) {
            return fixtureName;
        }
        return fixtureName.substring(0, index);
    }

    public class PendingResolve {

        public String fixtureName;

        public NeedExistingStep needExisting;

        public PendingResolve(String fixtureName, NeedExistingStep needExisting) {
            this.fixtureName = fixtureName;
            this.needExisting = needExisting;
        }

    }

}
