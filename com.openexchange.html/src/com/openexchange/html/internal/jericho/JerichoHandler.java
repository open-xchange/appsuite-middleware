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

package com.openexchange.html.internal.jericho;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;


/**
 * {@link JerichoHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface JerichoHandler {

    /**
     * Handles the DOCTYPE declaration. Specified value is without leading "&lt;!DOCTYPE" and without trailing "&gt;"; e.g.
     *
     * <pre>
     * '&lt;!DOCTYPE html PUBLIC &quot;-//W3C//DTD XHTML 1.0 Transitional//EN&quot;
     *  &quot;http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd&quot;&gt;'
     * </pre>
     *
     * yields
     *
     * <pre>
     *  ' html PUBLIC &quot;-//W3C//DTD XHTML 1.0 Transitional//EN&quot;
     *  &quot;http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd&quot;'
     * </pre>
     *
     * @param docDecl
     */
    void handleDocDeclaration(String docDecl);

    void handleCharacterReference(CharacterReference characterReference);

    void handleSegment(CharSequence content);

    void handleStartTag(StartTag startTag);

    void handleEndTag(EndTag endTag);

    void handleCData(String cdata);

    void handleComment(String comment);

    void handleUnknownTag(Tag tag);

    void markCssStart(StartTag startTag);

    void markCssEnd(EndTag endTag);

    void markBodyAbsent();

}
