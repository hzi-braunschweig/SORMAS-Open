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
package de.symeda.sormas.backend.caze;

import static de.symeda.sormas.backend.common.CriteriaBuilderHelper.and;
import static de.symeda.sormas.backend.common.CriteriaBuilderHelper.or;
import static de.symeda.sormas.backend.visit.VisitLogic.getVisitResult;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.CaseMeasure;
import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.DiseaseHelper;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.VisitOrigin;
import de.symeda.sormas.api.caze.AgeAndBirthDateDto;
import de.symeda.sormas.api.caze.BirthDateDto;
import de.symeda.sormas.api.caze.BurialInfoDto;
import de.symeda.sormas.api.caze.CaseBulkEditData;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.CaseCriteria;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseExportDto;
import de.symeda.sormas.api.caze.CaseExportType;
import de.symeda.sormas.api.caze.CaseFacade;
import de.symeda.sormas.api.caze.CaseFollowUpDto;
import de.symeda.sormas.api.caze.CaseIndexDetailedDto;
import de.symeda.sormas.api.caze.CaseIndexDto;
import de.symeda.sormas.api.caze.CaseLogic;
import de.symeda.sormas.api.caze.CaseOrigin;
import de.symeda.sormas.api.caze.CaseOutcome;
import de.symeda.sormas.api.caze.CasePersonDto;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.caze.CaseSimilarityCriteria;
import de.symeda.sormas.api.caze.EmbeddedSampleExportDto;
import de.symeda.sormas.api.caze.InvestigationStatus;
import de.symeda.sormas.api.caze.MapCaseDto;
import de.symeda.sormas.api.caze.NewCaseDateType;
import de.symeda.sormas.api.caze.PlagueType;
import de.symeda.sormas.api.caze.maternalhistory.MaternalHistoryDto;
import de.symeda.sormas.api.caze.porthealthinfo.PortHealthInfoDto;
import de.symeda.sormas.api.clinicalcourse.ClinicalCourseDto;
import de.symeda.sormas.api.clinicalcourse.ClinicalCourseReferenceDto;
import de.symeda.sormas.api.clinicalcourse.ClinicalVisitCriteria;
import de.symeda.sormas.api.clinicalcourse.ClinicalVisitDto;
import de.symeda.sormas.api.clinicalcourse.HealthConditionsDto;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.contact.ContactCriteria;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.contact.ContactReferenceDto;
import de.symeda.sormas.api.document.DocumentRelatedEntityType;
import de.symeda.sormas.api.epidata.EpiDataDto;
import de.symeda.sormas.api.epidata.EpiDataHelper;
import de.symeda.sormas.api.event.EventParticipantReferenceDto;
import de.symeda.sormas.api.exposure.ExposureDto;
import de.symeda.sormas.api.exposure.ExposureType;
import de.symeda.sormas.api.externaldata.ExternalDataDto;
import de.symeda.sormas.api.externaldata.ExternalDataUpdateException;
import de.symeda.sormas.api.externalsurveillancetool.ExternalSurveillanceToolException;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityHelper;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.followup.FollowUpDto;
import de.symeda.sormas.api.followup.FollowUpPeriodDto;
import de.symeda.sormas.api.hospitalization.PreviousHospitalizationDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.importexport.ExportConfigurationDto;
import de.symeda.sormas.api.infrastructure.InfrastructureHelper;
import de.symeda.sormas.api.location.LocationDto;
import de.symeda.sormas.api.messaging.ManualMessageLogDto;
import de.symeda.sormas.api.messaging.MessageType;
import de.symeda.sormas.api.person.ApproximateAgeType;
import de.symeda.sormas.api.person.CauseOfDeath;
import de.symeda.sormas.api.person.PersonContactDetailDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.api.person.PresentCondition;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.DistrictDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.sample.AdditionalTestDto;
import de.symeda.sormas.api.sample.PathogenTestDto;
import de.symeda.sormas.api.sample.PathogenTestResultType;
import de.symeda.sormas.api.sample.SampleCriteria;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import de.symeda.sormas.api.symptoms.SymptomsHelper;
import de.symeda.sormas.api.task.TaskContext;
import de.symeda.sormas.api.task.TaskCriteria;
import de.symeda.sormas.api.task.TaskHelper;
import de.symeda.sormas.api.task.TaskPriority;
import de.symeda.sormas.api.task.TaskStatus;
import de.symeda.sormas.api.task.TaskType;
import de.symeda.sormas.api.therapy.PrescriptionCriteria;
import de.symeda.sormas.api.therapy.PrescriptionDto;
import de.symeda.sormas.api.therapy.TherapyDto;
import de.symeda.sormas.api.therapy.TherapyReferenceDto;
import de.symeda.sormas.api.therapy.TreatmentCriteria;
import de.symeda.sormas.api.therapy.TreatmentDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DataHelper.Pair;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.InfoProvider;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.api.utils.fieldvisibility.FieldVisibilityCheckers;
import de.symeda.sormas.api.visit.VisitDto;
import de.symeda.sormas.api.visit.VisitResultDto;
import de.symeda.sormas.api.visit.VisitStatus;
import de.symeda.sormas.backend.caze.classification.CaseClassificationFacadeEjb.CaseClassificationFacadeEjbLocal;
import de.symeda.sormas.backend.caze.maternalhistory.MaternalHistoryFacadeEjb;
import de.symeda.sormas.backend.caze.maternalhistory.MaternalHistoryFacadeEjb.MaternalHistoryFacadeEjbLocal;
import de.symeda.sormas.backend.caze.porthealthinfo.PortHealthInfoFacadeEjb;
import de.symeda.sormas.backend.caze.porthealthinfo.PortHealthInfoFacadeEjb.PortHealthInfoFacadeEjbLocal;
import de.symeda.sormas.backend.clinicalcourse.ClinicalCourseFacadeEjb;
import de.symeda.sormas.backend.clinicalcourse.ClinicalCourseFacadeEjb.ClinicalCourseFacadeEjbLocal;
import de.symeda.sormas.backend.clinicalcourse.ClinicalVisit;
import de.symeda.sormas.backend.clinicalcourse.ClinicalVisitFacadeEjb;
import de.symeda.sormas.backend.clinicalcourse.ClinicalVisitFacadeEjb.ClinicalVisitFacadeEjbLocal;
import de.symeda.sormas.backend.clinicalcourse.ClinicalVisitService;
import de.symeda.sormas.backend.clinicalcourse.HealthConditions;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.ConfigFacadeEjb.ConfigFacadeEjbLocal;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.common.messaging.ManualMessageLogService;
import de.symeda.sormas.backend.common.messaging.MessageSubject;
import de.symeda.sormas.backend.common.messaging.MessagingService;
import de.symeda.sormas.backend.common.messaging.NotificationDeliveryFailedException;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactFacadeEjb;
import de.symeda.sormas.backend.contact.ContactFacadeEjb.ContactFacadeEjbLocal;
import de.symeda.sormas.backend.contact.ContactService;
import de.symeda.sormas.backend.contact.VisitSummaryExportDetails;
import de.symeda.sormas.backend.disease.DiseaseConfigurationFacadeEjb.DiseaseConfigurationFacadeEjbLocal;
import de.symeda.sormas.backend.document.Document;
import de.symeda.sormas.backend.document.DocumentService;
import de.symeda.sormas.backend.epidata.EpiData;
import de.symeda.sormas.backend.epidata.EpiDataFacadeEjb;
import de.symeda.sormas.backend.epidata.EpiDataFacadeEjb.EpiDataFacadeEjbLocal;
import de.symeda.sormas.backend.event.Event;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.event.EventParticipantService;
import de.symeda.sormas.backend.event.EventService;
import de.symeda.sormas.backend.event.EventSummaryDetails;
import de.symeda.sormas.backend.exposure.Exposure;
import de.symeda.sormas.backend.externaljournal.ExternalJournalService;
import de.symeda.sormas.backend.externalsurveillancetool.ExternalSurveillanceToolGatewayFacadeEjb.ExternalSurveillanceToolGatewayFacadeEjbLocal;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.facility.FacilityFacadeEjb;
import de.symeda.sormas.backend.facility.FacilityFacadeEjb.FacilityFacadeEjbLocal;
import de.symeda.sormas.backend.facility.FacilityService;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal;
import de.symeda.sormas.backend.hospitalization.Hospitalization;
import de.symeda.sormas.backend.hospitalization.HospitalizationFacadeEjb;
import de.symeda.sormas.backend.hospitalization.HospitalizationFacadeEjb.HospitalizationFacadeEjbLocal;
import de.symeda.sormas.backend.hospitalization.PreviousHospitalization;
import de.symeda.sormas.backend.infrastructure.PointOfEntry;
import de.symeda.sormas.backend.infrastructure.PointOfEntryFacadeEjb;
import de.symeda.sormas.backend.infrastructure.PointOfEntryService;
import de.symeda.sormas.backend.infrastructure.PopulationDataFacadeEjb.PopulationDataFacadeEjbLocal;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.outbreak.OutbreakService;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.person.PersonFacadeEjb;
import de.symeda.sormas.backend.person.PersonFacadeEjb.PersonFacadeEjbLocal;
import de.symeda.sormas.backend.person.PersonService;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.CommunityFacadeEjb;
import de.symeda.sormas.backend.region.CommunityFacadeEjb.CommunityFacadeEjbLocal;
import de.symeda.sormas.backend.region.CommunityService;
import de.symeda.sormas.backend.region.Country;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.DistrictFacadeEjb;
import de.symeda.sormas.backend.region.DistrictFacadeEjb.DistrictFacadeEjbLocal;
import de.symeda.sormas.backend.region.DistrictService;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.region.RegionFacadeEjb;
import de.symeda.sormas.backend.region.RegionService;
import de.symeda.sormas.backend.sample.AdditionalTest;
import de.symeda.sormas.backend.sample.AdditionalTestFacadeEjb.AdditionalTestFacadeEjbLocal;
import de.symeda.sormas.backend.sample.PathogenTest;
import de.symeda.sormas.backend.sample.PathogenTestFacadeEjb.PathogenTestFacadeEjbLocal;
import de.symeda.sormas.backend.sample.PathogenTestService;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.sample.SampleFacadeEjb;
import de.symeda.sormas.backend.sample.SampleFacadeEjb.SampleFacadeEjbLocal;
import de.symeda.sormas.backend.sample.SampleService;
import de.symeda.sormas.backend.share.ExternalShareInfoCountAndLatestDate;
import de.symeda.sormas.backend.share.ExternalShareInfoService;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfoFacadeEjb;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfoFacadeEjb.SormasToSormasOriginInfoFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoCase;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoHelper;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.symptoms.Symptoms;
import de.symeda.sormas.backend.symptoms.SymptomsFacadeEjb;
import de.symeda.sormas.backend.symptoms.SymptomsFacadeEjb.SymptomsFacadeEjbLocal;
import de.symeda.sormas.backend.task.Task;
import de.symeda.sormas.backend.task.TaskService;
import de.symeda.sormas.backend.therapy.Prescription;
import de.symeda.sormas.backend.therapy.PrescriptionFacadeEjb;
import de.symeda.sormas.backend.therapy.PrescriptionFacadeEjb.PrescriptionFacadeEjbLocal;
import de.symeda.sormas.backend.therapy.PrescriptionService;
import de.symeda.sormas.backend.therapy.TherapyFacadeEjb;
import de.symeda.sormas.backend.therapy.TherapyFacadeEjb.TherapyFacadeEjbLocal;
import de.symeda.sormas.backend.therapy.Treatment;
import de.symeda.sormas.backend.therapy.TreatmentFacadeEjb;
import de.symeda.sormas.backend.therapy.TreatmentFacadeEjb.TreatmentFacadeEjbLocal;
import de.symeda.sormas.backend.therapy.TreatmentService;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserFacadeEjb;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.IterableHelper;
import de.symeda.sormas.backend.util.JurisdictionHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.Pseudonymizer;
import de.symeda.sormas.backend.visit.Visit;
import de.symeda.sormas.backend.visit.VisitFacadeEjb;
import de.symeda.sormas.backend.visit.VisitFacadeEjb.VisitFacadeEjbLocal;
import de.symeda.sormas.backend.visit.VisitService;
import de.symeda.sormas.utils.CaseJoins;

@Stateless(name = "CaseFacade")
public class CaseFacadeEjb implements CaseFacade {

	private static final int ARCHIVE_BATCH_SIZE = 1000;
	private static final long SECONDS_30_DAYS = 30L * 24L * 60L * 60L;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private CaseClassificationFacadeEjbLocal caseClassificationFacade;
	@EJB
	private CaseService caseService;
	@EJB
	private CaseListCriteriaBuilder listQueryBuilder;
	@EJB
	private PersonService personService;
	@EJB
	private FacilityService facilityService;
	@EJB
	private UserService userService;
	@EJB
	private VisitService visitService;
	@EJB
	private VisitFacadeEjbLocal visitFacade;
	@EJB
	private SymptomsFacadeEjbLocal symptomsFacade;
	@EJB
	private RegionService regionService;
	@EJB
	private DistrictFacadeEjbLocal districtFacade;
	@EJB
	private DistrictService districtService;
	@EJB
	private CommunityFacadeEjbLocal communityFacade;
	@EJB
	private CommunityService communityService;
	@EJB
	private FacilityFacadeEjbLocal facilityFacade;
	@EJB
	private TaskService taskService;
	@EJB
	private ContactService contactService;
	@EJB
	private EventParticipantService eventParticipantService;
	@EJB
	private EventService eventService;
	@EJB
	private SampleService sampleService;
	@EJB
	private PathogenTestService pathogenTestService;
	@EJB
	private PathogenTestFacadeEjbLocal sampleTestFacade;
	@EJB
	private HospitalizationFacadeEjbLocal hospitalizationFacade;
	@EJB
	private EpiDataFacadeEjbLocal epiDataFacade;
	@EJB
	private ContactFacadeEjbLocal contactFacade;
	@EJB
	private SampleFacadeEjbLocal sampleFacade;
	@EJB
	private TreatmentFacadeEjbLocal treatmentFacade;
	@EJB
	private PrescriptionFacadeEjbLocal prescriptionFacade;
	@EJB
	private ClinicalVisitFacadeEjbLocal clinicalVisitFacade;
	@EJB
	private MessagingService messagingService;
	@EJB
	private PersonFacadeEjbLocal personFacade;
	@EJB
	private ConfigFacadeEjbLocal configFacade;
	@EJB
	private TherapyFacadeEjbLocal therapyFacade;
	@EJB
	private ClinicalCourseFacadeEjbLocal clinicalCourseFacade;
	@EJB
	private PrescriptionService prescriptionService;
	@EJB
	private TreatmentService treatmentService;
	@EJB
	private ClinicalVisitService clinicalVisitService;
	@EJB
	private OutbreakService outbreakService;
	@EJB
	private MaternalHistoryFacadeEjbLocal maternalHistoryFacade;
	@EJB
	private PointOfEntryService pointOfEntryService;
	@EJB
	private PortHealthInfoFacadeEjbLocal portHealthInfoFacade;
	@EJB
	private PopulationDataFacadeEjbLocal populationDataFacade;
	@EJB
	private FeatureConfigurationFacadeEjbLocal featureConfigurationFacade;
	@EJB
	private CaseJurisdictionChecker caseJurisdictionChecker;
	@EJB
	private SormasToSormasOriginInfoFacadeEjbLocal originInfoFacade;
	@EJB
	private ManualMessageLogService manualMessageLogService;
	@EJB
	private AdditionalTestFacadeEjbLocal additionalTestFacade;
	@EJB
	private ExternalJournalService externalJournalService;
	@EJB
	private DiseaseConfigurationFacadeEjbLocal diseaseConfigurationFacade;
	@EJB
	private ExternalSurveillanceToolGatewayFacadeEjbLocal externalSurveillanceToolGatewayFacade;
	@EJB
	private ExternalShareInfoService externalShareInfoService;
	@EJB
	private DocumentService documentService;

	@Override
	public List<CaseDataDto> getAllActiveCasesAfter(Date date) {
		return getAllActiveCasesAfter(date, false);
	}

	@Override
	public List<CaseDataDto> getAllActiveCasesAfter(Date date, boolean includeExtendedChangeDateFilters) {

		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return caseService.getAllActiveCasesAfter(date, includeExtendedChangeDateFilters)
			.stream()
			.map(c -> convertToDto(c, pseudonymizer))
			.collect(Collectors.toList());
	}

	@Override
	public List<CaseDataDto> getByUuids(List<String> uuids) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return caseService.getByUuids(uuids).stream().map(c -> convertToDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	public CaseDataDto getByUuid(String uuid) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return convertToDto(caseService.getByUuid(uuid), pseudonymizer);
	}

	public Page<CaseIndexDto> getIndexPage(CaseCriteria caseCriteria, Integer offset, Integer size, List<SortProperty> sortProperties) {
		List<CaseIndexDto> caseIndexList = getIndexList(caseCriteria, offset, size, sortProperties);
		long totalElementCount = count(caseCriteria);
		return new Page<CaseIndexDto>(caseIndexList, offset, size, totalElementCount);
	}

	@Override
	public String getUuidByUuidEpidNumberOrExternalId(String searchTerm) {
		return caseService.getUuidByUuidEpidNumberOrExternalId(searchTerm);
	}

	@Override
	public long count(CaseCriteria caseCriteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Case> root = cq.from(Case.class);

		final CaseQueryContext caseQueryContext = new CaseQueryContext(cb, cq, root);

		CaseUserFilterCriteria caseUserFilterCriteria = new CaseUserFilterCriteria();
		if (caseCriteria != null) {
			caseUserFilterCriteria.setIncludeCasesFromOtherJurisdictions(caseCriteria.getIncludeCasesFromOtherJurisdictions());
		}
		Predicate filter = caseService.createUserFilter(cb, cq, root, caseUserFilterCriteria);

		if (caseCriteria != null) {
			Predicate criteriaFilter = caseService.createCriteriaFilter(caseCriteria, caseQueryContext);
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
		}
		if (filter != null) {
			cq.where(filter);
		}

		cq.select(cb.countDistinct(root));
		return em.createQuery(cq).getSingleResult();
	}

	public Page<CaseIndexDetailedDto> getIndexDetailedPage(
		CaseCriteria caseCriteria,
		Integer offset,
		Integer size,
		List<SortProperty> sortProperties) {
		List<CaseIndexDetailedDto> caseIndexDetailedList = getIndexDetailedList(caseCriteria, offset * size, size, sortProperties);
		long totalElementCount = count(caseCriteria);
		return new Page<>(caseIndexDetailedList, offset, size, totalElementCount);
	}

	@Override
	public List<CaseIndexDto> getIndexList(CaseCriteria caseCriteria, Integer first, Integer max, List<SortProperty> sortProperties) {

		CriteriaQuery<CaseIndexDto> cq = listQueryBuilder.buildIndexCriteria(caseCriteria, sortProperties);

		List<CaseIndexDto> cases;
		if (first != null && max != null) {
			TypedQuery<CaseIndexDto> query = em.createQuery(cq);
			cases = query.setFirstResult(first).setMaxResults(max).getResultList();
		} else {
			cases = em.createQuery(cq).getResultList();
		}

		List<Long> caseIds = cases.stream().map(CaseIndexDto::getId).collect(Collectors.toList());

		Map<String, ExternalShareInfoCountAndLatestDate> survToolShareCountAndDates = null;
		if (externalSurveillanceToolGatewayFacade.isFeatureEnabled()) {
			survToolShareCountAndDates = externalShareInfoService.getCaseShareCountAndLatestDate(caseIds)
				.stream()
				.collect(Collectors.toMap(ExternalShareInfoCountAndLatestDate::getAssociatedObjectUuid, Function.identity()));
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue));
		for (CaseIndexDto caze : cases) {
			if (survToolShareCountAndDates != null) {
				ExternalShareInfoCountAndLatestDate survToolShareCountAndDate = survToolShareCountAndDates.get(caze.getUuid());

				if (survToolShareCountAndDate != null) {
					caze.setSurveillanceToolShareCount(survToolShareCountAndDate.getCount());
					caze.setSurveillanceToolLastShareDate(survToolShareCountAndDate.getLatestDate());
					caze.setSurveillanceToolStatus(survToolShareCountAndDate.getLatestStatus());
				}
			}

			Boolean isInJurisdiction = caseJurisdictionChecker.isInJurisdictionOrOwned(caze.getJurisdiction());
			pseudonymizer.pseudonymizeDto(CaseIndexDto.class, caze, isInJurisdiction, (c) -> {
				pseudonymizer.pseudonymizeDto(AgeAndBirthDateDto.class, caze.getAgeAndBirthDate(), isInJurisdiction, null);
			});
		}

