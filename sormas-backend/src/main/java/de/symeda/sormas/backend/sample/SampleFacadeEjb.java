/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.backend.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.DiseaseHelper;
import de.symeda.sormas.api.caze.CaseCriteria;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.contact.ContactReferenceDto;
import de.symeda.sormas.api.event.EventParticipantReferenceDto;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityHelper;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.messaging.MessageType;
import de.symeda.sormas.api.sample.PathogenTestResultType;
import de.symeda.sormas.api.sample.SampleCriteria;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sample.SampleExportDto;
import de.symeda.sormas.api.sample.SampleFacade;
import de.symeda.sormas.api.sample.SampleIndexDto;
import de.symeda.sormas.api.sample.SampleJurisdictionDto;
import de.symeda.sormas.api.sample.SampleMaterial;
import de.symeda.sormas.api.sample.SampleReferenceDto;
import de.symeda.sormas.api.sample.SampleSimilarityCriteria;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseFacadeEjb;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.caze.CaseJurisdictionChecker;
import de.symeda.sormas.backend.caze.CaseQueryContext;
import de.symeda.sormas.backend.caze.CaseService;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.common.messaging.MessageSubject;
import de.symeda.sormas.backend.common.messaging.MessagingService;
import de.symeda.sormas.backend.common.messaging.NotificationDeliveryFailedException;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactFacadeEjb;
import de.symeda.sormas.backend.contact.ContactJurisdictionChecker;
import de.symeda.sormas.backend.contact.ContactService;
import de.symeda.sormas.backend.event.Event;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.event.EventParticipantFacadeEjb;
import de.symeda.sormas.backend.event.EventParticipantJurisdictionChecker;
import de.symeda.sormas.backend.event.EventParticipantService;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.facility.FacilityFacadeEjb;
import de.symeda.sormas.backend.facility.FacilityService;
import de.symeda.sormas.backend.infrastructure.PointOfEntry;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.sample.AdditionalTestFacadeEjb.AdditionalTestFacadeEjbLocal;
import de.symeda.sormas.backend.sample.PathogenTestFacadeEjb.PathogenTestFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfoFacadeEjb;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfoFacadeEjb.SormasToSormasOriginInfoFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoHelper;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfoService;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserFacadeEjb;
import de.symeda.sormas.backend.user.UserRoleConfigFacadeEjb.UserRoleConfigFacadeEjbLocal;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.IterableHelper;
import de.symeda.sormas.backend.util.JurisdictionHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.Pseudonymizer;

@Stateless(name = "SampleFacade")
public class SampleFacadeEjb implements SampleFacade {

	private static final int DELETED_BATCH_SIZE = 1000;

	public static final String CONTACT_CASE_DISTRICT = "contactCaseDistrict";
	public static final String DISEASE = "disease";
	public static final String DISEASE_DETAILS = "diseaseDetails";
	public static final String REGION = "region";
	public static final String DISTRICT = "district";
	public static final String DISTRICT_NAME = "districtName";

	private static final int SIMILARITY_DATE_TIME_THRESHOLD = 2;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private SampleService sampleService;
	@EJB
	private AdditionalTestService additionalTestService;
	@EJB
	private AdditionalTestFacadeEjbLocal additionalTestFacade;
	@EJB
	private UserService userService;
	@EJB
	private CaseService caseService;
	@EJB
	private ContactService contactService;
	@EJB
	private EventParticipantService eventParticipantService;
	@EJB
	private FacilityService facilityService;
	@EJB
	private CaseFacadeEjbLocal caseFacade;
	@EJB
	private MessagingService messagingService;
	@EJB
	private UserRoleConfigFacadeEjbLocal userRoleConfigFacade;
	@EJB
	private PathogenTestFacadeEjbLocal pathogenTestFacade;
	@EJB
	private SampleJurisdictionChecker sampleJurisdictionChecker;
	@EJB
	private CaseJurisdictionChecker caseJurisdictionChecker;
	@EJB
	private ContactJurisdictionChecker contactJurisdictionChecker;
	@EJB
	private EventParticipantJurisdictionChecker eventParticipantJurisdictionChecker;
	@EJB
	private SormasToSormasOriginInfoFacadeEjbLocal originInfoFacade;
	@EJB
	private SormasToSormasShareInfoService sormasToSormasShareInfoService;

	@Override
	public List<String> getAllActiveUuids() {

		User user = userService.getCurrentUser();
		if (user == null) {
			return Collections.emptyList();
		}

		return sampleService.getAllActiveUuids(user);
	}

