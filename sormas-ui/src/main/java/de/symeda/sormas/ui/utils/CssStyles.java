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

package de.symeda.sormas.ui.utils;

import java.util.Arrays;
import java.util.Collections;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

import de.symeda.sormas.api.Disease;

public final class CssStyles {

	private CssStyles() {
		// Hide Utility Class Constructor
	}

	// Headlines
	public static final String H1 = "h1";
	public static final String H2 = "h2";
	public static final String H3 = "h3";
	public static final String H4 = "h4";
	public static final String H5 = "h5";

	public static final String VR = "vertical-rule";

	public static final String RICH_TEXT_CONTENT_CONTAINER = "rich-text-content-container";
	public static final String REPLY = "reply";

	// Vertical space
	public static final String VSPACE_0 = "vspace-0";
	public static final String VSPACE_1 = "vspace-1";
	public static final String VSPACE_2 = "vspace-2";
	public static final String VSPACE_3 = "vspace-3";
	public static final String VSPACE_4 = "vspace-4";
	public static final String VSPACE_5 = "vspace-5";
	public static final String VSPACE_NONE = "vspace-none";

	public static final String VSPACE_TOP_0 = "vspace-top-0";
	public static final String VSPACE_TOP_1 = "vspace-top-1";
	public static final String VSPACE_TOP_2 = "vspace-top-2";
	public static final String VSPACE_TOP_3 = "vspace-top-3";
	public static final String VSPACE_TOP_4 = "vspace-top-4";
	public static final String VSPACE_TOP_5 = "vspace-top-5";
	public static final String VSPACE_TOP_NONE = "vspace-top-none";

	// Horizontal space
	public static final String HSPACE_LEFT_0 = "hspace-left-0";
	public static final String HSPACE_LEFT_1 = "hspace-left-1";
	public static final String HSPACE_LEFT_2 = "hspace-left-2";
	public static final String HSPACE_LEFT_3 = "hspace-left-3";
	public static final String HSPACE_LEFT_4 = "hspace-left-4";
	public static final String HSPACE_LEFT_5 = "hspace-left-5";
	public static final String HSPACE_LEFT_NONE = "hspace-left-none";

	public static final String HSPACE_RIGHT_0 = "hspace-right-0";
	public static final String HSPACE_RIGHT_1 = "hspace-right-1";
	public static final String HSPACE_RIGHT_2 = "hspace-right-2";
	public static final String HSPACE_RIGHT_3 = "hspace-right-3";
	public static final String HSPACE_RIGHT_4 = "hspace-right-4";
	public static final String HSPACE_RIGHT_5 = "hspace-right-5";
	public static final String HSPACE_RIGHT_NONE = "hspace-right-none";

	public static final String INDENT_LEFT_1 = "indent-left-1";
	public static final String INDENT_LEFT_2 = "indent-left-2";
	public static final String INDENT_LEFT_3 = "indent-left-3";

	public static final String VAADIN_LABEL = "v-label";
	public static final String FIELD_WRAPPER = "fieldwrapper";
	public static final String FIELD_EXTRA_INFO = "fieldextrainfo";

	// Font colors
	public static final String LABEL_PRIMARY = "primary";
	public static final String LABEL_SECONDARY = "secondary";
	public static final String LABEL_CRITICAL = "critical";
	public static final String LABEL_WARNING = "warning";
	public static final String LABEL_IMPORTANT = "important";
	public static final String LABEL_RELEVANT = "relevant";
	public static final String LABEL_NEUTRAL = "neutral";
	public static final String LABEL_POSITIVE = "positive";
	public static final String LABEL_MINOR = "minor";
	public static final String LABEL_DISCARDED = "discarded";
	public static final String LABEL_DONE = "done";
	public static final String LABEL_NOT = "not";
	public static final String LABEL_WHITE = "white";

	// Font sizes
	public static final String LABEL_SMALL = "small";
	public static final String LABEL_MEDIUM = "medium";
	public static final String LABEL_LARGE = "large";
	public static final String LABEL_LARGE_ALT = "large-alt";
	public static final String LABEL_XLARGE = "xlarge";
	public static final String LABEL_XXLARGE = "xxlarge";
	public static final String LABEL_XXXLARGE = "xxxlarge";
	public static final String LABEL_REALLY_LARGE = "really-large";

	// Font styles
	public static final String LABEL_BOLD = "bold";
	public static final String LABEL_UPPERCASE = "uppercase";
	public static final String LABEL_ITALIC = "italic";

