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


package com.openexchange.mail.text;

import java.awt.Color;
import java.io.IOException;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.rtf.RTFEditorKit;
import com.openexchange.java.Charsets;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link Rtf2HtmlConverter} - Converts RTF to HTML based on <code>javax.swing.text.*</code> package.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Rtf2HtmlConverter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Rtf2HtmlConverter.class);

    private static final class HTMLStateMachine {

        private static final String alignNames[] = { "left", "center", "right" };

        private final boolean acceptFonts;

        private String fontName;

        private Color color;

        private int size;

        private int alignment;

        private boolean bold;

        private boolean italic;

        private boolean underline;

        private double firstLineIndent;

        private double oldLeftIndent;

        private double oldRightIndent;

        private double leftIndent;

        private double rightIndent;

        private boolean firstLine;

        /**
         * Initializes a new {@link HTMLStateMachine}
         */
        HTMLStateMachine() {
            acceptFonts = true;
            fontName = "";
            alignment = -1;
            bold = false;
            italic = false;
            underline = false;
            color = null;
            size = -1;
            firstLineIndent = 0.0D;
            oldLeftIndent = 0.0D;
            oldRightIndent = 0.0D;
            leftIndent = 0.0D;
            rightIndent = 0.0D;
            firstLine = false;
        }

        public void updateState(AttributeSet attributeset, StringBuilder sb, Element element) {
            final String s = element.getName();
            if (s.equalsIgnoreCase("paragraph")) {
                firstLine = true;
            }
            leftIndent = updateDouble(attributeset, leftIndent, StyleConstants.LeftIndent);
            rightIndent = updateDouble(attributeset, rightIndent, StyleConstants.RightIndent);
            if (leftIndent != oldLeftIndent || rightIndent != oldRightIndent) {
                closeIndentTable(sb, oldLeftIndent, oldRightIndent);
            }
            bold = updateBoolean(attributeset, StyleConstants.Bold, "b", bold, sb);
            italic = updateBoolean(attributeset, StyleConstants.Italic, "i", italic, sb);
            underline = updateBoolean(attributeset, StyleConstants.Underline, "u", underline, sb);
            size = updateFontSize(attributeset, size, sb);
            color = updateFontColor(attributeset, color, sb);
            if (acceptFonts) {
                fontName = updateFontName(attributeset, fontName, sb);
            }
            alignment = updateAlignment(attributeset, alignment, sb);
            firstLineIndent = updateDouble(attributeset, firstLineIndent, StyleConstants.FirstLineIndent);
            if (leftIndent != oldLeftIndent || rightIndent != oldRightIndent) {
                openIndentTable(sb, leftIndent, rightIndent);
                oldLeftIndent = leftIndent;
                oldRightIndent = rightIndent;
            }
        }

        private void openIndentTable(StringBuilder sb, double d, double d1) {
            if (d != 0.0D || d1 != 0.0D) {
                closeSubsetTags(sb);
                sb.append("<table><tr>");
                final String s = getSpaceTab((int) (d / 4D));
                if (s.length() > 0) {
                    sb.append("<td>").append(s).append("</td>");
                }
                sb.append("<td>");
            }
        }

        private void closeIndentTable(StringBuilder sb, double d, double d1) {
            if (d != 0.0D || d1 != 0.0D) {
                closeSubsetTags(sb);
                sb.append("</td>");
                final String s = getSpaceTab((int) (d1 / 4D));
                if (s.length() > 0) {
                    sb.append("<td>").append(s).append("</td>");
                }
                sb.append("</tr></table>");
            }
        }

        void closeTags(StringBuilder sb) {
            closeSubsetTags(sb);
            closeTag(alignment, -1, "div", sb);
            alignment = -1;
            closeIndentTable(sb, oldLeftIndent, oldRightIndent);
        }

        private void closeSubsetTags(StringBuilder sb) {
            closeTag(bold, "b", sb);
            closeTag(italic, "i", sb);
            closeTag(underline, "u", sb);
            closeTag(color, "font", sb);
            closeTag(fontName, "font", sb);
            closeTag(size, -1, "font", sb);
            bold = false;
            italic = false;
            underline = false;
            color = null;
            fontName = "";
            size = -1;
        }

        private void closeTag(boolean flag, String s, StringBuilder sb) {
            if (flag) {
                sb.append("</").append(s).append('>');
            }
        }

        private void closeTag(Color color1, String s, StringBuilder sb) {
            if (color1 != null) {
                sb.append("</").append(s).append('>');
            }
        }

        private void closeTag(String s, String s1, StringBuilder sb) {
            if (s.length() > 0) {
                sb.append("</").append(s1).append('>');
            }
        }

        private void closeTag(int i, int j, String s, StringBuilder sb) {
            if (i > j) {
                sb.append("</").append(s).append('>');
            }
        }

        private int updateAlignment(AttributeSet attributeset, int k, StringBuilder sb) {
            int i = k;
            final Object obj = attributeset.getAttribute(StyleConstants.Alignment);
            if (obj == null) {
                return i;
            }
            int j = ((Integer) obj).intValue();
            if (j == 3) {
                j = 0;
            }
            if (j != i && j >= 0 && j <= 2) {
                if (i > -1) {
                    sb.append("</div>");
                }
                sb.append("<div align=\"").append(alignNames[j]).append("\">");
                i = j;
            }
            return i;
        }

        private Color updateFontColor(AttributeSet attributeset, Color color3, StringBuilder sb) {
            Color color1 = color3;
            final Object obj = attributeset.getAttribute(StyleConstants.Foreground);
            if (obj == null) {
                return color1;
            }
            final Color color2 = (Color) obj;
            if (color2 != color1) {
                if (color1 != null) {
                    sb.append("</font>");
                }
                sb.append("<font color=\"#").append(makeColorString(color2)).append("\">");
            }
            color1 = color2;
            return color1;
        }

        private String updateFontName(AttributeSet attributeset, String s2, StringBuilder sb) {
            String s = s2;
            final Object obj = attributeset.getAttribute(StyleConstants.FontFamily);
            if (obj == null) {
                return s;
            }
            final String s1 = (String) obj;
            if (!s1.equals(s)) {
                if (!"".equals(s)) {
                    sb.append("</font>");
                }
                sb.append("<font face=\"").append(s1).append("\">");
            }
            s = s1;
            return s;
        }

        private double updateDouble(AttributeSet attributeset, double d2, Object obj) {
            double d = d2;
            final Object obj1 = attributeset.getAttribute(obj);
            if (obj1 != null) {
                d = ((Float) obj1).floatValue();
            }
            return d;
        }

        private int updateFontSize(AttributeSet attributeset, int k, StringBuilder sb) {
            int i = k;
            final Object obj = attributeset.getAttribute(StyleConstants.FontSize);
            if (obj == null) {
                return i;
            }
            final int j = ((Integer) obj).intValue();
            if (j != i) {
                if (i != -1) {
                    sb.append("</font>");
                }
                sb.append("<font size=\"").append(j >> 2).append("\">");
            }
            i = j;
            return i;
        }

        private boolean updateBoolean(AttributeSet attributeset, Object obj, String s, boolean flag2, StringBuilder sb) {
            boolean flag = flag2;
            final Object obj1 = attributeset.getAttribute(obj);
            if (obj1 != null) {
                final boolean flag1 = ((Boolean) obj1).booleanValue();
                if (flag1 != flag) {
                    if (flag1) {
                        sb.append('<').append(s).append('>');
                    } else {
                        sb.append("</").append(s).append('>');
                    }
                }
                flag = flag1;
            }
            return flag;
        }

        private String makeColorString(Color color1) {
            String s = Long.toString(color1.getRGB() & 0xffffff, 16);
            if (s.length() < 6) {
                final StringBuilder sb = new StringBuilder();
                for (int i = s.length(); i < 6; i++) {
                    sb.append('0');
                }
                sb.append(s);
                s = sb.toString();
            }
            return s;
        }

        public String performFirstLineIndent(String s2) {
            String s = s2;
            if (firstLine) {
                if (firstLineIndent != 0.0D) {
                    final int i = (int) (firstLineIndent / 4D);
                    s = new StringBuilder(getSpaceTab(i)).append(s).toString();
                }
                firstLine = false;
            }
            return s;
        }

        public String getSpaceTab(int i) {
            final StringBuilder sb = new StringBuilder();
            for (int j = 0; j < i; j++) {
                sb.append("&nbsp;");
            }
            return sb.toString();
        }

    }

    /**
     * Initializes a new {@link Rtf2HtmlConverter}
     */
    private Rtf2HtmlConverter() {
        super();
    }

    /**
     * Triggers the rtf2html conversion
     *
     * @param rtfContent The RTF content
     * @return The converted HTML content
     */
    public static String convertRTFToHTML(String rtfContent) {
        final HTMLStateMachine hsm = new HTMLStateMachine();
        final String htmlBody = convertRTFStringToHTML(rtfContent, hsm);
        final StringBuilder docBuilder = new StringBuilder(htmlBody.length() + 26).append("<html><body>");
        docBuilder.append(HtmlProcessing.formatHrefLinks(htmlBody));
        return docBuilder.append("</body></html>").toString();
    }

    /**
     * Converts a single RTF string to HTML without wrapping returned converted HTML body inside a HTML document.
     *
     * @param rtfString The RTF string
     * @return The converted HTML body
     */
    public static String convertRTFStringToHTML(String rtfString) {
        final HTMLStateMachine hsm = new HTMLStateMachine();
        final RTFEditorKit rtfeditorkit = new RTFEditorKit();
        final DefaultStyledDocument defaultstyleddocument = new DefaultStyledDocument();
        readString(rtfString, defaultstyleddocument, rtfeditorkit);
        return scanDocument(defaultstyleddocument, hsm);
    }

    private static String convertRTFStringToHTML(String s2, HTMLStateMachine hsm) {
        String s = s2;
        final RTFEditorKit rtfeditorkit = new RTFEditorKit();
        final DefaultStyledDocument defaultstyleddocument = new DefaultStyledDocument();
        readString(s, defaultstyleddocument, rtfeditorkit);
        s = scanDocument(defaultstyleddocument, hsm);
        return s;
    }

    private static void readString(String s, Document document, RTFEditorKit rtfeditorkit) {
        try {
            rtfeditorkit.read(new UnsynchronizedByteArrayInputStream(Charsets.toAsciiBytes(s)), document, 0);
        } catch (IOException e) {
            LOG.error("", e);
            return;
        } catch (Exception e) {
            LOG.error("", e);
            return;
        }
    }

    private static String scanDocument(Document document, HTMLStateMachine hsm) {
        try {
            final StringBuilder sb = new StringBuilder(document.getLength());
            final Element element = document.getDefaultRootElement();
            recurseElements(element, document, sb, hsm);
            hsm.closeTags(sb);
            return sb.toString();
        } catch (Exception e) {
            LOG.error("", e);
        }
        return "";
    }

    private static void recurseElements(Element element, Document document, StringBuilder sb, HTMLStateMachine hsm) {
        for (int i = 0; i < element.getElementCount(); i++) {
            final Element element1 = element.getElement(i);
            scanAttributes(element1, document, sb, hsm);
            recurseElements(element1, document, sb, hsm);
        }
    }

    private static void scanAttributes(Element element, Document document, StringBuilder sb, HTMLStateMachine hsm) {
        try {
            final int i = element.getStartOffset();
            final int j = element.getEndOffset();
            String s = document.getText(i, j - i);
            final javax.swing.text.AttributeSet attributeset = element.getAttributes();
            hsm.updateState(attributeset, sb, element);
            final String s1 = element.getName();
            if (s1.equalsIgnoreCase("content")) {
                s = s.replaceAll("\\t", com.openexchange.java.Strings.quoteReplacement(hsm.getSpaceTab(8)));
                s = s.replaceAll("\\n", "<br />\n");
                s = hsm.performFirstLineIndent(s);
                sb.append(s);
            }
        } catch (Exception e) {
            LOG.error("", e);
            return;
        }
    }

}
