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
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.i18n.tools.TemplateToken;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * Unit tests for {@link FormatLocalizedStringReplacement}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(I18nServiceRegistry.class)
public class FormatLocalizedStringReplacementTest {

    private FormatLocalizedStringReplacement formatLocalizedStringReplacement;

    private final String format = "Priority: %1$s";

    private final String formatTranslated = "Priorit\\u00e4t: %1$s";

    private final String result = "Priorit\\u00e4t: Niedrig";

    private final String replacement = "Low";

    private final Locale locale = new Locale("de_DE");

    private final TemplateToken templateToken = TemplateToken.TASK_PRIORITY;

    @Mock
    private I18nService i18nService;

    /**
     */
    @Before
    public void setUp() {
        formatLocalizedStringReplacement = new FormatLocalizedStringReplacement(templateToken, format, replacement);
        formatLocalizedStringReplacement.setChanged(false);
        formatLocalizedStringReplacement.setLocale(locale);

        I18nServiceRegistry mock = PowerMockito.mock(I18nServiceRegistry.class);
        PowerMockito.when(mock.getI18nService(locale)).thenReturn(i18nService);
        ServerServiceRegistry.getInstance().addService(I18nServiceRegistry.class, mock);

        PowerMockito.mockStatic(I18nServiceRegistry.class);

        PowerMockito.when(i18nService.getLocalized(format)).thenReturn(formatTranslated);
        PowerMockito.when(i18nService.getLocalized(replacement)).thenReturn("Niedrig");
        // TODO check with new implementation
    }

    @Test
    public void testGetReplacement_replacementObjectStateFine_returnTranslatedPrioritySentence() {
        String replacedString = formatLocalizedStringReplacement.getReplacement();
        Assert.assertEquals("String not localized", result, replacedString);
    }

}
