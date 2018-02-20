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

package com.openexchange.html.internal.jericho.handler;

import static org.junit.Assert.assertTrue;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.internal.jericho.handler.FilterJerichoHandler.CellPadding;
import com.openexchange.test.mock.MockUtils;
import net.htmlparser.jericho.StartTag;

/**
 * {@link FilterJerichoHandlerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ HtmlServiceImpl.class, StartTag.class, FilterJerichoHandler.class })
public class FilterJerichoHandlerTest {

    @Mock
    private HtmlServiceImpl htmlServiceImpl;

    private FilterJerichoHandler filterJerichoHandler;
    
    private FilterJerichoHandler spyOnHandler;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        filterJerichoHandler = new FilterJerichoHandler(100000, htmlServiceImpl);
        spyOnHandler = PowerMockito.spy(filterJerichoHandler);
    }

     @Test
     public void testSetMaxContentSize_infiniteWithMinusOne_setInfinite() {
        filterJerichoHandler.setMaxContentSize(-1);

        int valueFromField = (Integer)MockUtils.getValueFromField(filterJerichoHandler, "maxContentSize");
        Assert.assertEquals(-1, valueFromField);
    }

     @Test
     public void testSetMaxContentSize_infiniteWithMinusZero_setInfinite() {
        filterJerichoHandler.setMaxContentSize(0);

        int valueFromField = (Integer) MockUtils.getValueFromField(filterJerichoHandler, "maxContentSize");
        Assert.assertEquals(0, valueFromField);
    }

     @Test
     public void testSetMaxContentSize_lessThanMinimum_setMinimum10000() {
        filterJerichoHandler.setMaxContentSize(555);

        int valueFromField = (Integer) MockUtils.getValueFromField(filterJerichoHandler, "maxContentSize");
        Assert.assertEquals(10000, valueFromField);
    }

     @Test
     public void testSetMaxContentSize_validMaxSize_setMaxSize() {
        filterJerichoHandler.setMaxContentSize(22222);

        int valueFromField = (Integer) MockUtils.getValueFromField(filterJerichoHandler, "maxContentSize");
        Assert.assertEquals(22222, valueFromField);
    }

     @Test
     public void testHandleTableCellpaddingAttribute_mapNull_return() {
        filterJerichoHandler.handleTableCellpaddingAttribute(null);

        LinkedList<CellPadding> tablePaddings = (LinkedList<CellPadding>) MockUtils.getValueFromField(filterJerichoHandler, "tablePaddings");
        Assert.assertEquals(0, tablePaddings.size());
    }

     @Test
     public void testHandleTableCellpaddingAttribute_emptyMap_addEmptyCellpadding() {
        Map<String, String> map = new LinkedHashMap<String, String>();

        filterJerichoHandler.handleTableCellpaddingAttribute(map);

        LinkedList<CellPadding> tablePaddings = (LinkedList<CellPadding>) MockUtils.getValueFromField(filterJerichoHandler, "tablePaddings");
        Assert.assertEquals(1, tablePaddings.size());
        Assert.assertEquals(null, tablePaddings.getFirst().cellPadding);
        Assert.assertEquals(0, map.size());
    }

     @Test
     public void testHandleTableCellpaddingAttribute_cellpaddingInAttributes_addCellpadding() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("cellpadding", "10");

        filterJerichoHandler.handleTableCellpaddingAttribute(map);

        LinkedList<CellPadding> tablePaddings = (LinkedList<CellPadding>) MockUtils.getValueFromField(filterJerichoHandler, "tablePaddings");
        Assert.assertEquals(1, tablePaddings.size());
        Assert.assertEquals("10", tablePaddings.getFirst().cellPadding);
    }

     @Test
     public void testHandleTableCellpaddingAttribute_cellpaddingZero_addCellpadding() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("cellpadding", "0");

        filterJerichoHandler.handleTableCellpaddingAttribute(map);

        LinkedList<CellPadding> tablePaddings = (LinkedList<CellPadding>) MockUtils.getValueFromField(filterJerichoHandler, "tablePaddings");
        Assert.assertEquals(1, tablePaddings.size());
        Assert.assertEquals("0", tablePaddings.getFirst().cellPadding);
    }

     @Test
     public void testHandleTableCellpaddingAttribute_cellsapcingZero_addCellpaddingAndBorderCollapse() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("cellpadding", "0");
        map.put("cellspacing", "0");

        filterJerichoHandler.handleTableCellpaddingAttribute(map);

        LinkedList<CellPadding> tablePaddings = (LinkedList<CellPadding>) MockUtils.getValueFromField(filterJerichoHandler, "tablePaddings");
        Assert.assertEquals(1, tablePaddings.size());
        Assert.assertEquals("0", tablePaddings.getFirst().cellPadding);
        Assert.assertEquals(3, map.size());
    }

     @Test
     public void testHandleTableCellpaddingAttribute_cellpaddingInStyle_addCellpadding() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("style", "border-collapse:collapse;margin:2px;padding:20;width:546px;background-color:#dbefff;");

        filterJerichoHandler.handleTableCellpaddingAttribute(map);

        LinkedList<CellPadding> tablePaddings = (LinkedList<CellPadding>) MockUtils.getValueFromField(filterJerichoHandler, "tablePaddings");
        Assert.assertEquals(1, tablePaddings.size());
        Assert.assertEquals("20", tablePaddings.getFirst().cellPadding);
        Assert.assertEquals(1, map.size());
    }

     @Test
     public void testHandleTableCellpaddingAttribute_styleAvailableButNoCellpadding_doNotAdd() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("style", "border-collapse:collapse;margin:2px;width:546px;background-color:#dbefff;");

        filterJerichoHandler.handleTableCellpaddingAttribute(map);

        LinkedList<CellPadding> tablePaddings = (LinkedList<CellPadding>) MockUtils.getValueFromField(filterJerichoHandler, "tablePaddings");
        Assert.assertEquals(1, tablePaddings.size());
        Assert.assertEquals(null, tablePaddings.getFirst().cellPadding);
        Assert.assertEquals(1, map.size());
    }
     
    @Test
    public void testMarkCssStart_DepthCalculatedCorrectly() throws Exception {
        PowerMockito.doNothing().when(spyOnHandler, PowerMockito.method(FilterJerichoHandler.class, "addStartTag", StartTag.class, boolean.class, Map.class)).withArguments(Matchers.any(StartTag.class), Matchers.anyBoolean(), Matchers.anyMap());
        Whitebox.setInternalState(spyOnHandler, "depth", 1);
        StartTag startTag = PowerMockito.mock(StartTag.class);
        spyOnHandler.markCssStart(startTag);
        int depth = (int) Whitebox.getInternalState(spyOnHandler, "depth");
        assertTrue("depth attribute is not 2", depth == 2);
    }
    
    @Test
    public void testMarkCssStart_SkiplevelBiggerZeroToReturn() throws Exception {
        Whitebox.setInternalState(spyOnHandler, "skipLevel", 1);
        StartTag startTag = PowerMockito.mock(StartTag.class);
        spyOnHandler.markCssStart(startTag);
        int skipLevel = (int) Whitebox.getInternalState(spyOnHandler, "skipLevel");
        assertTrue("skipLevel attribute is not 2", skipLevel == 2);
        PowerMockito.verifyPrivate(spyOnHandler, Mockito.times(0)).invoke("addStartTag", Matchers.any(StartTag.class), Matchers.anyBoolean(), Matchers.anyMap());
    }
    
    @Test
    public void testMarkCssStart_StartTagAddedAndCssSet() throws Exception {
        PowerMockito.doNothing().when(spyOnHandler, PowerMockito.method(FilterJerichoHandler.class, "addStartTag", StartTag.class, boolean.class, Map.class)).withArguments(Matchers.any(StartTag.class), Matchers.anyBoolean(), Matchers.anyMap());
        StartTag startTag = PowerMockito.mock(StartTag.class);
        Mockito.when(startTag.getName()).thenReturn("style");
        spyOnHandler.markCssStart(startTag);
        boolean isCss = (boolean) Whitebox.getInternalState(spyOnHandler, "isCss");
        assertTrue("CSS flag not set", isCss);
        PowerMockito.verifyPrivate(spyOnHandler, Mockito.times(1)).invoke("addStartTag", Matchers.any(StartTag.class), Matchers.anyBoolean(), Matchers.anyMap());
    }
}
