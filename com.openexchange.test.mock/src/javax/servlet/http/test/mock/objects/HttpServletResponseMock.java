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

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.test.mock.objects.AbstractMock;
import com.openexchange.test.mock.util.MockDefaultValues;

/**
 * Mock for {@link HttpServletResponse}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class HttpServletResponseMock<T extends HttpServletResponse> extends AbstractMock {

    /**
     * The mock for {@link HttpServletResponse}
     */
    private T httpServletResponse;

    /**
     * Mock for the {@link ServletOutputStream}
     */
    private ServletOutputStream servletOutputStream;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get() {
        return (T) this.httpServletResponse;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createMocks() {
        this.httpServletResponse = (T) PowerMockito.mock(HttpServletResponse.class);
        this.servletOutputStream = PowerMockito.mock(ServletOutputStream.class);
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
            PowerMockito.when(this.httpServletResponse.getOutputStream()).thenReturn(this.servletOutputStream);
        } catch (IOException e) {
            // should be fine
        }
        PowerMockito.when(this.httpServletResponse.containsHeader(anyString())).thenReturn(false);
        PowerMockito.when(this.httpServletResponse.encodeURL(anyString())).thenReturn(MockDefaultValues.DEFAULT_ENCODED_URL);
        PowerMockito.when(this.httpServletResponse.getBufferSize()).thenReturn(1024);
        PowerMockito.when(this.httpServletResponse.getCharacterEncoding()).thenReturn(MockDefaultValues.DEFAULT_CHARACTER_ENCODING);
        PowerMockito.when(this.httpServletResponse.getContentType()).thenReturn(MockDefaultValues.DEFAULT_CONTENT_TYPE);
        PowerMockito.when(this.httpServletResponse.getLocale()).thenReturn(MockDefaultValues.DEFAULT_LOCALE);
        PowerMockito.when(this.httpServletResponse.isCommitted()).thenReturn(false);
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
            result.append(" getOutputStream(): " + this.httpServletResponse.getOutputStream() + newLine);
        } catch (IOException e) {
            // should be fine
        }
        result.append(" containsHeader(...): " + this.httpServletResponse.containsHeader(MockDefaultValues.DEFAULT_ANY_STRING) + newLine);
        result.append(" encodeURL(...): " + this.httpServletResponse.encodeURL(MockDefaultValues.DEFAULT_ANY_STRING) + newLine);
        result.append(" getBufferSize(): " + this.httpServletResponse.getBufferSize() + newLine);
        result.append(" getCharacterEncoding(): " + this.httpServletResponse.getCharacterEncoding() + newLine);
        result.append(" getContentType(): " + this.httpServletResponse.getContentType() + newLine);
        result.append(" getLocale(): " + this.httpServletResponse.getLocale() + newLine);
        result.append(" isCommitted(): " + this.httpServletResponse.isCommitted() + newLine);
        result.append("}");

        return result.toString();
    }

}
