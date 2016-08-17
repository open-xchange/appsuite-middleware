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

package com.openexchange.html.bugtests;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link Bug40189Test}
 * 
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class Bug40189Test extends AbstractSanitizing {
    @Test
    public void testInsecureHref() {

        String content = "<a href=3D\"http://neon-response.hmmh.de/argon/jsp/DoubleOptNLZG=" +
            ".jsp?DOINLZGID=3D1SBPO-WgpKBxr3e3AzwHxpL8UI1qj72ypu-bJfhmKYs&SecureAdressKe=" +
            "y=3Dej7wSI1OsAQcy3wVWJAF%2Bg%3D%3D&amp;SendRedirect=3Dhttp%3A%2F%2Femp-onli=" +
            "ne.co.uk%2Fnew_registered__%2F%3Fcrm_id%3D%3C%25SecureAdressKey%25%3E\" targ=" +
            "et=3D\"_blank\"><strong>clicking here</strong></a>";

        String test = getHtmlService().sanitize(content, null, true, null, null);

        assertTrue("Unexpected return value", test.contains("http://neon-response.hmmh.de/argon/jsp/DoubleOptNLZG=.jsp?DOINLZGID=3D1SBPO-WgpKBxr3e3AzwHxpL8UI1qj72ypu-bJfhmKYs&amp;SecureAdressKe=y=3Dej7wSI1OsAQcy3wVWJAF%2Bg%3D%3D&amp;SendRedirect=3Dhttp%3A%2F%2Femp-onli=ne.co.uk%2Fnew_registered__%2F%3Fcrm_id%3D%3C%25SecureAdressKey%25%3E"));
    }
}
