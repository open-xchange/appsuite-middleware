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
