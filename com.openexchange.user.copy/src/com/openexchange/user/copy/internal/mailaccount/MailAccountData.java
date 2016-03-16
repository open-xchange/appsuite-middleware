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


package com.openexchange.user.copy.internal.mailaccount;

/**
 * {@link MailAccountData}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class MailAccountData extends GenericAccountData {
    private String spamHandler;

    private String trash;
    private String sent;
    private String drafts;
    private String spam;
    private String confirmedSpam;
    private String confirmedHam;

    private String trashFullname;
    private String sentFullname;
    private String draftsFullname;
    private String spamFullname;
    private String confirmedSpamFullname;
    private String confirmedHamFullname;

    public MailAccountData() {
        super();
    }

    public String getSpamHandler() {
        return spamHandler;
    }

    public void setSpamHandler(final String spamHandler) {
        this.spamHandler = spamHandler;
    }

    public String getTrash() {
        return trash;
    }

    public void setTrash(final String trash) {
        this.trash = trash;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(final String sent) {
        this.sent = sent;
    }

    public String getDrafts() {
        return drafts;
    }

    public void setDrafts(final String drafts) {
        this.drafts = drafts;
    }

    public String getSpam() {
        return spam;
    }

    public void setSpam(final String spam) {
        this.spam = spam;
    }

    public String getConfirmedSpam() {
        return confirmedSpam;
    }

    public void setConfirmedSpam(final String confirmedSpam) {
        this.confirmedSpam = confirmedSpam;
    }

    public String getConfirmedHam() {
        return confirmedHam;
    }

    public void setConfirmedHam(final String confirmedHam) {
        this.confirmedHam = confirmedHam;
    }

    public String getTrashFullname() {
        return trashFullname;
    }

    public void setTrashFullname(final String trashFullname) {
        this.trashFullname = trashFullname;
    }

    public String getSentFullname() {
        return sentFullname;
    }

    public void setSentFullname(final String sentFullname) {
        this.sentFullname = sentFullname;
    }

    public String getDraftsFullname() {
        return draftsFullname;
    }

    public void setDraftsFullname(final String draftsFullname) {
        this.draftsFullname = draftsFullname;
    }

    public String getSpamFullname() {
        return spamFullname;
    }

    public void setSpamFullname(final String spamFullname) {
        this.spamFullname = spamFullname;
    }

    public String getConfirmedSpamFullname() {
        return confirmedSpamFullname;
    }

    public void setConfirmedSpamFullname(final String confirmedSpamFullname) {
        this.confirmedSpamFullname = confirmedSpamFullname;
    }

    public String getConfirmedHamFullname() {
        return confirmedHamFullname;
    }

    public void setConfirmedHamFullname(final String confirmedHamFullname) {
        this.confirmedHamFullname = confirmedHamFullname;
    }

}
