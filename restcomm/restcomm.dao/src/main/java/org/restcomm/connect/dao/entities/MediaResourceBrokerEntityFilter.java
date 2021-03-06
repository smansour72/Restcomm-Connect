/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.restcomm.connect.dao.entities;

import org.restcomm.connect.commons.annotations.concurrency.Immutable;

/**
 * @author maria.farooq@telestax.com (Maria Farooq)
 */

@Immutable
public class MediaResourceBrokerEntityFilter {

    private final String conferenceSid;
    private String slaveMsId;

    public MediaResourceBrokerEntityFilter(String conferenceSid, String slaveMsId) {
        this.conferenceSid = conferenceSid;
        this.slaveMsId = slaveMsId;
    }

    public String getConferenceSid() {
        return conferenceSid;
    }

    public String getSlaveMsId() {
        return slaveMsId;
    }
}
