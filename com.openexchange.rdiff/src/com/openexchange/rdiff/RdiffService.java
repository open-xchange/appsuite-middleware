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

package com.openexchange.rdiff;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link RdiffService} - The rdiff service.
 * <p>
 * <ul>
 * <li>Client generates the signatures for mutated file and transfers them to server</li>
 * <li>Server receives client's signatures and creates appropriate checksum-pairs for them. Then the server will create the deltas from base
 * file. Those deltas are in turn sent back to client</li>
 * <li>Client receives server's deltas and patches his local file using deltas</li>
 * </ul>
 *
 * <pre>
 * // client does this first
 * RdiffService clientRdiff = ...;
 * clientRdiff.createSignatures(new FileInputStream(mutated), new FileOutputStream(signature));
 *
 * // server gets signature file, and does this now
 * RdiffService serverRdiff = ...;
 * List&lt;ChecksumPair&gt; signatures = serverRdiff.readSignatures(new FileInputStream(signature));
 * serverRdiff.createDeltas(signatures, new FileInputStream(basis), new FileOutputStream(newdelta));
 *
 * // finally, client patches his file
 * clientRdiff.rebuildFile(mutated, new FileInputStream(newdelta), new FileOutputStream(output));
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface RdiffService {

    /**
     * Generates the signatures for specified resource's <code>InputStream</code> and writes them to specified <code>OutputStream</code>.
     * <p>
     * Provided streams are closed/flushed orderly.
     *
     * @param sourceIn The resource input stream to read from
     * @param signatureOut The output stream to write resulting signatures to
     * @throws OXException If generating signatures fails
     */
    void createSignatures(InputStream sourceIn, OutputStream signatureOut) throws OXException;

    /**
     * Generates the signatures for specified resource's <code>InputStream</code>.
     * <p>
     * Provided stream is closed orderly.
     *
     * @param sourceIn The resource input stream to read from
     * @return The resulting checksum pairs
     * @throws OXException If generating signatures fails
     */
    List<ChecksumPair> createSignatures(InputStream sourceIn) throws OXException;

    /**
     * Write the signatures to the specified output stream.
     * <p>
     * Provided stream is closed/flushed orderly.
     *
     * @param sigs The signatures to write.
     * @param out The OutputStream to write to.
     * @throws OXException If writing fails.
     */
    void writeSignatures(List<ChecksumPair> sigs, OutputStream out) throws OXException;

    /**
     * Reads the signatures from the input stream.
     * <p>
     * Provided stream is closed orderly.
     *
     * @param signatureIn The input stream to read from
     * @return A collection of {@link ChecksumPair}s
     * @throws OXException If reading signatures fails
     */
    List<ChecksumPair> readSignatures(InputStream signatureIn) throws OXException;

    /**
     * Generates the deltas for provided base input stream using given checksum pairs (generated from signatures) and writes resulting
     * deltas to specified output stream.
     * <p>
     * Provided streams are closed/flushed orderly.
     *
     * @param sums The checksum pairs (generated from signatures using <code>readSignatures</code> method)
     * @param baseIn The input stream providing data base
     * @param deltaOut The output stream to write resulting deltas to
     * @throws OXException If generating deltas fails
     */
    void createDeltas(List<ChecksumPair> sums, InputStream baseIn, OutputStream deltaOut) throws OXException;

    /**
     * Generates the deltas for provided base input stream using given checksum pairs (generated from signatures).
     *
     * @param sums The checksum pairs (generated from signatures using <code>readSignatures</code> method)
     * @param baseIn The input stream providing data base
     * @return The deltas
     * @throws OXException If generating deltas fails
     */
    List<Delta> createDeltas(List<ChecksumPair> sums, InputStream baseIn) throws OXException;

    /**
     * Rebuilds specified base file with given deltas and writes patched file to <code>patchOut</code>.
     *
     * @param baseFile The base file
     * @param deltaIn The input stream providing the deltas
     * @param patchOut The output stream to write patched file to
     * @throws OXException If rebuilding the file fails
     */
    void rebuildFile(File baseFile, InputStream deltaIn, OutputStream patchOut) throws OXException;

}
