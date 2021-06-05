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

package com.openexchange.file.storage.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.search.AndTerm;
import com.openexchange.file.storage.search.CameraApertureTerm;
import com.openexchange.file.storage.search.CameraExposureTimeTerm;
import com.openexchange.file.storage.search.CameraFocalLengthTerm;
import com.openexchange.file.storage.search.CameraIsoSpeedTerm;
import com.openexchange.file.storage.search.CameraMakeTerm;
import com.openexchange.file.storage.search.CameraModelTerm;
import com.openexchange.file.storage.search.CaptureDateTerm;
import com.openexchange.file.storage.search.CategoriesTerm;
import com.openexchange.file.storage.search.ColorLabelTerm;
import com.openexchange.file.storage.search.ComparisonType;
import com.openexchange.file.storage.search.ContentTerm;
import com.openexchange.file.storage.search.CreatedByTerm;
import com.openexchange.file.storage.search.CreatedTerm;
import com.openexchange.file.storage.search.CurrentVersionTerm;
import com.openexchange.file.storage.search.DescriptionTerm;
import com.openexchange.file.storage.search.FileMd5SumTerm;
import com.openexchange.file.storage.search.FileMimeTypeTerm;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.FileSizeTerm;
import com.openexchange.file.storage.search.HeightTerm;
import com.openexchange.file.storage.search.LastModifiedTerm;
import com.openexchange.file.storage.search.LastModifiedUtcTerm;
import com.openexchange.file.storage.search.LockedUntilTerm;
import com.openexchange.file.storage.search.MediaDateTerm;
import com.openexchange.file.storage.search.MetaTerm;
import com.openexchange.file.storage.search.ModifiedByTerm;
import com.openexchange.file.storage.search.NumberOfVersionsTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.SequenceNumberTerm;
import com.openexchange.file.storage.search.TitleTerm;
import com.openexchange.file.storage.search.UrlTerm;
import com.openexchange.file.storage.search.VersionCommentTerm;
import com.openexchange.file.storage.search.VersionTerm;
import com.openexchange.file.storage.search.WidthTerm;

/**
 * {@link FileSearchTermParserTest}
 *
 * @author <a href="mailto:alexander.schulze-ardey@open-xchange.com">Alexander Schulze-Ardey</a>
 * @since v7.10.5
 */
public class FileSearchTermParserTest {

    /**
     * Test correct conversion of complex filters into SearchTerms.
     *
     * @throws JSONException
     * @throws OXException
     */
    @Test
    public void testParseComplexTerm() throws JSONException, OXException {
        final JSONArray jsonArray = new JSONArray("[ 'or', [ 'and',  [ '=' , { 'field' : 'filename' }, 'stuff'], [ '<' , { 'field' : 'file_size' }, '100']],[ '=' , { 'field' : 'filename' }, 'changelog']]");

        com.openexchange.search.SearchTerm<?> term = SearchTermParser.parse(jsonArray);
        SearchTerm<?> result = FileSearchTermParser.parseSearchTerm(term);

        assertNotNull(result);
        assertTrue(result instanceof OrTerm);

        OrTerm orTerm = (OrTerm) result;
        FileNameTerm orFileNameTerm = (FileNameTerm) orTerm.getPattern().get(1);
        assertEquals(orFileNameTerm.getPattern(), "changelog");

        AndTerm orAndTerm = (AndTerm) orTerm.getPattern().get(0);
        FileNameTerm orAndFileNameTerm = (FileNameTerm) orAndTerm.getPattern().get(0);
        assertEquals(orAndFileNameTerm.getPattern(), "stuff");

        FileSizeTerm orAndFileSizeTerm = (FileSizeTerm) orAndTerm.getPattern().get(1);
        assertTrue(orAndFileSizeTerm.getPattern().getComparisonType().equals(ComparisonType.LESS_THAN));
        assertTrue(orAndFileSizeTerm.getPattern().getPattern().longValue() == 100);
    }

