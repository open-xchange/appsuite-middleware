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

package com.openexchange.test.mock.objects.hazelcast.configuration;

import org.powermock.api.mockito.PowerMockito;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.test.mock.objects.AbstractMock;


/**
 * Mock for the {@link HazelcastConfigurationService}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class HazelcastConfigurationServiceMock<T extends HazelcastConfigurationService> extends AbstractMock {

    /**
     * The mocked {@link HazelcastConfigurationService}
     */
    private T hazelcastConfigurationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get() {
        return (T) this.hazelcastConfigurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createMocks() throws Exception {
        this.hazelcastConfigurationService = (T) PowerMockito.mock(HazelcastConfigurationService.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeMembers() {
        // nothing to do yet
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void defineMockSpecificBehaviour() {
        try {
            PowerMockito.when(this.hazelcastConfigurationService.getConfig()).thenReturn(new com.hazelcast.config.Config());
            PowerMockito.when(this.hazelcastConfigurationService.isEnabled()).thenReturn(true);
        } catch (OXException oxException) {
            LOG.error("Not able to define mock specific behaviour", oxException);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append("State for: " + this.getClass().getSimpleName() + newLine);
        result.append("{" + newLine);

        try {
            result.append(" isEnabled(): " + this.hazelcastConfigurationService.isEnabled() + newLine);
            result.append(" getConfig(): " + this.hazelcastConfigurationService.getConfig().toString() + newLine);
        } catch (OXException oxException) {
            LOG.warn("Not able to read", oxException);
        }
        // add more values here
        result.append("}");

        return result.toString();
    }

}
