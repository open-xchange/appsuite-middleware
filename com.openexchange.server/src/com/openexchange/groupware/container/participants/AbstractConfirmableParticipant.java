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

package com.openexchange.groupware.container.participants;

/**
 * {@link AbstractConfirmableParticipant}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class AbstractConfirmableParticipant implements ConfirmableParticipant {

    private static final long serialVersionUID = 2232819928880304722L;

    private ConfirmStatus status = ConfirmStatus.NONE;

    private boolean bStatus = false;

    private String message;

    private boolean bMessage = false;

    protected AbstractConfirmableParticipant() {
        super();
    }

    protected AbstractConfirmableParticipant(ConfirmableParticipant copy) {
        this();
        status = copy.getStatus();
        bStatus = copy.containsStatus();
        message = copy.getMessage();
        bMessage = copy.containsMessage();
    }

    @Override
    public final boolean containsStatus() {
        return bStatus;
    }

    @Override
    public final int getConfirm() {
        return status.getId();
    }

    @Override
    public final String getMessage() {
        return message;
    }

    @Override
    public final ConfirmStatus getStatus() {
        return status;
    }

    @Override
    public final void setConfirm(int confirm) {
        status = ConfirmStatus.byId(confirm);
        bStatus = true;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
        bMessage = true;
    }

    @Override
    public void setStatus(ConfirmStatus status) {
        this.status = status;
        bStatus = true;
    }

    @Override
    public boolean containsMessage() {
        return bMessage;
    }

    @Override
    public AbstractConfirmableParticipant clone() throws CloneNotSupportedException {
        AbstractConfirmableParticipant retval = (AbstractConfirmableParticipant) super.clone();

        retval.bMessage = bMessage;
        retval.bStatus = bStatus;
        retval.message = message;
        retval.status = status;

        return retval;
    }
}
