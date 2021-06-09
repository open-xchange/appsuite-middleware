/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.html;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.html.bugtests.Bug16800Test;
import com.openexchange.html.bugtests.Bug16843Test;
import com.openexchange.html.bugtests.Bug17195Test;
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
import com.openexchange.html.bugtests.MWB990Test;
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
import com.openexchange.html.vulntests.Bug49005VulTest;
import com.openexchange.html.vulntests.Bug49014VulTest;
import com.openexchange.html.vulntests.Bug50382VulTest;
import com.openexchange.html.vulntests.Bug50734VulTest;
import com.openexchange.html.vulntests.Bug50943VulTest;
import com.openexchange.html.vulntests.Bug51474VulTest;
import com.openexchange.html.vulntests.Bug52040VulTest;
import com.openexchange.html.vulntests.Bug55603VulTest;
import com.openexchange.html.vulntests.Bug55830VulTest;
import com.openexchange.html.vulntests.Bug55882VulTest;
import com.openexchange.html.vulntests.Bug56582VulTest;
import com.openexchange.html.vulntests.Bug57095VulTest;

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
    Bug49005VulTest.class,
    Bug49014VulTest.class,
    Bug50382VulTest.class,
    Bug50734VulTest.class,
    Bug50943VulTest.class,
    Bug51474VulTest.class,
    Bug52040VulTest.class,
    Bug55603VulTest.class,
    Bug55830VulTest.class,
    Bug55882VulTest.class,
    Bug55406Test.class,
    Bug56400Test.class,
    Bug56420Test.class,
    Bug56582VulTest.class,
    Bug57095VulTest.class,
    MWB990Test.class,
    com.openexchange.html.vulntests.MWB1113VulTest.class,
    com.openexchange.html.vulntests.MWB1116VulTest.class,
    com.openexchange.html.vulntests.ImgVulTest.class,
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