		return cases;
	}

	@Override
	public List<CaseIndexDetailedDto> getIndexDetailedList(CaseCriteria caseCriteria, Integer first, Integer max, List<SortProperty> sortProperties) {

		CriteriaQuery<CaseIndexDetailedDto> cq = listQueryBuilder.buildIndexDetailedCriteria(caseCriteria, sortProperties);

		List<CaseIndexDetailedDto> cases;
		if (first != null && max != null) {
			cases = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
		} else {
			cases = em.createQuery(cq).getResultList();
		}

		// Load latest events info
		// Adding a second query here is not perfect, but selecting the last event with a criteria query
		// doesn't seem to be possible and using a native query is not an option because of user filters
		List<EventSummaryDetails> eventSummaries =
			eventService.getEventSummaryDetailsByCases(cases.stream().map(CaseIndexDetailedDto::getId).collect(Collectors.toList()));

		Map<String, ExternalShareInfoCountAndLatestDate> survToolShareCountAndDates = null;
		if (externalSurveillanceToolGatewayFacade.isFeatureEnabled()) {
			survToolShareCountAndDates =
				externalShareInfoService.getCaseShareCountAndLatestDate(cases.stream().map(CaseIndexDto::getId).collect(Collectors.toList()))
					.stream()
					.collect(Collectors.toMap(ExternalShareInfoCountAndLatestDate::getAssociatedObjectUuid, Function.identity()));
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue));
		for (CaseIndexDetailedDto caze : cases) {
			if (survToolShareCountAndDates != null) {
				ExternalShareInfoCountAndLatestDate survToolShareCountAndDate = survToolShareCountAndDates.get(caze.getUuid());

				if (survToolShareCountAndDate != null) {
					caze.setSurveillanceToolShareCount(survToolShareCountAndDate.getCount());
					caze.setSurveillanceToolLastShareDate(survToolShareCountAndDate.getLatestDate());
					caze.setSurveillanceToolStatus(survToolShareCountAndDate.getLatestStatus());
				}
			}

			if (caze.getEventCount() > 0) {
				eventSummaries.stream()
					.filter(v -> v.getCaseId() == caze.getId())
					.max(Comparator.comparing(EventSummaryDetails::getEventDate))
					.ifPresent(eventSummary -> {
						caze.setLatestEventId(eventSummary.getEventUuid());
						caze.setLatestEventStatus(eventSummary.getEventStatus());
						caze.setLatestEventTitle(eventSummary.getEventTitle());
					});
			}

			Boolean isInJurisdiction = caseJurisdictionChecker.isInJurisdictionOrOwned(caze.getJurisdiction());
			pseudonymizer.pseudonymizeDto(CaseIndexDetailedDto.class, caze, isInJurisdiction, (c) -> {
				pseudonymizer.pseudonymizeDto(AgeAndBirthDateDto.class, caze.getAgeAndBirthDate(), isInJurisdiction, null);
				pseudonymizer
					.pseudonymizeUser(userService.getByUuid(caze.getReportingUser().getUuid()), userService.getCurrentUser(), caze::setReportingUser);
			});
		}

		return cases;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<CaseExportDto> getExportList(
		CaseCriteria caseCriteria,
		Collection<String> selectedRows,
		CaseExportType exportType,
		int first,
		int max,
		ExportConfigurationDto exportConfiguration,
		Language userLanguage) {

		Boolean previousCaseManagementDataCriteria = caseCriteria.getMustHaveCaseManagementData();
		if (CaseExportType.CASE_MANAGEMENT == exportType) {
			caseCriteria.setMustHaveCaseManagementData(Boolean.TRUE);
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CaseExportDto> cq = cb.createQuery(CaseExportDto.class);
		Root<Case> caseRoot = cq.from(Case.class);

		final CaseQueryContext caseQueryContext = new CaseQueryContext(cb, cq, caseRoot);
		final CaseJoins<Case> joins = (CaseJoins<Case>) caseQueryContext.getJoins();

		// Events count subquery
		Subquery<Long> eventCountSq = cq.subquery(Long.class);
		Root<EventParticipant> eventCountRoot = eventCountSq.from(EventParticipant.class);
		Join<EventParticipant, Event> event = eventCountRoot.join(EventParticipant.EVENT, JoinType.INNER);
		Join<EventParticipant, Case> resultingCase = eventCountRoot.join(EventParticipant.RESULTING_CASE, JoinType.INNER);
		eventCountSq.where(
			cb.and(
				cb.equal(resultingCase.get(Case.ID), caseRoot.get(Case.ID)),
				cb.isFalse(event.get(Event.DELETED)),
				cb.isFalse(event.get(Event.ARCHIVED)),
				cb.isFalse(eventCountRoot.get(EventParticipant.DELETED))));
		eventCountSq.select(cb.countDistinct(event.get(Event.ID)));

		//@formatter:off
		cq.multiselect(caseRoot.get(Case.ID), joins.getPerson().get(Person.ID), joins.getPersonAddress().get(Location.ID),
				joins.getEpiData().get(EpiData.ID), joins.getSymptoms().get(Symptoms.ID), joins.getHospitalization().get(Hospitalization.ID),
				joins.getDistrict().get(District.ID), joins.getHealthConditions().get(HealthConditions.ID), caseRoot.get(Case.UUID),
				caseRoot.get(Case.EPID_NUMBER), caseRoot.get(Case.DISEASE), caseRoot.get(Case.DISEASE_VARIANT), caseRoot.get(Case.DISEASE_DETAILS),
				joins.getPerson().get(Person.FIRST_NAME), joins.getPerson().get(Person.LAST_NAME),
				joins.getPerson().get(Person.SALUTATION), joins.getPerson().get(Person.OTHER_SALUTATION), joins.getPerson().get(Person.SEX),
				caseRoot.get(Case.PREGNANT), joins.getPerson().get(Person.APPROXIMATE_AGE),
				joins.getPerson().get(Person.APPROXIMATE_AGE_TYPE), joins.getPerson().get(Person.BIRTHDATE_DD),
				joins.getPerson().get(Person.BIRTHDATE_MM), joins.getPerson().get(Person.BIRTHDATE_YYYY),
				caseRoot.get(Case.REPORT_DATE), joins.getReportingUser().get(User.UUID),
				joins.getRegion().get(Region.UUID), joins.getRegion().get(Region.NAME),
				joins.getDistrict().get(District.UUID), joins.getDistrict().get(District.NAME),
				joins.getCommunity().get(Community.UUID), joins.getCommunity().get(Community.NAME),
				caseRoot.get(Case.FACILITY_TYPE),
				joins.getFacility().get(Facility.NAME), joins.getFacility().get(Facility.UUID), caseRoot.get(Case.HEALTH_FACILITY_DETAILS),
				joins.getPointOfEntry().get(PointOfEntry.NAME), joins.getPointOfEntry().get(PointOfEntry.UUID), caseRoot.get(Case.POINT_OF_ENTRY_DETAILS),
				caseRoot.get(Case.CASE_CLASSIFICATION),
				caseRoot.get(Case.CLINICAL_CONFIRMATION), caseRoot.get(Case.EPIDEMIOLOGICAL_CONFIRMATION), caseRoot.get(Case.LABORATORY_DIAGNOSTIC_CONFIRMATION),
				caseRoot.get(Case.NOT_A_CASE_REASON_NEGATIVE_TEST),
				caseRoot.get(Case.NOT_A_CASE_REASON_PHYSICIAN_INFORMATION), caseRoot.get(Case.NOT_A_CASE_REASON_DIFFERENT_PATHOGEN),
				caseRoot.get(Case.NOT_A_CASE_REASON_OTHER), caseRoot.get(Case.NOT_A_CASE_REASON_DETAILS),
				caseRoot.get(Case.INVESTIGATION_STATUS), caseRoot.get(Case.OUTCOME), caseRoot.get(Case.OUTCOME_DATE),
				caseRoot.get(Case.BLOOD_ORGAN_OR_TISSUE_DONATED),
				caseRoot.get(Case.FOLLOW_UP_STATUS), caseRoot.get(Case.FOLLOW_UP_UNTIL),
				caseRoot.get(Case.NOSOCOMIAL_OUTBREAK), caseRoot.get(Case.INFECTION_SETTING),
				caseRoot.get(Case.RE_INFECTION), caseRoot.get(Case.PREVIOUS_INFECTION_DATE),
				// quarantine
				caseRoot.get(Case.QUARANTINE), caseRoot.get(Case.QUARANTINE_TYPE_DETAILS), caseRoot.get(Case.QUARANTINE_FROM), caseRoot.get(Case.QUARANTINE_TO),
				caseRoot.get(Case.QUARANTINE_ORDERED_VERBALLY),
				caseRoot.get(Case.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT),
				caseRoot.get(Case.QUARANTINE_ORDERED_VERBALLY_DATE),
				caseRoot.get(Case.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT_DATE),
				caseRoot.get(Case.QUARANTINE_EXTENDED),
				caseRoot.get(Case.QUARANTINE_REDUCED),
				caseRoot.get(Case.QUARANTINE_OFFICIAL_ORDER_SENT),
				caseRoot.get(Case.QUARANTINE_OFFICIAL_ORDER_SENT_DATE),

				joins.getHospitalization().get(Hospitalization.ADMITTED_TO_HEALTH_FACILITY), joins.getHospitalization().get(Hospitalization.ADMISSION_DATE),
				joins.getHospitalization().get(Hospitalization.DISCHARGE_DATE), joins.getHospitalization().get(Hospitalization.LEFT_AGAINST_ADVICE),
				joins.getPerson().get(Person.PRESENT_CONDITION), joins.getPerson().get(Person.DEATH_DATE), joins.getPerson().get(Person.BURIAL_DATE),
				joins.getPerson().get(Person.BURIAL_CONDUCTOR), joins.getPerson().get(Person.BURIAL_PLACE_DESCRIPTION),
				// address
				joins.getPersonAddressRegion().get(Region.NAME), joins.getPersonAddressDistrict().get(District.NAME), joins.getPersonAddressCommunity().get(Community.NAME),
				joins.getPersonAddress().get(Location.CITY), joins.getPersonAddress().get(Location.STREET), joins.getPersonAddress().get(Location.HOUSE_NUMBER),
				joins.getPersonAddress().get(Location.ADDITIONAL_INFORMATION), joins.getPersonAddress().get(Location.POSTAL_CODE),
				joins.getPersonAddressFacility().get(Facility.NAME), joins.getPersonAddressFacility().get(Facility.UUID), joins.getPersonAddress().get(Location.FACILITY_DETAILS),
				// phone
				caseQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_PHONE_SUBQUERY),
				caseQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_PHONE_OWNER_SUBQUERY),
				caseQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_EMAIL_SUBQUERY),
				caseQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_OTHER_CONTACT_DETAILS_SUBQUERY),
				joins.getPerson().get(Person.EDUCATION_TYPE),
				joins.getPerson().get(Person.EDUCATION_DETAILS), joins.getPerson().get(Person.OCCUPATION_TYPE),
				joins.getPerson().get(Person.OCCUPATION_DETAILS), joins.getPerson().get(Person.ARMED_FORCES_RELATION_TYPE), joins.getEpiData().get(EpiData.CONTACT_WITH_SOURCE_CASE_KNOWN),
				// vaccination
				caseRoot.get(Case.VACCINATION), caseRoot.get(Case.VACCINATION_DOSES), caseRoot.get(Case.VACCINATION_INFO_SOURCE),
				caseRoot.get(Case.FIRST_VACCINATION_DATE), caseRoot.get(Case.LAST_VACCINATION_DATE),
				caseRoot.get(Case.VACCINE_NAME), caseRoot.get(Case.OTHER_VACCINE_NAME),
				caseRoot.get(Case.VACCINE_MANUFACTURER), caseRoot.get(Case.OTHER_VACCINE_MANUFACTURER),
				caseRoot.get(Case.VACCINE_INN), caseRoot.get(Case.VACCINE_BATCH_NUMBER),
				caseRoot.get(Case.VACCINE_UNII_CODE), caseRoot.get(Case.VACCINE_ATC_CODE),
				// postpartum
				caseRoot.get(Case.POSTPARTUM), caseRoot.get(Case.TRIMESTER),
				eventCountSq,
				caseRoot.get(Case.EXTERNAL_ID),
				caseRoot.get(Case.EXTERNAL_TOKEN),
				caseRoot.get(Case.INTERNAL_TOKEN),
				joins.getPerson().get(Person.BIRTH_NAME),
				joins.getPersonBirthCountry().get(Country.ISO_CODE),
				joins.getPersonBirthCountry().get(Country.DEFAULT_NAME),
				joins.getPersonCitizenship().get(Country.ISO_CODE),
				joins.getPersonCitizenship().get(Country.DEFAULT_NAME),
				joins.getReportingDistrict().get(District.NAME),
				caseRoot.get(Case.CASE_IDENTIFICATION_SOURCE),
				caseRoot.get(Case.SCREENING_TYPE),
				// responsible jurisdiction
				joins.getResponsibleRegion().get(Region.UUID),
				joins.getResponsibleRegion().get(Region.NAME),
				joins.getResponsibleDistrict().get(District.UUID),
				joins.getResponsibleDistrict().get(District.NAME),
				joins.getResponsibleCommunity().get(Community.UUID),
				joins.getResponsibleCommunity().get(Community.NAME)
				);
		//@formatter:on

		cq.distinct(true);

		Predicate filter = caseService.createUserFilter(cb, cq, caseRoot);

		if (caseCriteria != null) {
			Predicate criteriaFilter = caseService.createCriteriaFilter(caseCriteria, caseQueryContext);
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
		}
		filter = CriteriaBuilderHelper.andInValues(selectedRows, filter, cb, caseRoot.get(Case.UUID));

		if (filter != null) {
			cq.where(filter);
		}

		/*
		 * Sort by report date DESC, but also by id for stable Sorting in case of equal report dates.
		 * Since this method supports paging, values might jump between pages when sorting is unstable.
		 */
		cq.orderBy(cb.desc(caseRoot.get(Case.REPORT_DATE)), cb.desc(caseRoot.get(Case.ID)));

		List<CaseExportDto> resultList =
			em.createQuery(cq).setHint(ModelConstants.HINT_HIBERNATE_READ_ONLY, true).setFirstResult(first).setMaxResults(max).getResultList();
		List<Long> resultCaseIds = resultList.stream().map(CaseExportDto::getId).collect(Collectors.toList());

		if (!resultList.isEmpty()) {
			Map<Long, Symptoms> symptoms = null;
			if (exportConfiguration == null || exportConfiguration.getProperties().contains(CaseDataDto.SYMPTOMS)) {
				List<Symptoms> symptomsList = null;
				CriteriaQuery<Symptoms> symptomsCq = cb.createQuery(Symptoms.class);
				Root<Symptoms> symptomsRoot = symptomsCq.from(Symptoms.class);
				Expression<String> symptomsIdsExpr = symptomsRoot.get(Symptoms.ID);
				symptomsCq.where(symptomsIdsExpr.in(resultList.stream().map(CaseExportDto::getSymptomsId).collect(Collectors.toList())));
				symptomsList = em.createQuery(symptomsCq).setHint(ModelConstants.HINT_HIBERNATE_READ_ONLY, true).getResultList();
				symptoms = symptomsList.stream().collect(Collectors.toMap(Symptoms::getId, Function.identity()));
			}

			Map<Long, Location> personAddresses = null;
			if (exportConfiguration == null
				|| exportConfiguration.getProperties().contains(PersonDto.ADDRESS)
				|| exportConfiguration.getProperties().contains(CaseExportDto.ADDRESS_GPS_COORDINATES)) {
				List<Location> personAddressesList = null;
				CriteriaQuery<Location> personAddressesCq = cb.createQuery(Location.class);
				Root<Location> personAddressesRoot = personAddressesCq.from(Location.class);
				Expression<String> personAddressesIdsExpr = personAddressesRoot.get(Location.ID);
				personAddressesCq
					.where(personAddressesIdsExpr.in(resultList.stream().map(CaseExportDto::getPersonAddressId).collect(Collectors.toList())));
				personAddressesList = em.createQuery(personAddressesCq).setHint(ModelConstants.HINT_HIBERNATE_READ_ONLY, true).getResultList();
				personAddresses = personAddressesList.stream().collect(Collectors.toMap(Location::getId, Function.identity()));
			}

			Map<Long, Integer> prescriptionCounts = null;
			Map<Long, Integer> treatmentCounts = null;
			Map<Long, Integer> clinicalVisitCounts = null;
			Map<Long, HealthConditions> healthConditions = null;
			if (exportType == null || exportType == CaseExportType.CASE_MANAGEMENT) {
				if (exportConfiguration == null || exportConfiguration.getProperties().contains(CaseExportDto.NUMBER_OF_PRESCRIPTIONS)) {
					prescriptionCounts = prescriptionService.getPrescriptionCountByCases(resultCaseIds)
						.stream()
						.collect(Collectors.toMap(e -> (Long) e[0], e -> ((Long) e[1]).intValue()));
				}
				if (exportConfiguration == null || exportConfiguration.getProperties().contains(CaseExportDto.NUMBER_OF_TREATMENTS)) {
					treatmentCounts = treatmentService.getTreatmentCountByCases(resultCaseIds)
						.stream()
						.collect(Collectors.toMap(e -> (Long) e[0], e -> ((Long) e[1]).intValue()));
				}
				if (exportConfiguration == null || exportConfiguration.getProperties().contains(CaseExportDto.NUMBER_OF_CLINICAL_VISITS)) {
					clinicalVisitCounts = clinicalVisitService.getClinicalVisitCountByCases(resultCaseIds)
						.stream()
						.collect(Collectors.toMap(e -> (Long) e[0], e -> ((Long) e[1]).intValue()));
				}
				if (exportConfiguration == null || exportConfiguration.getProperties().contains(ClinicalCourseDto.HEALTH_CONDITIONS)) {
					List<HealthConditions> healthConditionsList = null;
					CriteriaQuery<HealthConditions> healthConditionsCq = cb.createQuery(HealthConditions.class);
					Root<HealthConditions> healthConditionsRoot = healthConditionsCq.from(HealthConditions.class);
					Expression<String> healthConditionsIdsExpr = healthConditionsRoot.get(HealthConditions.ID);
					healthConditionsCq.where(
						healthConditionsIdsExpr.in(resultList.stream().map(CaseExportDto::getHealthConditionsId).collect(Collectors.toList())));
					healthConditionsList = em.createQuery(healthConditionsCq).setHint(ModelConstants.HINT_HIBERNATE_READ_ONLY, true).getResultList();
					healthConditions = healthConditionsList.stream().collect(Collectors.toMap(HealthConditions::getId, Function.identity()));
				}
			}

			Map<Long, PreviousHospitalization> firstPreviousHospitalizations = null;
			if (exportConfiguration == null || exportConfiguration.getProperties().contains(CaseExportDto.INITIAL_DETECTION_PLACE)) {
				List<PreviousHospitalization> prevHospsList = null;
				CriteriaQuery<PreviousHospitalization> prevHospsCq = cb.createQuery(PreviousHospitalization.class);
				Root<PreviousHospitalization> prevHospsRoot = prevHospsCq.from(PreviousHospitalization.class);
				Join<PreviousHospitalization, Hospitalization> prevHospsHospitalizationJoin =
					prevHospsRoot.join(PreviousHospitalization.HOSPITALIZATION, JoinType.LEFT);
				Expression<String> hospitalizationIdsExpr = prevHospsHospitalizationJoin.get(Hospitalization.ID);
				prevHospsCq
					.where(hospitalizationIdsExpr.in(resultList.stream().map(CaseExportDto::getHospitalizationId).collect(Collectors.toList())));
				prevHospsCq.orderBy(cb.asc(prevHospsRoot.get(PreviousHospitalization.ADMISSION_DATE)));
				prevHospsList = em.createQuery(prevHospsCq).setHint(ModelConstants.HINT_HIBERNATE_READ_ONLY, true).getResultList();
				firstPreviousHospitalizations =
					prevHospsList.stream().collect(Collectors.toMap(p -> p.getHospitalization().getId(), Function.identity(), (id1, id2) -> {
						return id1;
					}));
			}

			Map<Long, CaseClassification> sourceCaseClassifications = null;
			if (exportConfiguration == null || exportConfiguration.getProperties().contains(CaseExportDto.MAX_SOURCE_CASE_CLASSIFICATION)) {
				sourceCaseClassifications = contactService.getSourceCaseClassifications(resultCaseIds)
					.stream()
					.collect(
						Collectors
							.toMap(e -> (Long) e[0], e -> (CaseClassification) e[1], (c1, c2) -> c1.getSeverity() >= c2.getSeverity() ? c1 : c2));
			}

			List<Long> caseIdsWithOutbreak = null;
			if (exportConfiguration == null || exportConfiguration.getProperties().contains(CaseExportDto.ASSOCIATED_WITH_OUTBREAK)) {
				caseIdsWithOutbreak = outbreakService.getCaseIdsWithOutbreak(resultCaseIds);
			}

			Map<Long, List<Exposure>> exposures = null;
			if ((exportType == null || exportType == CaseExportType.CASE_SURVEILLANCE)
				&& (exportConfiguration == null
					|| exportConfiguration.getProperties().contains(CaseExportDto.TRAVELED)
					|| exportConfiguration.getProperties().contains(CaseExportDto.TRAVEL_HISTORY)
					|| exportConfiguration.getProperties().contains(CaseExportDto.BURIAL_ATTENDED))) {
				CriteriaQuery<Exposure> exposuresCq = cb.createQuery(Exposure.class);
				Root<Exposure> exposuresRoot = exposuresCq.from(Exposure.class);
				Join<Exposure, EpiData> exposuresEpiDataJoin = exposuresRoot.join(Exposure.EPI_DATA, JoinType.LEFT);
				Expression<String> epiDataIdsExpr = exposuresEpiDataJoin.get(EpiData.ID);
				Predicate exposuresPredicate = cb.and(
					epiDataIdsExpr.in(resultList.stream().map(CaseExportDto::getEpiDataId).collect(Collectors.toList())),
					cb.or(
						cb.equal(exposuresRoot.get(Exposure.EXPOSURE_TYPE), ExposureType.TRAVEL),
						cb.equal(exposuresRoot.get(Exposure.EXPOSURE_TYPE), ExposureType.BURIAL)));
				exposuresCq.where(exposuresPredicate);
				exposuresCq.orderBy(cb.asc(exposuresEpiDataJoin.get(EpiData.ID)));
				List<Exposure> exposureList = em.createQuery(exposuresCq).setHint(ModelConstants.HINT_HIBERNATE_READ_ONLY, true).getResultList();
				exposures = exposureList.stream().collect(Collectors.groupingBy(e -> e.getEpiData().getId()));
			}

			Map<Long, List<Sample>> samples = null;
			if ((exportType == null || exportType == CaseExportType.CASE_SURVEILLANCE)
				&& (exportConfiguration == null || exportConfiguration.getProperties().contains(CaseExportDto.SAMPLE_INFORMATION))) {
				List<Sample> samplesList = null;
				CriteriaQuery<Sample> samplesCq = cb.createQuery(Sample.class);
				Root<Sample> samplesRoot = samplesCq.from(Sample.class);
				Join<Sample, Case> samplesCaseJoin = samplesRoot.join(Sample.ASSOCIATED_CASE, JoinType.LEFT);
				Expression<String> caseIdsExpr = samplesCaseJoin.get(Case.ID);
				samplesCq.where(caseIdsExpr.in(resultList.stream().map(CaseExportDto::getId).collect(Collectors.toList())));
				samplesList = em.createQuery(samplesCq).setHint(ModelConstants.HINT_HIBERNATE_READ_ONLY, true).getResultList();
				samples = samplesList.stream().collect(Collectors.groupingBy(s -> s.getAssociatedCase().getId()));
			}

			List<VisitSummaryExportDetails> visitSummaries = null;
			if (featureConfigurationFacade.isFeatureEnabled(FeatureType.CASE_FOLLOWUP)
				&& (exportConfiguration == null
					|| exportConfiguration.getProperties().contains(CaseExportDto.NUMBER_OF_VISITS)
					|| exportConfiguration.getProperties().contains(CaseExportDto.LAST_COOPERATIVE_VISIT_DATE)
					|| exportConfiguration.getProperties().contains(CaseExportDto.LAST_COOPERATIVE_VISIT_SYMPTOMATIC)
					|| exportConfiguration.getProperties().contains(CaseExportDto.LAST_COOPERATIVE_VISIT_SYMPTOMS))) {
				CriteriaQuery<VisitSummaryExportDetails> visitsCq = cb.createQuery(VisitSummaryExportDetails.class);
				Root<Case> visitsCqRoot = visitsCq.from(Case.class);
				Join<Case, Visit> visitsJoin = visitsCqRoot.join(Case.VISITS, JoinType.LEFT);
				Join<Visit, Symptoms> visitSymptomsJoin = visitsJoin.join(Visit.SYMPTOMS, JoinType.LEFT);

				List<Long> exportCaseIds = resultList.stream().map(e -> e.getId()).collect(Collectors.toList());

				visitsCq.where(
					CriteriaBuilderHelper
						.and(cb, visitsCqRoot.get(AbstractDomainObject.ID).in(exportCaseIds), cb.isNotEmpty(visitsCqRoot.get(Case.VISITS))));
				visitsCq.multiselect(
					visitsCqRoot.get(AbstractDomainObject.ID),
					visitsJoin.get(Visit.VISIT_DATE_TIME),
					visitsJoin.get(Visit.VISIT_STATUS),
					visitSymptomsJoin);

				visitSummaries = em.createQuery(visitsCq).getResultList();
			}

			// Load latest events info
			// Adding a second query here is not perfect, but selecting the last event with a criteria query
			// doesn't seem to be possible and using a native query is not an option because of user filters
			List<EventSummaryDetails> eventSummaries = null;
			if (exportConfiguration == null
				|| exportConfiguration.getProperties().contains(CaseExportDto.LATEST_EVENT_ID)
				|| exportConfiguration.getProperties().contains(CaseExportDto.LATEST_EVENT_STATUS)
				|| exportConfiguration.getProperties().contains(CaseExportDto.LATEST_EVENT_TITLE)) {

				eventSummaries = eventService.getEventSummaryDetailsByCases(resultCaseIds);
			}

			Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue));

			for (CaseExportDto exportDto : resultList) {
				final boolean inJurisdiction = caseJurisdictionChecker.isInJurisdictionOrOwned(exportDto.getJurisdiction());

				if (exportConfiguration == null || exportConfiguration.getProperties().contains(CaseExportDto.COUNTRY)) {
					exportDto.setCountry(configFacade.getEpidPrefix());
				}
				if (symptoms != null) {
					Optional.ofNullable(symptoms.get(exportDto.getSymptomsId()))
						.ifPresent(symptom -> exportDto.setSymptoms(SymptomsFacadeEjb.toDto(symptom)));
				}
				if (personAddresses != null || exportConfiguration.getProperties().contains(CaseExportDto.ADDRESS_GPS_COORDINATES)) {
					Optional.ofNullable(personAddresses.get(exportDto.getPersonAddressId()))
						.ifPresent(personAddress -> exportDto.setAddressGpsCoordinates(personAddress.buildGpsCoordinatesCaption()));
				}
				if (prescriptionCounts != null) {
					Optional.ofNullable(prescriptionCounts.get(exportDto.getId()))
						.ifPresent(prescriptionCount -> exportDto.setNumberOfPrescriptions(prescriptionCount));
				}
				if (treatmentCounts != null) {
					Optional.ofNullable(treatmentCounts.get(exportDto.getId()))
						.ifPresent(treatmentCount -> exportDto.setNumberOfTreatments(treatmentCount));
				}
				if (clinicalVisitCounts != null) {
					Optional.ofNullable(clinicalVisitCounts.get(exportDto.getId()))
						.ifPresent(clinicalVisitCount -> exportDto.setNumberOfClinicalVisits(clinicalVisitCount));
				}
				if (healthConditions != null) {
					Optional.ofNullable(healthConditions.get(exportDto.getHealthConditionsId()))
						.ifPresent(healthCondition -> exportDto.setHealthConditions(ClinicalCourseFacadeEjb.toHealthConditionsDto(healthCondition)));
				}
				if (firstPreviousHospitalizations != null) {
					Optional.ofNullable(firstPreviousHospitalizations.get(exportDto.getHospitalizationId()))
						.ifPresent(
							firstPreviousHospitalization -> exportDto.setInitialDetectionPlace(
								FacilityHelper.buildFacilityString(
									firstPreviousHospitalization.getHealthFacility().getUuid(),
									firstPreviousHospitalization.getHealthFacility().getName(),
									firstPreviousHospitalization.getHealthFacilityDetails())));
					if (StringUtils.isEmpty(exportDto.getInitialDetectionPlace())) {
						if (!StringUtils.isEmpty(exportDto.getHealthFacility())) {
							exportDto.setInitialDetectionPlace(exportDto.getHealthFacility());
						} else {
							exportDto.setInitialDetectionPlace(exportDto.getPointOfEntry());
						}
					}
				}
				if (sourceCaseClassifications != null) {
					Optional.ofNullable(sourceCaseClassifications.get(exportDto.getId()))
						.ifPresent(sourceCaseClassification -> exportDto.setMaxSourceCaseClassification(sourceCaseClassification));
				}
				if (caseIdsWithOutbreak != null) {
					exportDto.setAssociatedWithOutbreak(caseIdsWithOutbreak.contains(exportDto.getId()));
				}
				if (exposures != null) {
					Optional.ofNullable(exposures.get(exportDto.getEpiDataId())).ifPresent(caseExposures -> {
						StringBuilder travelHistoryBuilder = new StringBuilder();
						if (caseExposures.stream().anyMatch(e -> ExposureType.BURIAL.equals(e.getExposureType()))) {
							exportDto.setBurialAttended(true);
						}
						caseExposures.stream().filter(e -> ExposureType.TRAVEL.equals(e.getExposureType())).forEach(exposure -> {
							travelHistoryBuilder.append(
								EpiDataHelper.buildDetailedTravelString(
									exposure.getLocation().toString(),
									exposure.getDescription(),
									exposure.getStartDate(),
									exposure.getEndDate(),
									userLanguage))
								.append(", ");
						});
						if (travelHistoryBuilder.length() > 0) {
							exportDto.setTraveled(true);
							travelHistoryBuilder.delete(travelHistoryBuilder.lastIndexOf(", "), travelHistoryBuilder.length() - 1);
						}
						exportDto.setTravelHistory(travelHistoryBuilder.toString());
					});
				}
				if (samples != null) {
					Optional.ofNullable(samples.get(exportDto.getId())).ifPresent(caseSamples -> {
						int count = 0;
						caseSamples.sort((o1, o2) -> o2.getSampleDateTime().compareTo(o1.getSampleDateTime()));
						for (Sample sample : caseSamples) {
							EmbeddedSampleExportDto sampleDto = new EmbeddedSampleExportDto(
								sample.getUuid(),
								sample.getSampleDateTime(),
								sample.getLab() != null
									? FacilityHelper.buildFacilityString(sample.getLab().getUuid(), sample.getLab().getName(), sample.getLabDetails())
									: null,
								sample.getPathogenTestResult());

							switch (++count) {
							case 1:
								exportDto.setSample1(sampleDto);
								break;
							case 2:
								exportDto.setSample2(sampleDto);
								break;
							case 3:
								exportDto.setSample3(sampleDto);
							default:
								exportDto.addOtherSample(sampleDto);
								break;
							}
						}
					});
				}

				if (visitSummaries != null) {
					List<VisitSummaryExportDetails> visits =
						visitSummaries.stream().filter(v -> v.getContactId() == exportDto.getId()).collect(Collectors.toList());

					VisitSummaryExportDetails lastCooperativeVisit = visits.stream()
						.filter(v -> v.getVisitStatus() == VisitStatus.COOPERATIVE)
						.max(Comparator.comparing(VisitSummaryExportDetails::getVisitDateTime))
						.orElse(null);

					exportDto.setNumberOfVisits(visits.size());
					if (lastCooperativeVisit != null) {
						exportDto.setLastCooperativeVisitDate(lastCooperativeVisit.getVisitDateTime());

						SymptomsDto visitSymptoms = SymptomsFacadeEjb.toDto(lastCooperativeVisit.getSymptoms());
						pseudonymizer.pseudonymizeDto(SymptomsDto.class, visitSymptoms, inJurisdiction, null);

						exportDto.setLastCooperativeVisitSymptoms(SymptomsHelper.buildSymptomsHumanString(visitSymptoms, true, userLanguage));
						exportDto.setLastCooperativeVisitSymptomatic(visitSymptoms.getSymptomatic() ? YesNoUnknown.YES : YesNoUnknown.NO);
					}
				}

				if (eventSummaries != null && exportDto.getEventCount() != 0) {
					eventSummaries.stream()
						.filter(v -> v.getCaseId() == exportDto.getId())
						.max(Comparator.comparing(EventSummaryDetails::getEventDate))
						.ifPresent(eventSummary -> {
							exportDto.setLatestEventId(eventSummary.getEventUuid());
							exportDto.setLatestEventStatus(eventSummary.getEventStatus());
							exportDto.setLatestEventTitle(eventSummary.getEventTitle());
						});
				}

				pseudonymizer.pseudonymizeDto(CaseExportDto.class, exportDto, inJurisdiction, (c) -> {
					pseudonymizer.pseudonymizeDto(BirthDateDto.class, c.getBirthdate(), inJurisdiction, null);
					pseudonymizer.pseudonymizeDto(EmbeddedSampleExportDto.class, c.getSample1(), inJurisdiction, null);
					pseudonymizer.pseudonymizeDto(EmbeddedSampleExportDto.class, c.getSample2(), inJurisdiction, null);
					pseudonymizer.pseudonymizeDto(EmbeddedSampleExportDto.class, c.getSample3(), inJurisdiction, null);
					pseudonymizer.pseudonymizeDtoCollection(EmbeddedSampleExportDto.class, c.getOtherSamples(), s -> inJurisdiction, null);
					pseudonymizer.pseudonymizeDto(BurialInfoDto.class, c.getBurialInfo(), inJurisdiction, null);
					pseudonymizer.pseudonymizeDto(SymptomsDto.class, c.getSymptoms(), inJurisdiction, null);
				});
			}
		}

		caseCriteria.setMustHaveCaseManagementData(previousCaseManagementDataCriteria);
		return resultList;
	}

	@Override
	public List<String> getAllActiveUuids() {

		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}

		return caseService.getAllActiveUuids();
	}

	public Long countCasesForMap(
		RegionReferenceDto regionRef,
		DistrictReferenceDto districtRef,
		Disease disease,
		Date from,
		Date to,
		NewCaseDateType dateType) {
		Region region = regionService.getByReferenceDto(regionRef);
		District district = districtService.getByReferenceDto(districtRef);

		return caseService.countCasesForMap(region, district, disease, from, to, dateType);
	}

	@Override
	public List<MapCaseDto> getCasesForMap(
		RegionReferenceDto regionRef,
		DistrictReferenceDto districtRef,
		Disease disease,
		Date from,
		Date to,
		NewCaseDateType dateType) {

		Region region = regionService.getByReferenceDto(regionRef);
		District district = districtService.getByReferenceDto(districtRef);

		List<MapCaseDto> cases = caseService.getCasesForMap(region, district, disease, from, to, dateType);

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		pseudonymizer.pseudonymizeDtoCollection(
			MapCaseDto.class,
			cases,
			c -> caseJurisdictionChecker.isInJurisdictionOrOwned(c.getJurisdiction()),
			(c, isInJurisdiction) -> {
				pseudonymizer.pseudonymizeDto(PersonReferenceDto.class, c.getPerson(), isInJurisdiction, null);
			});

		return cases;
	}

	@Override
	public List<CaseDataDto> getAllCasesOfPerson(String personUuid) {

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);

		return caseService.findBy(new CaseCriteria().person(new PersonReferenceDto(personUuid)), false)
			.stream()
			.map(c -> convertToDto(c, pseudonymizer))
			.collect(Collectors.toList());
	}

	@Override
	public List<CaseReferenceDto> getRandomCaseReferences(CaseCriteria criteria, int count, Random randomGenerator) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Case> caze = cq.from(Case.class);

		final CaseQueryContext caseQueryContext = new CaseQueryContext(cb, cq, caze);
		final CaseJoins<Case> joins = (CaseJoins<Case>) caseQueryContext.getJoins();

		Predicate filter = caseService.createUserFilter(cb, cq, caze, new CaseUserFilterCriteria().excludeCasesFromContacts(true));
		filter = CriteriaBuilderHelper.and(cb, filter, caseService.createCriteriaFilter(criteria, caseQueryContext));
		if (filter != null) {
			cq.where(filter);
		}

		cq.orderBy(cb.desc(caze.get(Case.UUID)));
		cq.select(caze.get(Case.UUID));

		List<String> uuids = em.createQuery(cq).getResultList();
		if (uuids.isEmpty()) {
			return null;
		}

		return randomGenerator.ints(count, 0, uuids.size()).mapToObj(i -> new CaseReferenceDto(uuids.get(i))).collect(Collectors.toList());
	}

	@Override
	public List<CaseIndexDto> getSimilarCases(CaseSimilarityCriteria criteria) {

		CaseCriteria caseCriteria = criteria.getCaseCriteria();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CaseIndexDto> cq = cb.createQuery(CaseIndexDto.class);
		Root<Case> root = cq.from(Case.class);
		Join<Case, Person> person = root.join(Case.PERSON, JoinType.LEFT);
		Join<Case, Region> region = root.join(Case.REGION, JoinType.LEFT);

		selectIndexDtoFields(cq, root);

		Predicate userFilter = caseService.createUserFilter(cb, cq, root);

		// In case you wonder: At this point in time the **person** duplicate check has already happen.
		// Here, we really just check if there is a similar case to the current one, therefore it is allowed to just
		// check if a case exists which references the same person to make sure that we are really talking about
		// the same case.
		Predicate personSimilarityFilter = criteria.getPersonUuid() != null ? cb.equal(person.get(Person.UUID), criteria.getPersonUuid()) : null;

		Predicate diseaseFilter = caseCriteria.getDisease() != null ? cb.equal(root.get(Case.DISEASE), caseCriteria.getDisease()) : null;
		Predicate regionFilter = caseCriteria.getRegion() != null ? cb.equal(region.get(Region.UUID), caseCriteria.getRegion().getUuid()) : null;
		Predicate reportDateFilter = criteria.getReportDate() != null
			? cb.between(
				root.get(Case.REPORT_DATE),
				DateHelper.subtractDays(criteria.getReportDate(), 30),
				DateHelper.addDays(criteria.getReportDate(), 30))
			: null;

		Predicate filter = caseService.createDefaultFilter(cb, root);
		filter = CriteriaBuilderHelper.and(cb, filter, userFilter);
		filter = CriteriaBuilderHelper.and(cb, filter, personSimilarityFilter);
		filter = CriteriaBuilderHelper.and(cb, filter, diseaseFilter);
		filter = CriteriaBuilderHelper.and(cb, filter, regionFilter);
		filter = CriteriaBuilderHelper.and(cb, filter, reportDateFilter);

		cq.where(filter);

		return em.createQuery(cq).getResultList();
	}

	@Override
	public List<CaseIndexDto[]> getCasesForDuplicateMerging(CaseCriteria criteria, boolean ignoreRegion) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<Case> root = cq.from(Case.class);
		final CaseQueryContext caseQueryContext = new CaseQueryContext(cb, cq, root);
		final CaseJoins<Case> joins = (CaseJoins<Case>) caseQueryContext.getJoins();

		Root<Case> root2 = cq.from(Case.class);
		Join<Case, Person> person = joins.getPerson();
		Join<Case, Person> person2 = root2.join(Case.PERSON, JoinType.LEFT);
		Join<Case, Region> region = joins.getRegion();
		Join<Case, Region> region2 = root2.join(Case.REGION, JoinType.LEFT);
		Join<Case, Symptoms> symptoms = joins.getSymptoms();
		Join<Case, Symptoms> symptoms2 = root2.join(Case.SYMPTOMS, JoinType.LEFT);

		cq.distinct(true);

		// similarity:
		// * first & last name concatenated with whitespace. Similarity function with default threshold of 0.65D
		// uses postgres pg_trgm: https://www.postgresql.org/docs/9.6/pgtrgm.html
		// * same disease
		// * same region (optional)
		// * report date within 30 days of each other
		// * same sex or same birth date (when defined)
		// * same birth date (when fully defined)
		// * onset date within 30 days of each other (when defined)

		Predicate userFilter = caseService.createUserFilter(cb, cq, root);
		Predicate criteriaFilter = criteria != null ? caseService.createCriteriaFilter(criteria, caseQueryContext) : null;
		Expression<String> nameSimilarityExpr = cb.concat(person.get(Person.FIRST_NAME), " ");
		nameSimilarityExpr = cb.concat(nameSimilarityExpr, person.get(Person.LAST_NAME));
		Expression<String> nameSimilarityExpr2 = cb.concat(person2.get(Person.FIRST_NAME), " ");
		nameSimilarityExpr2 = cb.concat(nameSimilarityExpr2, person2.get(Person.LAST_NAME));
		Predicate nameSimilarityFilter =
			cb.gt(cb.function("similarity", double.class, nameSimilarityExpr, nameSimilarityExpr2), configFacade.getNameSimilarityThreshold());
		Predicate diseaseFilter = cb.equal(root.get(Case.DISEASE), root2.get(Case.DISEASE));
		Predicate regionFilter = cb.equal(region.get(Region.ID), region2.get(Region.ID));
		Predicate reportDateFilter = cb.lessThanOrEqualTo(
			cb.abs(
				cb.diff(
					cb.function("date_part", Long.class, cb.parameter(String.class, "date_type"), root.get(Case.REPORT_DATE)),
					cb.function("date_part", Long.class, cb.parameter(String.class, "date_type"), root2.get(Case.REPORT_DATE)))),
			SECONDS_30_DAYS);

		// // todo this should use PersonService.buildSimilarityCriteriaFilter
		// Sex filter: only when sex is filled in for both cases
		Predicate sexFilter = cb.or(
			cb.or(cb.isNull(person.get(Person.SEX)), cb.isNull(person2.get(Person.SEX))),
			cb.or(cb.equal(person.get(Person.SEX), Sex.UNKNOWN), cb.equal(person2.get(Person.SEX), Sex.UNKNOWN)),
			cb.equal(person.get(Person.SEX), person2.get(Person.SEX)));
		// Birth date filter: only when birth date is filled in for both cases
		Predicate birthDateFilter = cb.or(
			cb.or(
				cb.isNull(person.get(Person.BIRTHDATE_DD)),
				cb.isNull(person.get(Person.BIRTHDATE_MM)),
				cb.isNull(person.get(Person.BIRTHDATE_YYYY)),
				cb.isNull(person2.get(Person.BIRTHDATE_DD)),
				cb.isNull(person2.get(Person.BIRTHDATE_MM)),
				cb.isNull(person2.get(Person.BIRTHDATE_YYYY))),
			cb.and(
				cb.equal(person.get(Person.BIRTHDATE_DD), person2.get(Person.BIRTHDATE_DD)),
				cb.equal(person.get(Person.BIRTHDATE_MM), person2.get(Person.BIRTHDATE_MM)),
				cb.equal(person.get(Person.BIRTHDATE_YYYY), person2.get(Person.BIRTHDATE_YYYY))));
		// Onset date filter: only when onset date is filled in for both cases
		Predicate onsetDateFilter = cb.or(
			cb.or(cb.isNull(symptoms.get(Symptoms.ONSET_DATE)), cb.isNull(symptoms2.get(Symptoms.ONSET_DATE))),
			cb.lessThanOrEqualTo(
				cb.abs(
					cb.diff(
						cb.function("date_part", Long.class, cb.parameter(String.class, "date_type"), symptoms.get(Symptoms.ONSET_DATE)),
						cb.function("date_part", Long.class, cb.parameter(String.class, "date_type"), symptoms2.get(Symptoms.ONSET_DATE)))),
				SECONDS_30_DAYS));

		Predicate creationDateFilter = cb.or(
			cb.lessThan(root.get(Case.CREATION_DATE), root2.get(Case.CREATION_DATE)),
			cb.or(
				cb.lessThanOrEqualTo(root2.get(Case.CREATION_DATE), DateHelper.getStartOfDay(criteria.getCreationDateFrom())),
				cb.greaterThanOrEqualTo(root2.get(Case.CREATION_DATE), DateHelper.getEndOfDay(criteria.getCreationDateTo()))));

		Predicate filter = cb.and(caseService.createDefaultFilter(cb, root), caseService.createDefaultFilter(cb, root2));
		if (userFilter != null) {
			filter = cb.and(filter, userFilter);
		}
		if (filter != null) {
			filter = cb.and(filter, criteriaFilter);
		} else {
			filter = criteriaFilter;
		}
		if (filter != null) {
			filter = cb.and(filter, nameSimilarityFilter);
		} else {
			filter = nameSimilarityFilter;
		}
		filter = cb.and(filter, diseaseFilter);

		if (!ignoreRegion) {
			filter = cb.and(filter, regionFilter);
		}

		filter = cb.and(filter, reportDateFilter);
		filter = cb.and(filter, cb.and(sexFilter, birthDateFilter));
		filter = cb.and(filter, onsetDateFilter);
		filter = cb.and(filter, creationDateFilter);
		filter = cb.and(filter, cb.notEqual(root.get(Case.ID), root2.get(Case.ID)));

		cq.where(filter);
		cq.multiselect(root.get(Case.ID), root2.get(Case.ID), root.get(Case.CREATION_DATE));
		cq.orderBy(cb.desc(root.get(Case.CREATION_DATE)));

		List<Object[]> foundIds = em.createQuery(cq).setParameter("date_type", "epoch").getResultList();
		List<CaseIndexDto[]> resultList = new ArrayList<>();

		if (!foundIds.isEmpty()) {
			CriteriaQuery<CaseIndexDto> indexCasesCq = cb.createQuery(CaseIndexDto.class);
			Root<Case> indexRoot = indexCasesCq.from(Case.class);
			selectIndexDtoFields(indexCasesCq, indexRoot);
			indexCasesCq.where(
				indexRoot.get(Case.ID).in(foundIds.stream().map(a -> Arrays.copyOf(a, 2)).flatMap(Arrays::stream).collect(Collectors.toSet())));
			Map<Long, CaseIndexDto> indexCases =
				em.createQuery(indexCasesCq).getResultStream().collect(Collectors.toMap(c -> c.getId(), Function.identity()));

			for (Object[] idPair : foundIds) {
				try {
					// Cloning is necessary here to allow us to add the same CaseIndexDto to the grid multiple times
					CaseIndexDto parent = (CaseIndexDto) indexCases.get(idPair[0]).clone();
					CaseIndexDto child = (CaseIndexDto) indexCases.get(idPair[1]).clone();

					if (parent.getCompleteness() == null && child.getCompleteness() == null
						|| parent.getCompleteness() != null
							&& (child.getCompleteness() == null || (parent.getCompleteness() >= child.getCompleteness()))) {
						resultList.add(
							new CaseIndexDto[] {
								parent,
								child });
					} else {
						resultList.add(
							new CaseIndexDto[] {
								child,
								parent });
					}
				} catch (CloneNotSupportedException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return resultList;
	}

	public void updateCompleteness(String caseUuid) {
		caseService.updateCompleteness(caseUuid);
	}

	private void selectIndexDtoFields(CriteriaQuery<CaseIndexDto> cq, Root<Case> root) {
		cq.multiselect(listQueryBuilder.getCaseIndexSelections(root, new CaseJoins<>(root)));
	}

	@Override
	public CaseDataDto getCaseDataByUuid(String uuid) {
		return convertToDto(caseService.getByUuid(uuid), Pseudonymizer.getDefault(userService::hasRight));
	}

	private CaseDataDto getCaseDataWithoutPseudonyimization(String uuid) {
		return toDto(caseService.getByUuid(uuid));
	}

	@Override
	public CaseReferenceDto getReferenceByUuid(String uuid) {
		return convertToReferenceDto(caseService.getByUuid(uuid));
	}

	@Override
	public CaseDataDto saveCase(@Valid CaseDataDto dto) throws ValidationRuntimeException {
		return saveCase(dto, true, true);
	}

	public void saveBulkCase(
		List<String> caseUuidList,
		CaseBulkEditData updatedCaseBulkEditData,
		boolean diseaseChange,
		boolean classificationChange,
		boolean investigationStatusChange,
		boolean outcomeChange,
		boolean surveillanceOfficerChange)
		throws ValidationRuntimeException {

		for (String caseUuid : caseUuidList) {
			Case caze = caseService.getByUuid(caseUuid);
			CaseDataDto existingCaseDto = toDto(caze);

			updateCaseWithBulkData(
				updatedCaseBulkEditData,
				caze,
				diseaseChange,
				classificationChange,
				investigationStatusChange,
				outcomeChange,
				surveillanceOfficerChange);
			doSave(caze, true, existingCaseDto);
		}
	}

	public void saveBulkEditWithFacilities(
		List<String> caseUuidList,
		CaseBulkEditData updatedCaseBulkEditData,
		boolean diseaseChange,
		boolean classificationChange,
		boolean investigationStatusChange,
		boolean outcomeChange,
		boolean surveillanceOfficerChange,
		Boolean doTransfer) {

		Region newRegion = regionService.getByUuid(updatedCaseBulkEditData.getRegion().getUuid());
		District newDistrict = districtService.getByUuid(updatedCaseBulkEditData.getDistrict().getUuid());
		CommunityReferenceDto communityDto = updatedCaseBulkEditData.getCommunity();
		Community newCommunity = null;
		if (communityDto != null) {
			newCommunity = communityService.getByUuid(updatedCaseBulkEditData.getCommunity().getUuid());
		}

		for (String caseUuid : caseUuidList) {
			Case caze = caseService.getByUuid(caseUuid);
			CaseDataDto existingCaseDto = toDto(caze);

			updateCaseWithBulkData(
				updatedCaseBulkEditData,
				caze,
				diseaseChange,
				classificationChange,
				investigationStatusChange,
				outcomeChange,
				surveillanceOfficerChange);

			caze.setRegion(newRegion);
			caze.setDistrict(newDistrict);
			caze.setCommunity(newCommunity);
			caze.setFacilityType(updatedCaseBulkEditData.getFacilityType());
			caze.setHealthFacility(facilityService.getByUuid(updatedCaseBulkEditData.getHealthFacility().getUuid()));
			caze.setHealthFacilityDetails(updatedCaseBulkEditData.getHealthFacilityDetails());
			CaseLogic.handleHospitalization(toDto(caze), existingCaseDto, doTransfer);
			doSave(caze, true, existingCaseDto);
		}
	}

	private void updateCaseWithBulkData(
		CaseBulkEditData updatedCaseBulkEditData,
		Case existingCase,
		boolean diseaseChange,
		boolean classificationChange,
		boolean investigationStatusChange,
		boolean outcomeChange,
		boolean surveillanceOfficerChange) {

		if (diseaseChange) {
			existingCase.setDisease(updatedCaseBulkEditData.getDisease());
			existingCase.setDiseaseVariant(updatedCaseBulkEditData.getDiseaseVariant());
			existingCase.setDiseaseDetails(updatedCaseBulkEditData.getDiseaseDetails());
			existingCase.setPlagueType(updatedCaseBulkEditData.getPlagueType());
			existingCase.setDengueFeverType(updatedCaseBulkEditData.getDengueFeverType());
			existingCase.setRabiesType(updatedCaseBulkEditData.getRabiesType());
		}
		if (classificationChange) {
			existingCase.setCaseClassification(updatedCaseBulkEditData.getCaseClassification());
		}
		if (investigationStatusChange) {
			existingCase.setInvestigationStatus(updatedCaseBulkEditData.getInvestigationStatus());
		}
		if (outcomeChange) {
			existingCase.setOutcome(updatedCaseBulkEditData.getOutcome());
		}
		// Setting the surveillance officer is only allowed if all selected cases are in
		// the same district
		if (surveillanceOfficerChange) {
			existingCase.setSurveillanceOfficer(userService.getByUuid(updatedCaseBulkEditData.getSurveillanceOfficer().getUuid()));
		}

		if (Objects.nonNull(updatedCaseBulkEditData.getHealthFacilityDetails())) {
			existingCase.setHealthFacilityDetails(updatedCaseBulkEditData.getHealthFacilityDetails());
		}

		if (updatedCaseBulkEditData.getDontShareWithReportingTool() != null) {
			existingCase.setDontShareWithReportingTool(updatedCaseBulkEditData.getDontShareWithReportingTool());
		}
	}

	public CaseDataDto saveCase(CaseDataDto dto, boolean handleChanges, boolean checkChangeDate) throws ValidationRuntimeException {

		Case caze = caseService.getByUuid(dto.getUuid());
		CaseDataDto existingCaseDto = handleChanges ? toDto(caze) : null;

		return caseSave(dto, handleChanges, caze, existingCaseDto, checkChangeDate);
	}

	private CaseDataDto caseSave(CaseDataDto dto, boolean handleChanges, Case caze, CaseDataDto existingCaseDto, boolean checkChangeDate) {
		SymptomsHelper.updateIsSymptomatic(dto.getSymptoms());

		restorePseudonymizedDto(dto, caze, existingCaseDto);

		validate(dto);

		externalJournalService.handleExternalJournalPersonUpdateAsync(dto.getPerson());

		caze = fillOrBuildEntity(dto, caze, checkChangeDate);

		// Set version number on a new case
		if (caze.getCreationDate() == null && StringUtils.isEmpty(dto.getCreationVersion())) {
			caze.setCreationVersion(InfoProvider.get().getVersion());
		}

		doSave(caze, handleChanges, existingCaseDto);

		return convertToDto(caze, Pseudonymizer.getDefault(userService::hasRight));
	}

	private void doSave(Case caze, boolean handleChanges, CaseDataDto existingCaseDto) {
		caseService.ensurePersisted(caze);
		if (handleChanges) {
			updateCaseVisitAssociations(existingCaseDto, caze);
			caseService.updateFollowUpDetails(caze, existingCaseDto != null && caze.getFollowUpStatus() != existingCaseDto.getFollowUpStatus());

			onCaseChanged(existingCaseDto, caze);
		}
	}

	private void updateCaseVisitAssociations(CaseDataDto existingCase, Case caze) {

		if (existingCase != null
			&& existingCase.getReportDate() == caze.getReportDate()
			&& existingCase.getFollowUpUntil() == caze.getFollowUpUntil()
			&& existingCase.getDisease() == caze.getDisease()) {
			return;
		}

		if (existingCase != null) {
			for (Visit visit : caze.getVisits()) {
				visit.setCaze(null);
			}
		}

		Set<Visit> allRelevantVisits = visitService.getAllRelevantVisits(
			caze.getPerson(),
			caze.getDisease(),
			CaseLogic.getStartDate(caze.getSymptoms().getOnsetDate(), caze.getSymptoms().getOnsetDate()),
			CaseLogic.getEndDate(caze.getSymptoms().getOnsetDate(), caze.getReportDate(), caze.getFollowUpUntil()));

		for (Visit visit : allRelevantVisits) {
			caze.getVisits().add(visit); // Necessary for further logic during the case save process
			visit.setCaze(caze);
		}
	}

	@Override
	public void setSampleAssociations(ContactReferenceDto sourceContact, CaseReferenceDto cazeRef) {

		if (sourceContact != null) {
			final Contact contact = contactService.getByUuid(sourceContact.getUuid());
			final Case caze = caseService.getByUuid(cazeRef.getUuid());
			contact.getSamples().forEach(sample -> sample.setAssociatedCase(caze));
		}
	}

	@Override
	public void setSampleAssociations(EventParticipantReferenceDto sourceEventParticipant, CaseReferenceDto cazeRef) {
		if (sourceEventParticipant != null) {
			final EventParticipant eventParticipant = eventParticipantService.getByUuid(sourceEventParticipant.getUuid());
			final Case caze = caseService.getByUuid(cazeRef.getUuid());
			eventParticipant.getSamples().forEach(sample -> sample.setAssociatedCase(caze));
		}
	}

	@Override
	public void setSampleAssociationsUnrelatedDisease(EventParticipantReferenceDto sourceEventParticipant, CaseReferenceDto cazeRef) {
		final EventParticipant eventParticipant = eventParticipantService.getByUuid(sourceEventParticipant.getUuid());
		final Case caze = caseService.getByUuid(cazeRef.getUuid());
		final Disease disease = caze.getDisease();
		eventParticipant.getSamples()
			.stream()
			.filter(sample -> sampleContainsTestForDisease(sample, disease))
			.forEach(sample -> sample.setAssociatedCase(caze));

	}

	private boolean sampleContainsTestForDisease(Sample sample, Disease disease) {
		return sample.getPathogenTests().stream().anyMatch(test -> test.getTestedDisease().equals(disease));
	}

	@Override
	public void validate(CaseDataDto caze) throws ValidationRuntimeException {

		// Check whether any required field that does not have a not null constraint in
		// the database is empty
		if (caze.getRegion() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validRegion));
		}
		if (caze.getDistrict() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validDistrict));
		}
		if ((caze.getCaseOrigin() == null || caze.getCaseOrigin() == CaseOrigin.IN_COUNTRY) && caze.getHealthFacility() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validFacility));
		}
		if (CaseOrigin.POINT_OF_ENTRY.equals(caze.getCaseOrigin()) && caze.getPointOfEntry() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validPointOfEntry));
		}
		if (caze.getDisease() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validDisease));
		}
		// Check whether there are any infrastructure errors
		if (!districtFacade.getDistrictByUuid(caze.getDistrict().getUuid()).getRegion().equals(caze.getRegion())) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noDistrictInRegion));
		}
		if (caze.getCommunity() != null && !communityFacade.getByUuid(caze.getCommunity().getUuid()).getDistrict().equals(caze.getDistrict())) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noCommunityInDistrict));
		}
		if (caze.getHealthFacility() != null) {
			FacilityDto healthFacility = facilityFacade.getByUuid(caze.getHealthFacility().getUuid());

			if (caze.getFacilityType() == null && !FacilityDto.NONE_FACILITY_UUID.equals(caze.getHealthFacility().getUuid())) {
				throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noFacilityType));
			}
			if (caze.getCommunity() == null && healthFacility.getDistrict() != null && !healthFacility.getDistrict().equals(caze.getDistrict())) {
				throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noFacilityInDistrict));
			}
			if (caze.getCommunity() != null && healthFacility.getCommunity() != null && !caze.getCommunity().equals(healthFacility.getCommunity())) {
				throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noFacilityInCommunity));
			}
			if (healthFacility.getRegion() != null && !caze.getRegion().equals(healthFacility.getRegion())) {
				throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noFacilityInRegion));
			}
		}
	}

	@Override
	public void archiveOrDearchiveCase(String caseUuid, boolean archive) {

		caseService.updateArchived(Collections.singletonList(caseUuid), archive);
	}

	/**
	 * Handles potential changes of related tasks that needs to be done after
	 * a case has been created/saved
	 */
	private void updateTasksOnCaseChanged(Case newCase, CaseDataDto existingCase) {
		// In case that *any* jurisdiction of the case has been changed, we need to see if we need to reassign related tasks.
		// Tasks can be assigned to various user roles and users, therefore it is crucial to make sure
		// that no tasks related to cases are assigned to officers lacking jurisdiction on the case.

		if (existingCase != null) {
			boolean responsibleRegionChanged = !DataHelper.isSame(existingCase.getResponsibleRegion(), newCase.getResponsibleRegion());
			boolean regionChanged = !DataHelper.isSame(existingCase.getRegion(), newCase.getRegion());

			boolean responsibleDistrictChanged = !DataHelper.isSame(existingCase.getResponsibleDistrict(), newCase.getResponsibleDistrict());
			boolean districtChanged = !DataHelper.isSame(existingCase.getDistrict(), newCase.getDistrict());

			// check if infrastructure was changed, added, or removed from the case

			boolean responsibleCommunityChanged = !DataHelper.isSame(existingCase.getResponsibleCommunity(), newCase.getResponsibleCommunity());
			boolean communityChanged = !DataHelper.isSame(existingCase.getCommunity(), newCase.getCommunity());

			boolean facilityChanged = !DataHelper.isSame(existingCase.getHealthFacility(), newCase.getHealthFacility());

			if (responsibleRegionChanged
				|| responsibleDistrictChanged
				|| responsibleCommunityChanged
				|| regionChanged
				|| districtChanged
				|| communityChanged
				|| facilityChanged) {
				reassignTasksOfCase(newCase, false);
			}

		}

		// Create a task to search for other cases for new Plague cases
		if (existingCase == null
			&& newCase.getDisease() == Disease.PLAGUE
			&& featureConfigurationFacade.isTaskGenerationFeatureEnabled(TaskType.ACTIVE_SEARCH_FOR_OTHER_CASES)) {
			createActiveSearchForOtherCasesTask(newCase);
		}
	}

	public void onCaseSampleChanged(Case associatedCase) {
		// Update case classification if the feature is enabled
		if (configFacade.isFeatureAutomaticCaseClassification()) {
			if (associatedCase.getCaseClassification() != CaseClassification.NO_CASE) {
				Long pathogenTestsCount = pathogenTestService.countByCase(associatedCase);
				if (pathogenTestsCount == 0) {
					return;
				}
				// calculate classification
				CaseDataDto newCaseDto = toDto(associatedCase);

				CaseClassification classification = caseClassificationFacade.getClassification(newCaseDto);

				// only update when classification by system changes - user may overwrite this
				if (classification != associatedCase.getSystemCaseClassification()) {
					associatedCase.setSystemCaseClassification(classification);

					// really a change? (user may have already set it)
					if (classification != associatedCase.getCaseClassification()) {
						associatedCase.setCaseClassification(classification);
						associatedCase.setClassificationUser(null);
						associatedCase.setClassificationDate(new Date());
					}
				}
			}
		}
	}

	/**
	 * Handles potential changes, processes and backend logic that needs to be done
	 * after a case has been created/saved
	 */
	public void onCaseChanged(CaseDataDto existingCase, Case newCase) {

		// If its a new case and the case is new and the geo coordinates of the case's
		// health facility are null, set its coordinates to the case's report
		// coordinates, if available. Else if case report coordinates are null set them
		// to the facility's coordinates
		Facility facility = newCase.getHealthFacility();
		if (existingCase == null && facility != null && !FacilityHelper.isOtherOrNoneHealthFacility(facility.getUuid())) {
			if ((facility.getLatitude() == null || facility.getLongitude() == null)
				&& newCase.getReportLat() != null
				&& newCase.getReportLon() != null) {
				facility.setLatitude(newCase.getReportLat());
				facility.setLongitude(newCase.getReportLon());
				facilityService.ensurePersisted(facility);
			} else if (newCase.getReportLat() == null && newCase.getReportLon() == null && newCase.getReportLatLonAccuracy() == null) {
				newCase.setReportLat(facility.getLatitude());
				newCase.setReportLon(facility.getLongitude());
			}
		}

		// Clear facility type if no facility or home was selected
		if (newCase.getHealthFacility() == null || FacilityDto.NONE_FACILITY_UUID.equals(newCase.getHealthFacility().getUuid())) {
			newCase.setFacilityType(null);
		}

		// Generate epid number if missing or incomplete
		FieldVisibilityCheckers fieldVisibilityCheckers = FieldVisibilityCheckers.withCountry(configFacade.getCountryLocale());
		if (fieldVisibilityCheckers.isVisible(CaseDataDto.class, CaseDataDto.EPID_NUMBER)
			&& !CaseLogic.isCompleteEpidNumber(newCase.getEpidNumber())) {
			newCase.setEpidNumber(
				generateEpidNumber(
					newCase.getEpidNumber(),
					newCase.getUuid(),
					newCase.getDisease(),
					newCase.getReportDate(),
					newCase.getDistrict().getUuid()));
		}

		// update the plague type based on symptoms
		if (newCase.getDisease() == Disease.PLAGUE) {
			PlagueType plagueType = DiseaseHelper.getPlagueTypeForSymptoms(SymptomsFacadeEjb.toDto(newCase.getSymptoms()));
			if (plagueType != newCase.getPlagueType() && plagueType != null) {
				newCase.setPlagueType(plagueType);
			}
		}

		if (newCase.getSurveillanceOfficer() == null || !newCase.getSurveillanceOfficer().getDistrict().equals(newCase.getDistrict())) {
			setResponsibleSurveillanceOfficer(newCase);
		}

		updateInvestigationByStatus(existingCase, newCase);

		updatePersonAndCaseByOutcome(existingCase, newCase);

		updateCaseAge(existingCase, newCase);

		// Change the disease of all contacts if the case disease or disease details have changed
		if (existingCase != null
			&& (newCase.getDisease() != existingCase.getDisease()
				|| !StringUtils.equals(newCase.getDiseaseDetails(), existingCase.getDiseaseDetails()))) {
			for (Contact contact : contactService.findBy(new ContactCriteria().caze(newCase.toReference()), null)) {
				if (contact.getDisease() != newCase.getDisease() || !StringUtils.equals(contact.getDiseaseDetails(), newCase.getDiseaseDetails())) {
					// Only do the change if it hasn't been done in the mobile app before
					contact.setDisease(newCase.getDisease());
					contact.setDiseaseDetails(newCase.getDiseaseDetails());
					contactService.ensurePersisted(contact);
				}
			}
		}

		if (existingCase != null
			&& (newCase.getDisease() != existingCase.getDisease()
				|| newCase.getReportDate() != existingCase.getReportDate()
				|| newCase.getSymptoms().getOnsetDate() != existingCase.getSymptoms().getOnsetDate())) {

			// Update follow-up until and status of all contacts
			for (Contact contact : contactService.findBy(new ContactCriteria().caze(newCase.toReference()), null)) {
				contactService.updateFollowUpDetails(contact, false);
				contactService.udpateContactStatus(contact);
			}
			for (Contact contact : contactService.getAllByResultingCase(newCase)) {
				contactService.updateFollowUpDetails(contact, false);
				contactService.udpateContactStatus(contact);
			}
		}

		updateTasksOnCaseChanged(newCase, existingCase);

		// Update case classification if the feature is enabled
		if (configFacade.isFeatureAutomaticCaseClassification()) {
			if (newCase.getCaseClassification() != CaseClassification.NO_CASE) {
				// calculate classification
				CaseDataDto newCaseDto = toDto(newCase);

				CaseClassification classification = caseClassificationFacade.getClassification(newCaseDto);

				// only update when classification by system changes - user may overwrite this
				if (classification != newCase.getSystemCaseClassification()) {
					newCase.setSystemCaseClassification(classification);

					// really a change? (user may have already set it)
					if (classification != newCase.getCaseClassification()) {
						newCase.setCaseClassification(classification);
						newCase.setClassificationUser(null);
						newCase.setClassificationDate(new Date());
					}
				}
			}
		}

		// Set Yes/No/Unknown fields associated with embedded lists to Yes if the lists
		// are not empty
		if (!newCase.getHospitalization().getPreviousHospitalizations().isEmpty()
			&& YesNoUnknown.YES != newCase.getHospitalization().getHospitalizedPreviously()) {
			newCase.getHospitalization().setHospitalizedPreviously(YesNoUnknown.YES);
		}
		if (!newCase.getEpiData().getExposures().isEmpty() && !YesNoUnknown.YES.equals(newCase.getEpiData().getExposureDetailsKnown())) {
			newCase.getEpiData().setExposureDetailsKnown(YesNoUnknown.YES);
		}

		// Update completeness value
		newCase.setCompleteness(null);

		// Send an email to all responsible supervisors when the case classification has
		// changed
		if (existingCase != null && existingCase.getCaseClassification() != newCase.getCaseClassification()) {
			List<User> messageRecipients = userService.getAllByRegionsAndUserRoles(
				JurisdictionHelper.getCaseRegions(newCase),
				UserRole.SURVEILLANCE_SUPERVISOR,
				UserRole.ADMIN_SUPERVISOR,
				UserRole.CASE_SUPERVISOR,
				UserRole.CONTACT_SUPERVISOR);
			for (User recipient : messageRecipients) {
				try {
					messagingService.sendMessage(
						recipient,
						MessageSubject.CASE_CLASSIFICATION_CHANGED,
						String.format(
							I18nProperties.getString(MessagingService.CONTENT_CASE_CLASSIFICATION_CHANGED),
							DataHelper.getShortUuid(newCase.getUuid()),
							newCase.getCaseClassification().toString()),
						MessageType.EMAIL,
						MessageType.SMS);
				} catch (NotificationDeliveryFailedException e) {
					logger.error(
						String.format(
							"NotificationDeliveryFailedException when trying to notify supervisors about the change of a case classification. "
								+ "Failed to send " + e.getMessageType() + " to user with UUID %s.",
							recipient.getUuid()));
				}
			}
		}

		// Send an email to all responsible supervisors when the disease of an
		// Unspecified VHF case has changed
		if (existingCase != null && existingCase.getDisease() == Disease.UNSPECIFIED_VHF && existingCase.getDisease() != newCase.getDisease()) {
			List<User> messageRecipients = userService.getAllByRegionsAndUserRoles(
				JurisdictionHelper.getCaseRegions(newCase),
				UserRole.SURVEILLANCE_SUPERVISOR,
				UserRole.ADMIN_SUPERVISOR,
				UserRole.CASE_SUPERVISOR,
				UserRole.CONTACT_SUPERVISOR);
			for (User recipient : messageRecipients) {
				try {
					messagingService.sendMessage(
						recipient,
						MessageSubject.DISEASE_CHANGED,
						String.format(
							I18nProperties.getString(MessagingService.CONTENT_DISEASE_CHANGED),
							DataHelper.getShortUuid(newCase.getUuid()),
							existingCase.getDisease().toString(),
							newCase.getDisease().toString()),
						MessageType.EMAIL,
						MessageType.SMS);
				} catch (NotificationDeliveryFailedException e) {
					logger.error(
						String.format(
							"NotificationDeliveryFailedException when trying to notify supervisors about the change of a case disease. "
								+ "Failed to send " + e.getMessageType() + " to user with UUID %s.",
							recipient.getUuid()));
				}
			}
		}

		// If the case is a newly created case or if it was not in a CONFIRMED status
		// and now the case is in a CONFIRMED status, notify related surveillance officers
		Set<CaseClassification> confirmedClassifications = CaseClassification.getConfirmedClassifications();
		if ((existingCase == null || !confirmedClassifications.contains(existingCase.getCaseClassification()))
			&& confirmedClassifications.contains(newCase.getCaseClassification())) {
			sendConfirmedCaseNotificationsForEvents(newCase);
		}
	}

	private void sendConfirmedCaseNotificationsForEvents(Case caze) {
		Date fromDate = Date.from(Instant.now().minus(Duration.ofDays(30)));
		Map<String, User> responsibleUserByEventByEventUuid =
			eventService.getAllEventUuidWithResponsibleUserByCaseAfterDateForNotification(caze, fromDate);
		for (Map.Entry<String, User> entry : responsibleUserByEventByEventUuid.entrySet()) {
			try {
				messagingService.sendMessage(
					entry.getValue(),
					MessageSubject.EVENT_PARTICIPANT_CASE_CLASSIFICATION_CONFIRMED,
					new Object[] {
						caze.getDisease().getName() },
					String.format(
						I18nProperties.getString(MessagingService.CONTENT_EVENT_PARTICIPANT_CASE_CLASSIFICATION_CONFIRMED),
						DataHelper.getShortUuid(entry.getKey()),
						caze.getDisease().getName(),
						DataHelper.getShortUuid(caze.getUuid())),
					MessageType.EMAIL,
					MessageType.SMS);
			} catch (NotificationDeliveryFailedException e) {
				logger.error(
					String.format(
						"NotificationDeliveryFailedException when trying to notify event responsible user about a newly confirmed case. "
							+ "Failed to send " + e.getMessageType() + " to user with UUID %s.",
						entry.getValue().getUuid()));
			}
		}
	}

	public void setResponsibleSurveillanceOfficer(Case caze) {
		if (caze.getReportingUser().getUserRoles().contains(UserRole.SURVEILLANCE_OFFICER)
			&& caze.getReportingUser().getDistrict().equals(caze.getDistrict())) {
			caze.setSurveillanceOfficer(caze.getReportingUser());
		} else {
			List<User> informants = caze.getHealthFacility() != null && FacilityType.HOSPITAL.equals(caze.getHealthFacility().getType())
				? userService.getInformantsOfFacility(caze.getHealthFacility())
				: new ArrayList<>();
			Random rand = new Random();
			if (!informants.isEmpty()) {
				caze.setSurveillanceOfficer(informants.get(rand.nextInt(informants.size())).getAssociatedOfficer());
			} else {
				User survOff = null;
				if (caze.getResponsibleDistrict() != null) {
					survOff = getRandomSurveillanceOfficer(caze.getResponsibleDistrict());
				}

				if (survOff == null) {
					survOff = getRandomSurveillanceOfficer(caze.getDistrict());
				}

				caze.setSurveillanceOfficer(survOff);
			}
		}
	}

	/**
	 * Reassigns tasks related to `caze`. With `forceReassignment` beeing false, the function will only reassign
	 * the tasks if the assignees lack jurisdiction on the case. When forced, all tasks will be reassigned.
	 * 
	 * @param caze
	 *            the case which related tasks are reassigned.
	 * @param forceReassignment
	 *            force reassignment of case tasks.
	 */
	public void reassignTasksOfCase(Case caze, boolean forceReassignment) {
		// for each task that is related to the case, the task assignee must match the jurisdiction of the case
		// otherwise we will reassign the task
		for (Task task : caze.getTasks()) {
			if (task.getTaskStatus() != TaskStatus.PENDING) {
				continue;
			}

			User taskAssignee = task.getAssigneeUser();
			boolean mismatch = false;

			if (taskAssignee == null) {
				// no one is assigned so we skip detailed checks and go directly to reassignment.
				mismatch = true;
			} else if (!forceReassignment) {
				boolean responsibleRegionMismatch = !DataHelper.isSame(taskAssignee.getRegion(), caze.getResponsibleRegion());
				boolean regionMismatch = !DataHelper.isSame(taskAssignee.getRegion(), caze.getRegion());

				boolean responsibleDistrictMismatch = !DataHelper.isSame(taskAssignee.getDistrict(), caze.getDistrict());
				boolean districtMismatch = !DataHelper.isSame(taskAssignee.getDistrict(), caze.getDistrict());

				boolean responsibleCommunityMismatch = !DataHelper.isSame(taskAssignee.getCommunity(), caze.getResponsibleCommunity());
				boolean communityMismatch = !DataHelper.isSame(taskAssignee.getCommunity(), caze.getCommunity());

				boolean facilityMismatch = !DataHelper.isSame(taskAssignee.getHealthFacility(), caze.getHealthFacility());

				mismatch = responsibleRegionMismatch
					|| responsibleDistrictMismatch
					|| responsibleCommunityMismatch
					|| regionMismatch
					|| districtMismatch
					|| communityMismatch
					|| facilityMismatch;
			}

			if (forceReassignment || mismatch) {
				// if there is any mismatch between the jurisdiction of the case and the assigned user,
				// we need to reassign the tasks
				assignOfficerOrSupervisorToTask(caze, task);
				taskService.ensurePersisted(task);
			}

		}

	}

	@Override
	public int updateCompleteness() {
		List<String> getCompletenessCheckCaseList = getCompletenessCheckNeededCaseList();

		IterableHelper.executeBatched(getCompletenessCheckCaseList, 10, caseCompletionBatch -> caseService.updateCompleteness(caseCompletionBatch));

		return getCompletenessCheckCaseList.size();
	}

	private List<String> getCompletenessCheckNeededCaseList() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Case> caze = cq.from(Case.class);

		cq.where(cb.isNull(caze.get(Case.COMPLETENESS)));

		cq.orderBy(cb.desc(caze.get(Case.CHANGE_DATE)));
		cq.select(caze.get(Case.UUID));

		return em.createQuery(cq).getResultList();
	}

	@Override
	public String generateEpidNumber(CaseDataDto caze) {
		return generateEpidNumber(caze.getEpidNumber(), caze.getUuid(), caze.getDisease(), caze.getReportDate(), caze.getDistrict().getUuid());
	}

	private String generateEpidNumber(String newEpidNumber, String caseUuid, Disease disease, Date reportDate, String districtUuid) {

		if (!CaseLogic.isEpidNumberPrefix(newEpidNumber)) {
			// Generate a completely new epid number if the prefix is not complete or doesn't match the pattern
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(reportDate);
			String year = String.valueOf(calendar.get(Calendar.YEAR)).substring(2);
			newEpidNumber = districtFacade.getFullEpidCodeForDistrict(districtUuid) + "-" + year + "-";
		}

		// Generate a suffix number
		String highestEpidNumber = caseService.getHighestEpidNumber(newEpidNumber, caseUuid, disease);
		if (highestEpidNumber == null || highestEpidNumber.endsWith("-")) {
			// If there is not yet a case with a suffix for this epid number in the database, use 001
			newEpidNumber = newEpidNumber + "001";
		} else {
			// Otherwise, extract the suffix from the highest existing epid number and increase it by 1
			String suffixString = highestEpidNumber.substring(highestEpidNumber.lastIndexOf('-'));
			// Remove all non-digits from the suffix to ignore earlier input errors
			suffixString = suffixString.replaceAll("[^\\d]", "");
			if (suffixString.isEmpty()) {
				// If the suffix is empty now, that means there is not yet an epid number with a
				// suffix containing numbers
				newEpidNumber = newEpidNumber + "001";
			} else {
				int suffix = Integer.parseInt(suffixString) + 1;
				newEpidNumber += String.format("%03d", suffix);
			}
		}

		return newEpidNumber;
	}

	private void updatePersonAndCaseByOutcome(CaseDataDto existingCase, Case newCase) {

		if (existingCase != null && newCase.getOutcome() != existingCase.getOutcome()) {

			if (newCase.getOutcome() == null || newCase.getOutcome() == CaseOutcome.NO_OUTCOME) {
				newCase.setOutcomeDate(null);
			} else if (newCase.getOutcomeDate() == null) {
				newCase.setOutcomeDate(new Date());
			}

			if (newCase.getOutcome() == CaseOutcome.DECEASED) {
				if (newCase.getPerson().getPresentCondition() != PresentCondition.DEAD
					&& newCase.getPerson().getPresentCondition() != PresentCondition.BURIED) {
					PersonDto existingPerson = PersonFacadeEjb.toDto(newCase.getPerson());
					newCase.getPerson().setPresentCondition(PresentCondition.DEAD);
					newCase.getPerson().setDeathDate(newCase.getOutcomeDate());
					newCase.getPerson().setCauseOfDeath(CauseOfDeath.EPIDEMIC_DISEASE);
					newCase.getPerson().setCauseOfDeathDisease(newCase.getDisease());
					// attention: this may lead to infinite recursion when not properly implemented
					personFacade.onPersonChanged(existingPerson, newCase.getPerson());
				}
			} else if (newCase.getOutcome() == CaseOutcome.NO_OUTCOME) {
				if (newCase.getPerson().getPresentCondition() != PresentCondition.ALIVE) {
					PersonDto existingPerson = PersonFacadeEjb.toDto(newCase.getPerson());
					newCase.getPerson().setPresentCondition(PresentCondition.ALIVE);
					// attention: this may lead to infinite recursion when not properly implemented
					personFacade.onPersonChanged(existingPerson, newCase.getPerson());
				}
			}
		} else if (existingCase != null
			&& CaseOutcome.DECEASED == newCase.getOutcome()
			&& (newCase.getPerson().getPresentCondition() == PresentCondition.DEAD
				|| newCase.getPerson().getPresentCondition() == PresentCondition.BURIED)
			&& (newCase.getPerson().getDeathDate() == null
				? newCase.getOutcomeDate() != null
				: !newCase.getPerson().getDeathDate().equals(newCase.getOutcomeDate()))) {
			PersonDto existingPerson = PersonFacadeEjb.toDto(newCase.getPerson());
			newCase.getPerson().setDeathDate(newCase.getOutcomeDate());
			personFacade.onPersonChanged(existingPerson, newCase.getPerson());
		}
	}

	private void updateCaseAge(CaseDataDto existingCase, Case newCase) {

		if (newCase.getPerson().getApproximateAge() != null) {
			Date newCaseStartDate = CaseLogic.getStartDate(newCase.getSymptoms().getOnsetDate(), newCase.getReportDate());
			if (existingCase == null || !CaseLogic.getStartDate(existingCase).equals(newCaseStartDate)) {
				if (newCase.getPerson().getApproximateAgeType() == ApproximateAgeType.MONTHS) {
					newCase.setCaseAge(0);
				} else {
					Date personChangeDate = newCase.getPerson().getChangeDate();
					Date referenceDate = newCaseStartDate;
					newCase.setCaseAge(newCase.getPerson().getApproximateAge() - DateHelper.getYearsBetween(referenceDate, personChangeDate));
					if (newCase.getCaseAge() < 0) {
						newCase.setCaseAge(0);
					}
				}

			}
		}
	}

	@Override
	public void deleteCase(String caseUuid) throws ExternalSurveillanceToolException {

		if (!userService.hasRight(UserRight.CASE_DELETE)) {
			throw new UnsupportedOperationException("User " + userService.getCurrentUser().getUuid() + " is not allowed to delete cases.");
		}

		Case caze = caseService.getByUuid(caseUuid);

		externalJournalService.handleExternalJournalPersonUpdateAsync(caze.getPerson().toReference());
		if (externalSurveillanceToolGatewayFacade.isFeatureEnabled() && caze.getExternalID() != null && !caze.getExternalID().isEmpty()) {
			List<CaseDataDto> casesWithSameExternalId = getByExternalId(caze.getExternalID());
			if (casesWithSameExternalId != null && casesWithSameExternalId.size() == 1) {
				externalSurveillanceToolGatewayFacade.deleteCases(Collections.singletonList(toDto(caze)));
			}
		}

		caseService.delete(caze);
	}

	@Override
	public void deleteCaseAsDuplicate(String caseUuid, String duplicateOfCaseUuid) throws ExternalSurveillanceToolException {

		Case caze = caseService.getByUuid(caseUuid);
		Case duplicateOfCase = caseService.getByUuid(duplicateOfCaseUuid);
		caze.setDuplicateOf(duplicateOfCase);
		caseService.ensurePersisted(caze);

		deleteCase(caseUuid);
	}

	@Override
	public List<String> getArchivedUuidsSince(Date since) {

		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}

		return caseService.getArchivedUuidsSince(since);
	}

	@Override
	public List<String> getDeletedUuidsSince(Date since) {

		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}
		return caseService.getDeletedUuidsSince(since);
	}

	public static CaseReferenceDto toReferenceDto(Case entity) {

		if (entity == null) {
			return null;
		}

		return entity.toReference();
	}

	public CaseDataDto convertToDto(Case source, Pseudonymizer pseudonymizer) {

		CaseDataDto dto = toDto(source);

		pseudonymizeDto(source, dto, pseudonymizer);

		return dto;
	}

	private void pseudonymizeDto(Case source, CaseDataDto dto, Pseudonymizer pseudonymizer) {
		if (dto != null) {
			boolean inJurisdiction = caseJurisdictionChecker.isInJurisdictionOrOwned(JurisdictionHelper.createCaseJurisdictionDto(source));

			pseudonymizer.pseudonymizeDto(CaseDataDto.class, dto, inJurisdiction, c -> {
				User currentUser = userService.getCurrentUser();
				pseudonymizer.pseudonymizeUser(source.getReportingUser(), currentUser, dto::setReportingUser);
				pseudonymizer.pseudonymizeUser(source.getClassificationUser(), currentUser, dto::setClassificationUser);

				pseudonymizer.pseudonymizeDto(EpiDataDto.class, dto.getEpiData(), inJurisdiction, e -> {
					pseudonymizer.pseudonymizeDtoCollection(ExposureDto.class, e.getExposures(), exp -> inJurisdiction, (exp, expInJurisdiction) -> {
						pseudonymizer.pseudonymizeDto(LocationDto.class, exp.getLocation(), expInJurisdiction, null);
					});
				});

				pseudonymizer.pseudonymizeDto(HealthConditionsDto.class, c.getClinicalCourse().getHealthConditions(), inJurisdiction, null);

				pseudonymizer.pseudonymizeDtoCollection(
					PreviousHospitalizationDto.class,
					c.getHospitalization().getPreviousHospitalizations(),
					h -> inJurisdiction,
					null);

				pseudonymizer.pseudonymizeDto(SymptomsDto.class, dto.getSymptoms(), inJurisdiction, null);
				pseudonymizer.pseudonymizeDto(MaternalHistoryDto.class, dto.getMaternalHistory(), inJurisdiction, null);
			});
		}
	}

	private void restorePseudonymizedDto(CaseDataDto dto, Case caze, CaseDataDto existingCaseDto) {
		if (existingCaseDto != null) {
			boolean inJurisdiction = caseJurisdictionChecker.isInJurisdictionOrOwned(JurisdictionHelper.createCaseJurisdictionDto(caze));
			Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
			User currentUser = userService.getCurrentUser();

			pseudonymizer.restoreUser(caze.getReportingUser(), currentUser, dto, dto::setReportingUser);
			pseudonymizer.restoreUser(caze.getClassificationUser(), currentUser, dto, dto::setClassificationUser);

			pseudonymizer.restorePseudonymizedValues(CaseDataDto.class, dto, existingCaseDto, inJurisdiction);

			EpiDataDto epiData = dto.getEpiData();
			EpiDataDto existingEpiData = existingCaseDto.getEpiData();

			pseudonymizer.restorePseudonymizedValues(EpiDataDto.class, epiData, existingEpiData, inJurisdiction);

			epiData.getExposures().forEach(exposure -> {
				ExposureDto existingExposure =
					existingEpiData.getExposures().stream().filter(exp -> DataHelper.isSame(exposure, exp)).findFirst().orElse(null);

				if (existingExposure != null) {
					pseudonymizer.restorePseudonymizedValues(ExposureDto.class, exposure, existingExposure, inJurisdiction);
					pseudonymizer
						.restorePseudonymizedValues(LocationDto.class, exposure.getLocation(), existingExposure.getLocation(), inJurisdiction);
				}
			});

			pseudonymizer.restorePseudonymizedValues(
				HealthConditionsDto.class,
				dto.getClinicalCourse().getHealthConditions(),
				existingCaseDto.getClinicalCourse().getHealthConditions(),
				inJurisdiction);

			dto.getHospitalization().getPreviousHospitalizations().forEach(previousHospitalization -> {
				existingCaseDto.getHospitalization()
					.getPreviousHospitalizations()
					.stream()
					.filter(eh -> DataHelper.isSame(previousHospitalization, eh))
					.findFirst()
					.ifPresent(
						existingPreviousHospitalization -> pseudonymizer.restorePseudonymizedValues(
							PreviousHospitalizationDto.class,
							previousHospitalization,
							existingPreviousHospitalization,
							inJurisdiction));

			});

			pseudonymizer.restorePseudonymizedValues(SymptomsDto.class, dto.getSymptoms(), existingCaseDto.getSymptoms(), inJurisdiction);
			pseudonymizer
				.restorePseudonymizedValues(MaternalHistoryDto.class, dto.getMaternalHistory(), existingCaseDto.getMaternalHistory(), inJurisdiction);
		}
	}

	public CaseReferenceDto convertToReferenceDto(Case source) {

		CaseReferenceDto dto = toReferenceDto(source);

		if (dto != null) {
			boolean inJurisdiction = caseJurisdictionChecker.isInJurisdictionOrOwned(JurisdictionHelper.createCaseJurisdictionDto(source));
			Pseudonymizer.getDefault(userService::hasRight).pseudonymizeDto(CaseReferenceDto.class, dto, inJurisdiction, null);
		}

		return dto;
	}

	public static CaseDataDto toDto(Case source) {

		if (source == null) {
			return null;
		}

		CaseDataDto target = new CaseDataDto();
		DtoHelper.fillDto(target, source);

		target.setDisease(source.getDisease());
		target.setDiseaseVariant(source.getDiseaseVariant());
		target.setDiseaseDetails(source.getDiseaseDetails());
		target.setPlagueType(source.getPlagueType());
		target.setDengueFeverType(source.getDengueFeverType());
		target.setRabiesType(source.getRabiesType());
		target.setCaseClassification(source.getCaseClassification());
		target.setCaseIdentificationSource(source.getCaseIdentificationSource());
		target.setScreeningType(source.getScreeningType());
		target.setClassificationUser(UserFacadeEjb.toReferenceDto(source.getClassificationUser()));
		target.setClassificationDate(source.getClassificationDate());
		target.setClassificationComment(source.getClassificationComment());
		target.setClinicalConfirmation(source.getClinicalConfirmation());
		target.setEpidemiologicalConfirmation(source.getEpidemiologicalConfirmation());
		target.setLaboratoryDiagnosticConfirmation(source.getLaboratoryDiagnosticConfirmation());
		target.setInvestigationStatus(source.getInvestigationStatus());
		target.setPerson(PersonFacadeEjb.toReferenceDto(source.getPerson()));
		target.setHospitalization(HospitalizationFacadeEjb.toDto(source.getHospitalization()));
		target.setEpiData(EpiDataFacadeEjb.toDto(source.getEpiData()));
		if (source.getTherapy() != null) {
			target.setTherapy(TherapyFacadeEjb.toDto(source.getTherapy()));
		}
		if (source.getClinicalCourse() != null) {
			target.setClinicalCourse(ClinicalCourseFacadeEjb.toDto(source.getClinicalCourse()));
		}
		if (source.getMaternalHistory() != null) {
			target.setMaternalHistory(MaternalHistoryFacadeEjb.toDto(source.getMaternalHistory()));
		}
		if (source.getPortHealthInfo() != null) {
			target.setPortHealthInfo(PortHealthInfoFacadeEjb.toDto(source.getPortHealthInfo()));
		}

		target.setResponsibleRegion(RegionFacadeEjb.toReferenceDto(source.getResponsibleRegion()));
		target.setResponsibleDistrict(DistrictFacadeEjb.toReferenceDto(source.getResponsibleDistrict()));
		target.setResponsibleCommunity(CommunityFacadeEjb.toReferenceDto(source.getResponsibleCommunity()));

		target.setRegion(RegionFacadeEjb.toReferenceDto(source.getRegion()));
		target.setDistrict(DistrictFacadeEjb.toReferenceDto(source.getDistrict()));
		target.setCommunity(CommunityFacadeEjb.toReferenceDto(source.getCommunity()));
		target.setHealthFacility(FacilityFacadeEjb.toReferenceDto(source.getHealthFacility()));
		target.setHealthFacilityDetails(source.getHealthFacilityDetails());

		target.setReportingUser(UserFacadeEjb.toReferenceDto(source.getReportingUser()));
		target.setReportDate(source.getReportDate());
		target.setInvestigatedDate(source.getInvestigatedDate());
		target.setRegionLevelDate(source.getRegionLevelDate());
		target.setNationalLevelDate(source.getNationalLevelDate());
		target.setDistrictLevelDate(source.getDistrictLevelDate());

		target.setSurveillanceOfficer(UserFacadeEjb.toReferenceDto(source.getSurveillanceOfficer()));
		target.setClinicianName(source.getClinicianName());
		target.setClinicianPhone(source.getClinicianPhone());
		target.setClinicianEmail(source.getClinicianEmail());
		target.setCaseOfficer(UserFacadeEjb.toReferenceDto(source.getCaseOfficer()));
		target.setSymptoms(SymptomsFacadeEjb.toDto(source.getSymptoms()));

		target.setPregnant(source.getPregnant());
		target.setVaccination(source.getVaccination());
		target.setVaccinationDoses(source.getVaccinationDoses());
		target.setVaccinationInfoSource(source.getVaccinationInfoSource());
		target.setVaccine(source.getVaccine());
		target.setSmallpoxVaccinationScar(source.getSmallpoxVaccinationScar());
		target.setSmallpoxVaccinationReceived(source.getSmallpoxVaccinationReceived());
		target.setFirstVaccinationDate(source.getFirstVaccinationDate());
		target.setLastVaccinationDate(source.getLastVaccinationDate());
		target.setVaccineName(source.getVaccineName());
		target.setOtherVaccineName(source.getOtherVaccineName());
		target.setVaccineManufacturer(source.getVaccineManufacturer());
		target.setOtherVaccineManufacturer(source.getOtherVaccineManufacturer());
		target.setVaccineInn(source.getVaccineInn());
		target.setVaccineBatchNumber(source.getVaccineBatchNumber());
		target.setVaccineUniiCode(source.getVaccineUniiCode());
		target.setVaccineAtcCode(source.getVaccineAtcCode());

		target.setEpidNumber(source.getEpidNumber());

		target.setReportLat(source.getReportLat());
		target.setReportLon(source.getReportLon());
		target.setReportLatLonAccuracy(source.getReportLatLonAccuracy());

		target.setOutcome(source.getOutcome());
		target.setOutcomeDate(source.getOutcomeDate());
		target.setSequelae(source.getSequelae());
		target.setSequelaeDetails(source.getSequelaeDetails());
		target.setNotifyingClinic(source.getNotifyingClinic());
		target.setNotifyingClinicDetails(source.getNotifyingClinicDetails());

		target.setCreationVersion(source.getCreationVersion());
		target.setCaseOrigin(source.getCaseOrigin());
		target.setPointOfEntry(PointOfEntryFacadeEjb.toReferenceDto(source.getPointOfEntry()));
		target.setPointOfEntryDetails(source.getPointOfEntryDetails());
		target.setAdditionalDetails(source.getAdditionalDetails());
		target.setExternalID(source.getExternalID());
		target.setExternalToken(source.getExternalToken());
		target.setInternalToken(source.getInternalToken());
		target.setSharedToCountry(source.isSharedToCountry());
		target.setQuarantine(source.getQuarantine());
		target.setQuarantineTypeDetails(source.getQuarantineTypeDetails());
		target.setQuarantineTo(source.getQuarantineTo());
		target.setQuarantineFrom(source.getQuarantineFrom());
		target.setQuarantineHelpNeeded(source.getQuarantineHelpNeeded());
		target.setQuarantineOrderedVerbally(source.isQuarantineOrderedVerbally());
		target.setQuarantineOrderedOfficialDocument(source.isQuarantineOrderedOfficialDocument());
		target.setQuarantineOrderedVerballyDate(source.getQuarantineOrderedVerballyDate());
		target.setQuarantineOrderedOfficialDocumentDate(source.getQuarantineOrderedOfficialDocumentDate());
		target.setQuarantineHomePossible(source.getQuarantineHomePossible());
		target.setQuarantineHomePossibleComment(source.getQuarantineHomePossibleComment());
		target.setQuarantineHomeSupplyEnsured(source.getQuarantineHomeSupplyEnsured());
		target.setQuarantineHomeSupplyEnsuredComment(source.getQuarantineHomeSupplyEnsuredComment());
		target.setQuarantineExtended(source.isQuarantineExtended());
		target.setQuarantineReduced(source.isQuarantineReduced());
		target.setQuarantineOfficialOrderSent(source.isQuarantineOfficialOrderSent());
		target.setQuarantineOfficialOrderSentDate(source.getQuarantineOfficialOrderSentDate());
		target.setPostpartum(source.getPostpartum());
		target.setTrimester(source.getTrimester());
		target.setFollowUpComment(source.getFollowUpComment());
		target.setFollowUpStatus(source.getFollowUpStatus());
		target.setFollowUpUntil(source.getFollowUpUntil());
		target.setOverwriteFollowUpUntil(source.isOverwriteFollowUpUntil());
		target.setFacilityType(source.getFacilityType());

		target.setCaseIdIsm(source.getCaseIdIsm());
		target.setContactTracingFirstContactType(source.getContactTracingFirstContactType());
		target.setContactTracingFirstContactDate(source.getContactTracingFirstContactDate());
		target.setWasInQuarantineBeforeIsolation(source.getWasInQuarantineBeforeIsolation());
		target.setQuarantineReasonBeforeIsolation(source.getQuarantineReasonBeforeIsolation());
		target.setQuarantineReasonBeforeIsolationDetails(source.getQuarantineReasonBeforeIsolationDetails());
		target.setEndOfIsolationReason(source.getEndOfIsolationReason());
		target.setEndOfIsolationReasonDetails(source.getEndOfIsolationReasonDetails());

		target.setNosocomialOutbreak(source.isNosocomialOutbreak());
		target.setInfectionSetting(source.getInfectionSetting());

		target.setProhibitionToWork(source.getProhibitionToWork());
		target.setProhibitionToWorkFrom(source.getProhibitionToWorkFrom());
		target.setProhibitionToWorkUntil(source.getProhibitionToWorkUntil());

		target.setReInfection(source.getReInfection());
		target.setPreviousInfectionDate(source.getPreviousInfectionDate());

		target.setReportingDistrict(DistrictFacadeEjb.toReferenceDto(source.getReportingDistrict()));
		target.setBloodOrganOrTissueDonated(source.getBloodOrganOrTissueDonated());

		target.setNotACaseReasonNegativeTest(source.isNotACaseReasonNegativeTest());
		target.setNotACaseReasonPhysicianInformation(source.isNotACaseReasonPhysicianInformation());
		target.setNotACaseReasonDifferentPathogen(source.isNotACaseReasonDifferentPathogen());
		target.setNotACaseReasonOther(source.isNotACaseReasonOther());
		target.setNotACaseReasonDetails(source.getNotACaseReasonDetails());
		target.setSormasToSormasOriginInfo(SormasToSormasOriginInfoFacadeEjb.toDto(source.getSormasToSormasOriginInfo()));
		target.setOwnershipHandedOver(source.getShareInfoCases().stream().anyMatch(ShareInfoHelper::isOwnerShipHandedOver));
		target.setFollowUpStatusChangeDate(source.getFollowUpStatusChangeDate());
		if (source.getFollowUpStatusChangeUser() != null) {
			target.setFollowUpStatusChangeUser(source.getFollowUpStatusChangeUser().toReference());
		}
		target.setDontShareWithReportingTool(source.isDontShareWithReportingTool());

		return target;
	}

	public Case fillOrBuildEntity(@NotNull CaseDataDto source, Case target, boolean checkChangeDate) {

		target = DtoHelper.fillOrBuildEntity(source, target, () -> {
			Case newCase = new Case();
			newCase.setSystemCaseClassification(CaseClassification.NOT_CLASSIFIED);

			return newCase;
		}, checkChangeDate);

		target.setDisease(source.getDisease());
		target.setDiseaseVariant(source.getDiseaseVariant());
		target.setDiseaseDetails(source.getDiseaseDetails());
		target.setPlagueType(source.getPlagueType());
		target.setDengueFeverType(source.getDengueFeverType());
		target.setRabiesType(source.getRabiesType());
		if (source.getReportDate() != null) {
			target.setReportDate(source.getReportDate());
		} else { // make sure we do have a report date
			target.setReportDate(new Date());
		}
		target.setReportingUser(userService.getByReferenceDto(source.getReportingUser()));
		target.setInvestigatedDate(source.getInvestigatedDate());
		target.setRegionLevelDate(source.getRegionLevelDate());
		target.setNationalLevelDate(source.getNationalLevelDate());
		target.setDistrictLevelDate(source.getDistrictLevelDate());
		target.setPerson(personService.getByReferenceDto(source.getPerson()));
		target.setCaseClassification(source.getCaseClassification());
		target.setCaseIdentificationSource(source.getCaseIdentificationSource());
		target.setScreeningType(source.getScreeningType());
		target.setClassificationUser(userService.getByReferenceDto(source.getClassificationUser()));
		target.setClassificationDate(source.getClassificationDate());
		target.setClassificationComment(source.getClassificationComment());
		target.setClinicalConfirmation(source.getClinicalConfirmation());
		target.setEpidemiologicalConfirmation(source.getEpidemiologicalConfirmation());
		target.setLaboratoryDiagnosticConfirmation(source.getLaboratoryDiagnosticConfirmation());
		target.setInvestigationStatus(source.getInvestigationStatus());
		target.setHospitalization(hospitalizationFacade.fromDto(source.getHospitalization(), checkChangeDate));
		target.setEpiData(epiDataFacade.fromDto(source.getEpiData(), checkChangeDate));
		if (source.getTherapy() == null) {
			source.setTherapy(TherapyDto.build());
		}
		target.setTherapy(therapyFacade.fromDto(source.getTherapy(), checkChangeDate));
		if (source.getClinicalCourse() == null) {
			source.setClinicalCourse(ClinicalCourseDto.build());
		}
		target.setClinicalCourse(clinicalCourseFacade.fromDto(source.getClinicalCourse(), checkChangeDate));
		if (source.getMaternalHistory() == null) {
			source.setMaternalHistory(MaternalHistoryDto.build());
		}
		target.setMaternalHistory(maternalHistoryFacade.fromDto(source.getMaternalHistory(), checkChangeDate));
		if (source.getPortHealthInfo() == null) {
			source.setPortHealthInfo(PortHealthInfoDto.build());
		}
		target.setPortHealthInfo(portHealthInfoFacade.fromDto(source.getPortHealthInfo(), checkChangeDate));

		target.setResponsibleRegion(regionService.getByReferenceDto(source.getResponsibleRegion()));
		target.setResponsibleDistrict(districtService.getByReferenceDto(source.getResponsibleDistrict()));
		target.setResponsibleCommunity(communityService.getByReferenceDto(source.getResponsibleCommunity()));

		target.setRegion(regionService.getByReferenceDto(source.getRegion()));
		target.setDistrict(districtService.getByReferenceDto(source.getDistrict()));
		target.setCommunity(communityService.getByReferenceDto(source.getCommunity()));
		target.setHealthFacility(facilityService.getByReferenceDto(source.getHealthFacility()));
		target.setHealthFacilityDetails(source.getHealthFacilityDetails());

		target.setSurveillanceOfficer(userService.getByReferenceDto(source.getSurveillanceOfficer()));
		target.setClinicianName(source.getClinicianName());
		target.setClinicianPhone(source.getClinicianPhone());
		target.setClinicianEmail(source.getClinicianEmail());
		target.setCaseOfficer(userService.getByReferenceDto(source.getCaseOfficer()));
		target.setSymptoms(symptomsFacade.fromDto(source.getSymptoms(), checkChangeDate));

		target.setPregnant(source.getPregnant());
		target.setVaccination(source.getVaccination());
		target.setVaccinationDoses(source.getVaccinationDoses());
		target.setVaccinationInfoSource(source.getVaccinationInfoSource());
		target.setVaccine(source.getVaccine());
		target.setSmallpoxVaccinationScar(source.getSmallpoxVaccinationScar());
		target.setSmallpoxVaccinationReceived(source.getSmallpoxVaccinationReceived());
		target.setFirstVaccinationDate(source.getFirstVaccinationDate());
		target.setLastVaccinationDate(source.getLastVaccinationDate());
		target.setVaccineName(source.getVaccineName());
		target.setOtherVaccineName(source.getOtherVaccineName());
		target.setVaccineManufacturer(source.getVaccineManufacturer());
		target.setOtherVaccineManufacturer(source.getOtherVaccineManufacturer());
		target.setVaccineInn(source.getVaccineInn());
		target.setVaccineBatchNumber(source.getVaccineBatchNumber());
		target.setVaccineUniiCode(source.getVaccineUniiCode());
		target.setVaccineAtcCode(source.getVaccineAtcCode());

		target.setEpidNumber(source.getEpidNumber());

		target.setReportLat(source.getReportLat());
		target.setReportLon(source.getReportLon());
		target.setReportLatLonAccuracy(source.getReportLatLonAccuracy());

		target.setOutcome(source.getOutcome());
		target.setOutcomeDate(source.getOutcomeDate());
		target.setSequelae(source.getSequelae());
		target.setSequelaeDetails(source.getSequelaeDetails());
		target.setNotifyingClinic(source.getNotifyingClinic());
		target.setNotifyingClinicDetails(source.getNotifyingClinicDetails());

		target.setCreationVersion(source.getCreationVersion());
		target.setCaseOrigin(source.getCaseOrigin());
		target.setPointOfEntry(pointOfEntryService.getByReferenceDto(source.getPointOfEntry()));
		target.setPointOfEntryDetails(source.getPointOfEntryDetails());
		target.setAdditionalDetails(source.getAdditionalDetails());
		target.setExternalID(source.getExternalID());
		target.setExternalToken(source.getExternalToken());
		target.setInternalToken(source.getInternalToken());
		target.setSharedToCountry(source.isSharedToCountry());
		target.setQuarantine(source.getQuarantine());
		target.setQuarantineTypeDetails(source.getQuarantineTypeDetails());
		target.setQuarantineTo(source.getQuarantineTo());
		target.setQuarantineFrom(source.getQuarantineFrom());
		target.setQuarantineHelpNeeded(source.getQuarantineHelpNeeded());
		target.setQuarantineOrderedVerbally(source.isQuarantineOrderedVerbally());
		target.setQuarantineOrderedOfficialDocument(source.isQuarantineOrderedOfficialDocument());
		target.setQuarantineOrderedVerballyDate(source.getQuarantineOrderedVerballyDate());
		target.setQuarantineOrderedOfficialDocumentDate(source.getQuarantineOrderedOfficialDocumentDate());
		target.setQuarantineHomePossible(source.getQuarantineHomePossible());
		target.setQuarantineHomePossibleComment(source.getQuarantineHomePossibleComment());
		target.setQuarantineHomeSupplyEnsured(source.getQuarantineHomeSupplyEnsured());
		target.setQuarantineHomeSupplyEnsuredComment(source.getQuarantineHomeSupplyEnsuredComment());
		target.setQuarantineExtended(source.isQuarantineExtended());
		target.setQuarantineReduced(source.isQuarantineReduced());
		target.setQuarantineOfficialOrderSent(source.isQuarantineOfficialOrderSent());
		target.setQuarantineOfficialOrderSentDate(source.getQuarantineOfficialOrderSentDate());
		target.setPostpartum(source.getPostpartum());
		target.setTrimester(source.getTrimester());
		target.setFacilityType(source.getFacilityType());
		if (source.getSormasToSormasOriginInfo() != null) {
			target.setSormasToSormasOriginInfo(originInfoFacade.fromDto(source.getSormasToSormasOriginInfo(), checkChangeDate));
		}

		// TODO this makes sure follow-up is not overriden from the mobile app side. remove once that is implemented
		if (source.getFollowUpStatus() != null) {
			target.setFollowUpComment(source.getFollowUpComment());
			target.setFollowUpStatus(source.getFollowUpStatus());
			target.setFollowUpUntil(source.getFollowUpUntil());
			target.setOverwriteFollowUpUntil(source.isOverwriteFollowUpUntil());
			target.setFollowUpStatusChangeDate(source.getFollowUpStatusChangeDate());
			target.setFollowUpStatusChangeUser(userService.getByReferenceDto(source.getFollowUpStatusChangeUser()));
		}

		target.setCaseIdIsm(source.getCaseIdIsm());
		target.setContactTracingFirstContactType(source.getContactTracingFirstContactType());
		target.setContactTracingFirstContactDate(source.getContactTracingFirstContactDate());
		target.setQuarantineReasonBeforeIsolation(source.getQuarantineReasonBeforeIsolation());
		target.setWasInQuarantineBeforeIsolation(source.getWasInQuarantineBeforeIsolation());
		target.setQuarantineReasonBeforeIsolationDetails(source.getQuarantineReasonBeforeIsolationDetails());
		target.setEndOfIsolationReason(source.getEndOfIsolationReason());
		target.setEndOfIsolationReasonDetails(source.getEndOfIsolationReasonDetails());

		target.setNosocomialOutbreak(source.isNosocomialOutbreak());
		target.setInfectionSetting(source.getInfectionSetting());

		target.setProhibitionToWork(source.getProhibitionToWork());
		target.setProhibitionToWorkFrom(source.getProhibitionToWorkFrom());
		target.setProhibitionToWorkUntil(source.getProhibitionToWorkUntil());

		target.setReInfection(source.getReInfection());
		target.setPreviousInfectionDate(source.getPreviousInfectionDate());

		target.setReportingDistrict(districtService.getByReferenceDto(source.getReportingDistrict()));
		target.setBloodOrganOrTissueDonated(source.getBloodOrganOrTissueDonated());

		target.setNotACaseReasonNegativeTest(source.isNotACaseReasonNegativeTest());
		target.setNotACaseReasonPhysicianInformation(source.isNotACaseReasonPhysicianInformation());
		target.setNotACaseReasonDifferentPathogen(source.isNotACaseReasonDifferentPathogen());
		target.setNotACaseReasonOther(source.isNotACaseReasonOther());
		target.setNotACaseReasonDetails(source.getNotACaseReasonDetails());
		target.setDontShareWithReportingTool(source.isDontShareWithReportingTool());

		return target;
	}

	public void updateInvestigationByStatus(CaseDataDto existingCase, Case caze) {

		CaseReferenceDto caseRef = caze.toReference();
		InvestigationStatus investigationStatus = caze.getInvestigationStatus();

		if (investigationStatus != InvestigationStatus.PENDING) {
			// Set the investigation date
			if (caze.getInvestigatedDate() == null) {
				caze.setInvestigatedDate(new Date());
			}

			// Set the task status of all investigation tasks to "Removed" because
			// the case status has been updated manually
			if (existingCase != null) {
				List<Task> pendingTasks =
					taskService.findBy(new TaskCriteria().taskType(TaskType.CASE_INVESTIGATION).caze(caseRef).taskStatus(TaskStatus.PENDING), true);
				final boolean caseStatusSetToDone =
					caze.getInvestigationStatus() == InvestigationStatus.DONE && existingCase.getInvestigationStatus() != InvestigationStatus.DONE;
				for (Task task : pendingTasks) {
					task.setTaskStatus(caseStatusSetToDone ? TaskStatus.DONE : TaskStatus.REMOVED);
					task.setStatusChangeDate(new Date());
				}

				if (caseStatusSetToDone) {
					sendInvestigationDoneNotifications(caze);
				}
			}

		} else {
			// Remove the investigation date
			caze.setInvestigatedDate(null);

			// Create a new investigation task if none is present
			long pendingCount = existingCase != null
				? taskService.getCount(new TaskCriteria().taskType(TaskType.CASE_INVESTIGATION).caze(caseRef).taskStatus(TaskStatus.PENDING))
				: 0;

			if (pendingCount == 0 && featureConfigurationFacade.isTaskGenerationFeatureEnabled(TaskType.CASE_INVESTIGATION)) {
				createInvestigationTask(caze);
			}
		}
	}

	public void updateInvestigationByTask(Case caze) {

		CaseReferenceDto caseRef = caze.toReference();

		// any pending case investigation task?
		long pendingCount =
			taskService.getCount(new TaskCriteria().taskType(TaskType.CASE_INVESTIGATION).caze(caseRef).taskStatus(TaskStatus.PENDING));

		if (pendingCount > 0) {
			// set status to investigation pending
			caze.setInvestigationStatus(InvestigationStatus.PENDING);
			// .. and clear date
			caze.setInvestigatedDate(null);
		} else {
			// get "case investigation" task created last
			List<Task> cazeTasks = taskService.findBy(new TaskCriteria().taskType(TaskType.CASE_INVESTIGATION).caze(caseRef), true);

			if (!cazeTasks.isEmpty()) {
				Task youngestTask = cazeTasks.stream().max(new Comparator<Task>() {

					@Override
					public int compare(Task o1, Task o2) {
						return o1.getCreationDate().compareTo(o2.getCreationDate());
					}
				}).get();

				switch (youngestTask.getTaskStatus()) {
				case PENDING:
					throw new UnsupportedOperationException("there should not be any pending tasks");
				case DONE:
					caze.setInvestigationStatus(InvestigationStatus.DONE);
					caze.setInvestigatedDate(youngestTask.getStatusChangeDate());
					sendInvestigationDoneNotifications(caze);
					break;
				case REMOVED:
					caze.setInvestigationStatus(InvestigationStatus.DISCARDED);
					caze.setInvestigatedDate(youngestTask.getStatusChangeDate());
					break;
				case NOT_EXECUTABLE:
					caze.setInvestigationStatus(InvestigationStatus.PENDING);
					caze.setInvestigatedDate(null);
					break;
				default:
					break;
				}
			}
		}
	}

	private void createInvestigationTask(Case caze) {

		Task task = new Task();
		task.setTaskStatus(TaskStatus.PENDING);
		task.setTaskContext(TaskContext.CASE);
		task.setCaze(caze);
		task.setTaskType(TaskType.CASE_INVESTIGATION);
		task.setSuggestedStart(TaskHelper.getDefaultSuggestedStart());
		task.setDueDate(TaskHelper.getDefaultDueDate());
		task.setPriority(TaskPriority.NORMAL);

		assignOfficerOrSupervisorToTask(caze, task);

		taskService.ensurePersisted(task);
	}

	private void createActiveSearchForOtherCasesTask(Case caze) {

		Task task = new Task();
		task.setTaskStatus(TaskStatus.PENDING);
		task.setTaskContext(TaskContext.CASE);
		task.setCaze(caze);
		task.setTaskType(TaskType.ACTIVE_SEARCH_FOR_OTHER_CASES);
		task.setSuggestedStart(TaskHelper.getDefaultSuggestedStart());
		task.setDueDate(TaskHelper.getDefaultDueDate());
		task.setPriority(TaskPriority.NORMAL);

		assignOfficerOrSupervisorToTask(caze, task);

		taskService.ensurePersisted(task);
	}

	private void assignOfficerOrSupervisorToTask(Case caze, Task task) {

		User assignee = null;

		if (caze.getSurveillanceOfficer() != null) {
			// 1) The surveillance officer that is responsible for the case
			assignee = caze.getSurveillanceOfficer();
		} else if (caze.getResponsibleDistrict() != null) {
			// 2) A random surveillance officer from the case responsible district
			assignee = getRandomSurveillanceOfficer(caze.getResponsibleDistrict());
		}

		if (assignee == null && caze.getDistrict() != null) {
			// 3) A random surveillance officer from the case district
			assignee = getRandomSurveillanceOfficer(caze.getDistrict());
		}

		if (assignee == null) {
			if (caze.getReportingUser() != null
				&& (caze.getReportingUser().getUserRoles().contains(UserRole.SURVEILLANCE_SUPERVISOR)
					|| caze.getReportingUser().getUserRoles().contains(UserRole.ADMIN_SUPERVISOR))) {
				// 4) If the case was created by a surveillance supervisor, assign them
				assignee = caze.getReportingUser();
			} else if (caze.getResponsibleRegion() != null) {
				// 5) Assign a random surveillance supervisor from the case responsible region
				assignee = getRandomRegionUser(caze.getResponsibleRegion());
			}
			if (caze.getRegion() != null) {
				// 6) Assign a random surveillance supervisor from the case region
				assignee = getRandomRegionUser(caze.getRegion());
			}
		}

		task.setAssigneeUser(assignee);
		if (assignee == null) {
			logger.warn("No valid assignee user found for task " + task.getUuid());
		}
	}

	private User getRandomSurveillanceOfficer(District district) {

		return userService.getRandomUser(district, UserRole.SURVEILLANCE_OFFICER);
	}

	private User getRandomRegionUser(Region region) {

		return userService.getRandomUser(region, UserRole.SURVEILLANCE_SUPERVISOR, UserRole.ADMIN_SUPERVISOR);
	}

	@Override
	public boolean doesEpidNumberExist(String epidNumber, String caseUuid, Disease caseDisease) {
		if (epidNumber == null) {
			return false;
		}

		int suffixSeperatorIndex = epidNumber.lastIndexOf('-');
		if (suffixSeperatorIndex == -1) {
			// no suffix - use the whole string as prefix
			suffixSeperatorIndex = epidNumber.length() - 1;
		}
		String prefixString = epidNumber.substring(0, suffixSeperatorIndex + 1);
		String suffixString = epidNumber.substring(suffixSeperatorIndex + 1);
		suffixString = suffixString.replaceAll("[^\\d]", "");

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Case> caze = cq.from(Case.class);

		Predicate filter = cb.and(cb.equal(caze.get(Case.DELETED), false), cb.equal(caze.get(Case.DISEASE), caseDisease));
		if (!DataHelper.isNullOrEmpty(caseUuid)) {
			filter = cb.and(filter, cb.notEqual(caze.get(Case.UUID), caseUuid));
		}

		ParameterExpression<String> regexPattern = null, regexReplacement = null, regexFlags = null;
		if (suffixString.length() > 0) {
			// has to start with prefix
			filter = cb.and(filter, cb.like(caze.get(Case.EPID_NUMBER), prefixString + "%"));

			// for the suffix only consider the actual number. Any other characters and leading zeros are ignored
			int suffixNumber;
			try {
				suffixNumber = Integer.parseInt(suffixString);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(
					String.format("Invalid suffix for epid number. epidNumber: '%s', suffixString: '%s'", epidNumber, suffixString),
					e);
			}
			regexPattern = cb.parameter(String.class);
			regexReplacement = cb.parameter(String.class);
			regexFlags = cb.parameter(String.class);
			Expression<String> epidNumberSuffixClean = cb.function(
				"regexp_replace",
				String.class,
				cb.substring(caze.get(Case.EPID_NUMBER), suffixSeperatorIndex + 2),
				regexPattern,
				regexReplacement,
				regexFlags);
			filter = cb.and(filter, cb.equal(cb.concat("0", epidNumberSuffixClean).as(Integer.class), suffixNumber));
		} else {
			filter = cb.and(filter, cb.equal(caze.get(Case.EPID_NUMBER), prefixString));
		}
		cq.where(filter);

		cq.select(caze.get(Case.EPID_NUMBER));
		TypedQuery<String> query = em.createQuery(cq);
		if (regexPattern != null) {
			query.setParameter(regexPattern, "\\D"); // Non-digits
			query.setParameter(regexReplacement, ""); // Replace all non-digits with empty string
			query.setParameter(regexFlags, "g"); // Global search
		}
		query.setMaxResults(1);
		return !query.getResultList().isEmpty();
	}

	@Override
	public boolean doesExternalTokenExist(String externalToken, String caseUuid) {
		return caseService.exists(
			(cb, caseRoot) -> CriteriaBuilderHelper.and(
				cb,
				cb.equal(caseRoot.get(Case.EXTERNAL_TOKEN), externalToken),
				cb.notEqual(caseRoot.get(Case.UUID), caseUuid),
				cb.notEqual(caseRoot.get(Case.DELETED), Boolean.TRUE)));
	}

	@Override
	public List<Pair<DistrictDto, BigDecimal>> getCaseMeasurePerDistrict(Date fromDate, Date toDate, Disease disease, CaseMeasure caseMeasure) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<Case> from = cq.from(Case.class);

		Predicate filter = caseService.createDefaultFilter(cb, from);
		if (fromDate != null || toDate != null) {
			filter = caseService.createCaseRelevanceFilter(cb, from, fromDate, toDate);
		}

		if (disease != null) {
			Predicate diseaseFilter = cb.equal(from.get(Case.DISEASE), disease);
			filter = filter != null ? cb.and(filter, diseaseFilter) : diseaseFilter;
		}

		if (filter != null) {
			cq.where(filter);
		}

		Join<Case, District> districtJoin = from.join(Case.DISTRICT, JoinType.LEFT);
		cq.groupBy(districtJoin);
		cq.multiselect(districtJoin, cb.count(from));
		if (caseMeasure == CaseMeasure.CASE_COUNT) {
			cq.orderBy(cb.asc(cb.count(from)));
		}
		List<Object[]> results = em.createQuery(cq).getResultList();

		if (caseMeasure == CaseMeasure.CASE_COUNT) {
			List<Pair<DistrictDto, BigDecimal>> resultList = results.stream()
				.map(e -> new Pair<DistrictDto, BigDecimal>(districtFacade.toDto((District) e[0]), new BigDecimal((Long) e[1])))
				.collect(Collectors.toList());
			return resultList;
		} else {
			List<Pair<DistrictDto, BigDecimal>> resultList = results.stream().map(e -> {
				District district = (District) e[0];
				Integer population = populationDataFacade.getProjectedDistrictPopulation(district.getUuid());
				Long caseCount = (Long) e[1];

				if (population == null || population <= 0) {
					// No or negative population - these entries will be cut off in the UI
					return new Pair<DistrictDto, BigDecimal>(districtFacade.toDto(district), new BigDecimal(0));
				} else {
					return new Pair<DistrictDto, BigDecimal>(
						districtFacade.toDto(district),
						InfrastructureHelper.getCaseIncidence(caseCount.intValue(), population, InfrastructureHelper.CASE_INCIDENCE_DIVISOR));
				}
			}).sorted(new Comparator<Pair<DistrictDto, BigDecimal>>() {

				@Override
				public int compare(Pair<DistrictDto, BigDecimal> o1, Pair<DistrictDto, BigDecimal> o2) {
					return o1.getElement1().compareTo(o2.getElement1());
				}
			}).collect(Collectors.toList());
			return resultList;
		}
	}

	private void sendInvestigationDoneNotifications(Case caze) {

		List<User> messageRecipients = userService.getAllByRegionsAndUserRoles(
			JurisdictionHelper.getCaseRegions(caze),
			UserRole.SURVEILLANCE_SUPERVISOR,
			UserRole.ADMIN_SUPERVISOR,
			UserRole.CASE_SUPERVISOR,
			UserRole.CONTACT_SUPERVISOR);
		for (User recipient : messageRecipients) {
			try {
				messagingService.sendMessage(
					recipient,
					MessageSubject.CASE_INVESTIGATION_DONE,
					String
						.format(I18nProperties.getString(MessagingService.CONTENT_CASE_INVESTIGATION_DONE), DataHelper.getShortUuid(caze.getUuid())),
					MessageType.EMAIL,
					MessageType.SMS);
			} catch (NotificationDeliveryFailedException e) {
				logger.error(
					String.format(
						"NotificationDeliveryFailedException when trying to notify supervisors about the completion of a case investigation. "
							+ "Failed to send " + e.getMessageType() + " to user with UUID %s.",
						recipient.getUuid()));
			}
		}
	}

	@Override
	public Date getOldestCaseOnsetDate() {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Timestamp> cq = cb.createQuery(Timestamp.class);
		Root<Case> from = cq.from(Case.class);
		Join<Case, Symptoms> symptoms = from.join(Case.SYMPTOMS, JoinType.LEFT);

		Path<Timestamp> expression = symptoms.get(Symptoms.ONSET_DATE);
		cq.select(cb.least(expression));
		cq.where(cb.greaterThan(symptoms.get(Symptoms.ONSET_DATE), DateHelper.getDateZero(2000, 1, 1)));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public Date getOldestCaseReportDate() {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Timestamp> cq = cb.createQuery(Timestamp.class);
		Root<Case> from = cq.from(Case.class);

		final Path<Timestamp> reportDate = from.get(Case.REPORT_DATE);
		cq.select(cb.least(reportDate));
		cq.where(cb.greaterThan(from.get(Case.REPORT_DATE), DateHelper.getDateZero(2000, 1, 1)));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public Date getOldestCaseOutcomeDate() {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Timestamp> cq = cb.createQuery(Timestamp.class);
		Root<Case> from = cq.from(Case.class);

		final Path<Timestamp> reportDate = from.get(Case.OUTCOME_DATE);
		cq.select(cb.least(reportDate));
		cq.where(cb.greaterThan(from.get(Case.OUTCOME_DATE), DateHelper.getDateZero(2000, 1, 1)));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public boolean isArchived(String caseUuid) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Case> from = cq.from(Case.class);

		// Workaround for probable bug in Eclipse Link/Postgre that throws a
		// NoResultException when trying to
		// query for a true Boolean result
		cq.where(cb.and(cb.equal(from.get(Case.ARCHIVED), true), cb.equal(from.get(AbstractDomainObject.UUID), caseUuid)));
		cq.select(cb.count(from));
		long count = em.createQuery(cq).getSingleResult();
		return count > 0;
	}

	@Override
	public boolean isDeleted(String caseUuid) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Case> from = cq.from(Case.class);

		cq.where(cb.and(cb.isTrue(from.get(Case.DELETED)), cb.equal(from.get(AbstractDomainObject.UUID), caseUuid)));
		cq.select(cb.count(from));
		long count = em.createQuery(cq).getSingleResult();
		return count > 0;
	}

	@Override
	public void mergeCase(String leadUuid, String otherUuid) {

		mergeCase(getCaseDataWithoutPseudonyimization(leadUuid), getCaseDataWithoutPseudonyimization(otherUuid), false);
	}

	private void mergeCase(CaseDataDto leadCaseData, CaseDataDto otherCaseData, boolean cloning) {

		// 1 Merge Dtos
		// 1.1 Case

		copyDtoValues(leadCaseData, otherCaseData, cloning);
		saveCase(leadCaseData, !cloning, true);

		// 1.2 Person - Only merge when the persons have different UUIDs
		if (!cloning && !DataHelper.equal(leadCaseData.getPerson().getUuid(), otherCaseData.getPerson().getUuid())) {
			PersonDto leadPerson = personFacade.getPersonByUuid(leadCaseData.getPerson().getUuid());
			PersonDto otherPerson = personFacade.getPersonByUuid(otherCaseData.getPerson().getUuid());
			Set primaryContactDetailTypes = new HashSet<>();
			for (PersonContactDetailDto contactDetailDto : leadPerson.getPersonContactDetails()) {
				if (contactDetailDto.isPrimaryContact()) {
					primaryContactDetailTypes.add(contactDetailDto.getPersonContactDetailType());
				}
			}
			for (PersonContactDetailDto contactDetailDto : otherPerson.getPersonContactDetails()) {
				if (contactDetailDto.isPrimaryContact() && primaryContactDetailTypes.contains(contactDetailDto.getPersonContactDetailType())) {
					contactDetailDto.setPrimaryContact(false);
				}
			}
			DtoHelper.copyDtoValues(leadPerson, otherPerson, false);
			personFacade.savePerson(leadPerson);
		} else {
			assert (DataHelper.equal(leadCaseData.getPerson().getUuid(), otherCaseData.getPerson().getUuid()));
		}

		// 2 Change CaseReference
		Case leadCase = caseService.getByUuid(leadCaseData.getUuid());
		Case otherCase = caseService.getByUuid(otherCaseData.getUuid());

		// 2.1 Contacts
		List<Contact> contacts = contactService.findBy(new ContactCriteria().caze(otherCase.toReference()), null);
		for (Contact contact : contacts) {
			if (cloning) {
				ContactDto newContact =
					ContactDto.build(leadCase.toReference(), leadCase.getDisease(), leadCase.getDiseaseDetails(), leadCase.getDiseaseVariant());
				newContact.setPerson(new PersonReferenceDto(contact.getPerson().getUuid()));
				DtoHelper.copyDtoValues(newContact, ContactFacadeEjb.toDto(contact), cloning);
				contactFacade.saveContact(newContact, false, false);
			} else {
				// simply move existing entities to the merge target
				contact.setCaze(leadCase);
				contactService.ensurePersisted(contact);
			}
		}

		// 2.2 Samples
		List<Sample> samples = sampleService.findBy(new SampleCriteria().caze(otherCase.toReference()), null);
		for (Sample sample : samples) {
			if (cloning) {
				SampleDto newSample = SampleDto.build(sample.getReportingUser().toReference(), leadCase.toReference());
				DtoHelper.copyDtoValues(newSample, SampleFacadeEjb.toDto(sample), cloning);
				sampleFacade.saveSample(newSample, false, true);

				// 2.2.1 Pathogen Tests
				for (PathogenTest pathogenTest : sample.getPathogenTests()) {
					PathogenTestDto newPathogenTest = PathogenTestDto.build(newSample.toReference(), pathogenTest.getLabUser().toReference());
					DtoHelper.copyDtoValues(newPathogenTest, PathogenTestFacadeEjbLocal.toDto(pathogenTest), cloning);
					sampleTestFacade.savePathogenTest(newPathogenTest);
				}

				for (AdditionalTest additionalTest : sample.getAdditionalTests()) {
					AdditionalTestDto newAdditionalTest = AdditionalTestDto.build(newSample.toReference());
					DtoHelper.copyDtoValues(newAdditionalTest, AdditionalTestFacadeEjbLocal.toDto(additionalTest), cloning);
					additionalTestFacade.saveAdditionalTest(newAdditionalTest);
				}
			} else {
				// simply move existing entities to the merge target
				sample.setAssociatedCase(leadCase);
				sampleService.ensurePersisted(sample);
			}
		}

		// 2.3 Tasks
		if (!cloning) {
			// simply move existing entities to the merge target

			List<Task> tasks = taskService.findBy(new TaskCriteria().caze(new CaseReferenceDto(otherCase.getUuid())), true);
			for (Task task : tasks) {
				task.setCaze(leadCase);
				taskService.ensurePersisted(task);
			}
		}

		// 3 Change Therapy Reference
		// 3.1 Treatments
		List<Treatment> treatments =
			treatmentService.findBy(new TreatmentCriteria().therapy(new TherapyReferenceDto(otherCase.getTherapy().getUuid())));
		TherapyReferenceDto leadCaseTherapyReference = new TherapyReferenceDto(leadCase.getTherapy().getUuid());
		for (Treatment treatment : treatments) {
			if (cloning) {
				TreatmentDto newTreatment = TreatmentDto.build(leadCaseTherapyReference);
				DtoHelper.copyDtoValues(newTreatment, TreatmentFacadeEjb.toDto(treatment), cloning);
				treatmentFacade.saveTreatment(newTreatment);
			} else {
				// simply move existing entities to the merge target
				treatment.setTherapy(leadCase.getTherapy());
				treatmentService.ensurePersisted(treatment);
			}
		}

		// 3.2 Prescriptions
		List<Prescription> prescriptions =
			prescriptionService.findBy(new PrescriptionCriteria().therapy(new TherapyReferenceDto(otherCase.getTherapy().getUuid())));
		for (Prescription prescription : prescriptions) {
			if (cloning) {
				PrescriptionDto newPrescription = PrescriptionDto.buildPrescription(leadCaseTherapyReference);
				DtoHelper.copyDtoValues(newPrescription, PrescriptionFacadeEjb.toDto(prescription), cloning);
				prescriptionFacade.savePrescription(newPrescription);
			} else {
				// simply move existing entities to the merge target
				prescription.setTherapy(leadCase.getTherapy());
				prescriptionService.ensurePersisted(prescription);
			}
		}

		// 4 Change Clinical Course Reference
		// 4.1 Clinical Visits
		List<ClinicalVisit> clinicalVisits = clinicalVisitService
			.findBy(new ClinicalVisitCriteria().clinicalCourse(new ClinicalCourseReferenceDto(otherCase.getClinicalCourse().getUuid())));
		for (ClinicalVisit clinicalVisit : clinicalVisits) {
			if (cloning) {
				ClinicalVisitDto newClinicalVisit = ClinicalVisitDto.build(leadCaseData.getClinicalCourse().toReference(), leadCase.getDisease());
				DtoHelper.copyDtoValues(newClinicalVisit, ClinicalVisitFacadeEjb.toDto(clinicalVisit), cloning);
				clinicalVisitFacade.saveClinicalVisit(newClinicalVisit, leadCase.getUuid(), false);
			} else {
				// simply move existing entities to the merge target
				clinicalVisit.setClinicalCourse(leadCase.getClinicalCourse());
				clinicalVisitService.ensurePersisted(clinicalVisit);
			}
		}

		// 5 Attach otherCase visits to leadCase
		// (set the person and the disease of the visit, saveVisit does the rest)
		for (VisitDto otherVisit : otherCase.getVisits().stream().map(VisitFacadeEjb::toDto).collect(Collectors.toList())) {
			otherVisit.setPerson(leadCaseData.getPerson());
			otherVisit.setDisease(leadCaseData.getDisease());
			visitFacade.saveVisit(otherVisit);
		}

		// 6 Documents
		List<Document> documents = documentService.getRelatedToEntity(DocumentRelatedEntityType.CASE, otherCase.getUuid());
		for (Document document : documents) {
			document.setRelatedEntityUuid(leadCaseData.getUuid());

			documentService.ensurePersisted(document);
		}
	}

	private void copyDtoValues(CaseDataDto leadCaseData, CaseDataDto otherCaseData, boolean cloning) {
		String leadAdditionalDetails = leadCaseData.getAdditionalDetails();
		String leadFollowUpComment = leadCaseData.getFollowUpComment();

		DtoHelper.copyDtoValues(leadCaseData, otherCaseData, cloning);

		if (!cloning) {
			leadCaseData.setAdditionalDetails(DataHelper.joinStrings(" ", leadAdditionalDetails, otherCaseData.getAdditionalDetails()));
			leadCaseData.setFollowUpComment(DataHelper.joinStrings(" ", leadFollowUpComment, otherCaseData.getFollowUpComment()));
		}
	}

	@Override
	public CaseDataDto cloneCase(CaseDataDto existingCaseDto) {

		CaseDataDto newCase = CaseDataDto.build(existingCaseDto.getPerson(), existingCaseDto.getDisease());
		mergeCase(newCase, existingCaseDto, true);
		return getCaseDataByUuid(newCase.getUuid());
	}

	/**
	 * Archives all cases that have not been changed for a defined amount of days
	 *
	 * @param daysAfterCaseGetsArchived
	 *            defines the amount of days
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void archiveAllArchivableCases(int daysAfterCaseGetsArchived) {

		archiveAllArchivableCases(daysAfterCaseGetsArchived, LocalDate.now());
	}

	void archiveAllArchivableCases(int daysAfterCaseGetsArchived, LocalDate referenceDate) {

		long startTime = DateHelper.startTime();

		LocalDate notChangedSince = referenceDate.minusDays(daysAfterCaseGetsArchived);

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Case> from = cq.from(Case.class);

		Timestamp notChangedTimestamp = Timestamp.valueOf(notChangedSince.atStartOfDay());
		cq.where(cb.equal(from.get(Case.ARCHIVED), false), cb.not(caseService.createChangeDateFilter(cb, from, notChangedTimestamp, true)));
		cq.select(from.get(Case.UUID));
		List<String> caseUuids = em.createQuery(cq).getResultList();

		IterableHelper.executeBatched(caseUuids, ARCHIVE_BATCH_SIZE, batchedCaseUuids -> caseService.updateArchived(batchedCaseUuids, true));
		logger.debug(
			"archiveAllArchivableCases() finished. caseCount = {}, daysAfterCaseGetsArchived = {}, {}ms",
			caseUuids.size(),
			daysAfterCaseGetsArchived,
			DateHelper.durationMillies(startTime));
	}

	@Override
	public void updateArchived(List<String> caseUuids, boolean archived) {

		long startTime = DateHelper.startTime();

		IterableHelper.executeBatched(caseUuids, ARCHIVE_BATCH_SIZE, batchedCaseUuids -> caseService.updateArchived(batchedCaseUuids, archived));
		logger.debug(
			"updateArchived() finished. caseCount = {}, archived = {}, {}ms",
			caseUuids.size(),
			archived,
			DateHelper.durationMillies(startTime));
	}

	@Override
	public boolean exists(String uuid) {
		return caseService.exists(uuid);
	}

	@Override
	public boolean hasPositiveLabResult(String caseUuid) {
		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		final CriteriaQuery<Sample> query = criteriaBuilder.createQuery(Sample.class);
		final Root<Sample> sampleRoot = query.from(Sample.class);
		final Join<Sample, Case> caseJoin = sampleRoot.join(Sample.ASSOCIATED_CASE, JoinType.INNER);
		final Predicate casePredicate = criteriaBuilder.equal(caseJoin.get(AbstractDomainObject.UUID), caseUuid);
		final Predicate testPositivityPredicate = criteriaBuilder.equal(sampleRoot.get(Sample.PATHOGEN_TEST_RESULT), PathogenTestResultType.POSITIVE);
		return sampleService.exists((cb, root) -> cb.and(casePredicate, testPositivityPredicate));
	}

	@Override
	public List<CaseFollowUpDto> getCaseFollowUpList(
		CaseCriteria caseCriteria,
		Date referenceDate,
		int interval,
		Integer first,
		Integer max,
		List<SortProperty> sortProperties) {

		Date end = DateHelper.getEndOfDay(referenceDate);
		Date start = DateHelper.getStartOfDay(DateHelper.subtractDays(end, interval));

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CaseFollowUpDto> cq = cb.createQuery(CaseFollowUpDto.class);
		Root<Case> caze = cq.from(Case.class);

		final CaseQueryContext caseQueryContext = new CaseQueryContext(cb, cq, caze);
		final CaseJoins<Case> joins = (CaseJoins<Case>) caseQueryContext.getJoins();

		final Stream<Selection<?>> select = Stream.of(
			caze.get(Case.UUID),
			joins.getPerson().get(Person.FIRST_NAME),
			joins.getPerson().get(Person.LAST_NAME),
			caze.get(Case.REPORT_DATE),
			joins.getSymptoms().get(Symptoms.ONSET_DATE),
			caze.get(Case.FOLLOW_UP_UNTIL),
			joins.getPerson().get(Person.SYMPTOM_JOURNAL_STATUS),
			caze.get(Case.DISEASE));
		cq.multiselect(Stream.concat(select, listQueryBuilder.getJurisdictionSelections(joins)).collect(Collectors.toList()));

		Predicate filter = CriteriaBuilderHelper
			.and(cb, caseService.createUserFilter(cb, cq, caze), caseService.createCriteriaFilter(caseCriteria, caseQueryContext));

		if (filter != null) {
			cq.where(filter);
		}

		if (sortProperties != null && !sortProperties.isEmpty()) {
			List<Order> order = new ArrayList<Order>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case FollowUpDto.UUID:
				case FollowUpDto.REPORT_DATE:
				case FollowUpDto.FOLLOW_UP_UNTIL:
					expression = caze.get(sortProperty.propertyName);
					break;
				case FollowUpDto.FIRST_NAME:
					expression = joins.getPerson().get(Person.FIRST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					break;
				case FollowUpDto.SYMPTOM_JOURNAL_STATUS:
					expression = joins.getPerson().get(Person.SYMPTOM_JOURNAL_STATUS);
					break;
				case FollowUpDto.LAST_NAME:
					expression = joins.getPerson().get(Person.LAST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(caze.get(Case.CHANGE_DATE)));
		}

		List<CaseFollowUpDto> resultList = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();

		if (!resultList.isEmpty()) {

			List<String> caseUuids = resultList.stream().map(FollowUpDto::getUuid).collect(Collectors.toList());

			CriteriaQuery<Object[]> visitsCq = cb.createQuery(Object[].class);
			Root<Case> visitsCqRoot = visitsCq.from(Case.class);
			Join<Case, Visit> visitsJoin = visitsCqRoot.join(Case.VISITS, JoinType.LEFT);
			Join<Visit, Symptoms> visitSymptomsJoin = visitsJoin.join(Visit.SYMPTOMS, JoinType.LEFT);

			visitsCq.where(
				CriteriaBuilderHelper.and(
					cb,
					caze.get(AbstractDomainObject.UUID).in(caseUuids),
					cb.isNotEmpty(visitsCqRoot.get(Case.VISITS)),
					cb.between(visitsJoin.get(Visit.VISIT_DATE_TIME), start, end)));
			visitsCq.multiselect(
				visitsCqRoot.get(Case.UUID),
				visitsJoin.get(Visit.VISIT_DATE_TIME),
				visitsJoin.get(Visit.VISIT_STATUS),
				visitsJoin.get(Visit.ORIGIN),
				visitSymptomsJoin.get(Symptoms.SYMPTOMATIC));
			// Sort by visit date so that we'll have the latest visit of each day
			visitsCq.orderBy(cb.asc(visitsJoin.get(Visit.VISIT_DATE_TIME)));

			visitsCq.orderBy(cb.asc(visitsJoin.get(Visit.VISIT_DATE_TIME)), cb.asc(visitsJoin.get(Visit.CREATION_DATE)));

			List<Object[]> visits = em.createQuery(visitsCq).getResultList();
			Map<String, CaseFollowUpDto> resultMap = resultList.stream().collect(Collectors.toMap(CaseFollowUpDto::getUuid, Function.identity()));

			Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue));

			resultMap.values().stream().forEach(caseFollowUpDto -> {
				caseFollowUpDto.initVisitSize(interval + 1);

				boolean isInJurisdiction = caseJurisdictionChecker.isInJurisdictionOrOwned(caseFollowUpDto.getJurisdiction());
				pseudonymizer.pseudonymizeDto(CaseFollowUpDto.class, caseFollowUpDto, isInJurisdiction, null);
			});

			visits.stream().forEach(v -> {
				int day = DateHelper.getDaysBetween(start, (Date) v[1]);
				VisitResultDto result = getVisitResult((VisitStatus) v[2], (VisitOrigin) v[3], (boolean) v[4]);
				resultMap.get(v[0]).getVisitResults()[day - 1] = result;
			});
		}

		return resultList;
	}

	@Override
	public FollowUpPeriodDto calculateFollowUpUntilDate(CaseDataDto caseDto, boolean ignoreOverwrite) {
		return CaseLogic.calculateFollowUpUntilDate(
			caseDto,
			CaseLogic.getFollowUpStartDate(caseDto, sampleFacade.getByCaseUuids(Collections.singletonList(caseDto.getUuid()))),
			visitFacade.getVisitsByCase(caseDto.toReference()),
			diseaseConfigurationFacade.getCaseFollowUpDuration(caseDto.getDisease()),
			ignoreOverwrite);
	}

	public boolean isCaseEditAllowed(String caseUuid) {
		Case caze = caseService.getByUuid(caseUuid);

		return caseService.isCaseEditAllowed(caze);
	}

	@Override
	public void sendMessage(List<String> caseUuids, String subject, String messageContent, MessageType... messageTypes) {
		caseUuids.forEach(uuid -> {
			final Case aCase = caseService.getByUuid(uuid);
			final Person person = aCase.getPerson();

			try {
				messagingService.sendMessage(person, subject, messageContent, messageTypes);
			} catch (NotificationDeliveryFailedException e) {
				logger.error(
					String.format(
						"NotificationDeliveryFailedException when trying to notify person about: %s" + "Failed to send " + e.getMessageType()
							+ " to person with UUID %s.",
						subject,
						person.getUuid()));
			}
		});
	}

	@Override
	public long countCasesWithMissingContactInformation(List<String> caseUuids, MessageType messageType) {

		final AtomicLong totalCount = new AtomicLong();

		IterableHelper.executeBatched(
			caseUuids,
			ModelConstants.PARAMETER_LIMIT,
			e -> totalCount.addAndGet(countCasesWithMissingContactInfo(caseUuids, messageType)));

		return totalCount.get();
	}

	private Long countCasesWithMissingContactInfo(List<String> caseUuids, MessageType messageType) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Case> root = cq.from(Case.class);

		final CaseQueryContext caseQueryContext = new CaseQueryContext(cb, cq, root);

		cq.select(cb.count(root));
		Expression<?> contactInformation = messageType == MessageType.EMAIL
			? caseQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_EMAIL_SUBQUERY)
			: caseQueryContext.getSubqueryExpression(CaseQueryContext.PERSON_PHONE_SUBQUERY);
		cq.where(cb.and(root.get(Case.UUID).in(caseUuids), cb.isNull(contactInformation)));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public List<ManualMessageLogDto> getMessageLog(String personUuid, MessageType messageType) {
		return manualMessageLogService.getByPersonUuid(personUuid, messageType)
			.stream()
			.map(
				mml -> new ManualMessageLogDto(
					mml.getMessageType(),
					mml.getSentDate(),
					mml.getSendingUser().toReference(),
					mml.getRecipientPerson().toReference()))
			.collect(Collectors.toList());
	}

	@Override
	public String getFirstUuidNotShareableWithExternalReportingTools(List<String> caseUuids) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Case> caseRoot = cq.from(Case.class);
		Join<ShareInfoCase, SormasToSormasShareInfo> sormasToSormasJoin =
			caseRoot.join(Case.SHARE_INFO_CASES, JoinType.LEFT).join(ShareInfoCase.SHARE_INFO, JoinType.LEFT);

		cq.select(caseRoot.get(Case.UUID));
		cq.where(
			cb.or(
				cb.isTrue(caseRoot.get(Case.DONT_SHARE_WITH_REPORTING_TOOL)),
				cb.and(caseRoot.get(Case.UUID).in(caseUuids), cb.isTrue(sormasToSormasJoin.get(SormasToSormasShareInfo.OWNERSHIP_HANDED_OVER)))));
		cq.orderBy(cb.asc(caseRoot.get(AbstractDomainObject.CREATION_DATE)));

		try {
			return em.createQuery(cq).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Find duplicates based on case and person dto
	 * Conditions:
	 * * same externalId
	 * * same externalToken
	 * * same first name, last name, date of birth, sex (null is considered equal to any sex), disease, reportDate (ignore time), district
	 *
	 * The reportDateThreshold allows to return duplicates where
	 * -reportDateThreshold <= match.reportDate <= reportDateThreshold
	 *
	 * @param casePerson
	 *            - case and person
	 * @param reportDateThreshold
	 *            - the range bounds on match.reportDate
	 * @return list of duplicate cases
	 */
	@Override
	public List<CasePersonDto> getDuplicates(CasePersonDto casePerson, int reportDateThreshold) {

		CaseDataDto searchCaze = casePerson.getCaze();
		PersonDto searchPerson = casePerson.getPerson();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<Case> caseRoot = cq.from(Case.class);
		CaseJoins<Case> caseCaseJoins = new CaseJoins<>(caseRoot);
		Join<Case, Person> person = caseCaseJoins.getPerson();

		cq.multiselect(caseRoot.get(Case.UUID), person.get(Person.UUID));

		Path<String> externalId = caseRoot.get(Case.EXTERNAL_ID);
		Path<String> externalToken = caseRoot.get(Case.EXTERNAL_TOKEN);

		Predicate externalIdPredicate = null;
		if (searchCaze.getExternalID() != null) {
			externalIdPredicate = and(cb, cb.isNotNull(externalId), cb.equal(cb.lower(externalId), searchCaze.getExternalID().toLowerCase().trim()));
		}

		Predicate externalTokenPredicate = null;
		if (searchCaze.getExternalToken() != null) {
			externalTokenPredicate =
				and(cb, cb.isNotNull(externalToken), cb.equal(cb.trim(cb.lower(externalToken)), searchCaze.getExternalToken().toLowerCase().trim()));
		}
		// todo this should use PersonService.buildSimilarityCriteriaFilter
		Predicate combinedPredicate = null;
		if (searchPerson.getFirstName() != null
			&& searchPerson.getLastName() != null
			&& searchCaze.getReportDate() != null
			&& searchCaze.getDistrict() != null) {
			Predicate personPredicate = and(
				cb,
				cb.equal(cb.trim(cb.lower(person.get(Person.FIRST_NAME))), searchPerson.getFirstName().toLowerCase().trim()),
				cb.equal(cb.trim(cb.lower(person.get(Person.LAST_NAME))), searchPerson.getLastName().toLowerCase().trim()));

			if (searchPerson.getBirthdateDD() != null) {
				personPredicate = and(
					cb,
					personPredicate,
					or(cb, cb.isNull(person.get(Person.BIRTHDATE_DD)), cb.equal(person.get(Person.BIRTHDATE_DD), searchPerson.getBirthdateDD())));
			}

			if (searchPerson.getBirthdateMM() != null) {
				personPredicate = and(
					cb,
					personPredicate,
					or(cb, cb.isNull(person.get(Person.BIRTHDATE_DD)), cb.equal(person.get(Person.BIRTHDATE_MM), searchPerson.getBirthdateMM())));
			}

			if (searchPerson.getBirthdateYYYY() != null) {
				personPredicate = and(
					cb,
					personPredicate,
					or(
						cb,
						cb.isNull(person.get(Person.BIRTHDATE_YYYY)),
						cb.equal(person.get(Person.BIRTHDATE_YYYY), searchPerson.getBirthdateYYYY())));
			}

			if (searchPerson.getSex() != null) {
				personPredicate =
					and(cb, personPredicate, or(cb, cb.isNull(person.get(Person.SEX)), cb.equal(person.get(Person.SEX), searchPerson.getSex())));
			}

			Predicate reportDatePredicate;

			if (reportDateThreshold == 0) {
				// threshold is zero: we want to get exact matches
				reportDatePredicate = cb.equal(
					cb.function("date", Date.class, caseRoot.get(Case.REPORT_DATE)),
					cb.function("date", Date.class, cb.literal(searchCaze.getReportDate())));
			} else {
				// threshold is nonzero: apply time range of threshold to the reportDate
				Date reportDate = casePerson.getCaze().getReportDate();
				Date dateBefore = new DateTime(reportDate).minusDays(reportDateThreshold).toDate();
				Date dateAfter = new DateTime(reportDate).plusDays(reportDateThreshold).toDate();

				reportDatePredicate = cb.between(
					cb.function("date", Date.class, caseRoot.get(Case.REPORT_DATE)),
					cb.function("date", Date.class, cb.literal(dateBefore)),
					cb.function("date", Date.class, cb.literal(dateAfter)));
			}

			combinedPredicate = and(
				cb,
				personPredicate,
				cb.equal(caseRoot.get(Case.DISEASE), searchCaze.getDisease()),
				reportDatePredicate,
				cb.equal(caseCaseJoins.getDistrict().get(District.UUID), searchCaze.getDistrict().getUuid()));
		}

		Predicate filters = or(cb, externalIdPredicate, externalTokenPredicate, combinedPredicate);
		if (filters == null) {
			return Collections.emptyList();
		}

		cq.where(filters);

		List<Object[]> duplicateUuids = em.createQuery(cq).getResultList();

		return duplicateUuids.stream()
			.map(
				(casePersonUuids) -> new CasePersonDto(
					getCaseDataByUuid((String) casePersonUuids[0]),
					personFacade.getPersonByUuid((String) casePersonUuids[1])))
			.collect(Collectors.toList());
	}

	@Override
	public List<CasePersonDto> getDuplicates(CasePersonDto casePerson) {
		return getDuplicates(casePerson, 0);
	}

	@Override
	public List<CaseDataDto> getByPersonUuids(List<String> personUuids) {
		return caseService.getByPersonUuids(personUuids).stream().map(CaseFacadeEjb::toDto).collect(Collectors.toList());
	}

	@Override
	public List<CaseDataDto> getByExternalId(String externalId) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return caseService.getByExternalId(externalId).stream().map(c -> convertToDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public void updateExternalData(List<ExternalDataDto> externalData) throws ExternalDataUpdateException {
		caseService.updateExternalData(externalData);
	}

	@LocalBean
	@Stateless
	public static class CaseFacadeEjbLocal extends CaseFacadeEjb {

	}
}
