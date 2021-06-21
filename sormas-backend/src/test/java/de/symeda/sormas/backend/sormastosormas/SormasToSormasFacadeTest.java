/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.backend.sormastosormas;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.function.Consumer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;

import org.mockito.Mockito;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.symeda.sormas.api.SormasToSormasConfig;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.contact.ContactReferenceDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.infrastructure.PointOfEntryReferenceDto;
import de.symeda.sormas.api.infrastructure.PointOfEntryType;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.sample.AdditionalTestDto;
import de.symeda.sormas.api.sample.PathogenTestDto;
import de.symeda.sormas.api.sample.PathogenTestType;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sample.SampleMaterial;
import de.symeda.sormas.api.sample.SamplePurpose;
import de.symeda.sormas.api.sample.SampleSource;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasSampleDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.AbstractBeanTest;
import de.symeda.sormas.backend.TestDataCreator;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.infrastructure.PointOfEntry;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.sormastosormas.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.user.User;

public class SormasToSormasFacadeTest extends AbstractBeanTest {

	// values are set in server-list.csv located in serveraccessdefault and serveraccesssecond
	public static final String DEFAULT_SERVER_ACCESS_CN = "default";
	public static final String DEFAULT_SERVER_ACCESS_DATA_CSV = "default-server-access-data.csv";
	public static final String SECOND_SERVER_ACCESS_CN = "second";
	public static final String SECOND_SERVER_ACCESS_DATA_CSV = "second-server-access-data.csv";
	public static final String SECOND_SERVER_REST_URL = "second.sormas.com";
	public static final String SECOND_SERVER_REST_PASSWORD = "RestPasswoRdish";

	private ObjectMapper objectMapper;

	@Override
	public void init() {
		super.init();

		objectMapper = new ObjectMapper();

		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

		mockDefaultServerAccess();
	}

	protected SormasToSormasOriginInfoDto createSormasToSormasOriginInfo(String serverId, boolean ownershipHandedOver) {
		SormasToSormasOriginInfoDto source = new SormasToSormasOriginInfoDto();
		source.setOrganizationId(serverId);
		source.setSenderName("John doe");
		source.setOwnershipHandedOver(ownershipHandedOver);

		return source;
	}

	protected PersonDto createPersonDto(MappableRdcf rdcf) {
		PersonDto person = PersonDto.build();
		person.setFirstName("John");
		person.setLastName("Smith");

		person.getAddress().setDistrict(rdcf.remoteRdcf.district);
		person.getAddress().setRegion(rdcf.remoteRdcf.region);
		person.getAddress().setCommunity(rdcf.remoteRdcf.community);

		return person;
	}

	protected SormasToSormasSampleDto createRemoteSampleDtoWithTests(
		TestDataCreator.RDCF rdcf,
		CaseReferenceDto caseRef,
		ContactReferenceDto contactRef) {
		UserReferenceDto userRef = UserDto.build().toReference();

		SampleDto sample;
		if (caseRef != null) {
			sample = SampleDto.build(userRef, caseRef);
		} else {
			sample = SampleDto.build(userRef, contactRef);
		}

		sample.setLab(rdcf.facility);
		sample.setSampleDateTime(new Date());
		sample.setSampleMaterial(SampleMaterial.BLOOD);
		sample.setSampleSource(SampleSource.HUMAN);
		sample.setSamplePurpose(SamplePurpose.INTERNAL);

		PathogenTestDto pathogenTest = PathogenTestDto.build(sample.toReference(), userRef);
		pathogenTest.setTestDateTime(new Date());
		pathogenTest.setTestResultVerified(true);
		pathogenTest.setTestType(PathogenTestType.RAPID_TEST);

		AdditionalTestDto additionalTest = AdditionalTestDto.build(sample.toReference());
		additionalTest.setTestDateTime(new Date());

		return new SormasToSormasSampleDto(sample, Collections.singletonList(pathogenTest), Collections.singletonList(additionalTest));
	}

	protected SormasToSormasShareInfo createShareInfo(
		User sender,
		String serverId,
		boolean ownershipHandedOver,
		Consumer<SormasToSormasShareInfo> setTarget) {
		SormasToSormasShareInfo shareInfo = new SormasToSormasShareInfo();

		shareInfo.setOwnershipHandedOver(ownershipHandedOver);
		shareInfo.setOrganizationId(serverId);
		shareInfo.setSender(sender);

		setTarget.accept(shareInfo);

		return shareInfo;
	}

	protected SormasToSormasEncryptedDataDto encryptShareDataAsArray(Object shareData) throws JsonProcessingException, SormasToSormasException {
		return encryptShareData(Collections.singletonList(shareData));
	}

	protected SormasToSormasEncryptedDataDto encryptShareData(Object shareData) throws SormasToSormasException {
		mockDefaultServerAccess();

		SormasToSormasEncryptedDataDto encryptedData = getSormasToSormasEncryptionEjb().signAndEncrypt(shareData, SECOND_SERVER_ACCESS_CN);

		mockSecondServerAccess();

		return encryptedData;
	}

