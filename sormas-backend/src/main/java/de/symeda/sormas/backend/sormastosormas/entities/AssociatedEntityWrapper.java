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

package de.symeda.sormas.backend.sormastosormas.entities;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoContact;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoEventParticipant;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoSample;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;

public class AssociatedEntityWrapper<T extends SormasToSormasEntity> {

	private final T entity;
	private final BiConsumer<SormasToSormasShareInfo, T> shareInfoAssociatedObjectFn;
	private final BiFunction<SormasToSormasShareInfo, T, Boolean> associatedObjectFindFn;

	public static List<AssociatedEntityWrapper<?>> forContacts(List<Contact> contacts) {
		return contacts.stream()
			.map(
				contact -> new AssociatedEntityWrapper<>(
					contact,
					(s, c) -> s.getContacts().add(new ShareInfoContact(s, c)),
					(shareInfo, contactToFind) -> shareInfo.getContacts()
						.stream()
						.anyMatch(c -> contactToFind.getUuid().equals(c.getContact().getUuid()))))
			.collect(Collectors.toList());
	}

	public static List<AssociatedEntityWrapper<?>> forEventParticipants(List<EventParticipant> eventParticipants) {
		return eventParticipants.stream()
			.map(
				eventParticipant -> new AssociatedEntityWrapper<>(
					eventParticipant,
					(s, ep) -> s.getEventParticipants().add(new ShareInfoEventParticipant(s, ep)),
					(shareInfo, epToFind) -> shareInfo.getEventParticipants()
						.stream()
						.anyMatch(ep -> epToFind.getUuid().equals(ep.getEventParticipant().getUuid()))))
			.collect(Collectors.toList());
	}

	public static List<AssociatedEntityWrapper<?>> forSamples(List<Sample> eventParticipants) {
		return eventParticipants.stream()
			.map(
				sample -> new AssociatedEntityWrapper<>(
					sample,
					(shareInfo, s) -> shareInfo.getSamples().add(new ShareInfoSample(shareInfo, sample)),
					(shareInfo, sampleToFind) -> shareInfo.getSamples()
						.stream()
						.anyMatch(c -> sampleToFind.getUuid().equals(c.getSample().getUuid()))))
			.collect(Collectors.toList());
	}

	private AssociatedEntityWrapper(
		T entity,
		BiConsumer<SormasToSormasShareInfo, T> shareInfoAssociatedObjectFn,
		BiFunction<SormasToSormasShareInfo, T, Boolean> associatedObjectFindFn) {
		this.entity = entity;
		this.shareInfoAssociatedObjectFn = shareInfoAssociatedObjectFn;
		this.associatedObjectFindFn = associatedObjectFindFn;
	}

	public T getEntity() {
		return entity;
	}

	public void addEntityToShareInfo(SormasToSormasShareInfo shareInfo) {
		shareInfoAssociatedObjectFn.accept(shareInfo, entity);
	}

	public boolean isAddedToShareInfo(SormasToSormasShareInfo shareInfo) {
		return associatedObjectFindFn.apply(shareInfo, entity);
	}
}
