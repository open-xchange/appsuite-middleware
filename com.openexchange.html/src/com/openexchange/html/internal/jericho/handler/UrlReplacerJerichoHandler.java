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

package com.openexchange.html.internal.jericho.handler;

import static com.openexchange.html.internal.HtmlServiceImpl.PATTERN_URL_SOLE;
import java.net.MalformedURLException;
import java.util.List;
import java.util.regex.Matcher;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.internal.jericho.JerichoHandler;
import com.openexchange.html.internal.parser.handler.HTMLURLReplacerHandler;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

/**
 * {@link UrlReplacerJerichoHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UrlReplacerJerichoHandler implements JerichoHandler {

    private final StringBuilder htmlBuilder;
    private final StringBuilder attrBuilder;
    private final StringBuilder urlBuilder;

    /**
     * Initializes a new {@link UrlReplacerJerichoHandler}.
     */
    public UrlReplacerJerichoHandler(int capacity) {
        super();
        this.urlBuilder = new StringBuilder(256);
        this.htmlBuilder = new StringBuilder(capacity);
        this.attrBuilder = new StringBuilder(128);
    }

    @Deprecated
    public boolean isChanged() {
        return false;
    }

    /**
     * Gets the filtered HTML content.
     *
     * @return The filtered HTML content
     */
    public String getHTML() {
        return htmlBuilder.toString();
    }

    @Override
    public void markBodyAbsent() {
        // Ignore
    }

    @Override
    public void handleUnknownTag(final Tag tag) {
        htmlBuilder.append(tag);
    }

    @Override
    public void handleCharacterReference(final CharacterReference characterReference) {
        htmlBuilder.append(CharacterReference.getDecimalCharacterReferenceString(characterReference.getChar()));
    }

    @Override
    public void handleSegment(final CharSequence content) {
        htmlBuilder.append(content);
    }

    @Override
    public void markCssEnd(EndTag endTag) {
        handleEndTag(endTag);
    }

    @Override
    public void handleEndTag(final EndTag endTag) {
        htmlBuilder.append(endTag);
    }

    @Override
    public void markCssStart(StartTag startTag) {
        handleStartTag(startTag);
    }

    @Override
    public void handleStartTag(final StartTag startTag) {
        addStartTag(startTag);
    }

    /**
     * Adds tag occurring in white list to HTML result.
     *
     * @param startTag The start tag to add
     */
    private void addStartTag(final StartTag startTag) {
        attrBuilder.setLength(0);

        List<Attribute> uriAttributes = startTag.getURIAttributes();
        for (Attribute attribute : startTag.getAttributes()) {
            String attr = attribute.getKey();
            String val = attribute.getValue();
            if (uriAttributes.contains(attribute)) {
                attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(checkPossibleURL(val))).append('"');
            } else {
                attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(val)).append('"');
            }
        }

        htmlBuilder.append('<').append(startTag.getName()).append(attrBuilder.toString());
        if (startTag.isSyntacticalEmptyElementTag()) {
            htmlBuilder.append('/');
        }
        htmlBuilder.append('>');
    }

    private String checkPossibleURL(final String val) {
        final Matcher m = PATTERN_URL_SOLE.matcher(val);
        if (!m.matches()) {
            return val;
        }
        urlBuilder.setLength(0);
        urlBuilder.append(val.substring(0, m.start()));
        //replaceURL(urlDecode(m.group()), urlBuilder);
        replaceURL(m.group(), urlBuilder);
        urlBuilder.append(val.substring(m.end()));
        return urlBuilder.toString();
    }

    private static void replaceURL(final String url, final StringBuilder builder) {
        /*
         * Contains any non-ascii character in host part?
         */
        final int restoreLen = builder.length();
        try {
            builder.append(HtmlServiceImpl.checkURL(url));
        } catch (MalformedURLException e) {
            /*
             * Not a valid URL
             */
            builder.setLength(restoreLen);
            builder.append(url);
        } catch (Exception e) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HTMLURLReplacerHandler.class);
            log.warn("URL replacement failed.", e);
            builder.setLength(restoreLen);
            builder.append(url);
        }
    }

    @Override
    public void handleDocDeclaration(final String docDecl) {
        htmlBuilder.append(docDecl);
    }

    @Override
    public void handleCData(final String cdata) {
        htmlBuilder.append(cdata);
    }

    @Override
    public void handleComment(final String comment) {
        htmlBuilder.append(comment);
    }

}
