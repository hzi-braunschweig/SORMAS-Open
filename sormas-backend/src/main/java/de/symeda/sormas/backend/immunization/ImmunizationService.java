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

package de.symeda.sormas.backend.immunization;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.api.immunization.ImmunizationCriteria;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.backend.common.AbstractCoreAdoService;
import de.symeda.sormas.backend.common.ChangeDateFilterBuilder;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.JurisdictionHelper;
import de.symeda.sormas.backend.vaccination.VaccinationEntity;

@Stateless
@LocalBean
public class ImmunizationService extends AbstractCoreAdoService<Immunization> {

	@EJB
	private UserService userService;

	public ImmunizationService() {
		super(Immunization.class);
	}

	public boolean inJurisdictionOrOwned(Immunization immunization) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> cq = cb.createQuery(Boolean.class);
		Root<Immunization> root = cq.from(Immunization.class);
		cq.multiselect(JurisdictionHelper.booleanSelector(cb, inJurisdictionOrOwned(new ImmunizationQueryContext(cb, cq, root))));
		cq.where(cb.equal(root.get(Immunization.UUID), immunization.getUuid()));
		return em.createQuery(cq).getSingleResult();
	}

	public Predicate inJurisdictionOrOwned(ImmunizationQueryContext qc) {
		final User currentUser = userService.getCurrentUser();
		return ImmunizationJurisdictionPredicateValidator.of(qc, currentUser).inJurisdictionOrOwned();
	}

	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, Immunization> immunizationPath) {
		return inJurisdictionOrOwned(new ImmunizationQueryContext(cb, cq, immunizationPath));
	}

	public Predicate createDefaultFilter(CriteriaBuilder cb, From<?, Immunization> root) {
		return cb.isFalse(root.get(Immunization.DELETED));
	}

	public Predicate buildCriteriaFilter(ImmunizationCriteria criteria, CriteriaBuilder cb, Root<Immunization> from) {
		return cb.conjunction();
	}

	@Override
	public Predicate createChangeDateFilter(CriteriaBuilder cb, From<?, Immunization> immunization, Timestamp date) {
		Join<Immunization, VaccinationEntity> vaccinations = immunization.join(Immunization.VACCINATIONS, JoinType.LEFT);

		return new ChangeDateFilterBuilder(cb, date).add(immunization).add(vaccinations).build();
	}

	public List<Immunization> getAllActiveAfter(Date date) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Immunization> cq = cb.createQuery(getElementClass());
		Root<Immunization> from = cq.from(getElementClass());

		Predicate filter = createDefaultFilter(cb, from);

		if (getCurrentUser() != null) {
			Predicate userFilter = createUserFilter(cb, cq, from);
			if (userFilter != null) {
				filter = cb.and(filter, userFilter);
			}
		}

		if (date != null) {
			Predicate dateFilter = createChangeDateFilter(cb, from, DateHelper.toTimestampUpper(date));
			if (dateFilter != null) {
				filter = cb.and(filter, dateFilter);
			}
		}

		cq.where(filter);
		cq.orderBy(cb.desc(from.get(Immunization.CHANGE_DATE)));
		cq.distinct(true);

		return em.createQuery(cq).getResultList();
	}

	public List<String> getArchivedUuidsSince(Date since) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Immunization> from = cq.from(getElementClass());

		Predicate filter = createUserFilter(cb, cq, from);
		if (since != null) {
			Predicate dateFilter = cb.greaterThanOrEqualTo(from.get(Immunization.CHANGE_DATE), since);
			if (filter != null) {
				filter = cb.and(filter, dateFilter);
			} else {
				filter = dateFilter;
			}
		}

		Predicate archivedFilter = cb.equal(from.get(Immunization.ARCHIVED), true);
		if (filter != null) {
			filter = cb.and(filter, archivedFilter);
		} else {
			filter = archivedFilter;
		}

		cq.where(filter);
		cq.select(from.get(Immunization.UUID));

		return em.createQuery(cq).getResultList();
	}

	public List<String> getDeletedUuidsSince(Date since) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Immunization> from = cq.from(getElementClass());

		Predicate filter = createUserFilter(cb, cq, from);
		if (since != null) {
			Predicate dateFilter = cb.greaterThanOrEqualTo(from.get(Immunization.CHANGE_DATE), since);
			if (filter != null) {
				filter = cb.and(filter, dateFilter);
			} else {
				filter = dateFilter;
			}
		}

		Predicate deletedFilter = cb.equal(from.get(Immunization.DELETED), true);
		if (filter != null) {
			filter = cb.and(filter, deletedFilter);
		} else {
			filter = deletedFilter;
		}

		cq.where(filter);
		cq.select(from.get(Immunization.UUID));

		return em.createQuery(cq).getResultList();
	}
}