	// Label styles
	public static final String LABEL_BOTTOM_LINE = "bottom-line";
	public static final String LABEL_ROUNDED_CORNERS = "rounded-corners";
	public static final String LABEL_ROUNDED_CORNERS_SLIM = "rounded-corners-slim";
	public static final String LABEL_BACKGROUND_FOCUS_LIGHT = "background-focus-light";
	public static final String LABEL_VERTICAL_ALIGN_SUPER = "vertical-align-super";
	public static final String LABEL_VERTICAL_ALIGN_TOP = "vertical-align-top";
	public static final String LABEL_BACKGROUND_FOLLOW_UP_SYMPTOMATIC = "bg-follow-up-symptomatic";
	public static final String LABEL_BACKGROUND_FOLLOW_UP_NOT_SYMPTOMATIC = "bg-follow-up-not-symptomatic";
	public static final String LABEL_BACKGROUND_FOLLOW_UP_UNAVAILABLE = "bg-follow-up-unavailable";
	public static final String LABEL_BACKGROUND_FOLLOW_UP_UNCOOPERATIVE = "bg-follow-up-uncooperative";
	public static final String LABEL_BACKGROUND_FOLLOW_UP_NOT_PERFORMED = "bg-follow-up-not-performed";
	public static final String LABEL_WHITE_SPACE_NORMAL = "white-space-normal";
	public static final String LABEL_TEXT_ALIGN_RIGHT = "text-align-right";

	// Layout styles
	public static final String LAYOUT_MINIMAL = "minimal";
	public static final String LAYOUT_SPACIOUS = "spacious";
	public static final String BACKGROUND_ROUNDED_CORNERS = "background-rounded-corners";
	public static final String BACKGROUND_CRITERIA = "background-criteria";
	public static final String BACKGROUND_SUB_CRITERIA = "background-sub-criteria";
	public static final String BACKGROUND_SUSPECT_CRITERIA = "background-suspect-criteria";
	public static final String BACKGROUND_PROBABLE_CRITERIA = "background-probable-criteria";
	public static final String BACKGROUND_CONFIRMED_CRITERIA = "background-confirmed-criteria";
	public static final String BACKGROUND_NOT_A_CASE_CRITERIA = "background-not-a-case-criteria";
	public static final String NO_BORDER = "no-border";
	public static final String BACKGROUND_HIGHLIGHT = "background-highlight";
	public static final String BACKGROUND_CRITICAL = "background-critical";
	public static final String BACKGROUND_DARKER = "background-darker";

	// Button styles
	public static final String VAADIN_BUTTON = "v-button";
	public static final String BUTTON_CRITICAL = "critical";
	public static final String BUTTON_WARNING = "warning";
	public static final String BUTTON_SUBTLE = "subtle";
	public static final String BUTTON_BORDER_NEUTRAL = "border-neutral";
	public static final String BUTTON_COMPACT = "compact";
	public static final String LINK_ACTIVE = "active";
	public static final String BUTTON_FILTER = "filter";
	public static final String BUTTON_FILTER_LIGHT = "filter-light";
	public static final String BUTTON_FILTER_DARK = "filter-dark";
	public static final String BUTTON_FILTER_SMALL = "filter-small";
	public static final String BUTTON_FILTER_ENABLED = "filter-enabled";
	public static final String BUTTON_FILTER_DISABLED = "filter-disabled";
	public static final String BUTTON_FONT_SIZE_LARGE = "font-size-large";
	public static final String GEOCODE_BUTTON_HIGHLIGHT = "geocode-button-highlight";

	// List styles
	public static final String SORMAS_LIST = "s-list";
	public static final String SORMAS_LIST_ENTRY = "s-list-entry";
	public static final String SORMAS_LIST_ENTRY_NO_BORDER = "s-list-entry-no-border";

	// SVG fill and stroke styles
	public static final String SVG_FILL_PRIMARY = "svg-fill-primary";
	public static final String SVG_FILL_BACKGROUND = "svg-fill-background";
	public static final String SVG_FILL_CRITICAL = "svg-fill-critical";
	public static final String SVG_FILL_IMPORTANT = "svg-fill-important";
	public static final String SVG_FILL_POSITIVE = "svg-fill-positive";
	public static final String SVG_FILL_MINOR = "svg-fill-minor";
	public static final String SVG_FILL_NEUTRAL = "svg-fill-neutral";
	public static final String SVG_STROKE_PRIMARY = "svg-stroke-primary";
	public static final String SVG_STROKE_CRITICAL = "svg-stroke-critical";
	public static final String SVG_STROKE_IMPORTANT = "svg-stroke-important";
	public static final String SVG_STROKE_POSITIVE = "svg-stroke-positive";
	public static final String SVG_STROKE_MINOR = "svg-stroke-minor";
	public static final String SVG_STROKE_BACKGROUND = "svg-stroke-background";

