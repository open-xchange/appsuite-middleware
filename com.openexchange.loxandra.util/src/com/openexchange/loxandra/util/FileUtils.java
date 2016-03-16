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
package com.openexchange.loxandra.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 *
 */
public class FileUtils {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileUtils.class);

	/**
	 * Read data from a file and convert it to a byte array.
	 * @param aInputFileName path
	 * @return the byte array representation of the file
	 */
	public static byte[] binToByteArray(String aInputFileName) {
		log.info("Reading in binary file named : {}", aInputFileName);
		File file = new File(aInputFileName);
		log.info("File size: {}", file.length());
		byte[] result = new byte[(int) file.length()];
		try {
			InputStream input = null;
			try {
				int totalBytesRead = 0;
				input = new BufferedInputStream(new FileInputStream(file), 65536);
				while (totalBytesRead < result.length) {
					int bytesRemaining = result.length - totalBytesRead;
					int bytesRead = input.read(result, totalBytesRead, bytesRemaining);
					if (bytesRead > 0) {
						totalBytesRead = totalBytesRead + bytesRead;
					}
				}
				log.info("Num bytes read: {}", totalBytesRead);
			} finally {
				log.info("Closing input stream.");
				if (input != null) {
                    input.close();
                }
			}
		} catch (FileNotFoundException ex) {
			log.error("File not found.");
		} catch (IOException ex) {
			log.error("", ex);
		}

		return result;
	}

	public static byte[] textToByteArray(String inputFileName) {
	    File file = new File(inputFileName);
	    byte[] result = new byte[(int)file.length()];

	    try {
	        InputStream input = null;
	        try {

	            input = new BufferedInputStream(new FileInputStream(file), 65536);

	        } finally {
	            log.info("Closing input stream.");
	            if (input != null) {
                    input.close();
                }
	        }
	    } catch (FileNotFoundException ex) {
	        log.error("File not found.");
	    } catch (IOException ex) {
	        log.error("", ex);
	    }

	    return result;

	}

	public static ByteBuffer binToByteBuffer(String filename) {
		return ByteBuffer.wrap(binToByteArray(filename));
	}

	public static ByteBuffer textToByteBuffer(String filename) {
	    return ByteBuffer.wrap(textToByteArray(filename));
	}

	/**
	 * Transforms a byte array to file
	 * @param aInput the byte array
	 * @param aOutputFileName file on disk
	 */
	public static void write(byte[] aInput, String aOutputFileName) {
		log.info("Writing binary file...");
		try {
			OutputStream output = null;
			try {
				output = new BufferedOutputStream(new FileOutputStream(
						aOutputFileName));
				output.write(aInput);
				log.info("OK");
			} finally {
			    if (output != null) {
                    output.close();
                }
			}
		} catch (FileNotFoundException ex) {
			log.error("File not found.");
		} catch (IOException ex) {
			log.error("", ex);
		}
	}

	public static void writeToFile(ByteBuffer input, String output) {
		try {
			FileChannel channel = new FileOutputStream(new File(output), false).getChannel();
			channel.write(input);
			channel.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}