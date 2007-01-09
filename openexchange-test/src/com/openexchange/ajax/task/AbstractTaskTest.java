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

package com.openexchange.ajax.task;

import java.io.IOException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.api2.OXException;

/**
 * Abstract super class for all task tests. This class contains convenient
 * methods for task tests.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class AbstractTaskTest extends AbstractAJAXTest {

    /**
     * Proxy attribute for the private task folder of the user.
     */
    private int privateTaskFolder;

    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public AbstractTaskTest(final String name) {
        super(name);
    }

    /**
     * @return the identifier of the private task folder of the primary user.
     * @throws IOException if the communication with the server fails.
     * @throws SAXException if a SAX error occurs.
     * @throws JSONException if parsing of serialized json fails.
     * @throws OXException if reading the folders fails.
     */
    protected int getPrivateTaskFolder() throws IOException, SAXException,
        JSONException, OXException {
        if (0 == privateTaskFolder) {
            privateTaskFolder = Tools.getPrivateTaskFolder(getWebConversation(),
                getHostName(), getSessionId());
        }
        return privateTaskFolder;
    }
}
