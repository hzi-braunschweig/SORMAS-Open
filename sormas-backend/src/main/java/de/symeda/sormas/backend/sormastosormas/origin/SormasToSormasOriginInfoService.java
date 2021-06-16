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

package de.symeda.sormas.backend.sormastosormas.origin;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.common.AdoServiceWithUserFilter;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.person.Person;

@Stateless
@LocalBean
public class SormasToSormasOriginInfoService extends AdoServiceWithUserFilter<SormasToSormasOriginInfo> {

	public SormasToSormasOriginInfoService() {
		super(SormasToSormasOriginInfo.class);
	}

	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, SormasToSormasOriginInfo> from) {
		// no user filter needed right now
		return null;
	}

	public SormasToSormasOriginInfo getByPerson(String personUuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SormasToSormasOriginInfo> cq = cb.createQuery(SormasToSormasOriginInfo.class);
		Root<SormasToSormasOriginInfo> from = cq.from(SormasToSormasOriginInfo.class);

		cq.where(
			cb.or(
				cb.equal(from.join(SormasToSormasOriginInfo.CASES, JoinType.LEFT).join(Case.PERSON, JoinType.LEFT).get(Person.UUID), personUuid),
				cb.equal(
					from.join(SormasToSormasOriginInfo.CONTACTS, JoinType.LEFT).join(Contact.PERSON, JoinType.LEFT).get(Person.UUID),
					personUuid),
				cb.equal(
					from.join(SormasToSormasOriginInfo.EVENT_PARTICIPANTS, JoinType.LEFT)
						.join(EventParticipant.PERSON, JoinType.LEFT)
						.get(Person.UUID),
					personUuid)));

		TypedQuery<SormasToSormasOriginInfo> q = em.createQuery(cq);

		return q.getResultList().stream().findFirst().orElse(null);
	}
}
