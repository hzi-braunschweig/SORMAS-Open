
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

package de.symeda.sormas.api.action;

import static de.symeda.sormas.api.utils.HtmlHelper.cleanHtml;

import java.util.Date;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.event.EventReferenceDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.HtmlHelper;
import javax.validation.constraints.NotNull;

public class ActionDto extends EntityDto {

	private static final long serialVersionUID = 2439546041916003652L;

	public static final String I18N_PREFIX = "Action";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String REPLY = "reply";
	public static final String PRIORITY = "priority";
	public static final String DATE = "date";
	public static final String EVENT = "event";
	public static final String ACTION_CONTEXT = "actionContext";
	public static final String ACTION_STATUS = "actionStatus";
	public static final String ACTION_MEASURE = "actionMeasure";

	@NotNull
	private ActionContext actionContext;
	private EventReferenceDto event;

	private ActionMeasure actionMeasure;
	private ActionPriority priority;
	@NotNull
	private Date date;
	private ActionStatus actionStatus;
	private Date statusChangeDate;

	private UserReferenceDto creatorUser;
	private String title;
	private String description;
	private String reply;
	private UserReferenceDto lastModifiedBy;

	public static ActionDto build(ActionContext context, ReferenceDto entityRef) {
		return build(context, null, entityRef);
	}

	public static ActionDto build(ActionContext context, ActionMeasure actionMeasure, ReferenceDto entityRef) {

		ActionDto action = new ActionDto();
		action.setUuid(DataHelper.createUuid());
		action.setDate(ActionHelper.getDefaultDate());
		action.setActionStatus(ActionStatus.PENDING);
		action.setPriority(ActionPriority.NORMAL);
		action.setActionMeasure(actionMeasure);
		action.setActionContext(context);
		switch (context) {
		case EVENT:
			action.setEvent((EventReferenceDto) entityRef);
			break;
		}
		return action;
	}

	public ActionContext getActionContext() {
		return actionContext;
	}

	public void setActionContext(ActionContext actionContext) {
		this.actionContext = actionContext;
	}

	public EventReferenceDto getEvent() {
		return event;
	}

	public void setEvent(EventReferenceDto event) {
		this.event = event;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public ActionStatus getActionStatus() {
		return actionStatus;
	}

	public void setActionStatus(ActionStatus actionStatus) {
		this.actionStatus = actionStatus;
	}

	public Date getStatusChangeDate() {
		return statusChangeDate;
	}

	public void setStatusChangeDate(Date statusChangeDate) {
		this.statusChangeDate = statusChangeDate;
	}

	public UserReferenceDto getCreatorUser() {
		return creatorUser;
	}

	public void setCreatorUser(UserReferenceDto creatorUser) {
		this.creatorUser = creatorUser;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return cleanHtml(description, HtmlHelper.EVENTACTION_WHITELIST);
	}

	public void setDescription(String description) {
		this.description = cleanHtml(description, HtmlHelper.EVENTACTION_WHITELIST);
	}

	public String getReply() {
		return cleanHtml(reply, HtmlHelper.EVENTACTION_WHITELIST);
	}

	public void setReply(String reply) {
		this.reply = cleanHtml(reply, HtmlHelper.EVENTACTION_WHITELIST);
	}

	public UserReferenceDto getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(UserReferenceDto lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public ActionPriority getPriority() {
		return priority;
	}

	public void setPriority(ActionPriority priority) {
		this.priority = priority;
	}

	public ActionMeasure getActionMeasure() {
		return actionMeasure;
	}

	public void setActionMeasure(ActionMeasure actionMeasure) {
		this.actionMeasure = actionMeasure;
	}

	public ActionReferenceDto toReference() {
		return new ActionReferenceDto(getUuid(), getTitle());
	}

	public ReferenceDto getContextReference() {

		switch (actionContext) {
		case EVENT:
			return getEvent();
		default:
			throw new IndexOutOfBoundsException(actionContext.toString());
		}
	}
}
