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
package de.symeda.sormas.ui.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.vaadin.v7.ui.renderers.HtmlRenderer;

import elemental.json.JsonValue;

@SuppressWarnings("serial")
public class V7ShortStringRenderer extends HtmlRenderer {

	private final int length;

	public V7ShortStringRenderer(int length) {
		this.length = length;
	}

	@Override
	public JsonValue encode(String value) {

		if (value != null && !value.isEmpty()) {
			if (value.length() > length) {
				value = value.substring(0, length);
				value += "...";
			}
			return super.encode(Jsoup.clean(value, Whitelist.none()));
		} else {
			return null;
		}
	}
}