	protected <T> T decryptSharesData(byte[] data, Class<T> dataType) throws SormasToSormasException, IOException {
		mockSecondServerAccess();

		T decryptData =
			getSormasToSormasEncryptionEjb().decryptAndVerify(new SormasToSormasEncryptedDataDto(DEFAULT_SERVER_ACCESS_CN, data), dataType);

		mockDefaultServerAccess();

		return decryptData;
	}

	protected void mockDefaultServerAccess() {

		File file = new File("src/test/java/de/symeda/sormas/backend/sormastosormas/serveraccessdefault");

		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getPath()).thenReturn(file.getAbsolutePath());
		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getServerAccessDataFileName()).thenReturn(DEFAULT_SERVER_ACCESS_DATA_CSV);
		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getKeystoreName()).thenReturn("default.sormas2sormas.keystore.p12");
		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getKeystorePass()).thenReturn("certPass");
		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getTruststoreName()).thenReturn("sormas2sormas.truststore.p12");
		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getTruststorePass()).thenReturn("truster");
	}

	protected void mockSecondServerAccess() {
		File file = new File("src/test/java/de/symeda/sormas/backend/sormastosormas/serveraccesssecond");

		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getPath()).thenReturn(file.getAbsolutePath());
		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getServerAccessDataFileName()).thenReturn(SECOND_SERVER_ACCESS_DATA_CSV);
		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getKeystoreName()).thenReturn("second.sormas2sormas.keystore.p12");
		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getKeystorePass()).thenReturn("certiPass");
		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getTruststoreName()).thenReturn("sormas2sormas.truststore.p12");
		Mockito.when(MockSormasToSormasConfigProducer.sormasToSormasConfig.getTruststorePass()).thenReturn("trusteR");
	}

	@Specializes
	private static class MockSormasToSormasConfigProducer extends SormasToSormasConfigProducer {

		static SormasToSormasConfig sormasToSormasConfig = mock(SormasToSormasConfig.class);

		@Override
		@Produces
		public SormasToSormasConfig sormas2SormasConfig() {
			return sormasToSormasConfig;
		}
	}

	protected MappableRdcf createRDCF(boolean withExternalId) {

		String regionName = "Region";
		String districtName = "District";
		String communityName = "Community";
		String facilityName = "Facility";
		String pointOfEntryName = "Point of Entry";

		String regionExternalId = null;
		String districtExternalId = null;
		String communityExternalId = null;
		String facilityExternalId = null;
		String pointOfEntryExternalId = null;

		if (withExternalId) {
			regionExternalId = "RegionExtId";
			districtExternalId = "DistrictExtId";
			communityExternalId = "CommunityExtId";
			facilityExternalId = "FacilityExtId";
			pointOfEntryExternalId = "Point of EntryExtId";
		}

		MappableRdcf rdcf = new MappableRdcf();
		rdcf.remoteRdcf = new TestDataCreator.RDCF(
			new RegionReferenceDto(DataHelper.createUuid(), withExternalId ? null : regionName, regionExternalId),
			new DistrictReferenceDto(DataHelper.createUuid(), withExternalId ? null : districtName, districtExternalId),
			new CommunityReferenceDto(DataHelper.createUuid(), withExternalId ? null : communityName, communityExternalId),
			new FacilityReferenceDto(DataHelper.createUuid(), withExternalId ? null : facilityName, facilityExternalId),
			new PointOfEntryReferenceDto(
				DataHelper.createUuid(),
				withExternalId ? null : pointOfEntryName,
				PointOfEntryType.AIRPORT,
				pointOfEntryExternalId));

		Region region = creator.createRegion(regionName, regionExternalId);
		District district = creator.createDistrict(districtName, region, districtExternalId);
		Community community = creator.createCommunity(communityName, district, communityExternalId);
		Facility facility = creator.createFacility(facilityName, FacilityType.HOSPITAL, region, district, community, facilityExternalId);
		PointOfEntry pointOfEntry = creator.createPointOfEntry(pointOfEntryName, region, district, pointOfEntryExternalId);

		rdcf.localRdcf = new TestDataCreator.RDCF(
			new RegionReferenceDto(region.getUuid(), region.getName(), region.getExternalID()),
			new DistrictReferenceDto(district.getUuid(), district.getName(), district.getExternalID()),
			new CommunityReferenceDto(community.getUuid(), community.getName(), community.getExternalID()),
			new FacilityReferenceDto(facility.getUuid(), facility.getName(), facility.getExternalID()),
			new PointOfEntryReferenceDto(pointOfEntry.getUuid(), pointOfEntry.getName(), PointOfEntryType.AIRPORT, pointOfEntry.getExternalID()));

		return rdcf;
	}

	protected static class MappableRdcf {

		protected TestDataCreator.RDCF remoteRdcf;
		protected TestDataCreator.RDCF localRdcf;
	}
}
