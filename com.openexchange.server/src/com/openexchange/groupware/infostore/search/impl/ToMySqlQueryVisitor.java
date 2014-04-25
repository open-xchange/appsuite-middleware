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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.groupware.infostore.search.impl;

import java.util.List;
import org.owasp.esapi.codecs.MySQLCodec;
import org.owasp.esapi.codecs.MySQLCodec.Mode;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.search.AbstractStringSearchTerm;
import com.openexchange.groupware.infostore.search.AndTerm;
import com.openexchange.groupware.infostore.search.CategoriesTerm;
import com.openexchange.groupware.infostore.search.ColorLabelTerm;
import com.openexchange.groupware.infostore.search.ComparablePattern;
import com.openexchange.groupware.infostore.search.ContentTerm;
import com.openexchange.groupware.infostore.search.CreatedByTerm;
import com.openexchange.groupware.infostore.search.CreatedTerm;
import com.openexchange.groupware.infostore.search.CurrentVersionTerm;
import com.openexchange.groupware.infostore.search.DescriptionTerm;
import com.openexchange.groupware.infostore.search.FileMd5SumTerm;
import com.openexchange.groupware.infostore.search.FileMimeTypeTerm;
import com.openexchange.groupware.infostore.search.FileNameTerm;
import com.openexchange.groupware.infostore.search.FileSizeTerm;
import com.openexchange.groupware.infostore.search.LastModifiedTerm;
import com.openexchange.groupware.infostore.search.LastModifiedUtcTerm;
import com.openexchange.groupware.infostore.search.LockedUntilTerm;
import com.openexchange.groupware.infostore.search.MetaTerm;
import com.openexchange.groupware.infostore.search.ModifiedByTerm;
import com.openexchange.groupware.infostore.search.NotTerm;
import com.openexchange.groupware.infostore.search.NumberOfVersionsTerm;
import com.openexchange.groupware.infostore.search.OrTerm;
import com.openexchange.groupware.infostore.search.SearchTerm;
import com.openexchange.groupware.infostore.search.SearchTermVisitor;
import com.openexchange.groupware.infostore.search.SequenceNumberTerm;
import com.openexchange.groupware.infostore.search.TitleTerm;
import com.openexchange.groupware.infostore.search.UrlTerm;
import com.openexchange.groupware.infostore.search.VersionCommentTerm;
import com.openexchange.groupware.infostore.search.VersionTerm;
import com.openexchange.groupware.infostore.utils.Metadata;

