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

package com.openexchange.html.internal;


/**
 * {@link OneCharSequence}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class OneCharSequence implements CharSequence {

    private char ch;

    /**
     * Initializes a new {@link OneCharSequence}.
     */
    public OneCharSequence(char ch) {
        super();
        this.ch = ch;
    }

    /**
     * Sets the character
     *
     * @param ch The character
     */
    public void setCharacter(char ch) {
        this.ch = ch;
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public char charAt(int index) {
        return ch;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return String.valueOf(ch).subSequence(start, end);
    }

    @Override
    public String toString() {
        return String.valueOf(ch);
    }
}
