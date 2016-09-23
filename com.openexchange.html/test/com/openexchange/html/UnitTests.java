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

package com.openexchange.html;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.html.bugtests.Bug16800Test;
import com.openexchange.html.bugtests.Bug16843Test;
import com.openexchange.html.bugtests.Bug17195Test;
import com.openexchange.html.bugtests.Bug19428Test;
import com.openexchange.html.bugtests.Bug19466Test;
import com.openexchange.html.bugtests.Bug19522Test;
import com.openexchange.html.bugtests.Bug20968Test;
import com.openexchange.html.bugtests.Bug21014Test;
import com.openexchange.html.bugtests.Bug21042Test;
import com.openexchange.html.bugtests.Bug21055Test;
import com.openexchange.html.bugtests.Bug21118Test;
import com.openexchange.html.bugtests.Bug21532Test;
import com.openexchange.html.bugtests.Bug21584Test;
import com.openexchange.html.bugtests.Bug21668Test;
import com.openexchange.html.bugtests.Bug21757Test;
import com.openexchange.html.bugtests.Bug22072Test;
import com.openexchange.html.bugtests.Bug22304Test;
import com.openexchange.html.bugtests.Bug23368Test;
import com.openexchange.html.bugtests.Bug24899Test;
import com.openexchange.html.bugtests.Bug25923Test;
import com.openexchange.html.bugtests.Bug26153Test;
import com.openexchange.html.bugtests.Bug26316Test;
import com.openexchange.html.bugtests.Bug26789Test;
import com.openexchange.html.bugtests.Bug27335Test;
import com.openexchange.html.bugtests.Bug28094Test;
import com.openexchange.html.bugtests.Bug28337Test;
import com.openexchange.html.bugtests.Bug28637Test;
import com.openexchange.html.bugtests.Bug29229Test;
import com.openexchange.html.bugtests.Bug29695Test;
import com.openexchange.html.bugtests.Bug29892Test;
import com.openexchange.html.bugtests.Bug31826Test;
import com.openexchange.html.bugtests.Bug35291Test;
import com.openexchange.html.bugtests.Bug35546Test;
import com.openexchange.html.bugtests.Bug40189Test;
import com.openexchange.html.internal.Bug27708Test;
import com.openexchange.html.internal.HtmlServiceImplTest;
import com.openexchange.html.internal.css.Bug30114Test;
import com.openexchange.html.internal.css.Bug36024Test;
import com.openexchange.html.internal.css.Bug43387Test;
import com.openexchange.html.internal.css.CSSMatcherTest;
import com.openexchange.html.internal.jericho.handler.FilterJerichoHandlerTest;
import com.openexchange.html.vulntests.Bug17991VulTest;
import com.openexchange.html.vulntests.Bug18911VulTest;
import com.openexchange.html.vulntests.Bug22284VulTest;
import com.openexchange.html.vulntests.Bug22286VulTest;
import com.openexchange.html.vulntests.Bug25321VulTest;
import com.openexchange.html.vulntests.Bug26090VulTest;
import com.openexchange.html.vulntests.Bug26237VulTest;
import com.openexchange.html.vulntests.Bug26611VulTest;
import com.openexchange.html.vulntests.Bug28642VulTest;
import com.openexchange.html.vulntests.Bug29412VulTest;
import com.openexchange.html.vulntests.Bug30357VulTest;
import com.openexchange.html.vulntests.Bug35982VulTest;
import com.openexchange.html.vulntests.Bug46894VulTest;
import com.openexchange.html.vulntests.Bug47781VulTest;
import com.openexchange.html.vulntests.Bug48083VulTest;
import com.openexchange.html.vulntests.Bug48230VulTest;
import com.openexchange.html.vulntests.Bug48843VulTest;

/**
 * Test suite for all integrated unit tests of the HTMLService implementation.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@RunWith(Suite.class)
@SuiteClasses({
    Bug16800Test.class,
    Bug16843Test.class,
    Bug17195Test.class,
    Bug17991VulTest.class,
    Bug18911VulTest.class,
    Bug19428Test.class,
    Bug19466Test.class,
    Bug19522Test.class,
    Bug20968Test.class,
    Bug21014Test.class,
    Bug21042Test.class,
    Bug21055Test.class,
    Bug21118Test.class,
    Bug21532Test.class,
    Bug21584Test.class,
    Bug21668Test.class,
    Bug21757Test.class,
    Bug22072Test.class,
    Bug22284VulTest.class,
    Bug22286VulTest.class,
    Bug22304Test.class,
    Bug23368Test.class,
    Bug24899Test.class,
    Bug25321VulTest.class,
    Bug25923Test.class,
    Bug26090VulTest.class,
    Bug26237VulTest.class,
    Bug26316Test.class,
    Bug26153Test.class,
    Bug26611VulTest.class,
    Bug26789Test.class,
    Bug27708Test.class,
    Bug27335Test.class,
    Bug28094Test.class,
    Bug28337Test.class,
    Bug28637Test.class,
    Bug28642VulTest.class,
    Bug29229Test.class,
    Bug29412VulTest.class,
    Bug29695Test.class,
    Bug29892Test.class,
    Bug30114Test.class,
    Bug30357VulTest.class,
    Bug31826Test.class,
    Bug35291Test.class,
    Bug35546Test.class,
    Bug35982VulTest.class,
    Bug36024Test.class,
    Bug36275Test.class,
    Bug36412Test.class,
    Bug40189Test.class,
    Bug43387Test.class,
    Bug46894VulTest.class,
    Bug47781VulTest.class,
    Bug48083VulTest.class,
    Bug48230VulTest.class,
    Bug48843VulTest.class,
    CSSMatcherTest.class,
    ConformHtmlTest.class,
    HtmlServiceImplTest.class,
    FilterJerichoHandlerTest.class,
    com.openexchange.html.internal.SaneScriptTagsTest.class
})
public class UnitTests {

    private UnitTests() {
        super();
    }
}
