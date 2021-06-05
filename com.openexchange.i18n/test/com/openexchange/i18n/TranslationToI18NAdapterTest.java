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

package com.openexchange.i18n;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.i18n.impl.TranslationsI18N;
import com.openexchange.i18n.parsing.Translations;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class TranslationToI18NAdapterTest {

    private I18nService i18n;

    public TranslationToI18NAdapterTest() {
        super();
    }

    @Before
    public void setUp() {
        final Translations tr = new Translations();
        tr.setTranslation("Key", "Schluessel");
        i18n = new TranslationsI18N(tr);
    }

    @Test
    public void testShouldPassAlongTranslation() {
        assertEquals(i18n.getLocalized("Key"), "Schluessel");
    }

    @Test
    public void testShoulPassKeyOnMissingTranslation() {
        assertEquals(i18n.getLocalized("Nonexisting Key"), "Nonexisting Key");
    }
}
