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
package de.symeda.sormas.ui.location;

import static de.symeda.sormas.ui.utils.LayoutUtil.divs;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidColumnLoc;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidRow;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidRowLocs;
import static de.symeda.sormas.ui.utils.LayoutUtil.loc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.AbstractField;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.TextField;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.facility.FacilityTypeGroup;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.location.LocationDto;
import de.symeda.sormas.api.person.PersonAddressType;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.ContinentReferenceDto;
import de.symeda.sormas.api.region.CountryReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.GeoLatLon;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.region.SubcontinentReferenceDto;
import de.symeda.sormas.api.utils.fieldaccess.UiFieldAccessCheckers;
import de.symeda.sormas.api.utils.fieldvisibility.FieldVisibilityCheckers;
import de.symeda.sormas.ui.map.LeafletMap;
import de.symeda.sormas.ui.map.LeafletMarker;
import de.symeda.sormas.ui.map.MarkerIcon;
import de.symeda.sormas.ui.utils.AbstractEditForm;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.FieldHelper;
import de.symeda.sormas.ui.utils.InfrastructureFieldsHelper;
import de.symeda.sormas.ui.utils.StringToAngularLocationConverter;
import de.symeda.sormas.ui.utils.VaadinUiUtil;

public class LocationEditForm extends AbstractEditForm<LocationDto> {

	private static final long serialVersionUID = 1L;

	private static final String FACILITY_TYPE_GROUP_LOC = "typeGroupLoc";
	private static final String GEO_BUTTONS_LOC = "geoButtons";

	private static final String HTML_LAYOUT =
		//XXX #1620 are the divs needed?
		divs(
			fluidRowLocs(LocationDto.ADDRESS_TYPE, LocationDto.ADDRESS_TYPE_DETAILS, ""),
			fluidRowLocs(LocationDto.CONTINENT, LocationDto.SUB_CONTINENT, ""),
			fluidRowLocs(LocationDto.COUNTRY, "", ""),
			fluidRowLocs(LocationDto.REGION, LocationDto.DISTRICT, LocationDto.COMMUNITY),
			fluidRowLocs(FACILITY_TYPE_GROUP_LOC, LocationDto.FACILITY_TYPE),
			fluidRowLocs(LocationDto.FACILITY, LocationDto.FACILITY_DETAILS),
			fluidRowLocs(LocationDto.STREET, LocationDto.HOUSE_NUMBER, LocationDto.ADDITIONAL_INFORMATION),
			fluidRowLocs(LocationDto.POSTAL_CODE, LocationDto.CITY, LocationDto.AREA_TYPE),
			fluidRow(
				loc(LocationDto.DETAILS),
				fluidRow(
					fluidColumnLoc(2, 0, GEO_BUTTONS_LOC),
					fluidColumnLoc(3, 0, LocationDto.LATITUDE),
					fluidColumnLoc(3, 0, LocationDto.LONGITUDE),
					fluidColumnLoc(4, 0, LocationDto.LAT_LON_ACCURACY))));

	private MapPopupView leafletMapPopup;
	private ComboBox addressType;
	private ComboBox facilityTypeGroup;
	private ComboBox facilityType;
	private ComboBox facility;
	private TextField facilityDetails;
	private ComboBox continent;
	private ComboBox subcontinent;

	private boolean districtRequiredOnDefaultCountry;

	public LocationEditForm(FieldVisibilityCheckers fieldVisibilityCheckers, UiFieldAccessCheckers fieldAccessCheckers) {
		super(LocationDto.class, LocationDto.I18N_PREFIX, true, fieldVisibilityCheckers, fieldAccessCheckers);

		if (FacadeProvider.getGeocodingFacade().isEnabled() && isEditableAllowed(LocationDto.LATITUDE) && isEditableAllowed(LocationDto.LONGITUDE)) {
			getContent().addComponent(createGeoButton(), GEO_BUTTONS_LOC);
		}
	}

	public ComboBox getFacilityTypeGroup() {
		return facilityTypeGroup;
	}

	private void setConvertedValue(String propertyId, Object value) {
		((AbstractField<?>) getField(propertyId)).setConvertedValue(value);
	}

	@SuppressWarnings("unchecked")
	private <T> T getConvertedValue(String propertyId) {
		return (T) ((AbstractField<?>) getField(propertyId)).getConvertedValue();
	}

