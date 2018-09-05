package de.symeda.sormas.api.facility;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;

public class FacilityDto extends EntityDto {

	private static final long serialVersionUID = -7987228795475507196L;

	public static final String I18N_PREFIX = "Facility";
	public static final String OTHER_FACILITY_UUID = "SORMAS-CONSTID-OTHERS-FACILITY";
	public static final String NONE_FACILITY_UUID = "SORMAS-CONSTID-ISNONE-FACILITY";
	public static final String OTHER_LABORATORY_UUID = "SORMAS-CONSTID-OTHERS-LABORATO";
	public static final String OTHER_FACILITY = "OTHER_FACILITY";
	public static final String NO_FACILITY = "NO_FACILITY";
	public static final String OTHER_LABORATORY = "OTHER_LABORATORY";
	public static final String NAME = "name";
	public static final String REGION = "region";
	public static final String DISTRICT = "district";
	public static final String COMMUNITY = "community";
	public static final String CITY = "city";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";

	private String name;
	private RegionReferenceDto region;
	private DistrictReferenceDto district;
	private CommunityReferenceDto community;
	private String city;
	private Double latitude;
	private Double longitude;
	private FacilityType type;
	private boolean publicOwnership;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RegionReferenceDto getRegion() {
		return region;
	}

	public void setRegion(RegionReferenceDto region) {
		this.region = region;
	}

	public DistrictReferenceDto getDistrict() {
		return district;
	}

	public void setDistrict(DistrictReferenceDto district) {
		this.district = district;
	}

	public CommunityReferenceDto getCommunity() {
		return community;
	}

	public void setCommunity(CommunityReferenceDto community) {
		this.community = community;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public FacilityType getType() {
		return type;
	}

	public void setType(FacilityType type) {
		this.type = type;
	}

	public boolean isPublicOwnership() {
		return publicOwnership;
	}

	public void setPublicOwnership(boolean publicOwnership) {
		this.publicOwnership = publicOwnership;
	}

	public FacilityReferenceDto toReference() {
		return new FacilityReferenceDto(getUuid(), toString());
	}

	@Override
	public String toString() {
		return FacilityHelper.buildToString(getUuid(), name);
	}

	public static FacilityDto build() {
		FacilityDto dto = new FacilityDto();
		return dto;
	}

}