/**
 * {@link ToMySqlQueryVisitor}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class ToMySqlQueryVisitor implements SearchTermVisitor {

    private final StringBuilder sb;

    private final MySQLCodec codec;

    private final Metadata sortedBy;

    private final int dir;

    private final int userId;

    private final int start;

    private final int end;

    private static final String INFOSTORE = "infostore.";

    private static final String DOCUMENT = "infostore_document.";

    private static final String PREFIX = " FROM infostore JOIN infostore_document ON infostore_document.cid = infostore.cid AND infostore_document.infostore_id = infostore.id AND infostore_document.version_number = infostore.version WHERE infostore.cid = ";

    private static final char[] IMMUNE = new char[] { ' ', '%', '_' };
    private static final char[] IMMUNE_WILDCARDS = new char[] { ' ', '%', '_', '\\' };

    /**
     * Initializes a new {@link ToMySqlQueryVisitor}.
     * @param cols
     */
    public ToMySqlQueryVisitor(final int[] allFolderIds, final int[] ownFolderIds, final int contextId, final int userId, final String cols) {
        this(allFolderIds, ownFolderIds, contextId, userId, cols, null, SearchEngineImpl.NOT_SET);
    }

    public ToMySqlQueryVisitor(final int[] allFolderIds, final int[] ownFolderIds, final int contextId, final int userId, final String cols, final int start, final int end) {
        this(allFolderIds, ownFolderIds, contextId, userId, cols, null, SearchEngineImpl.NOT_SET, start, end);
    }

    public ToMySqlQueryVisitor(final int[] allFolderIds, final int[] ownFolderIds, final int contextId, final int userId, final String cols, final Metadata sortedBy, final int dir) {
        this(allFolderIds, ownFolderIds, contextId, userId, cols, sortedBy, dir, -1, -1);
    }

    public ToMySqlQueryVisitor(final int[] allFolderIds, final int[] ownFolderIds, final int contextId, final int userId, final String cols, final Metadata sortedBy, final int dir, final int start, final int end) {
        super();
        this.sb = new StringBuilder(8192);
        this.codec = new MySQLCodec(Mode.STANDARD);
        this.sortedBy = sortedBy;
        this.dir = dir;
        this.userId = userId;
        this.start = start;
        this.end = end;
        sb.append(cols);
        sb.append(PREFIX).append(contextId).append(" AND ");
        appendInString(allFolderIds, ownFolderIds, sb);
    }

    private void appendInString(final int[] allFolderIds, final int[] ownFolderIds, final StringBuilder sb) {
        boolean needOr = false;
        if (null != allFolderIds && 0 < allFolderIds.length) {
            if (null != ownFolderIds && ownFolderIds.length > 0) {
                needOr = true;
                sb.append('(');
            }
            if (1 == allFolderIds.length) {
                sb.append(INFOSTORE).append("folder_id = ").append(allFolderIds[0]);
            } else {
                sb.append(INFOSTORE).append("folder_id IN ");
                sb.append(appendFolders(allFolderIds));
            }
        }
        if (null != ownFolderIds) {
            final int length = ownFolderIds.length;
            if (length > 0) {
                if (needOr) {
                    sb.append(" OR ");
                }
                if (1 == length) {
                    sb.append('(').append(INFOSTORE).append("folder_id = ").append(ownFolderIds[0]);
                    sb.append(" AND ").append(INFOSTORE).append("created_by = ").append(userId).append(')');
                } else {
                    sb.append('(').append(INFOSTORE).append("folder_id IN ");
                    sb.append(appendFolders(ownFolderIds));
                    sb.append(" AND ").append(INFOSTORE).append("created_by = ").append(userId).append(')');
                }
                if (needOr) {
                    sb.append(')');
                }
            }
        }
        sb.append(" AND ");
    }

    private String appendFolders(final int[] folders) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(folders[0]);
        for (int i = 1; i < folders.length; i++) {
            sb.append(',').append(folders[i]);
        }
        sb.append(')');
        return sb.toString();
    }

    public String getMySqlQuery() {
        if (null != sortedBy && dir != SearchEngineImpl.NOT_SET) {
            sb.append(" ORDER BY ").append(sortedBy.getName());
            if (dir == SearchEngineImpl.ASC) {
                sb.append(" ASC");
            } else if (dir == SearchEngineImpl.DESC) {
                sb.append(" DESC");
            }
        }
        if (start > -1 && end > -1 && start < end) {
            sb.append(" LIMIT ").append(start).append(",").append(end);
        }
        return sb.toString();
    }

    @Override
    public void visit(AndTerm andTerm) throws OXException {
        final List<SearchTerm<?>> terms = andTerm.getPattern();
        final int size = terms.size();

        // Empty?
        if (size <= 0) {
            return;
        }

        // Only one term?
        if (1 == size) {
            terms.get(0).visit(this);
            return;
        }

        // More than 1 term
        sb.append('(');
        terms.get(0).visit(this);
        for (int i = 1; i < size; i++) {
            sb.append("AND ");
            terms.get(i).visit(this);
        }
        sb.append(')');
    }

    @Override
    public void visit(OrTerm orTerm) throws OXException {
        final List<SearchTerm<?>> terms = orTerm.getPattern();
        final int size = terms.size();

        // Empty?
        if (size <= 0) {
            return;
        }

        // Only one term?
        if (1 == size) {
            terms.get(0).visit(this);
            return;
        }

        // More than 1 term
        sb.append('(');
        terms.get(0).visit(this);
        for (int i = 1; i < size; i++) {
            sb.append("OR ");
            terms.get(i).visit(this);
        }
        sb.append(')');
    }

    @Override
    public void visit(NotTerm notTerm) throws OXException {
        sb.append("IS NOT ");
        notTerm.getPattern().visit(this);
    }

    @Override
    public void visit(MetaTerm metaTerm) {
        // not supported
    }

    @Override
    public void visit(NumberOfVersionsTerm numberOfVersionsTerm) {
        String comp = getComparionType(numberOfVersionsTerm.getPattern());
        sb.append(INFOSTORE).append("version").append(comp).append("MAX(").append(numberOfVersionsTerm.getPattern().getPattern()).append(
            ") ");
    }

    @Override
    public void visit(LastModifiedUtcTerm lastModifiedUtcTerm) {
        String comp = getComparionType(lastModifiedUtcTerm.getPattern());
        sb.append(INFOSTORE).append("last_modified").append(comp).append(lastModifiedUtcTerm.getPattern().getPattern().getTime()).append(
            " ");
    }

    @Override
    public void visit(ColorLabelTerm colorLabelTerm) {
        String comp = getComparionType(colorLabelTerm.getPattern());
        sb.append(INFOSTORE).append("color_label").append(comp).append(colorLabelTerm.getPattern().getPattern()).append(" ");
    }

    @Override
    public void visit(CurrentVersionTerm currentVersionTerm) {
        sb.append(INFOSTORE).append("version = ").append(DOCUMENT).append("version_number").append(" ");
    }

    @Override
    public void visit(VersionCommentTerm versionCommentTerm) {
        String field = "file_version_comment";
        parseStringSearchTerm(versionCommentTerm, field);
    }

    @Override
    public void visit(FileMd5SumTerm fileMd5SumTerm) {
        String field = "file_md5sum";
        parseStringSearchTerm(fileMd5SumTerm, field);
    }

    @Override
    public void visit(LockedUntilTerm lockedUntilTerm) {
        String comp = getComparionType(lockedUntilTerm.getPattern());
        sb.append(INFOSTORE).append("locked_until").append(comp).append(lockedUntilTerm.getPattern().getPattern().getTime()).append(" ");
    }

    @Override
    public void visit(CategoriesTerm categoriesTerm) {
        String field = "categories";
        parseStringSearchTerm(categoriesTerm, field);
    }

    @Override
    public void visit(SequenceNumberTerm sequenceNumberTerm) {
        // not supported
    }

    @Override
    public void visit(FileMimeTypeTerm fileMimeTypeTerm) {
        String field = "file_mimetype";
        parseStringSearchTerm(fileMimeTypeTerm, field);
    }

    @Override
    public void visit(FileNameTerm fileNameTerm) {
        String field = "filename";
        parseStringSearchTerm(fileNameTerm, field);
    }

    @Override
    public void visit(LastModifiedTerm lastModifiedTerm) {
        String comp = getComparionType(lastModifiedTerm.getPattern());
        sb.append(INFOSTORE).append("last_modified").append(comp).append(lastModifiedTerm.getPattern().getPattern().getTime()).append(" ");
    }

    @Override
    public void visit(CreatedTerm createdTerm) {
        String comp = getComparionType(createdTerm.getPattern());
        sb.append(INFOSTORE).append("creating_date").append(comp).append(createdTerm.getPattern().getPattern().getTime()).append(" ");
    }

    @Override
    public void visit(ModifiedByTerm modifiedByTerm) {
        String comp = getComparionType(modifiedByTerm.getPattern());
        sb.append(INFOSTORE).append("changed_by =").append(comp).append(modifiedByTerm.getPattern().getPattern()).append(" ");
    }

    @Override
    public void visit(TitleTerm titleTerm) {
        String field = "title";
        parseStringSearchTerm(titleTerm, field);
    }

    @Override
    public void visit(VersionTerm versionTerm) {
        sb.append(DOCUMENT).append("version_number = ").append(versionTerm.getPattern()).append(" ");
    }

    @Override
    public void visit(ContentTerm contentTerm) {
        // not supported
    }

    @Override
    public void visit(FileSizeTerm fileSizeTerm) {
        String comp = getComparionType(fileSizeTerm.getPattern());
        sb.append(DOCUMENT).append("file_size").append(comp).append(fileSizeTerm.getPattern().getPattern()).append(" ");
    }

    @Override
    public void visit(DescriptionTerm descriptionTerm) {
        String field = "description";
        parseStringSearchTerm(descriptionTerm, field);
    }

    @Override
    public void visit(UrlTerm urlTerm) {
        String field = "url";
        parseStringSearchTerm(urlTerm, field);
    }

    @Override
    public void visit(CreatedByTerm createdByTerm) {
        String comp = getComparionType(createdByTerm.getPattern());
        sb.append(INFOSTORE).append("created_by").append(comp).append(createdByTerm.getPattern().getPattern()).append(" ");
    }

    private <T> String getComparionType(ComparablePattern<T> pattern) {
        String comp;
        switch (pattern.getComparisonType()) {
        case LESS_THAN:
            comp = "<";
            break;
        case GREATER_THAN:
            comp = ">";
            break;
        case EQUALS:
            comp = "=";
            break;
        default:
            comp = "";
        }
        return comp;
    }

    private boolean hasWildCards(final String pattern) {
        return pattern.indexOf('*') >= 0 || pattern.indexOf('?') >= 0;
    }

    private String replaceWildcards(final String pattern) {
        final int length = pattern.length();
        final StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            final char c = pattern.charAt(i);
            switch (c) {
            case '*':
                sb.append('%');
                break;
            case '?':
                sb.append('_');
                break;
            case '%':
                sb.append("\\%");
                break;
            case '_':
                sb.append("\\_");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            default:
                sb.append(c);
                break;
            }
        }

        return sb.toString();
    }

    private void parseStringSearchTerm(AbstractStringSearchTerm searchTerm, String field) {
        String pattern = searchTerm.getPattern();
        final boolean useLike = (searchTerm.isSubstringSearch() || hasWildCards(pattern));

        // Encode pattern
        String fieldName = new StringBuilder(DOCUMENT).append(field).toString();
        if (useLike) {
            pattern = codec.encode(IMMUNE_WILDCARDS, replaceWildcards(pattern));
            if (searchTerm.isSubstringSearch()) {
                final StringBuilder tmp = new StringBuilder(pattern.length() + 8);
                if (!pattern.startsWith("%")) {
                    tmp.append('%');
                }
                tmp.append(pattern);
                if (!pattern.endsWith("%")) {
                    tmp.append('%');
                }
                pattern = tmp.toString();
            }
        } else {
            pattern = codec.encode(IMMUNE, pattern);
        }

        // Check for case-insensitive search
        if (searchTerm.isIgnoreCase()) {
            fieldName = new StringBuilder("UPPER(").append(fieldName).append(')').toString();
            pattern = new StringBuilder("UPPER('").append(pattern).append("')").toString();
        } else {
            // Surround with single quotes
            pattern = new StringBuilder(pattern.length() + 2).append('\'').append(pattern).append('\'').toString();
        }

        // Append to query builder
        sb.append(fieldName);
        if (useLike) {
            sb.append(" LIKE ").append(pattern);
        } else {
            sb.append(" = ").append(pattern);
        }
    }

}
