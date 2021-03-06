/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.emm.core.mailing.bean.ComFollowUpStats;

public interface ComFollowUpStatsService {
	public int getStats(int mailingID, int baseMail, @VelocityCheck int companyID, boolean useTargetGroups) throws Exception;	
	// returns the resultID
	public int startCalculation(int followupID, int baseMailID, @VelocityCheck int companyID, String sessionID, boolean useTargetGroups);
	// this method returns the calculated value from the database, if available, otherwise 0.
	public String checkStats(int followUpID, String sessionID);
	public ComFollowUpStats getStatEntry(int followUpID, String sessionID);
}
