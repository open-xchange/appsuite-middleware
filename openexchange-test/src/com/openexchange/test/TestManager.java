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

package com.openexchange.test;

import com.openexchange.ajax.framework.AbstractAJAXResponse;


/**
 * This interface is implemented by test manager classes. These are classes
 * that manage tests by making sure you can check all kinds of responses,
 * get thrown errors, that after the tests the created objects are deleted
 * and, most importantly, that you can control the failure-behaviour of
 * the requests.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public interface TestManager {
    /**
     * Make sure that all requests called by the manager have
     * failOnError set to this value (if applicable).
     */
    public void setFailOnError(boolean doFail);

    /**
     * Check value of failOnError setting
     */
    public boolean getFailOnError();

    /**
     * Check value of failOnError setting
     */
    public boolean doesFailOnError();

    /**
     * Remove all entities created during the use of this manager.
     * Should not fail if entities have already been removed.
     */
    public void cleanUp();

    /**
     * Returns the last response that should have been executed.
     */
    public AbstractAJAXResponse getLastResponse();

    /**
     * Whether the manager got an exception while executing a request.
     */
    public boolean hasLastException();

    /**
     * Returns the last exception the manager received or null.
     */
    public Throwable getLastException();
}
