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

package com.openexchange.groupware.settings;


/**
 * Helper class that extends {@link ReadOnlyValue} by a ranking.
 * <p>
 * Useful to replace existing settings in case a higher ranking is specified through overwriting {@link #getRanking()} method.<br>
 * Example:
 *
 * <pre>
 * public class OverwritingPreferenceItemService implements PreferencesItemService {
 *
 *     &#064;Override
 *     public String[] getPath() {
 *         return new String[] { &quot;repacing&quot;, &quot;path&quot; };
 *     }
 *
 *     &#064;Override
 *     public IValueHandler getSharedValue() {
 *         return new RankedReadOnlyValue() {
 *
 *             &#064;Override
 *             public int getRanking() {
 *                 // A higher ranking than zero
 *                 return 10;
 *             }
 *
 *             &#064;Override
 *             public boolean isAvailable(UserConfiguration userConfig) {
 *                 return true;
 *             }
 *
 *             &#064;Override
 *             public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
 *                 // Overwrite value here...
 *             }
 *         };
 *     }
 * }
 * </pre>
 */
public abstract class RankedReadOnlyValue extends ReadOnlyValue implements Ranked {

    /**
     * Initializes a new {@link RankedReadOnlyValue}.
     */
    protected RankedReadOnlyValue() {
        super();
    }

    @Override
    public int getRanking() {
        return 0;
    }

}