	public void setFieldsRequirement(boolean required, String... fieldIds) {
		setRequired(required, fieldIds);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void addFields() {

		addressType = addField(LocationDto.ADDRESS_TYPE, ComboBox.class);
		addressType.setVisible(false);
		final PersonAddressType[] personAddressTypeValues = PersonAddressType.getValues(FacadeProvider.getConfigFacade().getCountryCode());
		if (!isConfiguredServer("ch")) {
			addressType.removeAllItems();
			addressType.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ID);
			addressType.addItems(personAddressTypeValues);
		}
		TextField addressTypeDetails = addField(LocationDto.ADDRESS_TYPE_DETAILS, TextField.class);
		addressTypeDetails.setVisible(false);
		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			LocationDto.ADDRESS_TYPE_DETAILS,
			addressType,
			Arrays.stream(personAddressTypeValues).filter(pat -> !pat.equals(PersonAddressType.HOME)).collect(Collectors.toList()),
			true);
		FieldHelper.setRequiredWhen(
			getFieldGroup(),
			addressType,
			Arrays.asList(LocationDto.ADDRESS_TYPE_DETAILS),
			Arrays.asList(PersonAddressType.OTHER_ADDRESS));

		facilityTypeGroup = new ComboBox();
		facilityTypeGroup.setId("typeGroup");
		facilityTypeGroup.setCaption(I18nProperties.getCaption(Captions.Facility_typeGroup));
		facilityTypeGroup.setWidth(100, Unit.PERCENTAGE);
		facilityTypeGroup.addItems(FacilityTypeGroup.values());
		getContent().addComponent(facilityTypeGroup, FACILITY_TYPE_GROUP_LOC);
		facilityType = addField(LocationDto.FACILITY_TYPE);
		facility = addInfrastructureField(LocationDto.FACILITY);
		facility.setImmediate(true);
		facilityDetails = addField(LocationDto.FACILITY_DETAILS, TextField.class);
		facilityDetails.setVisible(false);

		addressType.addValueChangeListener(e -> {
			FacilityTypeGroup oldGroup = (FacilityTypeGroup) facilityTypeGroup.getValue();
			FacilityType oldType = (FacilityType) facilityType.getValue();
			FacilityReferenceDto oldFacility = (FacilityReferenceDto) facility.getValue();
			String oldDetails = facilityDetails.getValue();
			if (PersonAddressType.HOME.equals(addressType.getValue())) {
				facilityTypeGroup.removeAllItems();
				facilityTypeGroup.addItems(FacilityTypeGroup.getAccomodationGroups());
				setOldFacilityValuesIfPossible(oldGroup, oldType, oldFacility, oldDetails);
			} else {
				facilityTypeGroup.removeAllItems();
				facilityTypeGroup.addItems(FacilityTypeGroup.values());
				setOldFacilityValuesIfPossible(oldGroup, oldType, oldFacility, oldDetails);
			}
		});

		TextField streetField = addField(LocationDto.STREET, TextField.class);
		TextField houseNumberField = addField(LocationDto.HOUSE_NUMBER, TextField.class);
		TextField additionalInformationField = addField(LocationDto.ADDITIONAL_INFORMATION, TextField.class);
		addField(LocationDto.DETAILS, TextField.class);
		TextField cityField = addField(LocationDto.CITY, TextField.class);
		TextField postalCodeField = addField(LocationDto.POSTAL_CODE, TextField.class);
		ComboBox areaType = addField(LocationDto.AREA_TYPE, ComboBox.class);
		areaType.setDescription(I18nProperties.getDescription(getPropertyI18nPrefix() + "." + LocationDto.AREA_TYPE));

		final TextField tfLatitude = addField(LocationDto.LATITUDE, TextField.class);
		final TextField tfLongitude = addField(LocationDto.LONGITUDE, TextField.class);
		final TextField tfAccuracy = addField(LocationDto.LAT_LON_ACCURACY, TextField.class);
		final StringToAngularLocationConverter stringToAngularLocationConverter = new StringToAngularLocationConverter();
		tfLatitude.setConverter(stringToAngularLocationConverter);
		tfLongitude.setConverter(stringToAngularLocationConverter);
		tfAccuracy.setConverter(stringToAngularLocationConverter);

		continent = addInfrastructureField(LocationDto.CONTINENT);
		subcontinent = addInfrastructureField(LocationDto.SUB_CONTINENT);
		ComboBox country = addInfrastructureField(LocationDto.COUNTRY);
		ComboBox region = addInfrastructureField(LocationDto.REGION);
		ComboBox district = addInfrastructureField(LocationDto.DISTRICT);
		ComboBox community = addInfrastructureField(LocationDto.COMMUNITY);

