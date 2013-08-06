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

package com.openexchange.test.mock.main;

import org.apache.commons.logging.Log;
import com.openexchange.session.Session;
import com.openexchange.test.mock.objects.AbstractMock;

/**
 * The {@link MockFactory} creates the pre-configured mocks that can be used for unit testing. <br>
 * <br>
 * It is important to give the mock the fully qualified name with the described pattern in
 * com.openexchange.test.mock.main.MockFactory.getMock(Class<T>)
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class MockFactory {

    /**
     * Logger for the factory
     */
    protected final static Log LOG = com.openexchange.log.Log.loggerFor(MockFactory.class);

    /**
     * Returns a mock with the given type.<br>
     * <br>
     * Therefore you have to create an appropriate mock that extends {@link AbstractMock}. The mock has to be placed in the package with the
     * following pattern and named with '<Name_of_the_class_to_mock>Mock'<br>
     * <br>
     * The package name of the mock must be extended with 'test.mock.objects' after 'com.openexchange'. So each mock package name should
     * start with 'com.openexchange.test.mock.objects'<br <br>
     * Example: The {@link Session} which canonical name is 'com.openexchange.session.Session' must have the mock:
     * {@link com.openexchange.test.mock.objects.session.SessionMock<T>}
     * 
     * @param type - The type the mock should be created for
     * @return Mock with the given type
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T> T getMock(Class<T> type) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String mockName = MockFactory.getFullyQualifiedMockName(type);

        Object mockObject = Class.forName(mockName).newInstance();

        if (mockObject instanceof AbstractMock) {
            AbstractMock mock = (AbstractMock) Class.forName(mockName).newInstance();

            return mock.get();
        }
        throw new InstantiationException("Wrong type of mock returned. Your mock must extend AbstractMock");
    }

    /**
     * Returns the qualified name of the mock to create
     * 
     * @param type - the class a mock should be created for
     * @return String - the name of the class the the mock should be located
     */
    private static String getFullyQualifiedMockName(Class<?> type) {
        String mockName = type.getCanonicalName();

        if (mockName.startsWith("com.openexchange.")) {
            String lastPart = type.getCanonicalName().substring("com.openexchange.".length());
            mockName = "com.openexchange.test.mock.objects." + lastPart + "Mock";
        } else {
            mockName = "com.openexchange.test.mock.objects.external." + mockName + "Mock";
        }

        return mockName;
    }
}
