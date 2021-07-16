/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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
package de.symeda.sormas.api;

import javax.naming.ConfigurationException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import de.symeda.sormas.api.action.ActionFacade;
import de.symeda.sormas.api.bagexport.BAGExportFacade;
import de.symeda.sormas.api.campaign.CampaignFacade;
import de.symeda.sormas.api.campaign.data.CampaignFormDataFacade;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDefinitionFacade;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaFacade;
import de.symeda.sormas.api.campaign.statistics.CampaignStatisticsFacade;
import de.symeda.sormas.api.caze.CaseFacade;
import de.symeda.sormas.api.caze.CaseStatisticsFacade;
import de.symeda.sormas.api.caze.caseimport.CaseImportFacade;
import de.symeda.sormas.api.caze.classification.CaseClassificationFacade;
import de.symeda.sormas.api.caze.maternalhistory.MaternalHistoryFacade;
import de.symeda.sormas.api.caze.surveillancereport.SurveillanceReportFacade;
import de.symeda.sormas.api.clinicalcourse.ClinicalCourseFacade;
import de.symeda.sormas.api.clinicalcourse.ClinicalVisitFacade;
import de.symeda.sormas.api.contact.ContactFacade;
import de.symeda.sormas.api.customizableenum.CustomizableEnumFacade;
import de.symeda.sormas.api.dashboard.DashboardFacade;
import de.symeda.sormas.api.disease.DiseaseConfigurationFacade;
import de.symeda.sormas.api.docgeneneration.DocumentTemplateFacade;
import de.symeda.sormas.api.docgeneneration.EventDocumentFacade;
import de.symeda.sormas.api.docgeneneration.QuarantineOrderFacade;
import de.symeda.sormas.api.document.DocumentFacade;
import de.symeda.sormas.api.epidata.EpiDataFacade;
import de.symeda.sormas.api.event.EventFacade;
import de.symeda.sormas.api.event.EventGroupFacade;
import de.symeda.sormas.api.event.EventParticipantFacade;
import de.symeda.sormas.api.event.eventimport.EventImportFacade;
import de.symeda.sormas.api.externaljournal.ExternalJournalFacade;
import de.symeda.sormas.api.externalsurveillancetool.ExternalSurveillanceToolFacade;
import de.symeda.sormas.api.facility.FacilityFacade;
import de.symeda.sormas.api.feature.FeatureConfigurationFacade;
import de.symeda.sormas.api.geocoding.GeocodingFacade;
import de.symeda.sormas.api.hospitalization.HospitalizationFacade;
import de.symeda.sormas.api.i18n.I18nFacade;
import de.symeda.sormas.api.immunization.ImmunizationFacade;
import de.symeda.sormas.api.importexport.ExportFacade;
import de.symeda.sormas.api.importexport.ImportFacade;
import de.symeda.sormas.api.info.InfoFacade;
import de.symeda.sormas.api.infrastructure.InfrastructureFacade;
import de.symeda.sormas.api.infrastructure.PointOfEntryFacade;
import de.symeda.sormas.api.infrastructure.PopulationDataFacade;
import de.symeda.sormas.api.labmessage.ExternalLabResultsFacade;
import de.symeda.sormas.api.labmessage.LabMessageFacade;
import de.symeda.sormas.api.outbreak.OutbreakFacade;
import de.symeda.sormas.api.person.PersonFacade;
import de.symeda.sormas.api.region.AreaFacade;
import de.symeda.sormas.api.region.CommunityFacade;
import de.symeda.sormas.api.region.ContinentFacade;
import de.symeda.sormas.api.region.CountryFacade;
import de.symeda.sormas.api.region.DistrictFacade;
import de.symeda.sormas.api.region.GeoShapeProvider;
import de.symeda.sormas.api.region.RegionFacade;
import de.symeda.sormas.api.region.SubcontinentFacade;
import de.symeda.sormas.api.report.AggregateReportFacade;
import de.symeda.sormas.api.report.WeeklyReportFacade;
import de.symeda.sormas.api.sample.AdditionalTestFacade;
import de.symeda.sormas.api.sample.PathogenTestFacade;
import de.symeda.sormas.api.sample.SampleFacade;
import de.symeda.sormas.api.share.ExternalShareInfoFacade;
import de.symeda.sormas.api.sormastosormas.SormasToSormasFacade;
import de.symeda.sormas.api.sormastosormas.SormasToSormasLabMessageFacade;
import de.symeda.sormas.api.sormastosormas.caze.SormasToSormasCaseFacade;
import de.symeda.sormas.api.sormastosormas.contact.SormasToSormasContactFacade;
import de.symeda.sormas.api.sormastosormas.event.SormasToSormasEventFacade;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasShareRequestFacade;
import de.symeda.sormas.api.symptoms.SymptomsFacade;
import de.symeda.sormas.api.systemevents.SystemEventFacade;
import de.symeda.sormas.api.task.TaskFacade;
import de.symeda.sormas.api.therapy.PrescriptionFacade;
import de.symeda.sormas.api.therapy.TherapyFacade;
import de.symeda.sormas.api.therapy.TreatmentFacade;
import de.symeda.sormas.api.travelentry.TravelEntryFacade;
import de.symeda.sormas.api.travelentry.travelentryimport.TravelEntryImportFacade;
import de.symeda.sormas.api.user.UserFacade;
import de.symeda.sormas.api.user.UserRightsFacade;
import de.symeda.sormas.api.user.UserRoleConfigFacade;
import de.symeda.sormas.api.vaccination.VaccinationEntityFacade;
import de.symeda.sormas.api.visit.VisitFacade;
import de.symeda.sormas.api.visualization.VisualizationFacade;

