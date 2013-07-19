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

package javax.servlet.http.test.mock.objects;

import javax.servlet.http.HttpSession;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.test.mock.objects.AbstractMock;

/**
 * Mock for the {@link HttpSession}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class HttpSessionMock<T extends HttpSession> extends AbstractMock {

    /**
     * The mock for {@link HttpSession}
     */
    private T httpSession;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get() {
        return (T) this.httpSession;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createMocks() throws Exception {
        this.httpSession = (T) PowerMockito.mock(HttpSession.class);
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
        // nothing to do yet
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
        result.append(" getId(): " + this.httpSession.getId() + newLine);
        // add more values here
        result.append("}");

        return result.toString();
    }
}
