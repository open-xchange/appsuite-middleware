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

package com.openexchange.html.internal.jericho.handler;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertTrue;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.internal.jericho.handler.FilterJerichoHandler.CellPadding;
import com.openexchange.test.mock.MockUtils;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.StartTag;

/**
 * {@link FilterJerichoHandlerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ HtmlServiceImpl.class, StartTag.class, FilterJerichoHandler.class, Attributes.class })
public class FilterJerichoHandlerTest {

    @Mock
    private HtmlServiceImpl htmlServiceImpl;

    private FilterJerichoHandler filterJerichoHandler;

    private FilterJerichoHandler spyOnHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        filterJerichoHandler = new FilterJerichoHandler(100000, htmlServiceImpl);
        spyOnHandler = PowerMockito.spy(filterJerichoHandler);
    }

    @Test
    public void testSetMaxContentSize_infiniteWithMinusOne_setInfinite() {
        filterJerichoHandler.setMaxContentSize(-1);

        int valueFromField = ((Integer) MockUtils.getValueFromField(filterJerichoHandler, "maxContentSize")).intValue();
        Assert.assertEquals(-1, valueFromField);
    }

    @Test
    public void testSetMaxContentSize_infiniteWithMinusZero_setInfinite() {
        filterJerichoHandler.setMaxContentSize(0);

        int valueFromField = ((Integer) MockUtils.getValueFromField(filterJerichoHandler, "maxContentSize")).intValue();
        Assert.assertEquals(0, valueFromField);
    }

    @Test
    public void testSetMaxContentSize_lessThanMinimum_setMinimum10000() {
        filterJerichoHandler.setMaxContentSize(555);

        int valueFromField = ((Integer) MockUtils.getValueFromField(filterJerichoHandler, "maxContentSize")).intValue();
        Assert.assertEquals(10000, valueFromField);
    }

    @Test
    public void testSetMaxContentSize_validMaxSize_setMaxSize() {
        filterJerichoHandler.setMaxContentSize(22222);

        int valueFromField = ((Integer) MockUtils.getValueFromField(filterJerichoHandler, "maxContentSize")).intValue();
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
        PowerMockito.doNothing().when(spyOnHandler, PowerMockito.method(FilterJerichoHandler.class, "addStartTag", StartTag.class, boolean.class, Map.class)).withArguments(ArgumentMatchers.any(StartTag.class), B(ArgumentMatchers.anyBoolean()), ArgumentMatchers.anyMap());
        PowerMockito.doNothing().when(spyOnHandler, PowerMockito.method(FilterJerichoHandler.class, "addStartTag", StartTag.class, boolean.class, Map.class)).withArguments(ArgumentMatchers.any(StartTag.class), B(ArgumentMatchers.anyBoolean()), ArgumentMatchers.isNull());
        Whitebox.setInternalState(spyOnHandler, "depth", I(1));
        StartTag startTag = PowerMockito.mock(StartTag.class);
        spyOnHandler.markCssStart(startTag);
        int depth = ((Integer) Whitebox.getInternalState(spyOnHandler, "depth")).intValue();
        assertTrue("depth attribute is not 2", depth == 2);
    }

    @Test
    public void testMarkCssStart_SkiplevelBiggerZeroToReturn() throws Exception {
        Whitebox.setInternalState(spyOnHandler, "skipLevel", I(1));
        StartTag startTag = PowerMockito.mock(StartTag.class);
        spyOnHandler.markCssStart(startTag);
        int skipLevel = ((Integer) Whitebox.getInternalState(spyOnHandler, "skipLevel")).intValue();
        assertTrue("skipLevel attribute is not 2", skipLevel == 2);
        PowerMockito.verifyPrivate(spyOnHandler, Mockito.times(0)).invoke("addStartTag", ArgumentMatchers.any(StartTag.class), B(ArgumentMatchers.anyBoolean()), ArgumentMatchers.anyMap());
    }

    @Test
    public void testMarkCssStart_StartTagAddedAndCssSet() throws Exception {
        PowerMockito.doNothing().when(spyOnHandler, PowerMockito.method(FilterJerichoHandler.class, "addStartTag", StartTag.class, boolean.class, Map.class)).withArguments(ArgumentMatchers.any(StartTag.class), B(ArgumentMatchers.anyBoolean()), ArgumentMatchers.anyMap());
        StartTag startTag = PowerMockito.mock(StartTag.class);
        Mockito.when(startTag.getName()).thenReturn("style");
        spyOnHandler.markCssStart(startTag);
        boolean isCss = ((Boolean) Whitebox.getInternalState(spyOnHandler, "isCss")).booleanValue();
        assertTrue("CSS flag not set", isCss);
        PowerMockito.verifyPrivate(spyOnHandler, Mockito.times(1)).invoke("addStartTag", ArgumentMatchers.any(StartTag.class), B(ArgumentMatchers.anyBoolean()), ArgumentMatchers.anyMap());
    }
}
