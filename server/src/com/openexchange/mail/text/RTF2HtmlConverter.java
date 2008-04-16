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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.text;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;

/**
 * {@link RTF2HtmlConverter}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RTF2HtmlConverter {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(RTF2HtmlConverter.class);

	/**
	 * Default constructor
	 */
	public RTF2HtmlConverter() {
		super();
	}

	/**
	 * COnverts specified rtf content to html
	 * 
	 * @param rtfContent
	 *            The rtf content
	 * @return Converted html content as {@link String}
	 */
	public String convert2HTML(final String rtfContent) {
		final RTFEditorKit rtf_edit = new RTFEditorKit();
		final JTextPane jtp_rtf = new JTextPane();
		final JTextPane jtp_html = new JTextPane();
		final StyleContext rtf_context = new StyleContext();
		final DefaultStyledDocument rtf_doc = new DefaultStyledDocument(rtf_context);
		jtp_rtf.setEditorKit(rtf_edit);
		jtp_rtf.setContentType("text/rtf");
		jtp_html.setContentType("text/html");
		try {
			rtf_edit.read(new StringReader(rtfContent), rtf_doc, 0);
			jtp_rtf.setDocument(rtf_doc);
			jtp_html.setText(rtf_doc.getText(0, rtf_doc.getLength()));
			HTMLDocument html_doc = null;
			for (int i = 0; i < rtf_doc.getLength(); i++) {
				final AttributeSet a = rtf_doc.getCharacterElement(i).getAttributes();
				final AttributeSet p = rtf_doc.getParagraphElement(i).getAttributes();
				final String s = jtp_rtf.getText(i, 1);
				jtp_html.select(i, i + 1);
				jtp_html.replaceSelection(s);
				html_doc = (HTMLDocument) jtp_html.getDocument();
				html_doc.putProperty("", "");
				html_doc.setCharacterAttributes(i, 1, a, false);
				final MutableAttributeSet attr = new SimpleAttributeSet(p);
				html_doc.setParagraphAttributes(i, 1, attr, false);
			}
			if (null == html_doc) {
				return rtfContent;
			}
			final StringWriter writer = new StringWriter();
			final HTMLEditorKit html_edit = new HTMLEditorKit();
			html_edit.write(writer, html_doc, 0, html_doc.getLength());
			return writer.toString();
		} catch (final IOException e) {
			LOG.error("Rtf2Html conversion failed", e);
			return rtfContent;
		} catch (final BadLocationException e) {
			LOG.error("Rtf2Html conversion failed", e);
			return rtfContent;
		}
	}

	public static void main(final String[] args) {
		final RTF2HtmlConverter con = new RTF2HtmlConverter();

		// Load an RTF file into the editor
		try {
			final FileInputStream fi = new FileInputStream("/home/thorben/rtf.txt");
			final ByteArrayOutputStream tmp = new ByteArrayOutputStream();
			final byte[] buf = new byte[8192];
			while ((fi.read(buf)) != -1) {
				tmp.write(buf);
			}

			final String html = con.convert2HTML(new String(tmp.toByteArray(), System.getProperty("file.encoding",
					"UTF-8")));
			System.out.println(html);
		} catch (final FileNotFoundException e) {
			System.out.println("File not found");
		} catch (final IOException e) {
			System.out.println("I/O error");
		}
	}
}
