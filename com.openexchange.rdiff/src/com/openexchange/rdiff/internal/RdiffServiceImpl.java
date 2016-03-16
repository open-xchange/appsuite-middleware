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

package com.openexchange.rdiff.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.metastatic.rsync.Rdiff;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.rdiff.ChecksumPair;
import com.openexchange.rdiff.Delta;
import com.openexchange.rdiff.RdiffExceptionCodes;
import com.openexchange.rdiff.RdiffService;

/**
 * {@link RdiffServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdiffServiceImpl implements RdiffService {

    private final Rdiff rdiff;

    /**
     * Initializes a new {@link RdiffServiceImpl}.
     */
    public RdiffServiceImpl() {
        super();
        rdiff = new Rdiff();
    }

    @Override
    public void createSignatures(final InputStream sourceIn, final OutputStream signatureOut) throws OXException {
        try {
            rdiff.makeSignatures(sourceIn, signatureOut);
        } catch (final NoSuchAlgorithmException e) {
            throw RdiffExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(sourceIn);
            Streams.flush(signatureOut);
            Streams.close(signatureOut);
        }
    }

    @Override
    public List<ChecksumPair> createSignatures(final InputStream sourceIn) throws OXException {
        try {
            @SuppressWarnings("unchecked")
            final List<org.metastatic.rsync.ChecksumPair> signatures = rdiff.makeSignatures(sourceIn);
            return toChecksums(signatures);
        } catch (final NoSuchAlgorithmException e) {
            throw RdiffExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(sourceIn);
        }
    }

    @Override
    public List<ChecksumPair> readSignatures(final InputStream signatureIn) throws OXException {
        try {
            return toChecksums(rdiff.readSignatures(signatureIn));
        } catch (final IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(signatureIn);
        }
    }

    @Override
    public void createDeltas(final List<ChecksumPair> sums, final InputStream baseIn, final OutputStream deltaOut) throws OXException {
        try {
            rdiff.makeDeltas(toMetastaticChecksums(sums), baseIn, deltaOut);
        } catch (final NoSuchAlgorithmException e) {
            throw RdiffExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(baseIn);
            Streams.flush(deltaOut);
            Streams.close(deltaOut);
        }
    }

    @Override
    public List<Delta> createDeltas(final List<ChecksumPair> sums, final InputStream baseIn) throws OXException {
        try {
            return toDeltas(rdiff.makeDeltas(toMetastaticChecksums(sums), baseIn));
        } catch (final NoSuchAlgorithmException e) {
            throw RdiffExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(baseIn);
        }
    }

    @Override
    public void writeSignatures(final List<ChecksumPair> sigs, final OutputStream out) throws OXException {
        try {
            rdiff.writeSignatures(toMetastaticChecksums(sigs), out);
        } catch (final IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.flush(out);
            Streams.close(out);
        }
    }

    @Override
    public void rebuildFile(final File baseFile, final InputStream deltaIn, final OutputStream patchOut) throws OXException {
        try {
            rdiff.rebuildFile(baseFile, deltaIn, patchOut);
        } catch (final IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(deltaIn);
            Streams.flush(patchOut);
            Streams.close(patchOut);
        }
    }

    private static List<ChecksumPair> toChecksums(final List<org.metastatic.rsync.ChecksumPair> signatures) {
        final List<ChecksumPair> retList = new ArrayList<ChecksumPair>(signatures.size());
        for (final org.metastatic.rsync.ChecksumPair sig : signatures) {
            retList.add(new ChecksumPairImpl(sig));
        }
        return retList;
    }

    private static List<org.metastatic.rsync.ChecksumPair> toMetastaticChecksums(final List<ChecksumPair> signatures) {
        final List<org.metastatic.rsync.ChecksumPair> retList = new ArrayList<org.metastatic.rsync.ChecksumPair>(signatures.size());
        for (final ChecksumPair sig : signatures) {
            retList.add(new org.metastatic.rsync.ChecksumPair(
                sig.getWeak(),
                sig.getStrong(),
                sig.getOffset(),
                sig.getLength(),
                sig.getSequence()));
        }
        return retList;
    }

    private static List<Delta> toDeltas(final List<org.metastatic.rsync.Delta> deltas) {
        final List<Delta> retList = new ArrayList<Delta>(deltas.size());
        for (final org.metastatic.rsync.Delta d : deltas) {
            retList.add(new DeltaImpl(d));
        }
        return retList;
    }

}
