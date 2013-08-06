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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.test.mock.objects;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * Main class for mocks. Each mock should extend {@link AbstractMock} and implement the abstract methods.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public abstract class AbstractMock extends Mockito {

    /**
     * Logger for all mocks
     */
    protected final static Log LOG = com.openexchange.log.Log.loggerFor(AbstractMock.class);

    /**
     * A temporary folder that could be used by each mock.
     */
    @Rule
    protected TemporaryFolder folder = new TemporaryFolder();

    /**
     * Initializes a new {@link AbstractMock}.
     */
    public AbstractMock() {
        super();

        this.init();
    }

    /**
     * Initialize all required members (mocks, fields, ...) during creation
     */
    private void init() {
        try {
            this.createMocks();
            this.initializeMembers();
            this.defineDefaultMockBehaviour();
            this.defineMockSpecificBehaviour();
        } catch (Exception exception) {
            LOG.error("Not able to create the required mocks and its behaviour!", exception);
        }
    }

    /**
     * Define the behavior for all globally usable mocks.
     */
    private void defineDefaultMockBehaviour() throws Exception {
        this.folder.create();
    }

    /**
     * Returns the defined and ready to use mock.
     * 
     * @return The mock with the desired type.
     */
    public abstract <T> T get();

    /**
     * Create the main mock and all other mocks used within the mock class.
     */
    protected abstract void createMocks() throws Exception;

    /**
     * Initialize all members of the mock class. Initialization must be done within this task because members defined in the class are
     * initialized to late and not ready to use within com.openexchange.test.mock.objects.AbstractMock.defineSpecificMockBehaviour()
     */
    protected abstract void initializeMembers() throws Exception;

    /**
     * Define the behavior of your mock. Use com.openexchange.test.mock.main.util.MockDefaultValues to return consistent values.
     */
    protected abstract void defineMockSpecificBehaviour() throws Exception;

    /**
     * Outlines the current configuration of an mock object.
     * 
     * @return The default configuration of this mock.
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}