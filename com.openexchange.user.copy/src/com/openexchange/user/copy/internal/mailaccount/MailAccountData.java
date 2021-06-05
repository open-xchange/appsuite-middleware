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