		continent.setVisible(false);
		subcontinent.setVisible(false);

		initializeVisibilitiesAndAllowedVisibilities();
		initializeAccessAndAllowedAccesses();

		if (!isEditableAllowed(LocationDto.COMMUNITY)) {
			setEnabled(false, LocationDto.COUNTRY, LocationDto.REGION, LocationDto.DISTRICT);
		}

		country.addValueChangeListener(e -> {
			CountryReferenceDto countryDto = (CountryReferenceDto) e.getProperty().getValue();
			if (countryDto != null) {
				continent.setValue(FacadeProvider.getContinentFacade().getByCountry(countryDto));
				subcontinent.setValue(FacadeProvider.getSubcontinentFacade().getByCountry(countryDto));
			}
		});

		region.addValueChangeListener(e -> {
			RegionReferenceDto regionDto = (RegionReferenceDto) e.getProperty().getValue();
			FieldHelper
				.updateItems(district, regionDto != null ? FacadeProvider.getDistrictFacade().getAllActiveByRegion(regionDto.getUuid()) : null);
		});
		district.addValueChangeListener(e -> {
			DistrictReferenceDto districtDto = (DistrictReferenceDto) e.getProperty().getValue();
			FieldHelper.updateItems(
				community,
				districtDto != null ? FacadeProvider.getCommunityFacade().getAllActiveByDistrict(districtDto.getUuid()) : null);
			if (districtDto == null) {
				FieldHelper.removeItems(facility);
			} else if (facilityType.getValue() != null) {
				FieldHelper.updateItems(
					facility,
					FacadeProvider.getFacilityFacade()
						.getActiveFacilitiesByDistrictAndType(districtDto, (FacilityType) facilityType.getValue(), true, false));
			}
		});
		community.addValueChangeListener(e -> {
			CommunityReferenceDto communityDto = (CommunityReferenceDto) e.getProperty().getValue();
			if (facilityType.getValue() != null) {
				FieldHelper.updateItems(
					facility,
					communityDto != null
						? FacadeProvider.getFacilityFacade()
							.getActiveFacilitiesByCommunityAndType(communityDto, (FacilityType) facilityType.getValue(), true, true)
						: district.getValue() != null
							? FacadeProvider.getFacilityFacade()
								.getActiveFacilitiesByDistrictAndType(
									(DistrictReferenceDto) district.getValue(),
									(FacilityType) facilityType.getValue(),
									true,
									false)
							: null);
			}
		});
		facilityTypeGroup.addValueChangeListener(e -> {
			FieldHelper.removeItems(facility);
			FieldHelper.updateEnumData(facilityType, FacilityType.getTypes((FacilityTypeGroup) facilityTypeGroup.getValue()));
			facilityType.setRequired(facilityTypeGroup.getValue() != null);
		});
		facilityType.addValueChangeListener(e -> {
			FieldHelper.removeItems(facility);
			if (facilityType.getValue() != null && facilityTypeGroup.getValue() == null) {
				facilityTypeGroup.setValue(((FacilityType) facilityType.getValue()).getFacilityTypeGroup());
			}
			if (facilityType.getValue() != null && district.getValue() != null) {
				if (community.getValue() != null) {
					FieldHelper.updateItems(
						facility,
						FacadeProvider.getFacilityFacade()
							.getActiveFacilitiesByCommunityAndType(
								(CommunityReferenceDto) community.getValue(),
								(FacilityType) facilityType.getValue(),
								true,
								false));
				} else {
					FieldHelper.updateItems(
						facility,
						FacadeProvider.getFacilityFacade()
							.getActiveFacilitiesByDistrictAndType(
								(DistrictReferenceDto) district.getValue(),
								(FacilityType) facilityType.getValue(),
								true,
								false));
				}
			}
		});
		facility.addValueChangeListener(e -> {
			if (facility.getValue() != null) {
				boolean visibleAndRequired = areFacilityDetailsRequired();

				facilityDetails.setVisible(visibleAndRequired);
				facilityDetails.setRequired(visibleAndRequired);

				if (!visibleAndRequired) {
					facilityDetails.clear();
				} else {
					facilityDetails.setValue(getValue().getFacilityDetails());
				}
			} else {
				facilityDetails.setVisible(false);
				facilityDetails.setRequired(false);
				facilityDetails.clear();
			}

			// Fill in the address fields based on the selected facility
			// We don't want the location form to automatically change even if the facility's address is updated later 
			// on, so we only trigger it upon a manual change of the facility field
			// We use isAttached() to avoid the fuss when initializing the form, it may seems a bit hacky, but it is
			// necessary because isModified() will still return true for a short duration even if we keep the very same 
			// value because of this field dependencies to other fields and the way updateEnumValues works
			if (facility.isAttached()) {
				if (facility.getValue() != null) {
					FacilityDto facilityDto =
						FacadeProvider.getFacilityFacade().getByUuid(((FacilityReferenceDto) getField(LocationDto.FACILITY).getValue()).getUuid());

					// Only if the facility's address is set
					if (StringUtils.isNotEmpty(facilityDto.getCity())
						|| StringUtils.isNotEmpty(facilityDto.getPostalCode())
						|| StringUtils.isNotEmpty(facilityDto.getStreet())
						|| StringUtils.isNotEmpty(facilityDto.getHouseNumber())
						|| StringUtils.isNotEmpty(facilityDto.getAdditionalInformation())
						|| facilityDto.getAreaType() != null
						|| facilityDto.getLatitude() != null
						|| facilityDto.getLongitude() != null) {

						// Show a confirmation popup if the location's address is already set and different from the facility one
						if ((StringUtils.isNotEmpty(cityField.getValue()) && !cityField.getValue().equals(facilityDto.getCity()))
							|| (StringUtils.isNotEmpty(postalCodeField.getValue()) && !postalCodeField.getValue().equals(facilityDto.getPostalCode()))
							|| (StringUtils.isNotEmpty(streetField.getValue()) && !streetField.getValue().equals(facilityDto.getStreet()))
							|| (StringUtils.isNotEmpty(houseNumberField.getValue())
								&& !houseNumberField.getValue().equals(facilityDto.getHouseNumber()))
							|| (StringUtils.isNotEmpty(additionalInformationField.getValue())
								&& !additionalInformationField.getValue().equals(facilityDto.getAdditionalInformation()))
							|| (areaType.getValue() != null && areaType.getValue() != facilityDto.getAreaType())
							|| (tfLatitude.getConvertedValue() != null
								&& Double.compare((Double) tfLatitude.getConvertedValue(), facilityDto.getLatitude()) != 0)
							|| (tfLongitude.getConvertedValue() != null
								&& Double.compare((Double) tfLongitude.getConvertedValue(), facilityDto.getLongitude()) != 0)) {

							VaadinUiUtil.showConfirmationPopup(
								I18nProperties.getString(Strings.headingLocation),
								new Label(I18nProperties.getString(Strings.confirmationLocationFacilityAddressOverride)),
								I18nProperties.getString(Strings.yes),
								I18nProperties.getString(Strings.no),
								640,
								confirmationEvent -> {
									if (confirmationEvent) {
										overrideLocationDetailsWithFacilityOnes(facilityDto);
									}
								});
						} else {
							overrideLocationDetailsWithFacilityOnes(facilityDto);
						}
					}
				}
			}
		});
		final List<ContinentReferenceDto> continents = FacadeProvider.getContinentFacade().getAllActiveAsReference();
		if (continents.isEmpty()) {
			continent.setVisible(false);
		} else {
			continent.addItems(continents);
		}
		final List<SubcontinentReferenceDto> subcontinents = FacadeProvider.getSubcontinentFacade().getAllActiveAsReference();
		if (subcontinents.isEmpty()) {
			subcontinent.setVisible(false);
		} else {
			subcontinent.addItems(subcontinents);
		}
		country.addItems(FacadeProvider.getCountryFacade().getAllActiveAsReference());
		updateRegionCombo(region, country);
		country.addValueChangeListener(e -> {
			updateRegionCombo(region, country);
			region.setValue(null);
		});

