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

package com.openexchange.fitnesse;

import java.util.List;
import junit.framework.AssertionFailedError;
import org.junit.ComparisonFailure;
import com.openexchange.ajax.kata.IdentitySource;
import com.openexchange.ajax.kata.NeedExistingStep;
import com.openexchange.ajax.kata.Step;
import com.openexchange.fitnesse.exceptions.FitnesseException;
import com.openexchange.fitnesse.wrappers.FitnesseResult;
import com.openexchange.fitnesse.wrappers.FixtureDataWrapper;

/**
 * {@link AbstractStepFixture}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractStepFixture extends AbstractTableTable {

    /**
     * Initializes a new {@link AbstractStepFixture}.
     */
    public AbstractStepFixture() {
        super();
        environment = FitnesseEnvironment.getInstance();
    }

    protected abstract Step createStep(FixtureDataWrapper data) throws Exception;

    @Override
    public List doTable() throws Exception {
        Step step = null;
        try {
            step = createStep(data);
        } catch(FitnesseException e){
            return new FitnesseResult(data, FitnesseResult.ERROR + e.getMessage() ).toResult();
        }
        
        if (NeedExistingStep.class.isInstance(step)) {
            IdentitySource identitySource = environment.getSymbol(data.getFixtureName());
            ((NeedExistingStep) step).setIdentitySource(identitySource);
        }
        
        FitnesseResult returnValues = new FitnesseResult(data, FitnesseResult.PASS);
        
        perform(step, returnValues);

        environment.registerStep(step);

        if (IdentitySource.class.isInstance(step)) {
            environment.registerSymbol(data.getFixtureName(), (IdentitySource) step);
        }

        return returnValues.toResult();
    }

    protected void perform(Step step, FitnesseResult returnValues) throws Exception {
        try {
            step.perform(environment.getClient());
        } catch (ComparisonFailure failure) {
            int pos = findFailedFieldPosition(failure.getExpected(), failure);
            returnValues.set(pos, createErrorColumn(failure));
            failure.printStackTrace();
        } catch (AssertionFailedError e) {
            returnValues.set(0, FitnesseResult.ERROR + e.getMessage());
            e.printStackTrace();
        }        
    }

    protected String createErrorColumn(ComparisonFailure failure) throws FitnesseException {
        return FitnesseResult.ERROR + " expected: " + failure.getExpected() + ", actual: " + failure.getActual();
    }

    public int findFailedFieldPosition(String expectedValue, Throwable t) {
        for (int i = 0; i < data.size(); i++) {
            if (expectedValue.equals(data.get(i)))
                return i;
        }
        throw new IllegalStateException("Could not find the broken field in the list of fields. This should not happen. The value expected to find was: "+ expectedValue);

    }

}