	@Override
	public List<SampleDto> getAllActiveSamplesAfter(Date date) {

		User user = userService.getCurrentUser();
		if (user == null) {
			return Collections.emptyList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return sampleService.getAllActiveSamplesAfter(date, user).stream().map(e -> convertToDto(e, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<SampleDto> getByUuids(List<String> uuids) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return sampleService.getByUuids(uuids).stream().map(c -> convertToDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<SampleDto> getByCaseUuids(List<String> caseUuids) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return sampleService.getByCaseUuids(caseUuids).stream().map(c -> convertToDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<SampleDto> getByContactUuids(List<String> contactUuids) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return sampleService.getByContactUuids(contactUuids).stream().map(c -> convertToDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<SampleDto> getSimilarSamples(SampleSimilarityCriteria criteria) {
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Sample> cq = cb.createQuery(Sample.class);
		final Root<Sample> root = cq.from(Sample.class);

		SampleJoins<Sample> joins = new SampleJoins<>(root);

		SampleCriteria sampleCriteria = new SampleCriteria();
		sampleCriteria.caze(criteria.getCaze()).contact(criteria.getContact()).eventParticipant(criteria.getEventParticipant());

		Predicate filter = sampleService.createUserFilter(cq, cb, joins, sampleCriteria);
		filter = CriteriaBuilderHelper.and(cb, filter, sampleService.buildCriteriaFilter(sampleCriteria, cb, joins));

		Predicate similarityFilter = null;
		if (criteria.getLabSampleId() != null) {
			similarityFilter = cb.equal(root.get(Sample.LAB_SAMPLE_ID), criteria.getLabSampleId());
		}

		Date sampleDateTime = criteria.getSampleDateTime();
		SampleMaterial sampleMaterial = criteria.getSampleMaterial();

		if (sampleDateTime != null && sampleMaterial != null) {
			Predicate dateAndMaterialFilter = cb.and(
				cb.between(
					root.get(Sample.SAMPLE_DATE_TIME),
					DateHelper.getStartOfDay(DateHelper.subtractDays(sampleDateTime, SIMILARITY_DATE_TIME_THRESHOLD)),
					DateHelper.getEndOfDay(DateHelper.addDays(sampleDateTime, SIMILARITY_DATE_TIME_THRESHOLD))),
				cb.equal(root.get(Sample.SAMPLE_MATERIAL), sampleMaterial));

			similarityFilter = CriteriaBuilderHelper.or(cb, similarityFilter, dateAndMaterialFilter);
		}

		filter = CriteriaBuilderHelper.and(cb, filter, similarityFilter);

		if (filter != null) {
			cq.where(filter);
		}

		List<Sample> samples = em.createQuery(cq).getResultList();

		if (samples.size() == 0 && (sampleDateTime == null || sampleMaterial == null)) {
			return getByCriteria(sampleCriteria);
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return samples.stream().map(s -> convertToDto(s, pseudonymizer)).collect(Collectors.toList());
	}

	private List<SampleDto> getByCriteria(SampleCriteria criteria) {
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Sample> cq = cb.createQuery(Sample.class);
		final Root<Sample> root = cq.from(Sample.class);

		SampleJoins<Sample> joins = new SampleJoins<>(root);

		Predicate filter = sampleService.createUserFilter(cq, cb, joins, criteria);
		filter = CriteriaBuilderHelper.and(cb, filter, sampleService.buildCriteriaFilter(criteria, cb, joins));

		if (filter != null) {
			cq.where(filter);
		}

		List<Sample> samples = em.createQuery(cq).getResultList();

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return samples.stream().map(s -> convertToDto(s, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public boolean exists(String uuid) {
		return sampleService.exists(uuid);
	}

	@Override
	public List<SampleDto> getByEventParticipantUuids(List<String> eventParticipantUuids) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return sampleService.getByEventParticipantUuids(eventParticipantUuids)
			.stream()
			.map(s -> convertToDto(s, pseudonymizer))
			.collect(Collectors.toList());
	}

	@Override
	public List<String> getDeletedUuidsSince(Date since) {

		User user = userService.getCurrentUser();
		if (user == null) {
			return Collections.emptyList();
		}

		return sampleService.getDeletedUuidsSince(user, since);
	}

	@Override
	public SampleDto getSampleByUuid(String uuid) {
		return convertToDto(sampleService.getByUuid(uuid), Pseudonymizer.getDefault(userService::hasRight));
	}

	@Override
	public SampleDto saveSample(@Valid SampleDto dto) {
		return saveSample(dto, true, true);
	}

	public SampleDto saveSample(SampleDto dto, boolean handleChanges, boolean checkChangeDate) {

		Sample existingSample = sampleService.getByUuid(dto.getUuid());
		SampleDto existingSampleDto = toDto(existingSample);

		restorePseudonymizedDto(dto, existingSample, existingSampleDto);

		Sample sample = fromDto(dto, checkChangeDate);

		// Set defaults for testing requests
		if (sample.getPathogenTestingRequested() == null) {
			sample.setPathogenTestingRequested(false);
		}
		if (sample.getAdditionalTestingRequested() == null) {
			sample.setAdditionalTestingRequested(false);
		}

		sampleService.ensurePersisted(sample);

		if (handleChanges) {
			onSampleChanged(existingSampleDto, sample);
		}

		return toDto(sample);
	}

	@Override
	public SampleReferenceDto getReferenceByUuid(String uuid) {
		return toReferenceDto(sampleService.getByUuid(uuid));
	}

	@Override
	public List<SampleIndexDto> getIndexList(SampleCriteria sampleCriteria, Integer first, Integer max, List<SortProperty> sortProperties) {

		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<SampleIndexDto> cq = cb.createQuery(SampleIndexDto.class);
		final Root<Sample> sample = cq.from(Sample.class);

		SampleJoins<Sample> joins = new SampleJoins<>(sample);

		final Join<Sample, Case> caze = joins.getCaze();
		final Join<Case, District> caseDistrict = joins.getCaseDistrict();

		final Join<Sample, Contact> contact = joins.getContact();
		final Join<Contact, District> contactDistrict = joins.getContactDistrict();
		final Join<Case, District> contactCaseDistrict = joins.getContactCaseDistrict();

		final Join<EventParticipant, Event> event = joins.getEvent();
		final Join<Location, District> eventDistrict = joins.getEventDistrict();

		Expression<Object> diseaseSelect = cb.selectCase()
			.when(cb.isNotNull(caze), caze.get(Case.DISEASE))
			.otherwise(cb.selectCase().when(cb.isNotNull(contact), contact.get(Contact.DISEASE)).otherwise(event.get(Event.DISEASE)));
		Expression<Object> diseaseDetailsSelect = cb.selectCase()
			.when(cb.isNotNull(caze), caze.get(Case.DISEASE_DETAILS))
			.otherwise(cb.selectCase().when(cb.isNotNull(contact), contact.get(Contact.DISEASE_DETAILS)).otherwise(event.get(Event.DISEASE_DETAILS)));

		Expression<Object> districtSelect = cb.selectCase()
			.when(cb.isNotNull(caseDistrict), caseDistrict.get(District.NAME))
			.otherwise(
				cb.selectCase()
					.when(cb.isNotNull(contactDistrict), contactDistrict.get(District.NAME))
					.otherwise(
						cb.selectCase()
							.when(cb.isNotNull(contactCaseDistrict), contactCaseDistrict.get(District.NAME))
							.otherwise(eventDistrict.get(District.NAME))));

		cq.distinct(true);

		List<Selection<?>> selections = new ArrayList<>(
			Arrays.asList(
				sample.get(Sample.UUID),
				caze.get(Case.EPID_NUMBER),
				sample.get(Sample.LAB_SAMPLE_ID),
				sample.get(Sample.SAMPLE_DATE_TIME),
				sample.get(Sample.SHIPPED),
				sample.get(Sample.SHIPMENT_DATE),
				sample.get(Sample.RECEIVED),
				sample.get(Sample.RECEIVED_DATE),
				sample.get(Sample.SAMPLE_MATERIAL),
				sample.get(Sample.SAMPLE_PURPOSE),
				sample.get(Sample.SPECIMEN_CONDITION),
				joins.getLab().get(Facility.NAME),
				joins.getReferredSample().get(Sample.UUID),
				sample.get(Sample.SAMPLING_REASON),
				sample.get(Sample.SAMPLING_REASON_DETAILS),
				caze.get(Case.UUID),
				joins.getCasePerson().get(Person.FIRST_NAME),
				joins.getCasePerson().get(Person.LAST_NAME),
				joins.getContact().get(Contact.UUID),
				joins.getContactPerson().get(Person.FIRST_NAME),
				joins.getContactPerson().get(Person.LAST_NAME),
				joins.getEventParticipant().get(EventParticipant.UUID),
				joins.getEventParticipantPerson().get(Person.FIRST_NAME),
				joins.getEventParticipantPerson().get(Person.LAST_NAME),
				diseaseSelect,
				diseaseDetailsSelect,
				sample.get(Sample.PATHOGEN_TEST_RESULT),
				sample.get(Sample.ADDITIONAL_TESTING_REQUESTED),
				cb.isNotEmpty(sample.get(Sample.ADDITIONAL_TESTS)),
				districtSelect,
				joins.getReportingUser().get(User.UUID),
				joins.getLab().get(Facility.UUID)));
		selections.addAll(getCaseJurisdictionSelections(joins));
		selections.addAll(getContactJurisdictionSelections(joins));
		selections.addAll(getEventJurisdictionSelections(joins));

		cq.multiselect(selections);

		Predicate filter = sampleService.createUserFilter(cq, cb, joins, sampleCriteria);

		if (sampleCriteria != null) {
			Predicate criteriaFilter = sampleService.buildCriteriaFilter(sampleCriteria, cb, joins);
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
		}

		if (filter != null) {
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case SampleIndexDto.UUID:
				case SampleIndexDto.LAB_SAMPLE_ID:
				case SampleIndexDto.SHIPPED:
				case SampleIndexDto.RECEIVED:
				case SampleIndexDto.REFERRED:
				case SampleIndexDto.SAMPLE_DATE_TIME:
				case SampleIndexDto.SHIPMENT_DATE:
				case SampleIndexDto.RECEIVED_DATE:
				case SampleIndexDto.SAMPLE_MATERIAL:
				case SampleIndexDto.SAMPLE_PURPOSE:
				case SampleIndexDto.PATHOGEN_TEST_RESULT:
				case SampleIndexDto.ADDITIONAL_TESTING_STATUS:
					expression = sample.get(sortProperty.propertyName);
					break;
				case SampleIndexDto.DISEASE:
					expression = diseaseSelect;
					break;
				case SampleIndexDto.EPID_NUMBER:
					expression = caze.get(Case.EPID_NUMBER);
					break;
				case SampleIndexDto.ASSOCIATED_CASE:
					expression = joins.getCasePerson().get(Person.LAST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = joins.getCasePerson().get(Person.FIRST_NAME);
					break;
				case SampleIndexDto.ASSOCIATED_CONTACT:
					expression = joins.getContactPerson().get(Person.LAST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = joins.getContactPerson().get(Person.FIRST_NAME);
					break;
				case SampleIndexDto.ASSOCIATED_EVENT_PARTICIPANT:
					expression = joins.getEventParticipantPerson().get(Person.LAST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = joins.getEventParticipantPerson().get(Person.FIRST_NAME);
					break;
				case SampleIndexDto.DISTRICT:
					expression = districtSelect;
					break;
				case SampleIndexDto.LAB:
					expression = joins.getLab().get(Facility.NAME);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(sample.get(Sample.SAMPLE_DATE_TIME)));
		}

		List<SampleIndexDto> samples;
		if (first != null && max != null) {
			samples = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
		} else {
			samples = em.createQuery(cq).getResultList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue));
		pseudonymizer.pseudonymizeDtoCollection(
			SampleIndexDto.class,
			samples,
			s -> sampleJurisdictionChecker.isInJurisdictionOrOwned(s.getJurisdiction()),
			null);

		return samples;
	}

	public Page<SampleIndexDto> getIndexPage(SampleCriteria sampleCriteria, Integer offset, Integer size, List<SortProperty> sortProperties) {
		List<SampleIndexDto> sampleIndexList = getIndexList(sampleCriteria, offset, size, sortProperties);
		long totalElementCount = count(sampleCriteria);
		return new Page<>(sampleIndexList, offset, size, totalElementCount);
	}

	public List<SampleDto> getPositiveOrLatest(SampleCriteria criteria, Function<Sample, AbstractDomainObject> associatedObjectFn) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);

		return sampleService.findBy(criteria, userService.getCurrentUser(), Sample.CREATION_DATE, false)
			.stream()
			.collect(
				Collectors.toMap(
					s -> associatedObjectFn.apply(s).getUuid(),
					(s) -> s,
					(s1, s2) -> {

						// keep the positive one
						if (s1.getPathogenTestResult() == PathogenTestResultType.POSITIVE) {
							return s1;
						} else if (s2.getPathogenTestResult() == PathogenTestResultType.POSITIVE) {
							return s2;
						}

						// ordered by creation date by default, so always keep the first one
						return s1;
					}))
			.values()
			.stream()
			.map(s -> convertToDto(s, pseudonymizer))
			.collect(Collectors.toList());
	}

	private Collection<Selection<?>> getCaseJurisdictionSelections(SampleJoins joins) {

		return Arrays.asList(
			joins.getCaseReportingUser().get(User.UUID),
			joins.getCaseResponsibleRegion().get(Region.UUID),
			joins.getCaseResponsibleDistrict().get(District.UUID),
			joins.getCaseResponsibleCommunity().get(Community.UUID),
			joins.getCaseRegion().get(Region.UUID),
			joins.getCaseDistrict().get(District.UUID),
			joins.getCaseCommunity().get(Community.UUID),
			joins.getCaseFacility().get(Facility.UUID),
			joins.getCasePointOfEntry().get(PointOfEntry.UUID));
	}

	private Collection<Selection<?>> getEventJurisdictionSelections(SampleJoins joins) {

		return Arrays.asList(
			joins.getEventReportingUser().get(User.UUID),
			joins.getEventResponsibleUser().get(User.UUID),
			joins.getEventRegion().get(Region.UUID),
			joins.getEventDistrict().get(District.UUID),
			joins.getEventCommunity().get(User.UUID));
	}

	private Collection<Selection<?>> getContactJurisdictionSelections(SampleJoins joins) {

		return Arrays.asList(
			joins.getContactReportingUser().get(User.UUID),
			joins.getContactRegion().get(Region.UUID),
			joins.getContactDistrict().get(District.UUID),
			joins.getContactCommunity().get(Community.UUID),
			joins.getContactCaseReportingUser().get(User.UUID),
			joins.getContactCaseResponsibleRegion().get(Region.UUID),
			joins.getContactCaseResponsibleDistrict().get(District.UUID),
			joins.getContactCaseResponsibleCommunity().get(Community.UUID),
			joins.getContactCaseRegion().get(Region.UUID),
			joins.getContactCaseDistrict().get(District.UUID),
			joins.getContactCaseCommunity().get(Community.UUID),
			joins.getContactCaseHealthFacility().get(Facility.UUID),
			joins.getContactCasePointOfEntry().get(PointOfEntry.UUID));
	}

	@Override
	public void validate(SampleDto sample) throws ValidationRuntimeException {

		if (sample.getAssociatedCase() == null && sample.getAssociatedContact() == null & sample.getAssociatedEventParticipant() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validCaseContactOrEventParticipant));
		}
		if (sample.getSampleDateTime() == null) {
			throw new ValidationRuntimeException(
				I18nProperties
					.getValidationError(Validations.required, I18nProperties.getPrefixCaption(SampleDto.I18N_PREFIX, SampleDto.SAMPLE_DATE_TIME)));
		}
		if (sample.getReportDateTime() == null) {
			throw new ValidationRuntimeException(
				I18nProperties
					.getValidationError(Validations.required, I18nProperties.getPrefixCaption(SampleDto.I18N_PREFIX, SampleDto.REPORT_DATE_TIME)));
		}
		if (sample.getSampleMaterial() == null) {
			throw new ValidationRuntimeException(
				I18nProperties
					.getValidationError(Validations.required, I18nProperties.getPrefixCaption(SampleDto.I18N_PREFIX, SampleDto.SAMPLE_MATERIAL)));
		}
		if (sample.getSamplePurpose() == null) {
			throw new ValidationRuntimeException(
				I18nProperties
					.getValidationError(Validations.required, I18nProperties.getPrefixCaption(SampleDto.I18N_PREFIX, SampleDto.SAMPLE_PURPOSE)));
		}
		if (sample.getLab() == null) {
			throw new ValidationRuntimeException(
				I18nProperties.getValidationError(Validations.required, I18nProperties.getPrefixCaption(SampleDto.I18N_PREFIX, SampleDto.LAB)));
		}
	}

	private List<SampleExportDto> getExportList(
		SampleCriteria sampleCriteria,
		CaseCriteria caseCriteria,
		Collection<String> selectedRows,
		int first,
		int max) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SampleExportDto> cq = cb.createQuery(SampleExportDto.class);
		Root<Sample> sampleRoot = cq.from(Sample.class);

		SampleJoins<Sample> joins = new SampleJoins<>(sampleRoot);

		List<Selection<?>> selections = new ArrayList<>(
			Arrays.asList(
				sampleRoot.get(Sample.ID),
				sampleRoot.get(Sample.UUID),
				sampleRoot.get(Sample.LAB_SAMPLE_ID),
				sampleRoot.get(Sample.REPORT_DATE_TIME),
				joins.getCaze().get(Case.EPID_NUMBER),
				joins.getCasePerson().get(Person.FIRST_NAME),
				joins.getCasePerson().get(Person.LAST_NAME),
				joins.getContactPerson().get(Person.FIRST_NAME),
				joins.getContactPerson().get(Person.LAST_NAME),
				joins.getEventParticipantPerson().get(Person.FIRST_NAME),
				joins.getEventParticipantPerson().get(Person.LAST_NAME),
				joins.getCaze().get(Case.DISEASE),
				joins.getCaze().get(Case.DISEASE_DETAILS),
				joins.getContact().get(Contact.DISEASE),
				joins.getContact().get(Contact.DISEASE_DETAILS),
				joins.getEvent().get(Event.DISEASE),
				joins.getEvent().get(Event.DISEASE_DETAILS),
				sampleRoot.get(Sample.SAMPLE_DATE_TIME),
				sampleRoot.get(Sample.SAMPLE_MATERIAL),
				sampleRoot.get(Sample.SAMPLE_MATERIAL_TEXT),
				sampleRoot.get(Sample.SAMPLE_PURPOSE),
				sampleRoot.get(Sample.SAMPLING_REASON),
				sampleRoot.get(Sample.SAMPLING_REASON_DETAILS),
				sampleRoot.get(Sample.SAMPLE_SOURCE),
				joins.getLab().get(Facility.NAME),
				sampleRoot.get(Sample.LAB_DETAILS),
				sampleRoot.get(Sample.PATHOGEN_TEST_RESULT),
				sampleRoot.get(Sample.PATHOGEN_TESTING_REQUESTED),
				sampleRoot.get(Sample.REQUESTED_PATHOGEN_TESTS_STRING),
				sampleRoot.get(Sample.REQUESTED_OTHER_PATHOGEN_TESTS),
				sampleRoot.get(Sample.ADDITIONAL_TESTING_REQUESTED),
				sampleRoot.get(Sample.REQUESTED_ADDITIONAL_TESTS_STRING),
				sampleRoot.get(Sample.REQUESTED_OTHER_ADDITIONAL_TESTS),
				sampleRoot.get(Sample.SHIPPED),
				sampleRoot.get(Sample.SHIPMENT_DATE),
				sampleRoot.get(Sample.SHIPMENT_DETAILS),
				sampleRoot.get(Sample.RECEIVED),
				sampleRoot.get(Sample.RECEIVED_DATE),
				sampleRoot.get(Sample.SPECIMEN_CONDITION),
				sampleRoot.get(Sample.NO_TEST_POSSIBLE_REASON),
				sampleRoot.get(Sample.COMMENT),
				joins.getReferredSample().get(Sample.UUID),
				joins.getCaze().get(Case.UUID),
				joins.getContact().get(Contact.UUID),
				joins.getEventParticipant().get(EventParticipant.UUID),
				joins.getCasePerson().get(Person.APPROXIMATE_AGE),
				joins.getCasePerson().get(Person.APPROXIMATE_AGE_TYPE),
				joins.getCasePerson().get(Person.SEX),
				joins.getContactPerson().get(Person.APPROXIMATE_AGE),
				joins.getContactPerson().get(Person.APPROXIMATE_AGE_TYPE),
				joins.getContactPerson().get(Person.SEX),
				joins.getEventParticipantPerson().get(Person.APPROXIMATE_AGE),
				joins.getEventParticipantPerson().get(Person.APPROXIMATE_AGE_TYPE),
				joins.getEventParticipantPerson().get(Person.SEX),
				joins.getCasePersonAddressRegion().get(Region.NAME),
				joins.getCasePersonAddressDistrict().get(District.NAME),
				joins.getCasePersonAddressCommunity().get(Community.NAME),
				joins.getCasePersonAddress().get(Location.CITY),
				joins.getCasePersonAddress().get(Location.STREET),
				joins.getCasePersonAddress().get(Location.HOUSE_NUMBER),
				joins.getCasePersonAddress().get(Location.ADDITIONAL_INFORMATION),
				joins.getContactPersonAddressRegion().get(Region.NAME),
				joins.getContactPersonAddressDistrict().get(District.NAME),
				joins.getContactPersonAddressCommunity().get(Community.NAME),
				joins.getContactPersonAddress().get(Location.CITY),
				joins.getContactPersonAddress().get(Location.STREET),
				joins.getContactPersonAddress().get(Location.HOUSE_NUMBER),
				joins.getContactPersonAddress().get(Location.ADDITIONAL_INFORMATION),
				joins.getEventRegion().get(Region.NAME),
				joins.getEventDistrict().get(District.NAME),
				joins.getEventCommunity().get(Community.NAME),
				joins.getEventLocation().get(Location.CITY),
				joins.getEventLocation().get(Location.STREET),
				joins.getEventLocation().get(Location.HOUSE_NUMBER),
				joins.getEventLocation().get(Location.ADDITIONAL_INFORMATION),
				joins.getCaze().get(Case.REPORT_DATE),
				joins.getCaze().get(Case.CASE_CLASSIFICATION),
				joins.getCaze().get(Case.OUTCOME),
				joins.getCaseRegion().get(Region.NAME),
				joins.getCaseDistrict().get(District.NAME),
				joins.getCaseCommunity().get(Community.NAME),
				joins.getCaseFacility().get(Facility.NAME),
				joins.getCaze().get(Case.HEALTH_FACILITY_DETAILS),
				joins.getContactRegion().get(Region.NAME),
				joins.getContactDistrict().get(District.NAME),
				joins.getContactCommunity().get(Community.NAME),
				joins.getContact().get(Contact.REPORT_DATE_TIME),
				joins.getContact().get(Contact.LAST_CONTACT_DATE),
				joins.getContact().get(Contact.CONTACT_CLASSIFICATION),
				joins.getContact().get(Contact.CONTACT_STATUS),
				joins.getReportingUser().get(User.UUID),
				joins.getLab().get(Facility.UUID)));

		cq.distinct(true);

		selections.addAll(getCaseJurisdictionSelections(joins));
		selections.addAll(getContactJurisdictionSelections(joins));
		selections.addAll(getEventJurisdictionSelections(joins));

		cq.multiselect(selections);

		Predicate filter = sampleService.createUserFilter(cb, cq, sampleRoot);

		if (sampleCriteria != null) {
			Predicate criteriaFilter = sampleService.buildCriteriaFilter(sampleCriteria, cb, joins);
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
			filter = CriteriaBuilderHelper.andInValues(selectedRows, filter, cb, sampleRoot.get(Sample.UUID));
		} else if (caseCriteria != null) {
			Predicate criteriaFilter = caseService.createCriteriaFilter(caseCriteria, new CaseQueryContext(cb, cq, joins.getCaze()));
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
			filter = CriteriaBuilderHelper.and(cb, filter, cb.isFalse(sampleRoot.get(Sample.DELETED)));
			filter = CriteriaBuilderHelper.andInValues(selectedRows, filter, cb, joins.getCaze().get(Case.UUID));
		}

		if (filter != null) {
			cq.where(filter);
		}

		cq.orderBy(cb.desc(sampleRoot.get(Sample.REPORT_DATE_TIME)), cb.desc(sampleRoot.get(Sample.ID)));

		List<SampleExportDto> resultList = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue));

		for (SampleExportDto exportDto : resultList) {
			Sample sampleFromExportDto = sampleService.getById(exportDto.getId());
			List<PathogenTest> pathogenTests = sampleFromExportDto.getPathogenTests();
			int count = 0;
			for (PathogenTest pathogenTest : pathogenTests) {
				String lab = pathogenTest.getLab() != null
					? FacilityHelper
						.buildFacilityString(pathogenTest.getLab().getUuid(), pathogenTest.getLab().getName(), pathogenTest.getLabDetails())
					: null;
				SampleExportDto.SampleExportPathogenTest sampleExportPathogenTest = new SampleExportDto.SampleExportPathogenTest(
					pathogenTest.getTestType(),
					pathogenTest.getTestTypeText(),
					DiseaseHelper.toString(pathogenTest.getTestedDisease(), pathogenTest.getTestedDiseaseDetails()),
					pathogenTest.getTestDateTime(),
					lab,
					pathogenTest.getTestResult(),
					pathogenTest.getTestResultVerified());

				switch (++count) {
				case 1:
					exportDto.setPathogenTest1(sampleExportPathogenTest);
					break;
				case 2:
					exportDto.setPathogenTest2(sampleExportPathogenTest);
					break;
				case 3:
					exportDto.setPathogenTest3(sampleExportPathogenTest);
					break;
				default:
					exportDto.addOtherPathogenTest(sampleExportPathogenTest);
					break;
				}
			}

			if (exportDto.getAdditionalTestingRequested()) {
				List<AdditionalTest> additionalTests = additionalTestService.getAllBySample(sampleFromExportDto);
				if (additionalTests.size() > 0) {
					exportDto.setAdditionalTest(additionalTestFacade.toDto(additionalTests.get(0)));
				}
				if (additionalTests.size() > 1) {
					exportDto.setOtherAdditionalTestsDetails(I18nProperties.getString(Strings.yes));
				} else {
					exportDto.setOtherAdditionalTestsDetails(I18nProperties.getString(Strings.no));
				}
			} else {
				exportDto.setOtherAdditionalTestsDetails(I18nProperties.getString(Strings.no));
			}

			boolean isInJurisdiction = sampleJurisdictionChecker.isInJurisdictionOrOwned(exportDto.getJurisdiction());
			pseudonymizer.pseudonymizeDto(SampleExportDto.class, exportDto, isInJurisdiction, s -> {
				pseudonymizer.pseudonymizeDtoCollection(
					SampleExportDto.SampleExportPathogenTest.class,
					exportDto.getOtherPathogenTests(),
					t -> isInJurisdiction,
					null);
			});
		}

		return resultList;
	}

	@Override
	public List<SampleExportDto> getExportList(SampleCriteria criteria, Collection<String> selectedRows, int first, int max) {
		return getExportList(criteria, null, selectedRows, first, max);
	}

	@Override
	public List<SampleExportDto> getExportList(CaseCriteria criteria, Collection<String> selectedRows, int first, int max) {
		return getExportList(null, criteria, selectedRows, first, max);
	}

	@Override
	public long count(SampleCriteria sampleCriteria) {

		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		final Root<Sample> root = cq.from(Sample.class);

		SampleJoins<Sample> joins = new SampleJoins<>(root);

		Predicate filter = sampleService.createUserFilter(cq, cb, joins, sampleCriteria);
		if (sampleCriteria != null) {
			Predicate criteriaFilter = sampleService.buildCriteriaFilter(sampleCriteria, cb, joins);
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
		}

		if (filter != null) {
			cq.where(filter);
		}

		cq.select(cb.countDistinct(root));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public SampleReferenceDto getReferredFrom(String sampleUuid) {
		return toReferenceDto(sampleService.getReferredFrom(sampleUuid));
	}

	@Override
	public void deleteSample(SampleReferenceDto sampleRef) {

		User user = userService.getCurrentUser();
		if (!userRoleConfigFacade.getEffectiveUserRights(user.getUserRoles().toArray(new UserRole[user.getUserRoles().size()]))
			.contains(UserRight.SAMPLE_DELETE)) {
			throw new UnsupportedOperationException("User " + user.getUuid() + " is not allowed to delete samples.");
		}

		Sample sample = sampleService.getByReferenceDto(sampleRef);
		sampleService.delete(sample);

		final Case associatedCase = sample.getAssociatedCase();
		if (associatedCase != null) {
			caseFacade.onCaseChanged(CaseFacadeEjbLocal.toDto(associatedCase), associatedCase);
		}
	}

	@Override
	public void deleteAllSamples(List<String> sampleUuids) {
		User user = userService.getCurrentUser();
		if (!userRoleConfigFacade.getEffectiveUserRights(user.getUserRoles().toArray(new UserRole[user.getUserRoles().size()]))
			.contains(UserRight.SAMPLE_DELETE)) {
			throw new UnsupportedOperationException("User " + user.getUuid() + " is not allowed to delete samples.");
		}
		long startTime = DateHelper.startTime();

		IterableHelper.executeBatched(sampleUuids, DELETED_BATCH_SIZE, batchedSampleUuids -> sampleService.deleteAll(batchedSampleUuids));
		logger.debug("deleteAllSamples(sampleUuids) finished. samplesCount = {}, {}ms", sampleUuids.size(), DateHelper.durationMillies(startTime));
	}

	@Override
	public Map<PathogenTestResultType, Long> getNewTestResultCountByResultType(List<Long> caseIds) {
		return sampleService.getNewTestResultCountByResultType(caseIds);
	}

	public Sample fromDto(@NotNull SampleDto source, boolean checkChangeDate) {

		Sample target = DtoHelper.fillOrBuildEntity(source, sampleService.getByUuid(source.getUuid()), Sample::new, checkChangeDate);

		target.setAssociatedCase(caseService.getByReferenceDto(source.getAssociatedCase()));
		target.setAssociatedContact(contactService.getByReferenceDto(source.getAssociatedContact()));
		target.setAssociatedEventParticipant(eventParticipantService.getByReferenceDto(source.getAssociatedEventParticipant()));
		target.setLabSampleID(source.getLabSampleID());
		target.setFieldSampleID(source.getFieldSampleID());
		target.setSampleDateTime(source.getSampleDateTime());
		target.setReportDateTime(source.getReportDateTime());
		target.setReportingUser(userService.getByReferenceDto(source.getReportingUser()));
		target.setSampleMaterial(source.getSampleMaterial());
		target.setSampleMaterialText(source.getSampleMaterialText());
		target.setSamplePurpose(source.getSamplePurpose());
		target.setLab(facilityService.getByReferenceDto(source.getLab()));
		target.setLabDetails(source.getLabDetails());
		target.setShipmentDate(source.getShipmentDate());
		target.setShipmentDetails(source.getShipmentDetails());
		target.setReceivedDate(source.getReceivedDate());
		target.setSpecimenCondition(source.getSpecimenCondition());
		target.setNoTestPossibleReason(source.getNoTestPossibleReason());
		target.setComment(source.getComment());
		target.setSampleSource(source.getSampleSource());
		target.setReferredTo(sampleService.getByReferenceDto(source.getReferredTo()));
		target.setShipped(source.isShipped());
		target.setReceived(source.isReceived());
		target.setPathogenTestingRequested(source.getPathogenTestingRequested());
		target.setAdditionalTestingRequested(source.getAdditionalTestingRequested());
		target.setRequestedPathogenTests(source.getRequestedPathogenTests());
		target.setRequestedAdditionalTests(source.getRequestedAdditionalTests());
		target.setPathogenTestResult(source.getPathogenTestResult());
		target.setRequestedOtherPathogenTests(source.getRequestedOtherPathogenTests());
		target.setRequestedOtherAdditionalTests(source.getRequestedOtherAdditionalTests());
		target.setSamplingReason(source.getSamplingReason());
		target.setSamplingReasonDetails(source.getSamplingReasonDetails());

		target.setReportLat(source.getReportLat());
		target.setReportLon(source.getReportLon());
		target.setReportLatLonAccuracy(source.getReportLatLonAccuracy());

		if (source.getSormasToSormasOriginInfo() != null) {
			target.setSormasToSormasOriginInfo(originInfoFacade.fromDto(source.getSormasToSormasOriginInfo(), checkChangeDate));
		}

		return target;
	}

	public SampleDto convertToDto(Sample source, Pseudonymizer pseudonymizer) {

		SampleDto dto = toDto(source);
		pseudonymizeDto(source, dto, pseudonymizer);

		return dto;
	}

	private void pseudonymizeDto(Sample source, SampleDto dto, Pseudonymizer pseudonymizer) {
		if (dto != null) {
			SampleJurisdictionDto sampleJurisdiction = JurisdictionHelper.createSampleJurisdictionDto(source);
			boolean isInJurisdiction = sampleJurisdictionChecker.isInJurisdictionOrOwned(sampleJurisdiction);
			User currentUser = userService.getCurrentUser();

			boolean samplePseudonimized = true;
			if (dto.getAssociatedEventParticipant() != null) {
				samplePseudonimized = eventParticipantJurisdictionChecker.isPseudonymized(dto.getAssociatedEventParticipant().getUuid());
			}
			EventParticipantReferenceDto eventParticipantReference = dto.getAssociatedEventParticipant();

			pseudonymizer.pseudonymizeDto(SampleDto.class, dto, eventParticipantReference != null ? samplePseudonimized : isInJurisdiction, s -> {
				pseudonymizer.pseudonymizeUser(source.getReportingUser(), currentUser, s::setReportingUser);
				pseudonymizeAssociatedObjects(
					sampleJurisdiction,
					s.getAssociatedCase(),
					s.getAssociatedContact(),
					s.getAssociatedEventParticipant(),
					pseudonymizer);
			});
		}
	}

	private void restorePseudonymizedDto(SampleDto dto, Sample existingSample, SampleDto existingSampleDto) {
		if (existingSampleDto != null) {
			boolean inJurisdiction = sampleJurisdictionChecker.isInJurisdictionOrOwned(existingSample);
			User currentUser = userService.getCurrentUser();

			Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);

			pseudonymizer.restoreUser(existingSample.getReportingUser(), currentUser, dto, dto::setReportingUser);
			pseudonymizer.restorePseudonymizedValues(SampleDto.class, dto, existingSampleDto, inJurisdiction);
		}
	}

	private void pseudonymizeAssociatedObjects(
		SampleJurisdictionDto sampleJurisdiction,
		CaseReferenceDto sampleCase,
		ContactReferenceDto sampleContact,
		EventParticipantReferenceDto sampleEventParticipant,
		Pseudonymizer pseudonymizer) {

		if (sampleCase != null) {
			pseudonymizer.pseudonymizeDto(
				CaseReferenceDto.class,
				sampleCase,
				caseJurisdictionChecker.isInJurisdictionOrOwned(sampleJurisdiction.getCaseJurisdiction()),
				null);
		}

		if (sampleContact != null) {
			pseudonymizer.pseudonymizeDto(
				ContactReferenceDto.PersonName.class,
				sampleContact.getContactName(),
				contactJurisdictionChecker.isInJurisdictionOrOwned(sampleJurisdiction.getContactJurisdiction()),
				null);

			if (sampleContact.getCaseName() != null) {
				pseudonymizer.pseudonymizeDto(
					ContactReferenceDto.PersonName.class,
					sampleContact.getCaseName(),
					caseJurisdictionChecker.isInJurisdictionOrOwned(sampleJurisdiction.getContactJurisdiction().getCaseJurisdiction()),
					null);
			}
		}

		if (sampleEventParticipant != null) {
			pseudonymizer.pseudonymizeDto(
				EventParticipantReferenceDto.class,
				sampleEventParticipant,
				eventParticipantJurisdictionChecker.isPseudonymized(sampleEventParticipant.getUuid()),
				null);
		}
	}

	public static SampleDto toDto(Sample source) {

		if (source == null) {
			return null;
		}

		SampleDto target = new SampleDto();
		DtoHelper.fillDto(target, source);

		target.setAssociatedCase(CaseFacadeEjb.toReferenceDto(source.getAssociatedCase()));
		target.setAssociatedContact(ContactFacadeEjb.toReferenceDto(source.getAssociatedContact()));
		target.setAssociatedEventParticipant(EventParticipantFacadeEjb.toReferenceDto(source.getAssociatedEventParticipant()));
		target.setLabSampleID(source.getLabSampleID());
		target.setFieldSampleID(source.getFieldSampleID());
		target.setSampleDateTime(source.getSampleDateTime());
		target.setReportDateTime(source.getReportDateTime());
		target.setReportingUser(UserFacadeEjb.toReferenceDto(source.getReportingUser()));
		target.setSampleMaterial(source.getSampleMaterial());
		target.setSampleMaterialText(source.getSampleMaterialText());
		target.setSamplePurpose(source.getSamplePurpose());
		target.setLab(FacilityFacadeEjb.toReferenceDto(source.getLab()));
		target.setLabDetails(source.getLabDetails());
		target.setShipmentDate(source.getShipmentDate());
		target.setShipmentDetails(source.getShipmentDetails());
		target.setReceivedDate(source.getReceivedDate());
		target.setSpecimenCondition(source.getSpecimenCondition());
		target.setNoTestPossibleReason(source.getNoTestPossibleReason());
		target.setComment(source.getComment());
		target.setSampleSource(source.getSampleSource());
		target.setReferredTo(SampleFacadeEjb.toReferenceDto(source.getReferredTo()));
		target.setShipped(source.isShipped());
		target.setReceived(source.isReceived());
		target.setPathogenTestingRequested(source.getPathogenTestingRequested());
		target.setAdditionalTestingRequested(source.getAdditionalTestingRequested());
		target.setRequestedPathogenTests(source.getRequestedPathogenTests());
		target.setRequestedAdditionalTests(source.getRequestedAdditionalTests());
		target.setPathogenTestResult(source.getPathogenTestResult());
		target.setRequestedOtherPathogenTests(source.getRequestedOtherPathogenTests());
		target.setRequestedOtherAdditionalTests(source.getRequestedOtherAdditionalTests());
		target.setSamplingReason(source.getSamplingReason());
		target.setSamplingReasonDetails(source.getSamplingReasonDetails());

		target.setReportLat(source.getReportLat());
		target.setReportLon(source.getReportLon());
		target.setReportLatLonAccuracy(source.getReportLatLonAccuracy());

		target.setSormasToSormasOriginInfo(SormasToSormasOriginInfoFacadeEjb.toDto(source.getSormasToSormasOriginInfo()));
		target.setOwnershipHandedOver(source.getShareInfoSamples().stream().anyMatch(ShareInfoHelper::isOwnerShipHandedOver));

		return target;
	}

	public static SampleReferenceDto toReferenceDto(Sample entity) {

		if (entity == null) {
			return null;
		}

		return new SampleReferenceDto(entity.getUuid(), entity.toString());
	}

	private void onSampleChanged(SampleDto existingSample, Sample newSample) {

		// Change pathogenTestResultChangeDate if the pathogen test result has changed
		if (existingSample != null
			&& existingSample.getPathogenTestResult() != null
			&& existingSample.getPathogenTestResult() != newSample.getPathogenTestResult()) {
			Date latestPathogenTestDate = pathogenTestFacade.getLatestPathogenTestDate(newSample.getUuid());
			if (latestPathogenTestDate != null) {
				newSample.setPathogenTestResultChangeDate(latestPathogenTestDate);
			}
		}

		if (newSample.getAssociatedCase() != null) {
			caseFacade.onCaseChanged(CaseFacadeEjbLocal.toDto(newSample.getAssociatedCase()), newSample.getAssociatedCase());
		}

		// Send an email to the lab user when a sample has been shipped to their lab
		if (newSample.isShipped()
			&& (existingSample == null || !existingSample.isShipped())
			&& !StringUtils.equals(newSample.getLab().getUuid(), FacilityDto.OTHER_FACILITY_UUID)) {
			List<User> messageRecipients = userService.getLabUsersOfLab(newSample.getLab());

			for (User recipient : messageRecipients) {
				try {
					String messageContent = null;
					if (newSample.getAssociatedCase() != null) {
						messageContent = String.format(
							I18nProperties.getString(MessagingService.CONTENT_LAB_SAMPLE_SHIPPED_SHORT),
							DataHelper.getShortUuid(newSample.getAssociatedCase().getUuid()));
					} else if (newSample.getAssociatedContact() != null) {
						messageContent = String.format(
							I18nProperties.getString(MessagingService.CONTENT_LAB_SAMPLE_SHIPPED_SHORT_FOR_CONTACT),
							DataHelper.getShortUuid(newSample.getAssociatedContact().getUuid()));
					} else if (newSample.getAssociatedEventParticipant() != null) {
						messageContent = String.format(
							I18nProperties.getString(MessagingService.CONTENT_LAB_SAMPLE_SHIPPED_SHORT_FOR_EVENT_PARTICIPANT),
							DataHelper.getShortUuid(newSample.getAssociatedEventParticipant().getUuid()));
					}
					messagingService.sendMessage(recipient, MessageSubject.LAB_SAMPLE_SHIPPED, messageContent, MessageType.EMAIL, MessageType.SMS);

				} catch (NotificationDeliveryFailedException e) {
					logger.error(
						String.format(
							"EmailDeliveryFailedException when trying to notify supervisors about " + "the shipment of a lab sample. "
								+ "Failed to send " + e.getMessageType() + " to user with UUID %s.",
							recipient.getUuid()));
				}
			}
		}
	}

	@Override
	public boolean isDeleted(String sampleUuid) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Sample> from = cq.from(Sample.class);

		cq.where(cb.and(cb.isTrue(from.get(Sample.DELETED)), cb.equal(from.get(AbstractDomainObject.UUID), sampleUuid)));
		cq.select(cb.count(from));
		long count = em.createQuery(cq).getSingleResult();
		return count > 0;
	}

	@LocalBean
	@Stateless
	public static class SampleFacadeEjbLocal extends SampleFacadeEjb {

	}

	public Boolean isSampleEditAllowed(String sampleUuid) {
		Sample sample = sampleService.getByUuid(sampleUuid);

		if (sample.getSormasToSormasOriginInfo() != null) {
			return sample.getSormasToSormasOriginInfo().isOwnershipHandedOver();
		}

		return sampleJurisdictionChecker.isInJurisdictionOrOwned(sample) && !sormasToSormasShareInfoService.isSamlpeOwnershipHandedOver(sample);
	}
}
