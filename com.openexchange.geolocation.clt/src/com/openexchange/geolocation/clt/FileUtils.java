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

package com.openexchange.geolocation.clt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.openexchange.cli.ProgressMonitor;
import com.openexchange.java.Strings;

/**
 * {@link FileUtils}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public final class FileUtils {

    /**
     * The default buffer size
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Checks the first four bytes of the specified file to determine whether
     * it is a ZIP archive. The signatures of a ZIP archive are listed
     * <a href="https://en.wikipedia.org/wiki/List_of_file_signatures">here</a>.
     * 
     * @return <code>true</code> if the specified file is an archive; <code>false</code> otherwise.
     * @throws IOException if an I/O error is occurred
     */
    public static boolean isArchive(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        File f = new File(filePath);
        int fileSignature = 0;
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            fileSignature = raf.readInt();
        }
        switch (fileSignature) {
            case 0x504B0304:
                return true;
            case 0x504B0506:
                System.out.println("ERROR: It seems that the archive you provided is empty.");
                return false;
            case 0x504B0708:
                System.out.println("ERROR: It seems that the archive you provided is spanned over multiple files.");
                return false;
            default:
                return false;
        }
    }

    /**
     * Downloads the file denoted by the specified downloadUrl to the specified downloadPath and uses the optional name
     * as the file name (if no filename is provided by the 'Content-Disposition' header.
     * 
     * @param downloadUrl the download URL
     * @param downloadPath The download path
     * @param optionalName The optional file name
     * @return the downloaded {@link File}
     * @throws IOException
     * @throws MalformedURLException if a malformed URL is specified
     * @throws InvalidContentType
     */
    public static File downloadFile(String downloadUrl, String downloadPath, String optionalName, String allowedContentTypes) throws MalformedURLException, IOException {
        URLConnection connection = new URL(downloadUrl).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);

        ConnectionUtils.checkContentType(connection, allowedContentTypes);

        String downloadFilename = extractFilename(connection.getHeaderField("Content-Disposition"), optionalName);
        long contentLength = connection.getContentLength();
        System.out.println("Database size: " + Strings.humanReadableByteCount(contentLength, true) + ".\n");
        try (InputStream inputStream = connection.getInputStream()) {
            File dbfile = Paths.get(downloadPath, downloadFilename).toFile();
            FileUtils.writeFile(inputStream, dbfile, contentLength);
            System.out.println();
            return dbfile;
        }
    }

    /**
     * Writes the specified {@link InputStream} to the specified {@link File}
     * 
     * @param inputStream The {@link InputStream} to write
     * @param outputFile The {@link File} to write to
     * @param size The size
     * @throws IOException if an I/O error is occurred
     */
    public static void writeFile(InputStream inputStream, File outputFile, long size) throws IOException {
        try (FileOutputStream output = org.apache.commons.io.FileUtils.openOutputStream(outputFile)) {
            long sum = 0;
            int count = 0;
            byte[] data = new byte[BUFFER_SIZE];
            ProgressMonitor progressMonitor = new ProgressMonitor(50, outputFile.getAbsolutePath());
            while ((count = inputStream.read(data, 0, BUFFER_SIZE)) != -1) {
                output.write(data, 0, count);
                sum += count;
                if (size > 0) {
                    progressMonitor.update(Strings.humanReadableByteCount(sum, true), ((double) sum / size));
                }
            }
        }
    }

    /**
     * Extracts the contents of the specified archive to the specified extractDirectory.
     * 
     * @param filePath The file path that denotes to a ZIP archive
     * @param extractDirectory The extract directory
     * @param keep <code>true</code> if the files should be kept after the JVM exits; <code>false</code> to delete them
     * @return an unmodifiable {@link List} with the extracted files
     * @throws FileNotFoundException if the filePath does not exist
     * @throws IOException if an I/O error is occurred
     */
    public static final List<File> extractArchive(String filePath, String extractDirectory, boolean keep) throws FileNotFoundException, IOException {
        System.out.println("Extracting the archive '" + filePath + "' in '" + extractDirectory + "'...");
        byte[] buffer = new byte[BUFFER_SIZE];
        List<File> extractedFiles = new LinkedList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath)); ZipInputStream zipInputStream = new ZipInputStream(fis)) {
            while (true) {
                ZipEntry zipEntry = null;
                try {
                    zipEntry = zipInputStream.getNextEntry();
                    if (zipEntry == null) {
                        break;
                    }
                    extractZipEntry(zipInputStream, zipEntry, buffer, extractedFiles, extractDirectory, keep);
                } finally {
                    if (zipEntry != null) {
                        zipInputStream.closeEntry();
                    }
                }
            }
        }
        return extractedFiles;
    }

    /**
     * Extracts the ZIP entries from the specified {@link ZipInputStream}
     * 
     * @param zipInputStream The {@link ZipInputStream} containing the entries
     * @param zipEntry The ZipEntry to extract
     * @param buffer the buffer to use when writing the extracted entry
     * @throws IOException if an I/O error is occurred
     */
    private static final void extractZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry, byte[] buffer, List<File> extractedFiles, String extractDirectory, boolean keep) throws IOException {
        String fileName = zipEntry.getName();
        File newFile = Paths.get(extractDirectory, fileName).toFile();
        if (false == keep) {
            newFile.deleteOnExit();
        }
        extractedFiles.add(newFile);
        System.out.print("Extracting to '" + newFile.getAbsolutePath() + "'...");

        new File(newFile.getParent()).mkdirs();
        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            System.out.println("failed.");
            throw e;
        }
        System.out.println("OK");
    }

    /**
     * Extracts the 'filename' from the specified content disposition header
     * 
     * @param contentDisposition The content disposition header
     * @return The filename value
     */
    private static String extractFilename(String contentDisposition, String defaultName) {
        if (contentDisposition == null || contentDisposition.isEmpty()) {
            return defaultName;
        }
        int index = contentDisposition.indexOf("filename=");
        if (index < 0) {
            return defaultName;
        }
        return contentDisposition.substring(index + "filename=".length()).replaceAll("\"", "");
    }
}