	public static final String FORCE_CAPTION = "force-caption";
	public static final String FORCE_CAPTION_CHECKBOX = "force-caption-checkbox";
	public static final String CAPTION_ON_TOP = "on-top";
	public static final String CAPTION_HIDDEN = "caption-hidden";
	public static final String CAPTION_OVERFLOW = "caption-overflow";
	public static final String CAPTION_FIXED_WIDTH_100 = "caption-fixed-width-100";

	public static final String SOFT_REQUIRED = "soft-required";
	public static final String ERROR_COLOR_PRIMARY = "error-color-primary";

	public static final String ALIGN_CENTER = "align-center";
	public static final String ALIGN_RIGHT = "align-right";

	public static final String OPTIONGROUP_HORIZONTAL_SUBTLE = "horizontal-subtle";
	public static final String OPTIONGROUP_HORIZONTAL_PRIMARY = "horizontal-primary";
	public static final String OPTIONGROUP_HORIZONTAL_SWITCH_CRITICAL = "horizontal-switch-critical";
	public static final String OPTIONGROUP_CHECKBOXES_HORIZONTAL = "checkboxes-horizontal";
	public static final String OPTIONGROUP_GRID_LAYOUT = "grid-layout";
	public static final String OPTIONGROUP_CAPTION_INLINE = "caption-inline";
	public static final String OPTIONGROUP_CAPTION_AREA_INLINE = "caption-area-inline";

	public static final String GRID_CELL_PRIORITY_HIGH = "priority-high";
	public static final String GRID_CELL_PRIORITY_NORMAL = "priority-normal";
	public static final String GRID_CELL_PRIORITY_LOW = "priority-low";
	public static final String GRID_CELL_WARNING = "warning";
	public static final String GRID_CELL_SYMPTOMATIC = "follow-up-symptomatic";
	public static final String GRID_CELL_NOT_SYMPTOMATIC = "follow-up-not-symptomatic";
	public static final String GRID_CELL_UNAVAILABLE = "follow-up-unavailable";
	public static final String GRID_CELL_UNCOOPERATIVE = "follow-up-uncooperative";
	public static final String GRID_CELL_NOT_PERFORMED = "follow-up-not-performed";
	public static final String GRID_CELL_SYMPTOMATIC_EXTERNAL = "follow-up-symptomatic-external";
	public static final String GRID_CELL_NOT_SYMPTOMATIC_EXTERNAL = "follow-up-not-symptomatic-external";
	public static final String GRID_CELL_UNAVAILABLE_EXTERNAL = "follow-up-unavailable-external";
	public static final String GRID_CELL_UNCOOPERATIVE_EXTERNAL = "follow-up-uncooperative-external";
	public static final String GRID_CELL_NOT_PERFORMED_EXTERNAL = "follow-up-not-performed-external";
	public static final String GRID_CELL_LINK = "link";

	public static final String GRID_CELL_ODD = "odd";

	public static final String GRID_ROW_STATUS_DISCARDED = "status-discarded";
	public static final String GRID_ROW_STATUS_NOT = "status-not";
	public static final String GRID_ROW_STATUS_DONE = "status-done";
	public static final String GRID_ROW_STATUS_PENDING = "status-pending";
	public static final String GRID_ROW_TITLE = "row-title";

	public static final String LABEL_CONFIGURATION_SEVERITY_INDICATOR = "severity-indicator";
	public static final String BADGE = "badge";

	public static final String SPACING_SMALL = "spacing-small";

	// Checkbox styles
	public static final String CHECKBOX_FILTER_INLINE = "filter-inline";

	// Combo Box styles
	public static final String COMBO_BOX_SUBTLE = "subtle";
	public static final String COMBO_BOX_WITH_FLAG_ICON = "with-flag-icon";

	// Layout components
	public static final String ROOT_COMPONENT = "root-component";
	public static final String MAIN_COMPONENT = "main-component";
	public static final String SIDE_COMPONENT = "side-component";

	// Statistics layout
	public static final String STATISTICS_TITLE_BOX = "title-box";
	public static final String STATISTICS_TITLE = "title";

	// Notification styles
	public static final String NOTIFICATION_ERROR = "notification-error";

	// Login
	public static final String LOGINDEATILS = "login-details";
	public static final String LOGINFORM = "login-form";
	public static final String LOGINFORM_CONTAINER = "login-form-container";
	public static final String LOGINSCREEN = "login-screen";
	public static final String LOGINSCREEN_BACK = "login-screen-back";
	public static final String LOGINSIDEBAR = "login-sidebar";
	public static final String LOGINSIDEBAR_CUSTOM_HEADER = "login-sidebar-custom-header";
	public static final String LOGIN_HEADLINELABEL = "headline-label";
	public static final String LOGIN_LOGOCONTAINER = "logo-container";

	// MISC
	public static final String RESIZABLE = "resizable";

	// Grid layout
	public static final String GRID_LAYOUT_EVEN = "even";
	public static final String GRID_LAYOUT_ODD = "odd";
	public static final String GRID_LAYOUT_SECTION = "section";

