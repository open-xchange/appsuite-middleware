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

package com.openexchange.multifactor.provider.totp;

import static com.openexchange.java.Autoboxing.I;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;

/**
 * {@link QRCodeGenerator} generates a QR code containing a specific string / URL.
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class QRCodeGenerator {

    private static final String FORMAT_PNG = "PNG";
    private final QRCodeWriter qrwrite = new QRCodeWriter();

    /**
     * Generates a QR code including the given string/URL
     *
     * @param url The string to include into the code
     * @return The QR code as byte representation
     * @throws OXException if the given URL exceeded the max. allowed URL length
     */
    public byte[] generate(String url) throws OXException {
        try {
            final Map<EncodeHintType, Object> encodingHints = new HashMap<>();
            encodingHints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            encodingHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            encodingHints.put(EncodeHintType.MARGIN, I(4));
            final BitMatrix bitMatrix = qrwrite.encode(url, BarcodeFormat.QR_CODE, 300, 300, encodingHints);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, FORMAT_PNG, out);
            return out.toByteArray();
        } catch (IOException | WriterException e) {
            throw MultifactorExceptionCodes.UNKNOWN_ERROR.create(e);
        }
    }
}