public class FacadeProvider {

	private static final String JNDI_PREFIX = "java:global/sormas-ear/sormas-backend/";

	private final InitialContext ic;

	private static FacadeProvider instance;

	protected FacadeProvider() {

		try {
			ic = new InitialContext();
		} catch (NamingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static FacadeProvider get() {
		if (instance == null) {
			instance = new FacadeProvider();
		}
		return instance;
	}

	public static CaseFacade getCaseFacade() {
		return get().lookupEjbRemote(CaseFacade.class);
	}

	public static ImmunizationFacade getImmunizationFacade() {
		return get().lookupEjbRemote(ImmunizationFacade.class);
	}

	public static VaccinationEntityFacade getVaccinationEntityFacade() {
		return get().lookupEjbRemote(VaccinationEntityFacade.class);
	}

	public static TravelEntryFacade getTravelEntryFacade() {
		return get().lookupEjbRemote(TravelEntryFacade.class);
	}

	public static CaseStatisticsFacade getCaseStatisticsFacade() {
		return get().lookupEjbRemote(CaseStatisticsFacade.class);
	}

	public static CaseImportFacade getCaseImportFacade() {
		return get().lookupEjbRemote(CaseImportFacade.class);
	}

	public static TravelEntryImportFacade getTravelEntryImportFacade() {
		return get().lookupEjbRemote(TravelEntryImportFacade.class);
	}

	public static ContactFacade getContactFacade() {
		return get().lookupEjbRemote(ContactFacade.class);
	}

	public static EventFacade getEventFacade() {
		return get().lookupEjbRemote(EventFacade.class);
	}

	public static EventGroupFacade getEventGroupFacade() {
		return get().lookupEjbRemote(EventGroupFacade.class);
	}

	public static EventImportFacade getEventImportFacade() {
		return get().lookupEjbRemote(EventImportFacade.class);
	}

	public static EventParticipantFacade getEventParticipantFacade() {
		return get().lookupEjbRemote(EventParticipantFacade.class);
	}

	public static VisitFacade getVisitFacade() {
		return get().lookupEjbRemote(VisitFacade.class);
	}

	public static PersonFacade getPersonFacade() {
		return get().lookupEjbRemote(PersonFacade.class);
	}

	public static TaskFacade getTaskFacade() {
		return get().lookupEjbRemote(TaskFacade.class);
	}

	public static ActionFacade getActionFacade() {
		return get().lookupEjbRemote(ActionFacade.class);
	}

	public static SampleFacade getSampleFacade() {
		return get().lookupEjbRemote(SampleFacade.class);
	}

	public static PathogenTestFacade getPathogenTestFacade() {
		return get().lookupEjbRemote(PathogenTestFacade.class);
	}

	public static AdditionalTestFacade getAdditionalTestFacade() {
		return get().lookupEjbRemote(AdditionalTestFacade.class);
	}

	public static SymptomsFacade getSymptomsFacade() {
		return get().lookupEjbRemote(SymptomsFacade.class);
	}

	public static FacilityFacade getFacilityFacade() {
		return get().lookupEjbRemote(FacilityFacade.class);
	}

	public static ContinentFacade getContinentFacade() {
		return get().lookupEjbRemote(ContinentFacade.class);
	}

	public static SubcontinentFacade getSubcontinentFacade() {
		return get().lookupEjbRemote(SubcontinentFacade.class);
	}

	public static CountryFacade getCountryFacade() {
		return get().lookupEjbRemote(CountryFacade.class);
	}

	public static RegionFacade getRegionFacade() {
		return get().lookupEjbRemote(RegionFacade.class);
	}

	public static DistrictFacade getDistrictFacade() {
		return get().lookupEjbRemote(DistrictFacade.class);
	}

	public static CommunityFacade getCommunityFacade() {
		return get().lookupEjbRemote(CommunityFacade.class);
	}

	public static UserFacade getUserFacade() {
		return get().lookupEjbRemote(UserFacade.class);
	}

	public static UserRoleConfigFacade getUserRoleConfigFacade() {
		return get().lookupEjbRemote(UserRoleConfigFacade.class);
	}

	public static HospitalizationFacade getHospitalizationFacade() {
		return get().lookupEjbRemote(HospitalizationFacade.class);
	}

	public static EpiDataFacade getEpiDataFacade() {
		return get().lookupEjbRemote(EpiDataFacade.class);
	}

	public static WeeklyReportFacade getWeeklyReportFacade() {
		return get().lookupEjbRemote(WeeklyReportFacade.class);
	}

	public static GeoShapeProvider getGeoShapeProvider() {
		return get().lookupEjbRemote(GeoShapeProvider.class);
	}

	public static OutbreakFacade getOutbreakFacade() {
		return get().lookupEjbRemote(OutbreakFacade.class);
	}

	public static ConfigFacade getConfigFacade() {
		return get().lookupEjbRemote(ConfigFacade.class);
	}

	public static ExportFacade getExportFacade() {
		return get().lookupEjbRemote(ExportFacade.class);
	}

	public static ImportFacade getImportFacade() {
		return get().lookupEjbRemote(ImportFacade.class);
	}

	public static CaseClassificationFacade getCaseClassificationFacade() {
		return get().lookupEjbRemote(CaseClassificationFacade.class);
	}

	public static TherapyFacade getTherapyFacade() {
		return get().lookupEjbRemote(TherapyFacade.class);
	}

	public static PrescriptionFacade getPrescriptionFacade() {
		return get().lookupEjbRemote(PrescriptionFacade.class);
	}

	public static TreatmentFacade getTreatmentFacade() {
		return get().lookupEjbRemote(TreatmentFacade.class);
	}

	public static ClinicalCourseFacade getClinicalCourseFacade() {
		return get().lookupEjbRemote(ClinicalCourseFacade.class);
	}

	public static ClinicalVisitFacade getClinicalVisitFacade() {
		return get().lookupEjbRemote(ClinicalVisitFacade.class);
	}

	public static DashboardFacade getDashboardFacade() {
		return get().lookupEjbRemote(DashboardFacade.class);
	}

	public static DiseaseConfigurationFacade getDiseaseConfigurationFacade() {
		return get().lookupEjbRemote(DiseaseConfigurationFacade.class);
	}

	public static MaternalHistoryFacade getMaternalHistoryFacade() {
		return get().lookupEjbRemote(MaternalHistoryFacade.class);
	}

	public static PointOfEntryFacade getPointOfEntryFacade() {
		return get().lookupEjbRemote(PointOfEntryFacade.class);
	}

	public static PopulationDataFacade getPopulationDataFacade() {
		return get().lookupEjbRemote(PopulationDataFacade.class);
	}

	public static InfrastructureFacade getInfrastructureFacade() {
		return get().lookupEjbRemote(InfrastructureFacade.class);
	}

	public static FeatureConfigurationFacade getFeatureConfigurationFacade() {
		return get().lookupEjbRemote(FeatureConfigurationFacade.class);
	}

	public static AggregateReportFacade getAggregateReportFacade() {
		return get().lookupEjbRemote(AggregateReportFacade.class);
	}

	public static VisualizationFacade getVisualizationFacade() {
		return get().lookupEjbRemote(VisualizationFacade.class);
	}

	public static GeocodingFacade getGeocodingFacade() {
		return get().lookupEjbRemote(GeocodingFacade.class);
	}

	public static CampaignFacade getCampaignFacade() {
		return get().lookupEjbRemote(CampaignFacade.class);
	}

	public static CampaignDiagramDefinitionFacade getCampaignDiagramDefinitionFacade() {
		return get().lookupEjbRemote(CampaignDiagramDefinitionFacade.class);
	}

	public static CampaignFormMetaFacade getCampaignFormMetaFacade() {
		return get().lookupEjbRemote(CampaignFormMetaFacade.class);
	}

	public static CampaignFormDataFacade getCampaignFormDataFacade() {
		return get().lookupEjbRemote(CampaignFormDataFacade.class);
	}

	public static CampaignStatisticsFacade getCampaignStatisticsFacade() {
		return get().lookupEjbRemote(CampaignStatisticsFacade.class);
	}

	public static SormasToSormasFacade getSormasToSormasFacade() {
		return get().lookupEjbRemote(SormasToSormasFacade.class);
	}

	public static SormasToSormasCaseFacade getSormasToSormasCaseFacade() {
		return get().lookupEjbRemote(SormasToSormasCaseFacade.class);
	}

	public static SormasToSormasContactFacade getSormasToSormasContactFacade() {
		return get().lookupEjbRemote(SormasToSormasContactFacade.class);
	}

	public static SormasToSormasEventFacade getSormasToSormasEventFacade() {
		return get().lookupEjbRemote(SormasToSormasEventFacade.class);
	}

	public static SormasToSormasLabMessageFacade getSormasToSormasLabMessageFacade() {
		return get().lookupEjbRemote(SormasToSormasLabMessageFacade.class);
	}

	public static BAGExportFacade getBAGExportFacade() {
		return get().lookupEjbRemote(BAGExportFacade.class);
	}

	public static ExternalSurveillanceToolFacade getExternalSurveillanceToolFacade() {
		return get().lookupEjbRemote(ExternalSurveillanceToolFacade.class);
	}

	public static AreaFacade getAreaFacade() {
		return get().lookupEjbRemote(AreaFacade.class);
	}

	public static QuarantineOrderFacade getQuarantineOrderFacade() {
		return get().lookupEjbRemote(QuarantineOrderFacade.class);
	}

	public static EventDocumentFacade getEventDocumentFacade() {
		return get().lookupEjbRemote(EventDocumentFacade.class);
	}

	public static DocumentTemplateFacade getDocumentTemplateFacade() {
		return get().lookupEjbRemote(DocumentTemplateFacade.class);
	}

	public static ExternalJournalFacade getExternalJournalFacade() {
		return get().lookupEjbRemote(ExternalJournalFacade.class);
	}

	public static DocumentFacade getDocumentFacade() {
		return get().lookupEjbRemote(DocumentFacade.class);
	}

	public static SystemEventFacade getSystemEventFacade() {

		return get().lookupEjbRemote(SystemEventFacade.class);
	}

	public static LabMessageFacade getLabMessageFacade() {
		return get().lookupEjbRemote(LabMessageFacade.class);
	}

	public static ExternalLabResultsFacade getExternalLabResultsFacade() throws NamingException {

		String jndiName = FacadeProvider.getConfigFacade().getDemisJndiName();
		if (jndiName == null) {
			throw new ConfigurationException("No LabResultAdapter JNDI name is configured in the sormas.properties");
		} else {
			return (ExternalLabResultsFacade) get().ic.lookup(jndiName);
		}
	}

	public static SurveillanceReportFacade getSurveillanceReportFacade() {
		return get().lookupEjbRemote(SurveillanceReportFacade.class);
	}

	public static ExternalShareInfoFacade getExternalShareInfoFacade() {
		return get().lookupEjbRemote(ExternalShareInfoFacade.class);
	}

	public static I18nFacade getI18nFacade() {
		return get().lookupEjbRemote(I18nFacade.class);
	}

	public static UserRightsFacade getUserRightsFacade() {
		return get().lookupEjbRemote(UserRightsFacade.class);
	}

	public static SormasToSormasShareRequestFacade getSormasToSormasShareRequestFacade() {
		return get().lookupEjbRemote(SormasToSormasShareRequestFacade.class);
	}

	public static CustomizableEnumFacade getCustomizableEnumFacade() {
		return get().lookupEjbRemote(CustomizableEnumFacade.class);
	}

	public static InfoFacade getInfoFacae() {
		return get().lookupEjbRemote(InfoFacade.class);
	}

	@SuppressWarnings("unchecked")
	public <P> P lookupEjbRemote(Class<P> clazz) {
		try {
			return (P) get().ic.lookup(buildJndiLookupName(clazz));
		} catch (NamingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String buildJndiLookupName(Class<?> clazz) {
		return JNDI_PREFIX + clazz.getSimpleName();
	}
}
