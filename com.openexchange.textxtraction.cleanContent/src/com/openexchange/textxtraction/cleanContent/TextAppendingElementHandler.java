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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.textxtraction.cleanContent;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.regex.Pattern;
import net.bitform.api.elements.AddedCellElement;
import net.bitform.api.elements.AddedElement;
import net.bitform.api.elements.AnnotElement;
import net.bitform.api.elements.ArticleThreadElement;
import net.bitform.api.elements.AuthorHistoryElement;
import net.bitform.api.elements.BodyElement;
import net.bitform.api.elements.BooleanCellElement;
import net.bitform.api.elements.BooleanElement;
import net.bitform.api.elements.BooleanPropertyElement;
import net.bitform.api.elements.CellElement;
import net.bitform.api.elements.ChartElement;
import net.bitform.api.elements.CodepagePropertyElement;
import net.bitform.api.elements.ColInfoElement;
import net.bitform.api.elements.CollectionElement;
import net.bitform.api.elements.ContentElement;
import net.bitform.api.elements.ContentRefElement;
import net.bitform.api.elements.DataCellElement;
import net.bitform.api.elements.DataPropertyElement;
import net.bitform.api.elements.DatabaseQueriesElement;
import net.bitform.api.elements.DateElement;
import net.bitform.api.elements.DatePropertyElement;
import net.bitform.api.elements.DeletedCellElement;
import net.bitform.api.elements.DeletedElement;
import net.bitform.api.elements.DocumentCollectionElement;
import net.bitform.api.elements.DurationPropertyElement;
import net.bitform.api.elements.Element;
import net.bitform.api.elements.ElementHandler;
import net.bitform.api.elements.EmbeddedContentElement;
import net.bitform.api.elements.ExceptionElement;
import net.bitform.api.elements.ExportDocumentElement;
import net.bitform.api.elements.ExtremeCellsElement;
import net.bitform.api.elements.FastSaveDataElement;
import net.bitform.api.elements.FingerprintElement;
import net.bitform.api.elements.FloatPropertyElement;
import net.bitform.api.elements.FormFieldElement;
import net.bitform.api.elements.FrameElement;
import net.bitform.api.elements.HeaderFooterElement;
import net.bitform.api.elements.HeaderFooterRefElement;
import net.bitform.api.elements.HiddenElement;
import net.bitform.api.elements.HyperlinkBeginElement;
import net.bitform.api.elements.HyperlinkEndElement;
import net.bitform.api.elements.IntegerElement;
import net.bitform.api.elements.IntegerPropertyElement;
import net.bitform.api.elements.LElement;
import net.bitform.api.elements.LinkedContentElement;
import net.bitform.api.elements.ListPropertyElement;
import net.bitform.api.elements.LocalePropertyElement;
import net.bitform.api.elements.MacrosAndCodeElement;
import net.bitform.api.elements.NoteElement;
import net.bitform.api.elements.NoteRefElement;
import net.bitform.api.elements.ObfuscatedElement;
import net.bitform.api.elements.OfficeXMLPartElement;
import net.bitform.api.elements.OutlineItemElement;
import net.bitform.api.elements.PElement;
import net.bitform.api.elements.PTElement;
import net.bitform.api.elements.PageElement;
import net.bitform.api.elements.PageInfoElement;
import net.bitform.api.elements.PrinterInformationElement;
import net.bitform.api.elements.ProcessingInfoElement;
import net.bitform.api.elements.RevisionsElement;
import net.bitform.api.elements.RootElement;
import net.bitform.api.elements.RoutingSlipElement;
import net.bitform.api.elements.RowElement;
import net.bitform.api.elements.RowInfoElement;
import net.bitform.api.elements.ScenarioElement;
import net.bitform.api.elements.SectionElement;
import net.bitform.api.elements.SecureResultElement;
import net.bitform.api.elements.SheetElement;
import net.bitform.api.elements.SheetNameElement;
import net.bitform.api.elements.SlideBodyElement;
import net.bitform.api.elements.SlideElement;
import net.bitform.api.elements.SlideTitleElement;
import net.bitform.api.elements.StringElement;
import net.bitform.api.elements.StringPropertyElement;
import net.bitform.api.elements.SubContentElement;
import net.bitform.api.elements.TableElement;
import net.bitform.api.elements.TemplateElement;
import net.bitform.api.elements.TextBoxElement;
import net.bitform.api.elements.TextCellElement;
import net.bitform.api.elements.TextElement;
import net.bitform.api.elements.TextPropertyElement;
import net.bitform.api.elements.ThumbnailElement;
import net.bitform.api.elements.TraceElement;
import net.bitform.api.elements.VersionsElement;
import net.bitform.api.elements.WeakProtectionsElement;

