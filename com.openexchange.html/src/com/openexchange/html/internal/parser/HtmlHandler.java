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

package com.openexchange.html.internal.parser;

import java.util.Map;

/**
 * {@link HtmlHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface HtmlHandler {

    /**
     * Handles the <i>&lt;?xml... ?&gt;</i> declaration.
     *
     * @param version The version; either "1.0" or <code>null</code>
     * @param standalone The standalone boolean value; either {@link Boolean#TRUE}, {@link Boolean#FALSE}, or <code>null</code>
     * @param encoding The encoding; the charset name or <code>null</code>
     */
    public void handleXMLDeclaration(String version, Boolean standalone, String encoding);

    /**
     * Handles the DOCTYPE declaration. Specified value is without leading "&lt;!DOCTYPE" and without trailing "&gt;"; e.g.
     *
     * <pre>
     * '&lt;!DOCTYPE html PUBLIC &quot;-//W3C//DTD XHTML 1.0 Transitional//EN&quot;
     * 	&quot;http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd&quot;&gt;'
     * </pre>
     *
     * yields
     *
     * <pre>
     *  ' html PUBLIC &quot;-//W3C//DTD XHTML 1.0 Transitional//EN&quot;
     * 	&quot;http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd&quot;'
     * </pre>
     *
     * @param docDecl
     */
    public void handleDocDeclaration(String docDecl);

    /**
     * Handles specified CDATA segment's text; e.g. '<i>fo&lt;o</i>' from '<i>&lt;![CDATA[fo&lt;o]]&gt;</i>'.
     *
     * @param text The CDATA segment's text
     */
    public void handleCDATA(String text);

    /**
     * Handles specified text.
     * <p>
     * <b>Note</b>: Specified text contains all control characters from corresponding HTML content; e.g.:
     *
     * <pre>
     * Sorry if my article tried to imply that this is a
     * 	   new thing (I hope it hasn't).
     * </pre>
     *
     * will be given as:
     *
     * <pre>
     * Sorry if my article tried to imply that this is a
     * 	   new thing (I hope it hasn't).
     * </pre>
     * <p>
     * <b>Note</b>: A text only containing whitespace characters is omitted.
     *
     * @param text The text
     * @param ignorable <code>true</code> if specified text may be ignored since it probably serves for formatting; otherwise <code>false</code>
     */
    public void handleText(String text, boolean ignorable);

    /**
     * Handles specified comment. Specified value is without leading "&lt;!--" and without trailing "--&gt;".
     *
     * @param comment The comment
     */
    public void handleComment(String comment);

    /**
     * Handles specified start tag.
     *
     * @param tag The tag's name
     * @param attributes The tag's attributes as an unmodifiable map
     */
    public void handleStartTag(String tag, Map<String, String> attributes);

    /**
     * Handles specified end tag.
     *
     * @param tag The tag's name
     */
    public void handleEndTag(String tag);

    /**
     * Handles specified simple tag.
     *
     * @param tag The tag's name
     * @param attributes The tag's attributes as an unmodifiable map
     */
    public void handleSimpleTag(String tag, Map<String, String> attributes);

    /**
     * Handles specified error.
     *
     * @param errorMsg The error message
     * @throws IllegalStateException If handler decides that occurred error is worth being thrown
     */
    public void handleError(String errorMsg);
}
