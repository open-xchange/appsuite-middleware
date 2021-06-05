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

package com.openexchange.html.internal.emoji;

/**
 * {@link EmojiRegistry} - The Emoji registry based on <a href="http://unicode.org/emoji/charts/full-emoji-list.html">full emoji list</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class EmojiRegistry {

    private static final EmojiRegistry INSTANCE = new EmojiRegistry();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static EmojiRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link EmojiRegistry}.
     */
    private EmojiRegistry() {
        super();
    }

    /**
     * Checks if specified character is known to be an Emoji character.
     *
     * @param c The character to check
     * @return <code>true</code> if character is an Emoji character; otherwise <code>false</code>
     */
    public boolean isEmoji(char c) {
        return com.openexchange.emoji.EmojiRegistry.getInstance().isEmoji(c);
    }

    /**
     * Checks if specified code point is known to be an Emoji character.
     *
     * @param codePoint The code point to check
     * @return <code>true</code> if codePoint is an Emoji character; otherwise <code>false</code>
     */
    public boolean isEmoji(int codePoint) {
        return com.openexchange.emoji.EmojiRegistry.getInstance().isEmoji(codePoint);
    }

}
