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

package com.openexchange.groupware.infostore.media;

import java.io.InputStream;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * {@link EstimationResult} - Represents the result of the effort estimation for extracting media metadata from an input stream.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class EstimationResult {

    /** The constant for high effort w/o optional arguments */
    private static final EstimationResult HIGH_EFFORT = new EstimationResult(Effort.HIGH_EFFORT, null, null, null);

    /** The constant for not applicable */
    private static final EstimationResult NOT_APPLICABLE = new EstimationResult(Effort.NOT_APPLICABLE, null, null, null);

    /**
     * Gets the high effort result for given arguments.
     *
     * @param optArguments Optional additional arguments
     * @return The high effort result
     */
    public static EstimationResult highEffortFor(Map<String, Object> optArguments) {
        return null == optArguments ? HIGH_EFFORT : new EstimationResult(Effort.HIGH_EFFORT, null, null, optArguments);
    }

    /**
     * Gets the not-applicable result.
     *
     * @return The not-applicable result
     */
    public static EstimationResult notApplicable() {
        return NOT_APPLICABLE;
    }

    /**
     * Creates a low effort result for given arguments.
     *
     * @param documentData The optional input stream, which is supposed to be re-used
     * @param optArguments Optional additional arguments
     * @param extractor The associated extractor
     * @return The fast result
     */
    public static EstimationResult lowEffortFor(InputStream documentData, MediaMetadataExtractor extractor, Map<String, Object> optArguments) {
        return new EstimationResult(Effort.LOW_EFFORT, documentData, extractor, optArguments);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private final Effort effort;
    private final MediaMetadataExtractor extractor;
    private final InputStream documentData;
    private final Map<String, Object> optArguments;

    /**
     * Initializes a new {@link EstimationResult}.
     *
     * @param effort The estimated effort
     * @param documentData The document's input stream, which is supposed to be re-used
     * @param extractor The associated extractor
     * @param optArguments Optional additional arguments
     */
    private EstimationResult(Effort effort, InputStream documentData, MediaMetadataExtractor extractor, Map<String, Object> optArguments) {
        super();
        this.effort = effort;
        this.documentData = documentData;
        this.extractor = extractor;
        this.optArguments = null == optArguments ? null : ImmutableMap.copyOf(optArguments);
    }

    /**
     * Checks if estimated effort is considered to be low.
     *
     * @return <code>true</code> for low effort; otherwise <code>false</code>
     */
    public boolean isLowEffort() {
        return Effort.LOW_EFFORT == effort;
    }

    /**
     * Checks if provided data could not be handled at all.
     *
     * @return <code>true</code> for not applicable; otherwise <code>false</code>
     */
    public boolean isNotApplicable() {
        return Effort.NOT_APPLICABLE == effort;
    }

    /**
     * Gets the estimated effort
     *
     * @return The estimated effort
     */
    public Effort getEffort() {
        return effort;
    }

    /**
     * Gets the extractor
     *
     * @return The extractor or <code>null</code>
     */
    public MediaMetadataExtractor getExtractor() {
        return extractor;
    }

    /**
     * Gets the document's input stream, which is supposed to be re-used
     *
     * @return The document's input stream
     */
    public InputStream getDocumentData() {
        return documentData;
    }

    /**
     * Gets the optional arguments
     *
     * @return The (immutable) arguments or <code>null</code>
     */
    public Map<String, Object> getOptionalArguments() {
        return optArguments;
    }
}
