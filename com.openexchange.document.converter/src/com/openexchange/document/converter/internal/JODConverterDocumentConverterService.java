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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.document.converter.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.activation.MimetypesFileTypeMap;
import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;
import com.openexchange.document.converter.DocumentContent;
import com.openexchange.document.converter.DocumentConverterExceptionCodes;
import com.openexchange.document.converter.DocumentConverterService;
import com.openexchange.document.converter.FileDocumentContent;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceLookup;

/**
 * {@link JODConverterDocumentConverterService} - The {@link DocumentConverterService} implementation based on <a
 * href="http://code.google.com/p/jodconverter/">JODConverter</a>.
 * <p>
 * <a href="http://shervinasgari.blogspot.com/2010/08/migrating-from-jodconverter-2-to.html">Example 1</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JODConverterDocumentConverterService implements DocumentConverterService {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(JODConverterDocumentConverterService.class));

    private final ServiceLookup serviceLookup;

    private final OfficeManager officeManager;

    private volatile OfficeDocumentConverter converter;

    private volatile OfficeDocumentConverter pdfConverter;

    /**
     * Initializes a new {@link JODConverterDocumentConverterService}.
     */
    public JODConverterDocumentConverterService(final ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
        /*
         * Start-up JODConverter
         */
        final DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
//        configuration.setOfficeHome("/usr/lib/openoffice");
//        configuration.setConnectionProtocol(OfficeConnectionProtocol.PIPE);
//        {
//            final int processors = Runtime.getRuntime().availableProcessors();
//            final String[] pipes = new String[processors];
//            final StringBuilder sb = new StringBuilder("office");
//            for (int i = 0; i < pipes.length; i++) {
//                sb.setLength(6);
//                pipes[i] = sb.append(i + 1).toString();
//            }
//            configuration.setPipeNames(pipes);
//        }
        configuration.setTaskExecutionTimeout(240000L); // 4 minutes
        configuration.setTaskQueueTimeout(60000L); // 1 minute
        officeManager = configuration.buildOfficeManager();
    }

    /**
     * Gets the all-purpose converter
     *
     * @return The all-purpose converter
     */
    private OfficeDocumentConverter getConverter() {
        OfficeDocumentConverter tmp = converter;
        if (null == tmp) {
            synchronized (this) {
                tmp = converter;
                if (null == tmp) {
                    converter = tmp = new OfficeDocumentConverter(officeManager);
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the PDF converter
     *
     * @return The PDF converter
     */
    private OfficeDocumentConverter getPDFConverter() {
        OfficeDocumentConverter tmp = pdfConverter;
        if (null == tmp) {
            synchronized (this) {
                tmp = pdfConverter;
                if (null == tmp) {
                    final DocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();
                    formatRegistry.getFormatByExtension(PDF).setInputFamily(DocumentFamily.DRAWING);
                    tmp = new OfficeDocumentConverter(officeManager, formatRegistry);
                    converter = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Starts-up this {@link JODConverterDocumentConverterService} instance.
     *
     * @return This <i>started</i> {@link JODConverterDocumentConverterService}
     */
    public JODConverterDocumentConverterService startUp() {
        officeManager.start();
        return this;
    }

    /**
     * Shuts down this {@link JODConverterDocumentConverterService} instance.
     */
    public void shutDown() {
        officeManager.stop();
    }

    private static final int BUFLEN = 2048;

    private static final String PDF = "pdf";

    private static final Integer PDFX1A2001 = Integer.valueOf(1);

    /**
     * This DocumentFormat must be used when converting from document (not pdf) to pdf/a For some reason "PDF/A-1" is called
     * "SelectPdfVersion" internally; maybe they plan to add other PdfVersions later.
     */
    private DocumentFormat toFormatPDFA() {
        final DocumentFormat format = new DocumentFormat("PDF/A", PDF, "application/pdf");
        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("FilterName", "writer_pdf_Export");

        final Map<String, Object> filterData = new HashMap<String, Object>();
        filterData.put("SelectPdfVersion", PDFX1A2001);
        properties.put("FilterData", filterData);

        format.setStoreProperties(DocumentFamily.TEXT, properties);
        return format;
    }

    /**
     * This DocumentFormat must be used when converting from pdf to pdf/a For some reason "PDF/A-1" is called "SelectPdfVersion" internally;
     * maybe they plan to add other PdfVersions later.
     */
    private DocumentFormat toFormatPDFA_DRAW() {
        final DocumentFormat format = new DocumentFormat("PDF/A", PDF, "application/pdf");
        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("FilterName", "draw_pdf_Export");

        final Map<String, Object> filterData = new HashMap<String, Object>();
        filterData.put("SelectPdfVersion", PDFX1A2001);
        properties.put("FilterData", filterData);

        format.setStoreProperties(DocumentFamily.DRAWING, properties);
        return format;
    }

    @Override
    public DocumentContent convert(final DocumentContent inputContent, final String extension) throws OXException {
        if (null == inputContent) {
            throw DocumentConverterExceptionCodes.MISSING_ARGUMENT.create("inputContent");
        }
        if (isEmpty(extension)) {
            throw DocumentConverterExceptionCodes.MISSING_ARGUMENT.create("extension");
        }
        try {
            String ext = extension;
            if (ext.charAt(0) == '.') {
                ext = ext.substring(1);
            }
            final String inputExtension = FilenameUtils.getExtension(inputContent.getName());
            /*
             * If inputContent is a PDF you will need to use another FormatRegistery, namely DRAWING
             */
            final boolean pdf2pdf;
            final OfficeDocumentConverter converter;
            if (PDF.equalsIgnoreCase(ext) && PDF.equalsIgnoreCase(inputExtension)) {
                converter = getPDFConverter();
                pdf2pdf = true;
            } else {
                converter = getConverter();
                pdf2pdf = false;
            }
            /*
             * Get (generate if absent) input file
             */
            File inputFile = inputContent.optFile();
            if (null == inputFile) {
                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    inputFile = File.createTempFile(FilenameUtils.getBaseName(inputContent.getName()), "." + inputExtension);
                    outputStream = new FileOutputStream(inputFile);
                    inputStream = inputContent.getInputStream();
                    final byte[] buf = new byte[BUFLEN];
                    for (int read; (read = inputStream.read(buf, 0, BUFLEN)) > 0;) {
                        outputStream.write(buf, 0, read);
                    }
                    outputStream.flush();
                } finally {
                    deleteOnExit(inputFile);
                    Streams.close(outputStream);
                    Streams.close(inputStream);
                }
            }
            if (null == inputFile) {
                throw DocumentConverterExceptionCodes.ERROR.create("Input file is null.");
            }
            /*
             * Create output file
             */
            final File outputFile = File.createTempFile(FilenameUtils.getBaseName(inputContent.getName()), "." + ext);
            try {
                final long startTime = System.currentTimeMillis();
                /*
                 * If both input and output file is PDF
                 */
                if (pdf2pdf) {
                    /*
                     * Add the DocumentFormat with DRAW
                     */
                    converter.convert(inputFile, outputFile, toFormatPDFA_DRAW());
                } else if (PDF.equalsIgnoreCase(ext)) {
                    converter.convert(inputFile, outputFile, toFormatPDFA());
                } else {
                    converter.convert(inputFile, outputFile);
                }
                final long conversionTime = System.currentTimeMillis() - startTime;
                LOG.info(String.format(
                    "Successful conversion: %s [%db] to %s in %dmsec",
                    inputExtension,
                    Long.valueOf(inputFile.length()),
                    extension,
                    Long.valueOf(conversionTime)));
                /*
                 * Add resulting file to file management
                 */
                final ManagedFileManagement fileManagement = serviceLookup.getService(ManagedFileManagement.class);
                if (null != fileManagement) {
                    fileManagement.createManagedFile(outputFile);
                }
                return new FileDocumentContent(outputFile, new MimetypesFileTypeMap().getContentType(outputFile));
            } finally {
                deleteOnExit(outputFile);
            }
        } catch (final IOException e) {
            throw DocumentConverterExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final OfficeException e) {
            throw DocumentConverterExceptionCodes.OFFICE_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DocumentConverterExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static void deleteOnExit(final File file) {
        if (null != file) {
            try {
                file.deleteOnExit();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }
}
