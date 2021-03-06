/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.agnitas.reporting.birt.external.beans.LightMailingList;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

public class RecipientsDetailedStatisticDataSet extends RecipientsBasedDataSet {
    private static final transient Logger logger = Logger.getLogger(RecipientsDetailedStatisticDataSet.class);

    private List<RecipientsDetailedStatisticsRow> statList = new ArrayList<>();
    private List<RecipientsDetailedStatisticsRow> dynamicStatList = new ArrayList<>();

    /**
     * Get Data for Recipient Report
     * message key "report.recipient.statistics.recipientDevelopmentDetailed.label"
     * en: "Recipient development detailed (Opt-ins, Opt-outs, Bounces)"
     * de: "Empfängerentwicklung detailliert (Anmeldungen, Abmeldungen, Bounces)"
     */
    public void initRecipientsStatistic(@VelocityCheck int companyId, String selectedMailingLists, String selectedTargetsAsString, String startDate, String stopDate) throws Exception {
    	List<Integer> mailingListIds = new ArrayList<>();
		for (String mailingListIdString : selectedMailingLists.split(",")) {
			mailingListIds.add(NumberUtils.toInt(mailingListIdString));
		}
		
        Date dateStart = RECIPIENT_DATE_FORMAT.parse(startDate);
        Date dateStop = RECIPIENT_DATE_FORMAT.parse(stopDate);

        int mailinglistIndex = 0;
        for (LightMailingList mailinglist : getMailingLists(mailingListIds, companyId)) {
			int mailinglistID = mailinglist.getMailingListId();
			mailinglistIndex++;

			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
			insertStatistic(companyId, mailinglistID, mailinglistIndex, null, targetGroupIndex, dateStart, dateStop);
   
			for (LightTarget target : getTargets(selectedTargetsAsString, companyId)) {
            	targetGroupIndex++;
            	insertStatistic(companyId, mailinglistID, mailinglistIndex, target, targetGroupIndex, dateStart, dateStop);
            }
        }
    }
    
	/**
     * Get Data for Recipient Report
     * message key : report.recipient.statistics.recipientDevelopmentNet.label
     * en: "Net recipient development (progress of active recipients)"
     * de: "Empfängerentwicklung netto (Verlauf der aktiven Empfänger)"
     */
    public void initRecipientsDynamicStatistic(@VelocityCheck int companyId, String selectedMailingLists, String selectedTargetsAsString, String startDate, String stopDate) throws Exception {
    	List<Integer> mailingListIds = new ArrayList<>();
		for (String mailingListIdString : selectedMailingLists.split(",")) {
			mailingListIds.add(NumberUtils.toInt(mailingListIdString));
		}
		
        Date dateStart = RECIPIENT_DATE_FORMAT.parse(startDate);
        Date dateStop = RECIPIENT_DATE_FORMAT.parse(stopDate);
        
		int mailinglistIndex = 0;
        for (LightMailingList mailinglist : getMailingLists(mailingListIds, companyId)) {
        	int mailinglistID = mailinglist.getMailingListId();
			mailinglistIndex++;
	
			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
			insertDynamicStatistic(companyId, mailinglistID, mailinglistIndex, null, targetGroupIndex, dateStart, dateStop);
			
            for (LightTarget target : getTargets(selectedTargetsAsString, companyId)) {
            	targetGroupIndex++;
            	insertDynamicStatistic(companyId, mailinglistID, mailinglistIndex, target, targetGroupIndex, dateStart, dateStop);
            }
        }
    }
	
	private void insertDynamicStatistic(int companyId, int mailinglistID, int mailinglistIndex, LightTarget target, int targetGroupIndex, Date dateStart, Date dateStop) throws Exception {
    	String mailinglistName = getMailinglistName(companyId, mailinglistID);
		RecipientsDetailedStatisticsRow currentAmounts = getRecipientDetailedStatAmountsBeforeDate(companyId, mailinglistID, mailinglistName, mailinglistIndex, target, targetGroupIndex, dateStart);
		List<RecipientsDetailedStatisticsRow> data = getRecipientDetailedStat(companyId, mailinglistID, mailinglistName, mailinglistIndex, target, targetGroupIndex, dateStart, dateStop);
		// Sum up data for absolute numbers per date
		for (RecipientsDetailedStatisticsRow row : data) {
			currentAmounts.countActive += row.countActive;
			currentAmounts.countBounced += row.countBounced;
			currentAmounts.countOptout += row.countOptout;
			currentAmounts.countBlacklisted += row.countBlacklisted;
			row.countActive = currentAmounts.countActive;
			row.countBounced = currentAmounts.countBounced;
			row.countOptout = currentAmounts.countOptout;
			row.countBlacklisted = currentAmounts.countBlacklisted;
		}
		
		dynamicStatList.addAll(data);
	}
	
	private void insertStatistic(int companyId, int mailinglistID, int mailinglistIndex, LightTarget target, int targetGroupIndex, Date dateStart, Date dateStop) throws Exception {
		String mailinglistName = getMailinglistName(companyId, mailinglistID);
    	statList.addAll(getRecipientDetailedStat(companyId, mailinglistID, mailinglistName, mailinglistIndex, target, targetGroupIndex, dateStart, dateStop));
	}
	
	public List<RecipientsDetailedStatisticsRow> getStatistic() {
        return statList;
    }

    public List<RecipientsDetailedStatisticsRow> getDynamicStatistic() {
        return dynamicStatList;
    }

