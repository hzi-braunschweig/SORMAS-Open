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
package de.symeda.sormas.ui;

import de.symeda.sormas.ui.action.ActionController;
import de.symeda.sormas.ui.campaign.CampaignController;
import de.symeda.sormas.ui.caze.CaseController;
import de.symeda.sormas.ui.caze.surveillancereport.SurveillanceReportController;
import de.symeda.sormas.ui.clinicalcourse.ClinicalCourseController;
import de.symeda.sormas.ui.configuration.infrastructure.InfrastructureController;
import de.symeda.sormas.ui.configuration.outbreak.OutbreakController;
import de.symeda.sormas.ui.contact.ContactController;
import de.symeda.sormas.ui.customexport.CustomExportController;
import de.symeda.sormas.ui.dashboard.DashboardController;
import de.symeda.sormas.ui.docgeneration.DocGenerationController;
import de.symeda.sormas.ui.events.EventController;
import de.symeda.sormas.ui.events.EventGroupController;
import de.symeda.sormas.ui.events.EventParticipantsController;
import de.symeda.sormas.ui.immunization.ImmunizationController;
import de.symeda.sormas.ui.labmessage.LabMessageController;
import de.symeda.sormas.ui.person.PersonController;
import de.symeda.sormas.ui.reports.aggregate.AggregateReportController;
import de.symeda.sormas.ui.samples.AdditionalTestController;
import de.symeda.sormas.ui.samples.PathogenTestController;
import de.symeda.sormas.ui.samples.SampleController;
import de.symeda.sormas.ui.sormastosormas.SormasToSormasController;
import de.symeda.sormas.ui.statistics.StatisticsController;
import de.symeda.sormas.ui.task.TaskController;
import de.symeda.sormas.ui.therapy.TherapyController;
import de.symeda.sormas.ui.user.UserController;
import de.symeda.sormas.ui.utils.BaseControllerProvider;
import de.symeda.sormas.ui.visit.VisitController;

public class ControllerProvider extends BaseControllerProvider {

	private final CaseController caseController;
	private final ContactController contactController;
	private final EventController eventController;
	private final EventParticipantsController eventParticipantController;
	private final EventGroupController eventGroupController;
	private final InfrastructureController infrastructureController;
	private final VisitController visitController;
	private final PersonController personController;
	private final UserController userController;
	private final TaskController taskController;
	private final ActionController actionController;
	private final SampleController sampleController;
	private final ImmunizationController immunizationController;
	private final PathogenTestController pathogenTestController;
	private final AdditionalTestController additionalTestController;
	private final OutbreakController outbreakController;
	private final StatisticsController statisticsController;
	private final DashboardController dashboardController;
	private final TherapyController therapyController;
	private final ClinicalCourseController clinicalCourseController;
	private final AggregateReportController aggregateReportController;
	private final CampaignController campaignController;
	private final SormasToSormasController sormasToSormasController;
	private final CustomExportController customExportController;
	private final LabMessageController labMessageController;
	private final SurveillanceReportController surveillanceReportController;
	private final DocGenerationController docGenerationController;

	public ControllerProvider() {
		super();

		caseController = new CaseController();
		contactController = new ContactController();
		eventController = new EventController();
		eventParticipantController = new EventParticipantsController();
		eventGroupController = new EventGroupController();
		infrastructureController = new InfrastructureController();
		visitController = new VisitController();
		personController = new PersonController();
		userController = new UserController();
		taskController = new TaskController();
		actionController = new ActionController();
		sampleController = new SampleController();
		immunizationController = new ImmunizationController();
		pathogenTestController = new PathogenTestController();
		additionalTestController = new AdditionalTestController();
		outbreakController = new OutbreakController();
		statisticsController = new StatisticsController();
		dashboardController = new DashboardController();
		therapyController = new TherapyController();
		clinicalCourseController = new ClinicalCourseController();
		aggregateReportController = new AggregateReportController();
		campaignController = new CampaignController();
		sormasToSormasController = new SormasToSormasController();
		customExportController = new CustomExportController();
		labMessageController = new LabMessageController();
		surveillanceReportController = new SurveillanceReportController();
		docGenerationController = new DocGenerationController();
	}

	protected static ControllerProvider get() {
		return (ControllerProvider) BaseControllerProvider.get();
	}

	public static CaseController getCaseController() {
		return get().caseController;
	}

	public static ContactController getContactController() {
		return get().contactController;
	}

	public static EventController getEventController() {
		return get().eventController;
	}

	public static EventParticipantsController getEventParticipantController() {
		return get().eventParticipantController;
	}

	public static EventGroupController getEventGroupController() {
		return get().eventGroupController;
	}

	public static InfrastructureController getInfrastructureController() {
		return get().infrastructureController;
	}

	public static VisitController getVisitController() {
		return get().visitController;
	}

	public static PersonController getPersonController() {
		return get().personController;
	}

	public static UserController getUserController() {
		return get().userController;
	}

	public static TaskController getTaskController() {
		return get().taskController;
	}

	public static ActionController getActionController() {
		return get().actionController;
	}

	public static SampleController getSampleController() {
		return get().sampleController;
	}

	public static ImmunizationController getImmunizationController() {
		return get().immunizationController;
	}

	public static AdditionalTestController getAdditionalTestController() {
		return get().additionalTestController;
	}

	public static PathogenTestController getPathogenTestController() {
		return get().pathogenTestController;
	}

	public static OutbreakController getOutbreakController() {
		return get().outbreakController;
	}

	public static StatisticsController getStatisticsController() {
		return get().statisticsController;
	}

	public static DashboardController getDashboardController() {
		return get().dashboardController;
	}

	public static TherapyController getTherapyController() {
		return get().therapyController;
	}

	public static ClinicalCourseController getClinicalCourseController() {
		return get().clinicalCourseController;
	}

	public static AggregateReportController getAggregateReportController() {
		return get().aggregateReportController;
	}

	public static CampaignController getCampaignController() {
		return get().campaignController;
	}

	public static SormasToSormasController getSormasToSormasController() {
		return get().sormasToSormasController;
	}

	public static CustomExportController getCustomExportController() {
		return get().customExportController;
	}

	public static LabMessageController getLabMessageController() {
		return get().labMessageController;
	}

	public static SurveillanceReportController getSurveillanceReportController() {
		return get().surveillanceReportController;
	}

	public static DocGenerationController getDocGenerationController() {
		return get().docGenerationController;
	}
}