	public static final String TEXTFIELD_ROW = "row";
	public static final String TEXTFIELD_CAPTION_INLINE = "caption-inline";

	public static final String FLOAT_RIGHT = "float-right";

	/**
	 * Example: <code>LayoutUtil.fluidColumnLocCss(CssStyles.LAYOUT_COL_HIDE_INVSIBLE, 3, 0, PersonDto.CAUSE_OF_DEATH_DISEASE)</code>
	 */
	public static final String LAYOUT_COL_HIDE_INVSIBLE = "hide-invisble";

	public static final String INACCESSIBLE_FIELD = "inaccessible-field";
	public static final String INACCESSIBLE_COLUMN = "inaccessible-column";
	public static final String INACCESSIBLE_LABEL = "inaccessible-label";

	// Print
	public static final String PRINT_CONTAINER = "print-container";
	public static final String PRINT_MENU = "print-menu";
	public static final String PRINT_VIEW_CONTAINER = "print-view-container";
	public static final String FILTER_FORM = "filter-form";

	@Deprecated
	public static final String CALLOUT = "callout";

	public static String buildVaadinStyle(String primaryStyle, String... styles) {
		StringBuilder styleBuilder = new StringBuilder();
		styleBuilder.append(primaryStyle);
		for (String style : styles) {
			styleBuilder.append(" ").append(primaryStyle).append("-").append(style);
		}
		return styleBuilder.toString();
	}

	public static void style(Component component, String... styles) {
		for (String style : styles)
			component.addStyleName(style);
	}

	public static void style(String style, Component... components) {
		for (Component component : components) {
			component.addStyleName(style);
		}
	}

	public static void removeStyles(Component component, String... styles) {
		for (String style : styles) {
			component.removeStyleName(style);
		}
	}

	public static void stylePrimary(Component component, String primaryStyle, String... styles) {
		component.setPrimaryStyleName(primaryStyle);
		for (String style : styles)
			component.addStyleName(style);
	}

	/**
	 * Styles and (de-)activates the given buttons.
	 * 
	 * @param activeButton
	 *            This button is styled as active but disabled.
	 * @param allOrOtherButtons
	 *            These buttons lose their active styling and become enabled. {@code activeButton} may be included here.
	 */
	public static void styleSectionFilterButton(Button activeButton, Button... allOrOtherButtons) {

		styleSectionFilterButton(activeButton, allOrOtherButtons == null ? Collections.emptyList() : Arrays.asList(allOrOtherButtons));
	}

	/**
	 * Styles and (de-)activates the given buttons.
	 * 
	 * @param activeButton
	 *            This button is styled as active but disabled.
	 * @param allOrOtherButtons
	 *            These buttons lose their active styling and become enabled. {@code activeButton} may be included here.
	 */
	public static <B extends Button> void styleSectionFilterButton(Button activeButton, Iterable<B> allOrOtherButtons) {

		for (Button button : allOrOtherButtons) {
			button.setEnabled(true);
			button.removeStyleName(CssStyles.LINK_ACTIVE);
		}

		activeButton.setEnabled(false);
		activeButton.addStyleName(CssStyles.LINK_ACTIVE);
	}

	/**
	 * Returns CSS style name defined in VAADIN/themes/sormas/views/disease.scss
	 */
	public static String getDiseaseColor(Disease disease) {
		switch (disease) {
		case AFP:
			return "background-disease-afp";
		case CHOLERA:
			return "background-disease-cholera";
		case CSM:
			return "background-disease-csm";
		case DENGUE:
			return "background-disease-dengue";
		case EVD:
			return "background-disease-evd";
		case GUINEA_WORM:
			return "background-disease-guinea-worm";
		case LASSA:
			return "background-disease-lassa";
		case MEASLES:
			return "background-disease-measles";
		case MONKEYPOX:
			return "background-disease-monkeypox";
		case NEW_INFLUENZA:
			return "background-disease-new-flu";
		case OTHER:
			return "background-disease-other";
		case PLAGUE:
			return "background-disease-plague";
		case POLIO:
			return "background-disease-polio";
		case UNSPECIFIED_VHF:
			return "background-disease-unspecified-vhf";
		case WEST_NILE_FEVER:
			return "background-disease-west-nile-fever";
		case YELLOW_FEVER:
			return "background-disease-yellow-fever";
		case CONGENITAL_RUBELLA:
			return "background-disease-congenital-rubella";
		case ANTHRAX:
			return "background-disease-anthrax";
		case RABIES:
			return "background-disease-rabies";
		case CORONAVIRUS:
			return "background-disease-coronavirus";
		case UNDEFINED:
		default:
			return "background-disease-undefined";
		}
	}
}