/**
 * {@link TextAppendingElementHandler}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TextAppendingElementHandler implements ElementHandler {

    private final StringBuilder sb;

    /**
     * Initializes a new {@link TextAppendingElementHandler.ElementHandlerImpl}.
     */
    public TextAppendingElementHandler() {
        super();
        sb = new StringBuilder(1024);
    }

    private static final Pattern P = Pattern.compile("\\s+");
    
    /**
     * Gets the extracted text
     *
     * @return The text
     */
    public String getText() {
        return P.matcher(sb.toString()).replaceAll(" ");
    }

    /**
     * Resets this handler.
     */
    public void reset() {
        sb.setLength(0);
    }

    @Override
    public void text(final CharBuffer arg0) throws IOException {
        sb.append(' ').append(arg0);
    }

    @Override
    public void startWeakProtections(final WeakProtectionsElement arg0) throws IOException {
        // Nothing to do
    }

    @Override
    public void startVersions(final VersionsElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startTrace(final TraceElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startThumbnail(final ThumbnailElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startTextProperty(final TextPropertyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startTextCell(final TextCellElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startTextBox(final TextBoxElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startText(final TextElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startTemplate(final TemplateElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startTable(final TableElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startSubContent(final SubContentElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startStringProperty(final StringPropertyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startString(final StringElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startSlideTitle(final SlideTitleElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startSlideBody(final SlideBodyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startSlide(final SlideElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startSheetName(final SheetNameElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startSheet(final SheetElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startSecureResult(final SecureResultElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startSection(final SectionElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startScenario(final ScenarioElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startRowInfo(final RowInfoElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startRow(final RowElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startRoutingSlip(final RoutingSlipElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startRoot(final RootElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startRevisions(final RevisionsElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startProcessingInfo(final ProcessingInfoElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startPrinterInformation(final PrinterInformationElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startPageInfo(final PageInfoElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startPage(final PageElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startPT(final PTElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startP(final PElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startOutlineItem(final OutlineItemElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startOfficeXMLPart(final OfficeXMLPartElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startObfuscated(final ObfuscatedElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startNoteRef(final NoteRefElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startNote(final NoteElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startMacrosAndCode(final MacrosAndCodeElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startLocaleProperty(final LocalePropertyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startListProperty(final ListPropertyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startLinkedContent(final LinkedContentElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startL(final LElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startIntegerProperty(final IntegerPropertyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startInteger(final IntegerElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startHyperlinkEnd(final HyperlinkEndElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startHyperlinkBegin(final HyperlinkBeginElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startHidden(final HiddenElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startHeaderFooterRef(final HeaderFooterRefElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startHeaderFooter(final HeaderFooterElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startFrame(final FrameElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startFormField(final FormFieldElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startFloatProperty(final FloatPropertyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startFingerprint(final FingerprintElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startFastSaveData(final FastSaveDataElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startExtremeCells(final ExtremeCellsElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startExportDocument(final ExportDocumentElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startException(final ExceptionElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startEmbeddedContent(final EmbeddedContentElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startDurationProperty(final DurationPropertyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startDocumentCollection(final DocumentCollectionElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startDeletedCell(final DeletedCellElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startDeleted(final DeletedElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startDateProperty(final DatePropertyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startDate(final DateElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startDatabaseQueries(final DatabaseQueriesElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startDataProperty(final DataPropertyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startDataCell(final DataCellElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startContentRef(final ContentRefElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startContent(final ContentElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startCollection(final CollectionElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startColInfo(final ColInfoElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startCodepageProperty(final CodepagePropertyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startChart(final ChartElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startCell(final CellElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startBooleanProperty(final BooleanPropertyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startBooleanCell(final BooleanCellElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startBoolean(final BooleanElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startBody(final BodyElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startAuthorHistory(final AuthorHistoryElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startArticleThread(final ArticleThreadElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startAnnot(final AnnotElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startAddedCell(final AddedCellElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void startAdded(final AddedElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void processExportDocument(final ExportDocumentElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void processEmbeddedContent(final EmbeddedContentElement arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void locator(final long arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public CharBuffer getCharBuffer(final int arg0) {
        // Nothing to do
        return null;
    }

    @Override
    public void endWeakProtections(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endVersions(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endTrace(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endThumbnail(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endTextProperty(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endTextCell(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endTextBox(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endText(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endTemplate(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endTable(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endSubContent(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endStringProperty(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endString(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endSlideTitle(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endSlideBody(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endSlide(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endSheetName(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endSheet(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endSecureResult(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endSection(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endScenario(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endRowInfo(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endRow(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endRoutingSlip(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endRoot(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endRevisions(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endProcessingInfo(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endPrinterInformation(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endPageInfo(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endPage(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endPT(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endP(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endOutlineItem(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endOfficeXMLPart(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endObfuscated(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endNoteRef(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endNote(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endMacrosAndCode(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endLocaleProperty(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endListProperty(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endLinkedContent(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endL(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endIntegerProperty(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endInteger(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endHyperlinkEnd(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endHyperlinkBegin(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endHidden(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endHeaderFooterRef(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endHeaderFooter(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endFrame(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endFormField(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endFloatProperty(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endFingerprint(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endFastSaveData(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endExtremeCells(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endExportDocument(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endException(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endEmbeddedContent(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endDurationProperty(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endDocumentCollection(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endDeletedCell(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endDeleted(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endDateProperty(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endDate(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endDatabaseQueries(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endDataProperty(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endDataCell(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endContentRef(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endContent(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endCollection(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endColInfo(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endCodepageProperty(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endChart(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endCell(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endBooleanProperty(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endBooleanCell(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endBoolean(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endBody(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endAuthorHistory(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endArticleThread(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endAnnot(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endAddedCell(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void endAdded(final Element arg0) throws IOException {
        // Nothing to do

    }

    @Override
    public void close() {
        // Nothing to do

    }
}