    private List<RecipientsDetailedStatisticsRow> getRecipientDetailedStat(@VelocityCheck int companyId, int mailinglistId, String mailinglistName, int mailinglistIndex, LightTarget target, int targetGroupIndex, Date dateStart, Date dateStop) throws Exception {
    	try {
    		TreeMap<String, RecipientsDetailedStatisticsRow> dataMap = new TreeMap<>();
    		target = getDefaultTarget(target);
	        
	        // Create a RecipientsDetailedStatisticsRow entry for each day within the given time period
	        GregorianCalendar nextDate = new GregorianCalendar();
	        nextDate.setTime(dateStart);
	        while (nextDate.getTime().getTime() <= dateStop.getTime()) {
	        	RecipientsDetailedStatisticsRow nextRecipientsDetailedStatisticsRow =
						new RecipientsDetailedStatisticsRow(nextDate.getTime(),
								mailinglistId, mailinglistName, mailinglistIndex,
								target.getId(), target.getName(), targetGroupIndex);
	        	dataMap.put(RECIPIENT_DATE_FORMAT.format(nextDate.getTime()), nextRecipientsDetailedStatisticsRow);
	        	nextDate.add(Calendar.DAY_OF_MONTH, 1);
	        }
	        
	        String dateTruncFunction = isOracleDB() ? "TRUNC" : "DATE";
	        
	        String sql = "SELECT " + dateTruncFunction + "(bind.timestamp) changedate, user_status, COUNT(*) amount " +
					" FROM " + getCustomerBindingTableName(companyId) + " bind";
	        
	        if (StringUtils.isNotBlank(target.getTargetSQL())) {
	        	sql += ", customer_" + companyId + "_tbl cust";
	        }
	        sql += " WHERE bind.mailinglist_id = ?";
	        sql += " AND " + dateTruncFunction + "(bind.timestamp) >= ?";
	        sql += " AND " + dateTruncFunction + "(bind.timestamp) <= ?";
	        
	        if (StringUtils.isNotBlank(target.getTargetSQL())) {
	        	sql += " AND bind.customer_id = cust.customer_id AND (" + target.getTargetSQL() + ")";
	        }
	        
	        sql += " GROUP BY " + dateTruncFunction + "(bind.timestamp), user_status";
	        sql += " ORDER BY " + dateTruncFunction + "(bind.timestamp), user_status";
	        
	        List<Map<String, Object>> result = select(logger, sql, mailinglistId, dateStart, dateStop);
	        for (Map<String, Object> resultRow : result) {
	            Date entryDate = (Date) resultRow.get("changedate");
	            RecipientsDetailedStatisticsRow row = dataMap.get(RECIPIENT_DATE_FORMAT.format(entryDate));
	            int userStatusCode = ((Number) resultRow.get("user_status")).intValue();
	            int amount = ((Number) resultRow.get("amount")).intValue();
	            UserStatus status = getUserStatus(userStatusCode);
	            calculateAmount(status, amount, row);
	        }
	        
	        // Sort and collect ouput data by date 
	        List<RecipientsDetailedStatisticsRow> returnList = new ArrayList<>();
        	SortedSet<String> keys = new TreeSet<>(dataMap.keySet());
    		for (String key : keys) { 
    		   returnList.add(dataMap.get(key));
    		}
    		
    		return returnList;
    	} catch (Exception e) {
			logger.error("Error in getRecipientDetailedStat: " + e.getMessage(), e);
			throw e;
		}
    }

    private RecipientsDetailedStatisticsRow getRecipientDetailedStatAmountsBeforeDate(@VelocityCheck int companyId, int mailinglistId, String mailinglistName, int mailinglistIndex, LightTarget target, int targetGroupIndex, Date beforeDate) throws Exception {
    	try {
    		target = getDefaultTarget(target);
        	RecipientsDetailedStatisticsRow nextRecipientsDetailedStatisticsRow =
					new RecipientsDetailedStatisticsRow(beforeDate,
							mailinglistId, mailinglistName, mailinglistIndex,
							target.getId(), target.getName(), targetGroupIndex);
	        
	        String sql = "SELECT user_status, COUNT(*) amount FROM " + getCustomerBindingTableName(companyId) + " bind";
	        if (StringUtils.isNotBlank(target.getTargetSQL())) {
	        	sql += ", customer_" + companyId + "_tbl cust";
	        }
	        sql += " WHERE bind.mailinglist_id = ?";
	        sql += " AND bind.timestamp < ?";
	        
	        if (StringUtils.isNotBlank(target.getTargetSQL())) {
	        	sql += " AND bind.customer_id = cust.customer_id AND (" + target.getTargetSQL() + ")";
	        }
	        
	        sql += " GROUP BY user_status";
	        
	        List<Map<String, Object>> result = select(logger, sql, mailinglistId, beforeDate);
	        for (Map<String, Object> resultRow : result) {
	            int userStatusCode = ((Number) resultRow.get("user_status")).intValue();
	            int amount = ((Number) resultRow.get("amount")).intValue();
	            UserStatus status = getUserStatus(userStatusCode);
	            calculateAmount(status, amount, nextRecipientsDetailedStatisticsRow);
	        }
    		
    		return nextRecipientsDetailedStatisticsRow;
    	} catch (Exception e) {
			logger.error("Error in getRecipientDetailedStatAmountsBeforeDate: " + e.getMessage(), e);
			throw e;
		}
    }
}
