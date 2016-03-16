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

package com.openexchange.test.report;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * {@link TestResult}. Represents a test result from a single HTTP request.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TestResult {

    private String testClass;
    private String testMethod;
    private String description;

    private String action;
    private Map<String, String> parameters;
    private Object body;
    private long duration;

    private boolean assertationStatus;
    private String failureReason;
    private Throwable throwable;

    /**
     * Initializes a new {@link TestResult}.
     */
    public TestResult() {
        super();
    }

    /**
     * Gets the duration
     *
     * @return The duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the duration
     *
     * @param duration The duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Gets the assertationStatus
     *
     * @return The assertationStatus
     */
    public boolean isAssertationStatus() {
        return assertationStatus;
    }

    /**
     * Sets the assertationStatus
     *
     * @param assertationStatus The assertationStatus to set
     */
    public void setAssertationStatus(boolean assertationStatus) {
        this.assertationStatus = assertationStatus;
    }

    /**
     * Gets the parameters
     *
     * @return The parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets the parameters
     *
     * @param parameters The parameters to set
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the failureReason
     *
     * @return The failureReason
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Sets the failureReason
     *
     * @param failureReason The failureReason to set
     */
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    /**
     * Gets the testClass
     *
     * @return The testClass
     */
    public String getTestClass() {
        return testClass;
    }

    /**
     * Sets the testClass
     *
     * @param testClass The testClass to set
     */
    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    /**
     * Gets the testMethod
     *
     * @return The testMethod
     */
    public String getTestMethod() {
        return testMethod;
    }

    /**
     * Sets the testMethod
     *
     * @param testMethod The testMethod to set
     */
    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getStacktrace() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter stacktraceWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(stacktraceWriter);
        return stringWriter.toString();
    }

    /**
     * Gets the action
     *
     * @return The action
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the action
     *
     * @param action The action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Gets the body
     *
     * @return The body
     */
    public Object getBody() {
        return body;
    }

    /**
     * Sets the body
     *
     * @param body The body to set
     */
    public void setBody(Object body) {
        this.body = body;
    }
}
