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

package com.openexchange.i18n.tools.replacement;

import java.util.Locale;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.tools.TemplateToken;
import com.openexchange.server.services.I18nServices;


/**
 * Unit tests for {@link FormatLocalizedStringReplacement}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ I18nServices.class })
public class FormatLocalizedStringReplacementTest {

    private FormatLocalizedStringReplacement formatLocalizedStringReplacement;

    private String format = "Priority: %1$s";

    private String formatTranslated = "Priorit\\u00e4t: %1$s";

    private String result = "Priorit\\u00e4t: Niedrig";

    private String replacement = "Low";

    private Locale locale = new Locale("de_DE");

    private TemplateToken templateToken = TemplateToken.TASK_PRIORITY;

    @Mock
    private I18nServices i18nServices;

    @Mock
    private I18nService i18nService;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        formatLocalizedStringReplacement = new FormatLocalizedStringReplacement(templateToken, format, replacement);
        formatLocalizedStringReplacement.setChanged(false);
        formatLocalizedStringReplacement.setLocale(locale);

        PowerMockito.mockStatic(I18nServices.class);

        PowerMockito.when(i18nServices.getService(locale)).thenReturn(i18nService);
        PowerMockito.when(i18nService.getLocalized(format)).thenReturn(formatTranslated);
        PowerMockito.when(i18nService.getLocalized(replacement)).thenReturn("Niedrig");

        PowerMockito.when(I18nServices.getInstance()).thenReturn(i18nServices);
    }

    @Test
    public void testGetReplacement_replacementObjectStateFine_returnTranslatedPrioritySentence() {
        String replacedString = formatLocalizedStringReplacement.getReplacement();
        Assert.assertEquals("String not localized", result, replacedString);
    }

}
