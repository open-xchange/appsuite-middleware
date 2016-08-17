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

package com.openexchange.textxtraction.internal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;
import org.apache.xmlbeans.XmlException;
import com.openexchange.java.Streams;

/**
 * {@link ReadFileFormat}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ReadFileFormat {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReadFileFormat.class);

    private final StringBuilder sb;

    private final StringBuilder textBuffer;


    /**
     * Initializes a new {@link ReadFileFormat}.
     */
    public ReadFileFormat() {
        super();
        sb = new StringBuilder(8192);
        textBuffer = new StringBuilder();
    }

    /**
     * Expects the input to be a MS document.
     *
     * @param in The input stream
     * @return The extracted text or <code>null</code>
     * @throws IOException If an I/O error occurs
     */
    public String ms2text(final InputStream in) throws IOException {
        try {
            return ExtractorFactory.createExtractor(in).getText();
        } catch (final InvalidFormatException e) {
            LOG.debug("", e);
        } catch (final OpenXML4JException e) {
            LOG.debug("", e);
        } catch (final XmlException e) {
            LOG.debug("", e);
        } catch (final RuntimeException e) {
            LOG.debug("", e);
        } finally {
            Streams.close(in);
        }
        return null;
    }

    /**
     *
     *
     * @param is
     * @return
     * @throws Exception
     */
    public String rtf2text(final InputStream is) throws Exception {
        final DefaultStyledDocument styledDoc = new DefaultStyledDocument();
        new RTFEditorKit().read(is, styledDoc, 0);
        return styledDoc.getText(0, styledDoc.getLength());
    }

    public String ppt2text(final String fileName) throws Exception {
        final POIFSReader poifReader = new POIFSReader();
        poifReader.registerListener(new ReadFileFormat.MyPOIFSReaderListener());
        poifReader.read(new FileInputStream(fileName));
        return sb.toString();
    }

    class MyPOIFSReaderListener implements POIFSReaderListener {

        @Override
        public void processPOIFSReaderEvent(final POIFSReaderEvent event) {
            final char ch0 = (char) 0;
            final char ch11 = (char) 11;
            try {
                DocumentInputStream dis = null;
                dis = event.getStream();
                final byte btoWrite[] = new byte[dis.available()];
                dis.read(btoWrite, 0, dis.available());
                for (int i = 0; i < btoWrite.length - 20; i++) {
                    final long type = LittleEndian.getUShort(btoWrite, i + 2);
                    final long size = LittleEndian.getUInt(btoWrite, i + 4);
                    if (type == 4008) {
                        try {
                            final String s = new String(btoWrite, i + 4 + 1, (int) size + 3).replace(ch0, ' ').replace(ch11, ' ');
                            if (s.trim().startsWith("Click to edit") == false) {
                                sb.append(s);
                            }
                        } catch (final Exception ee) {
                            System.out.println("error:" + ee);
                        }
                    }
                }
            } catch (final Exception e) {
                LOG.error("", e);
                return;
            }
        }
    }

//    public String xls2text(final InputStream in) throws Exception {
//        final HSSFWorkbook excelWb = new HSSFWorkbook(in);
//        final StringBuffer result = new StringBuffer(4096);
//        final int numberOfSheets = excelWb.getNumberOfSheets();
//        for (int i = 0; i < numberOfSheets; i++) {
//            final HSSFSheet sheet = excelWb.getSheetAt(i);
//            final int numberOfRows = sheet.getPhysicalNumberOfRows();
//            if (numberOfRows > 0) {
//                if (excelWb.getSheetName(i) != null && excelWb.getSheetName(i).length() != 0) {
//                    // append sheet name to content
//                    if (i > 0) {
//                        result.append("\n\n");
//                    }
//                    result.append(excelWb.getSheetName(i).trim());
//                    result.append(":\n\n");
//                }
//
//                final Iterator<HSSFRow> rowIt = sheet.rowIterator();
//                while (rowIt.hasNext()) {
//                    final HSSFRow row = rowIt.next();
//                    if (row != null) {
//                        boolean hasContent = false;
//                        final Iterator<HSSFCell> it = row.cellIterator();
//                        while (it.hasNext()) {
//                            final HSSFCell cell = it.next();
//                            String text = null;
//                            try {
//                                switch (cell.getCellType()) {
//                                case HSSFCell.CELL_TYPE_BLANK:
//                                case HSSFCell.CELL_TYPE_ERROR:
//                                    // ignore all blank or error cells
//                                    break;
//                                case HSSFCell.CELL_TYPE_NUMERIC:
//                                    text = Double.toString(cell.getNumericCellValue());
//                                    break;
//                                case HSSFCell.CELL_TYPE_BOOLEAN:
//                                    text = Boolean.toString(cell.getBooleanCellValue());
//                                    break;
//                                case HSSFCell.CELL_TYPE_STRING:
//                                default:
//                                    text = cell.getStringCellValue();
//                                    break;
//                                }
//                            } catch (final Exception e) {
//                            }
//                            if ((text != null) && (text.length() != 0)) {
//                                result.append(text.trim());
//                                result.append(' ');
//                                hasContent = true;
//                            }
//                        }
//                        if (hasContent) {
//                            // append a newline at the end of each row that has content
//                            result.append('\n');
//                        }
//                    }
//                }
//            }
//        }
//        return result.toString();
//    }

