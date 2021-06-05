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

package com.openexchange.find.facet;

/**
 * A display item that is localizable and shall be string formatted by clients.
 * The result string then consists of two parts: An argument (most likely user
 * input) and a suffix that is appended to this argument.
 * <br>
 * Example:<br>
 * new FormattableDisplayItem("in E-Mails", "term") can be constructed to a result
 * string of "term in E-Mails" by clients.
 *
 * {@link FormattableDisplayItem#getSuffix()} returns the suffix.
 * {@link FormattableDisplayItem#getItem} returns the argument.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class FormattableDisplayItem implements DisplayItem {

    private static final long serialVersionUID = -5707165345744171006L;

    private final String suffix;
    private final String arg;

    /**
     * Initializes a new {@link FormattableDisplayItem}.
     * 
     * @param suffix The suffix that is appended to the argument. Never <code>null</code>.
     * @param args The format argument. Never <code>null</code>.
     */
    public FormattableDisplayItem(final String suffix, final String arg) {
        super();
        this.suffix = suffix;
        this.arg = arg;
    }

    @Override
    public String getDisplayName() {
        return arg + ' ' + suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getArgument() {
        return arg;
    }

    @Override
    public void accept(DisplayItemVisitor visitor) {
        visitor.visit(this);
    }

}
