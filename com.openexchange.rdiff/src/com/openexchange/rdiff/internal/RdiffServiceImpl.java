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
        } catch (NoSuchAlgorithmException e) {
            throw RdiffExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (IOException e) {
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
            if (signatures == null) {
                throw RdiffExceptionCodes.ERROR.create("Stream is empty.");
            }
            return toChecksums(signatures);
        } catch (NoSuchAlgorithmException e) {
            throw RdiffExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(sourceIn);
        }
    }

    @Override
    public List<ChecksumPair> readSignatures(final InputStream signatureIn) throws OXException {
        try {
            return toChecksums(rdiff.readSignatures(signatureIn));
        } catch (IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(signatureIn);
        }
    }

    @Override
    public void createDeltas(final List<ChecksumPair> sums, final InputStream baseIn, final OutputStream deltaOut) throws OXException {
        try {
            rdiff.makeDeltas(toMetastaticChecksums(sums), baseIn, deltaOut);
        } catch (NoSuchAlgorithmException e) {
            throw RdiffExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (IOException e) {
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
        } catch (NoSuchAlgorithmException e) {
            throw RdiffExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(baseIn);
        }
    }

    @Override
    public void writeSignatures(final List<ChecksumPair> sigs, final OutputStream out) throws OXException {
        try {
            rdiff.writeSignatures(toMetastaticChecksums(sigs), out);
        } catch (IOException e) {
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
            patchOut.flush();
        } catch (IOException e) {
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