    /**
     * Ensures all filterable Fields are recognized correctly and converted into the right terms.
     *
     * @throws JSONException
     * @throws OXException
     */
    @Test
    public void testEnsureConversionTerm() throws JSONException, OXException {

        // @formatter:off
        final JSONArray jsonArray = new JSONArray("[ 'or', " + 
            "[ '=' , { 'field' : '" + File.Field.CAMERA_APERTURE.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.CAMERA_EXPOSURE_TIME.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.CAMERA_FOCAL_LENGTH.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.CAMERA_ISO_SPEED.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.CAMERA_MAKE.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.CAMERA_MODEL.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.CAPTURE_DATE.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.CATEGORIES.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.COLOR_LABEL.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.CONTENT.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.CREATED.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.CREATED_BY.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.CURRENT_VERSION.getName() + "' }, 'true']," +
            "[ '=' , { 'field' : '" + File.Field.DESCRIPTION.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.FILENAME.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.FILE_MD5SUM.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.FILE_MIMETYPE.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.FILE_SIZE.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.HEIGHT.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.LAST_MODIFIED.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.LAST_MODIFIED_UTC.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.LOCKED_UNTIL.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.MEDIA_DATE.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.META.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.MODIFIED_BY.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.NUMBER_OF_VERSIONS.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.SEQUENCE_NUMBER.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.TITLE.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.URL.getName() + "' }, 'https://www.open-xchange.com']," +
            "[ '=' , { 'field' : '" + File.Field.VERSION.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.VERSION_COMMENT.getName() + "' }, '100']," +
            "[ '=' , { 'field' : '" + File.Field.WIDTH.getName() + "' }, '100']" +
            "]");
        // @formatter:on

        com.openexchange.search.SearchTerm<?> term = SearchTermParser.parse(jsonArray);
        SearchTerm<?> result = FileSearchTermParser.parseSearchTerm(term);

        assertNotNull(result);
        assertTrue(result instanceof OrTerm);
        assertEquals(((OrTerm) result).getPattern().size(), 32);

        OrTerm orTerm = (OrTerm) result;

        assertTrue(orTerm.getPattern().get(0) instanceof CameraApertureTerm);
        assertTrue(orTerm.getPattern().get(1) instanceof CameraExposureTimeTerm);
        assertTrue(orTerm.getPattern().get(2) instanceof CameraFocalLengthTerm);
        assertTrue(orTerm.getPattern().get(3) instanceof CameraIsoSpeedTerm);
        assertTrue(orTerm.getPattern().get(4) instanceof CameraMakeTerm);
        assertTrue(orTerm.getPattern().get(5) instanceof CameraModelTerm);
        assertTrue(orTerm.getPattern().get(6) instanceof CaptureDateTerm);
        assertTrue(orTerm.getPattern().get(7) instanceof CategoriesTerm);
        assertTrue(orTerm.getPattern().get(8) instanceof ColorLabelTerm);
        assertTrue(orTerm.getPattern().get(9) instanceof ContentTerm);
        assertTrue(orTerm.getPattern().get(10) instanceof CreatedTerm);
        assertTrue(orTerm.getPattern().get(11) instanceof CreatedByTerm);
        assertTrue(orTerm.getPattern().get(12) instanceof CurrentVersionTerm);
        assertTrue(orTerm.getPattern().get(13) instanceof DescriptionTerm);
        assertTrue(orTerm.getPattern().get(14) instanceof FileNameTerm);
        assertTrue(orTerm.getPattern().get(15) instanceof FileMd5SumTerm);
        assertTrue(orTerm.getPattern().get(16) instanceof FileMimeTypeTerm);
        assertTrue(orTerm.getPattern().get(17) instanceof FileSizeTerm);
        assertTrue(orTerm.getPattern().get(18) instanceof HeightTerm);
        assertTrue(orTerm.getPattern().get(19) instanceof LastModifiedTerm);
        assertTrue(orTerm.getPattern().get(20) instanceof LastModifiedUtcTerm);
        assertTrue(orTerm.getPattern().get(21) instanceof LockedUntilTerm);
        assertTrue(orTerm.getPattern().get(22) instanceof MediaDateTerm);
        assertTrue(orTerm.getPattern().get(23) instanceof MetaTerm);
        assertTrue(orTerm.getPattern().get(24) instanceof ModifiedByTerm);
        assertTrue(orTerm.getPattern().get(25) instanceof NumberOfVersionsTerm);
        assertTrue(orTerm.getPattern().get(26) instanceof SequenceNumberTerm);
        assertTrue(orTerm.getPattern().get(27) instanceof TitleTerm);
        assertTrue(orTerm.getPattern().get(28) instanceof UrlTerm);
        assertTrue(orTerm.getPattern().get(29) instanceof VersionTerm);
        assertTrue(orTerm.getPattern().get(30) instanceof VersionCommentTerm);
        assertTrue(orTerm.getPattern().get(31) instanceof WidthTerm);
    }
}