		Stream.of(LocationDto.LATITUDE, LocationDto.LONGITUDE)
			.<Field<?>> map(this::getField)
			.forEach(f -> f.addValueChangeListener(e -> this.updateLeafletMapContent()));
	}

	private void updateRegionCombo(ComboBox region, ComboBox country) {
		InfrastructureFieldsHelper.updateRegionBasedOnCountry(country, region, (isServerCountry) -> {
			if (districtRequiredOnDefaultCountry) {
				setFieldsRequirement(isServerCountry, LocationDto.REGION, LocationDto.DISTRICT);
			}
		});
	}

	private void overrideLocationDetailsWithFacilityOnes(FacilityDto facilityDto) {
		((TextField) getField(LocationDto.CITY)).setValue(facilityDto.getCity());
		((TextField) getField(LocationDto.POSTAL_CODE)).setValue(facilityDto.getPostalCode());
		((TextField) getField(LocationDto.STREET)).setValue(facilityDto.getStreet());
		((TextField) getField(LocationDto.HOUSE_NUMBER)).setValue(facilityDto.getHouseNumber());
		((TextField) getField(LocationDto.ADDITIONAL_INFORMATION)).setValue(facilityDto.getAdditionalInformation());
		((ComboBox) getField(LocationDto.AREA_TYPE)).setValue(facilityDto.getAreaType());
		((TextField) getField(LocationDto.LATITUDE)).setConvertedValue(facilityDto.getLatitude());
		((TextField) getField(LocationDto.LONGITUDE)).setConvertedValue(facilityDto.getLongitude());
	}

	private void setOldFacilityValuesIfPossible(
		FacilityTypeGroup oldGroup,
		FacilityType oldType,
		FacilityReferenceDto oldFacility,
		String oldDetails) {
		facilityTypeGroup.setValue(oldGroup);
		facilityType.setValue(oldType);
		facility.setValue(oldFacility);
		facilityDetails.setValue(oldDetails);
	}

	private HorizontalLayout createGeoButton() {
		HorizontalLayout geoButtonLayout = new HorizontalLayout();
		geoButtonLayout.setMargin(false);
		geoButtonLayout.setSpacing(false);

		Page.getCurrent().getStyles().add(".geocode-button-red {color: #cc0000 !important;}");

		Button geocodeButton = ButtonHelper.createIconButtonWithCaption(
			"geocodeButton",
			null,
			VaadinIcons.MAP_MARKER,
			e -> triggerGeocoding(),
			ValoTheme.BUTTON_ICON_ONLY,
			ValoTheme.BUTTON_BORDERLESS,
			ValoTheme.BUTTON_LARGE);

		// Highlight geocode-button when the address changes
		ValueChangeListener addressListener = e -> geocodeButton.addStyleName("geocode-button-red");
		// adding the valuechangelistener inside another valuechangelistener seems counterintuitive, but it prevents the listener from being executed when the initial field values are set
		getField(LocationDto.STREET).addValueChangeListener(e -> {
			getField(LocationDto.STREET).removeValueChangeListener(addressListener);
			getField(LocationDto.STREET).addValueChangeListener(addressListener);
		});
		getField(LocationDto.POSTAL_CODE).addValueChangeListener(e -> {
			getField(LocationDto.POSTAL_CODE).removeValueChangeListener(addressListener);
			getField(LocationDto.POSTAL_CODE).addValueChangeListener(addressListener);
		});
		getField(LocationDto.CITY).addValueChangeListener(e -> {
			getField(LocationDto.CITY).removeValueChangeListener(addressListener);
			getField(LocationDto.CITY).addValueChangeListener(addressListener);
		});
		getField(LocationDto.HOUSE_NUMBER).addValueChangeListener(e -> {
			getField(LocationDto.HOUSE_NUMBER).removeValueChangeListener(addressListener);
			getField(LocationDto.HOUSE_NUMBER).addValueChangeListener(addressListener);
		});

		geocodeButton.addClickListener(e -> geocodeButton.removeStyleName("geocode-button-red"));
		geocodeButton.removeStyleName("geocode-button-red");

		geoButtonLayout.addComponent(geocodeButton);
		geoButtonLayout.setComponentAlignment(geocodeButton, Alignment.BOTTOM_RIGHT);

		leafletMapPopup = new MapPopupView();
		leafletMapPopup.setCaption(" ");
		leafletMapPopup.setEnabled(false);
		leafletMapPopup.setStyleName(ValoTheme.BUTTON_LARGE);
		leafletMapPopup.addStyleName(ValoTheme.BUTTON_ICON_ONLY);

		geoButtonLayout.addComponent(leafletMapPopup);
		geoButtonLayout.setComponentAlignment(leafletMapPopup, Alignment.BOTTOM_RIGHT);

		return geoButtonLayout;
	}

	private void updateLeafletMapContent() {

		if (leafletMapPopup == null) {
			return;
		}

		if (areFieldsValid(LocationDto.LATITUDE, LocationDto.LONGITUDE)) {
			Double lat = getConvertedValue(LocationDto.LATITUDE);
			Double lon = getConvertedValue(LocationDto.LONGITUDE);
			GeoLatLon coordinates;
			if (ObjectUtils.allNotNull(lat, lon)) {
				coordinates = new GeoLatLon(lat, lon);
			} else {
				coordinates = null;
			}
			leafletMapPopup.setEnabled(coordinates != null);
			leafletMapPopup.setCoordinates(coordinates);
		} else {
			leafletMapPopup.setEnabled(false);
		}
	}

	private void triggerGeocoding() {

		String street = getConvertedValue(LocationDto.STREET);
		String houseNumber = getConvertedValue(LocationDto.HOUSE_NUMBER);
		String postalCode = getConvertedValue(LocationDto.POSTAL_CODE);
		String city = getConvertedValue(LocationDto.CITY);

		GeoLatLon latLon = FacadeProvider.getGeocodingFacade().getLatLon(street, houseNumber, postalCode, city);

		if (latLon != null) {
			setConvertedValue(LocationDto.LATITUDE, latLon.getLat());
			setConvertedValue(LocationDto.LONGITUDE, latLon.getLon());
		}
	}

	public void showAddressType() {
		addressType.setVisible(true);
		addressType.setRequired(true);
	}

	public void setDistrictRequiredOnDefaultCountry(boolean required) {
		this.districtRequiredOnDefaultCountry = required;
	}

	@Override
	protected String createHtmlLayout() {
		return HTML_LAYOUT;
	}

	private static class MapPopupView extends PopupView {

		private static final long serialVersionUID = 6119339732442336000L;

		//eye-icon styled as button
		private static final String MINNIMIZED_HTML =
			"<div tabindex=\"0\" role=\"button\" class=\"v-button v-widget icon-only v-button-icon-only borderless v-button-borderless large v-button-large\"><span class=\"v-button-wrap\">"
				+ VaadinIcons.EYE.getHtml() + "<span class=\"v-button-caption\"></span></span></div>";

		private GeoLatLon coordinates = null;

		public MapPopupView() {
			setContent(new Content() {

				private static final long serialVersionUID = -1709597624862512304L;

				@Override
				public String getMinimizedValueAsHTML() {
					return MINNIMIZED_HTML;
				}

				@Override
				public Component getPopupComponent() {
					return createLeafletMap();
				}
			});
		}

		private LeafletMap createLeafletMap() {

			LeafletMap map = new LeafletMap();
			map.setWidth(420, Unit.PIXELS);
			map.setHeight(420, Unit.PIXELS);
			map.setZoom(12);

			map.setCenter(coordinates);

			LeafletMarker marker = new LeafletMarker();
			marker.setLatLon(coordinates);
			marker.setIcon(MarkerIcon.CASE_UNCLASSIFIED);
			marker.setMarkerCount(1);

			map.addMarkerGroup("cases", Collections.singletonList(marker));

			return map;
		}

		public void setCoordinates(GeoLatLon coordinates) {
			this.coordinates = coordinates;
		}
	}

	@Override
	protected <F extends Field> F addFieldToLayout(CustomLayout layout, String propertyId, F field) {
		field.addValueChangeListener(e -> fireValueChange(false));

		return super.addFieldToLayout(layout, propertyId, field);
	}

	public void setFacilityFieldsVisible(boolean visible, boolean clearOnHidden) {
		facility.setVisible(visible);
		facilityDetails.setVisible(visible && areFacilityDetailsRequired());
		facilityType.setVisible(visible);
		facilityTypeGroup.setVisible(visible);

		if (!visible && clearOnHidden) {
			facility.clear();
			facilityDetails.clear();
			facilityType.clear();
			facilityTypeGroup.clear();
		}
	}

	public void setContinentFieldsVisible(boolean visible, boolean clearOnHidden) {
		continent.setVisible(visible);
		subcontinent.setVisible(visible);

		if (!visible && clearOnHidden) {
			continent.clear();
			subcontinent.clear();
		}
	}

	private boolean areFacilityDetailsRequired() {
		return facility.getValue() != null && ((FacilityReferenceDto) facility.getValue()).getUuid().equals(FacilityDto.OTHER_FACILITY_UUID);
	}
}