//    public void processElement(final Object o) {
//        if (o instanceof Element) {
//            final Element e = (Element) o;
//            final String elementName = e.getQualifiedName();
//            if (elementName.startsWith("text")) {
//                if (elementName.equals("text:tab")) // add tab for text:tab
//                {
//                    textBuffer.append("\t");
//                } else if (elementName.equals("text:s")) // add space for text:s
//                {
//                    textBuffer.append(" ");
//                } else {
//                    final List children = e.getContent();
//                    final Iterator iterator = children.iterator();
//                    while (iterator.hasNext()) {
//                        final Object child = iterator.next();
//                        // If Child is a Text Node, then append the text
//                        if (child instanceof Text) {
//                            final Text t = (Text) child;
//                            textBuffer.append(t.getValue());
//                        } else {
//                            processElement(child); // Recursively process the child element
//                        }
//                    }
//                }
//                if (elementName.equals("text:p")) {
//                    textBuffer.append("\n");
//                }
//            } else {
//                final List non_text_list = e.getContent();
//                final Iterator it = non_text_list.iterator();
//                while (it.hasNext()) {
//                    final Object non_text_child = it.next();
//                    processElement(non_text_child);
//                }
//            }
//        }
//    }

//    public String getOpenOfficeText(final String fileName) throws Exception {
//        textBuffer = new StringBuffer();
//        // Unzip the openOffice Document
//        final ZipFile zipFile = new ZipFile(fileName);
//        final Enumeration entries = zipFile.entries();
//        ZipEntry entry;
//        while (entries.hasMoreElements()) {
//            entry = (ZipEntry) entries.nextElement();
//            if (entry.getName().equals("content.xml")) {
//                textBuffer = new StringBuffer();
//                final SAXBuilder sax = new SAXBuilder();
//                final Document doc = sax.build(zipFile.getInputStream(entry));
//                final Element rootElement = doc.getRootElement();
//                processElement(rootElement);
//                break;
//            }
//        }
//        return textBuffer.toString();
//    }
//
//    public String fileToStringNow(final File f) throws Exception {
//        final BufferedReader br = new BufferedReader(new FileReader(f));
//        String nextLine = "";
//        final StringBuffer sbuff = new StringBuffer();
//        while ((nextLine = br.readLine()) != null) {
//            sbuff.append(nextLine);
//            sbuff.append(System.getProperty("line.separator"));
//        }
//        return sbuff.toString();
//    }
//
//    public static void main(final String[] args) throws Exception {
//        final ReadFileFormat rff = new ReadFileFormat();
//        System.out.print("Enter File Name => ");
//        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        final String fileName = br.readLine();
//        final File f = new File(fileName);
//        if (!f.exists()) {
//            System.out.println("Sorry File does not Exists!");
//        } else {
//            if (f.getName().endsWith(".pdf") || f.getName().endsWith(".PDF")) {
//                System.out.println(rff.pdftotext(fileName));
//            } else if (f.getName().endsWith(".doc") || f.getName().endsWith(".DOC")) {
//                System.out.println(rff.ms2text(fileName));
//            } else if (f.getName().endsWith(".rtf") || f.getName().endsWith(".RTF")) {
//                System.out.println(rff.rtf2text(new FileInputStream(f)));
//            } else if (f.getName().endsWith(".ppt") || f.getName().endsWith(".PPT")) {
//                System.out.println(rff.ppt2text(fileName));
//            } else if (f.getName().endsWith(".xls") || f.getName().endsWith(".XLS")) {
//                System.out.println(rff.xls2text(new FileInputStream(f)));
//            } else if (f.getName().endsWith(".odt") || f.getName().endsWith(".ODT") || f.getName().endsWith(".ods") || f.getName().endsWith(
//                ".ODS") || f.getName().endsWith(".odp") || f.getName().endsWith(".ODP")) {
//                System.out.println(rff.getOpenOfficeText(fileName));
//            } else {
//                System.out.println(rff.fileToStringNow(f));
//            }
//        }
//        br.close();
//    }

}
