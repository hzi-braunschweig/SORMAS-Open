/*
 * ******************************************************************************
 * * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * *
 * * This program is free software: you can redistribute it and/or modify
 * * it under the terms of the GNU General Public License as published by
 * * the Free Software Foundation, either version 3 of the License, or
 * * (at your option) any later version.
 * *
 * * This program is distributed in the hope that it will be useful,
 * * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * * GNU General Public License for more details.
 * *
 * * You should have received a copy of the GNU General Public License
 * * along with this program. If not, see <https://www.gnu.org/licenses/>.
 * ******************************************************************************
 */

package de.symeda.sormas.api.activityascase;

import java.util.Date;

import de.symeda.sormas.api.event.MeansOfTransport;
import de.symeda.sormas.api.event.TypeOfPlace;
import de.symeda.sormas.api.exposure.ExposureRole;
import de.symeda.sormas.api.exposure.GatheringType;
import de.symeda.sormas.api.exposure.HabitationType;
import de.symeda.sormas.api.exposure.WorkEnvironment;
import de.symeda.sormas.api.location.LocationDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.HideForCountries;
import javax.validation.constraints.NotNull;
import de.symeda.sormas.api.utils.SensitiveData;
import de.symeda.sormas.api.utils.pseudonymization.PseudonymizableDto;

public class ActivityAsCaseDto extends PseudonymizableDto {

	private static final long serialVersionUID = 6551672739041643942L;

	public static final String I18N_PREFIX = "ActivityAsCase";

	public static final String REPORTING_USER = "reportingUser";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String DESCRIPTION = "description";
	public static final String ACTIVITY_AS_CASE_TYPE = "activityAsCaseType";
	public static final String ACTIVITY_AS_CASE_TYPE_DETAILS = "activityAsCaseTypeDetails";
	public static final String LOCATION = "location";
	public static final String TYPE_OF_PLACE = "typeOfPlace";
	public static final String TYPE_OF_PLACE_DETAILS = "typeOfPlaceDetails";
	public static final String MEANS_OF_TRANSPORT = "meansOfTransport";
	public static final String MEANS_OF_TRANSPORT_DETAILS = "meansOfTransportDetails";
	public static final String CONNECTION_NUMBER = "connectionNumber";
	public static final String SEAT_NUMBER = "seatNumber";
	public static final String WORK_ENVIRONMENT = "workEnvironment";

	public static final String GATHERING_TYPE = "gatheringType";
	public static final String GATHERING_DETAILS = "gatheringDetails";
	public static final String HABITATION_TYPE = "habitationType";
	public static final String HABITATION_DETAILS = "habitationDetails";

	public static final String ROLE = "role";

	@SensitiveData
	private UserReferenceDto reportingUser;
	private Date startDate;
	private Date endDate;
	@SensitiveData
	private String description;
	@NotNull
	private ActivityAsCaseType activityAsCaseType;
	@SensitiveData
	private String activityAsCaseTypeDetails;
	private LocationDto location;
	@HideForCountries
	private ExposureRole role;

	// Type of Place
	private TypeOfPlace typeOfPlace;
	@SensitiveData
	private String typeOfPlaceDetails;
	private MeansOfTransport meansOfTransport;
	@SensitiveData
	private String meansOfTransportDetails;
	@SensitiveData
	private String connectionNumber;
	@SensitiveData
	private String seatNumber;

	private WorkEnvironment workEnvironment;

	// Exposure sub-types
	private GatheringType gatheringType;
	@SensitiveData
	private String gatheringDetails;
	@HideForCountries
	private HabitationType habitationType;
	@HideForCountries
	@SensitiveData
	private String habitationDetails;

	public static ActivityAsCaseDto build(ActivityAsCaseType activityAsCaseType) {

		ActivityAsCaseDto activityAsCase = new ActivityAsCaseDto();
		activityAsCase.setUuid(DataHelper.createUuid());
		activityAsCase.setActivityAsCaseType(activityAsCaseType);
		LocationDto location = LocationDto.build();
		activityAsCase.setLocation(location);
		return activityAsCase;
	}

	public UserReferenceDto getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(UserReferenceDto reportingUser) {
		this.reportingUser = reportingUser;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ActivityAsCaseType getActivityAsCaseType() {
		return activityAsCaseType;
	}

	public void setActivityAsCaseType(ActivityAsCaseType activityAsCaseType) {
		this.activityAsCaseType = activityAsCaseType;
	}

	public String getActivityAsCaseTypeDetails() {
		return activityAsCaseTypeDetails;
	}

	public void setActivityAsCaseTypeDetails(String activityAsCaseTypeDetails) {
		this.activityAsCaseTypeDetails = activityAsCaseTypeDetails;
	}

	public LocationDto getLocation() {
		return location;
	}

	public void setLocation(LocationDto location) {
		this.location = location;
	}

	public ExposureRole getRole() {
		return role;
	}

	public void setRole(ExposureRole role) {
		this.role = role;
	}

	public TypeOfPlace getTypeOfPlace() {
		return typeOfPlace;
	}

	public void setTypeOfPlace(TypeOfPlace typeOfPlace) {
		this.typeOfPlace = typeOfPlace;
	}

	public String getTypeOfPlaceDetails() {
		return typeOfPlaceDetails;
	}

	public void setTypeOfPlaceDetails(String typeOfPlaceDetails) {
		this.typeOfPlaceDetails = typeOfPlaceDetails;
	}

	public MeansOfTransport getMeansOfTransport() {
		return meansOfTransport;
	}

	public void setMeansOfTransport(MeansOfTransport meansOfTransport) {
		this.meansOfTransport = meansOfTransport;
	}

	public String getMeansOfTransportDetails() {
		return meansOfTransportDetails;
	}

	public void setMeansOfTransportDetails(String meansOfTransportDetails) {
		this.meansOfTransportDetails = meansOfTransportDetails;
	}

	public String getConnectionNumber() {
		return connectionNumber;
	}

	public void setConnectionNumber(String connectionNumber) {
		this.connectionNumber = connectionNumber;
	}

	public String getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(String seatNumber) {
		this.seatNumber = seatNumber;
	}

	public WorkEnvironment getWorkEnvironment() {
		return workEnvironment;
	}

	public void setWorkEnvironment(WorkEnvironment workEnvironment) {
		this.workEnvironment = workEnvironment;
	}

	public GatheringType getGatheringType() {
		return gatheringType;
	}

	public void setGatheringType(GatheringType gatheringType) {
		this.gatheringType = gatheringType;
	}

	public String getGatheringDetails() {
		return gatheringDetails;
	}

	public void setGatheringDetails(String gatheringDetails) {
		this.gatheringDetails = gatheringDetails;
	}

	public HabitationType getHabitationType() {
		return habitationType;
	}

	public void setHabitationType(HabitationType habitationType) {
		this.habitationType = habitationType;
	}

	public String getHabitationDetails() {
		return habitationDetails;
	}

	public void setHabitationDetails(String habitationDetails) {
		this.habitationDetails = habitationDetails;
	}

	@Override
	public ActivityAsCaseDto clone() throws CloneNotSupportedException {
		ActivityAsCaseDto clone = (ActivityAsCaseDto) super.clone();
		clone.setLocation((LocationDto) clone.getLocation().clone());
		return clone;
	}
}
