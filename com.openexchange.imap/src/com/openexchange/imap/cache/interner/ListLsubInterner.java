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

package com.openexchange.imap.cache.interner;

import com.google.common.collect.Interner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.imap.config.IMAPReloadable;
import com.openexchange.imap.services.Services;

/**
 * {@link ListLsubInterner} - The interner for LIST/LSUB cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.6
 */
public class ListLsubInterner {

    static {
        IMAPReloadable.getInstance().addReloadable(new Reloadable() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                instance = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.imap.cache.internListLsubStrings");
            }
        });
    }

    private static volatile ListLsubInterner instance;

    /**
     * Gets the interner for LIS/LSUB cache.
     *
     * @return The interner instance
     */
    public static ListLsubInterner getInstance() {
        ListLsubInterner listLsubInterner = instance;
        if (listLsubInterner == null) {
            synchronized (ListLsubInterner.class) {
                listLsubInterner = instance;
                if (listLsubInterner == null) {
                    boolean defaultUseInterner = true;
                    ConfigurationService configService = Services.optService(ConfigurationService.class);
                    if (configService == null) {
                        return new ListLsubInterner(defaultUseInterner);
                    }
                    boolean useInterner = configService.getBoolProperty("com.openexchange.imap.cache.internListLsubStrings", defaultUseInterner);
                    listLsubInterner = new ListLsubInterner(useInterner);
                    instance = listLsubInterner;
                }
            }
        }
        return listLsubInterner;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Interner<String> fullNameInterner;
    private final Interner<String> attributeInterner;

    /**
     * Initializes a new {@link ListLsubInterner}.
     *
     * @param useInterner Whether to use interning of strings or not
     */
    private ListLsubInterner(boolean useInterner) {
        super();
        if (useInterner) {
            fullNameInterner = javax.mail.util.Interners.getFullNameInterner();
            attributeInterner = javax.mail.util.Interners.getAttributeInterner();
        } else {
            Interner<String> noopInterner = new Interner<String>() {

                @Override
                public String intern(String sample) {
                    return sample; // Return argument as-is
                }
            };
            fullNameInterner = noopInterner;
            attributeInterner = noopInterner;
        }
    }

    /**
     * Gets the interner for full names.
     *
     * @return The full name interner
     */
    public Interner<String> getFullNameInterner() {
        return fullNameInterner;
    }

    /**
     * Gets the interner for attributes.
     *
     * @return The interner
     */
    public Interner<String> getAttributeInterner() {
        return attributeInterner;
    }

}
