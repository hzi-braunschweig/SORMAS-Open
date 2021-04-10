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
package de.symeda.sormas.api.doc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.symeda.sormas.api.action.ActionDto;
import de.symeda.sormas.api.caze.surveillancereport.SurveillanceReportDto;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.clinicalcourse.ClinicalVisitDto;
import de.symeda.sormas.api.clinicalcourse.HealthConditionsDto;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.epidata.EpiDataDto;
import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.api.event.EventParticipantDto;
import de.symeda.sormas.api.exposure.ExposureDto;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.hospitalization.HospitalizationDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.location.LocationDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.region.CommunityDto;
import de.symeda.sormas.api.region.CountryDto;
import de.symeda.sormas.api.region.DistrictDto;
import de.symeda.sormas.api.region.RegionDto;
import de.symeda.sormas.api.sample.AdditionalTestDto;
import de.symeda.sormas.api.sample.PathogenTestDto;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import de.symeda.sormas.api.task.TaskDto;
import de.symeda.sormas.api.therapy.PrescriptionDto;
import de.symeda.sormas.api.therapy.TreatmentDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.Diseases;
import de.symeda.sormas.api.utils.HideForCountries;
import de.symeda.sormas.api.utils.HideForCountriesExcept;
import de.symeda.sormas.api.utils.Outbreaks;
import de.symeda.sormas.api.utils.PersonalData;
import javax.validation.constraints.NotNull;
import de.symeda.sormas.api.utils.SensitiveData;
import de.symeda.sormas.api.visit.VisitDto;

/**
 * Intentionally named *Generator because we don't want Maven to execute this
 * class automatically.
 */
public class DataDictionaryGenerator {

	@Test
	public void generateDataDictionary() throws IOException {

		XSSFWorkbook workbook = new XSSFWorkbook();

		createEntitySheet(workbook, PersonDto.class, PersonDto.I18N_PREFIX);
		createEntitySheet(workbook, LocationDto.class, LocationDto.I18N_PREFIX);
		createEntitySheet(workbook, CaseDataDto.class, CaseDataDto.I18N_PREFIX);
		createEntitySheet(workbook, HospitalizationDto.class, HospitalizationDto.I18N_PREFIX);
		createEntitySheet(workbook, SurveillanceReportDto.class, SurveillanceReportDto.I18N_PREFIX);
		createEntitySheet(workbook, SymptomsDto.class, SymptomsDto.I18N_PREFIX);
		createEntitySheet(workbook, EpiDataDto.class, EpiDataDto.I18N_PREFIX);
		createEntitySheet(workbook, ExposureDto.class, ExposureDto.I18N_PREFIX);
		createEntitySheet(workbook, HealthConditionsDto.class, HealthConditionsDto.I18N_PREFIX);
		createEntitySheet(workbook, PrescriptionDto.class, PrescriptionDto.I18N_PREFIX);
		createEntitySheet(workbook, TreatmentDto.class, TreatmentDto.I18N_PREFIX);
		createEntitySheet(workbook, ClinicalVisitDto.class, ClinicalVisitDto.I18N_PREFIX);
		createEntitySheet(workbook, ContactDto.class, ContactDto.I18N_PREFIX);
		createEntitySheet(workbook, VisitDto.class, VisitDto.I18N_PREFIX);
		createEntitySheet(workbook, SampleDto.class, SampleDto.I18N_PREFIX);
		createEntitySheet(workbook, PathogenTestDto.class, PathogenTestDto.I18N_PREFIX);
		createEntitySheet(workbook, AdditionalTestDto.class, AdditionalTestDto.I18N_PREFIX);
		createEntitySheet(workbook, TaskDto.class, TaskDto.I18N_PREFIX);
		createEntitySheet(workbook, EventDto.class, EventDto.I18N_PREFIX);
		createEntitySheet(workbook, EventParticipantDto.class, EventParticipantDto.I18N_PREFIX);
		createEntitySheet(workbook, ActionDto.class, ActionDto.I18N_PREFIX);
		createEntitySheet(workbook, FacilityDto.class, FacilityDto.I18N_PREFIX);
		createEntitySheet(workbook, CountryDto.class, CountryDto.I18N_PREFIX);
		createEntitySheet(workbook, RegionDto.class, RegionDto.I18N_PREFIX);
		createEntitySheet(workbook, DistrictDto.class, DistrictDto.I18N_PREFIX);
		createEntitySheet(workbook, CommunityDto.class, CommunityDto.I18N_PREFIX);
		createEntitySheet(workbook, UserDto.class, UserDto.I18N_PREFIX);

		XssfHelper.addAboutSheet(workbook);

		String filePath = "src/main/resources/doc/SORMAS_Data_Dictionary.xlsx";
		try (OutputStream fileOut = new FileOutputStream(filePath)) {
			workbook.write(fileOut);
		}
		workbook.close();
	}

	private enum EntityColumn {
		FIELD,
		TYPE,
		DATA_PROTECTION,
		CAPTION,
		DESCRIPTION,
		REQUIRED,
		NEW_DISEASE,
		DISEASES,
		OUTBREAKS,
		IGNORED_COUNTRIES,
		EXCLUSIVE_COUNTRIES,
	}

	@SuppressWarnings("unchecked")
	private XSSFSheet createEntitySheet(XSSFWorkbook workbook, Class<? extends EntityDto> entityClass, String i18nPrefix) {
		String name = I18nProperties.getCaption(i18nPrefix);
		String safeName = WorkbookUtil.createSafeSheetName(name);
		XSSFSheet sheet = workbook.createSheet(safeName);

		// Create
		XSSFTable table = sheet.createTable();
		String safeTableName = safeName.replaceAll("\\s", "_");
		table.setName(safeTableName);
		table.setDisplayName(safeTableName);

		XssfHelper.styleTable(table, 1);

		int columnCount = EntityColumn.values().length;
		int rowNumber = 0;
		// header
		XSSFRow headerRow = sheet.createRow(rowNumber++);
		for (EntityColumn column : EntityColumn.values()) {
			table.addColumn();
			String columnCaption = column.toString();
			columnCaption = columnCaption.charAt(0) + columnCaption.substring(1).toLowerCase().replaceAll("_", " ");
			headerRow.createCell(column.ordinal()).setCellValue(columnCaption);
		}

		// column width
		sheet.setColumnWidth(EntityColumn.FIELD.ordinal(), 256 * 30);
		sheet.setColumnWidth(EntityColumn.TYPE.ordinal(), 256 * 30);
		sheet.setColumnWidth(EntityColumn.DATA_PROTECTION.ordinal(), 256 * 30);
		sheet.setColumnWidth(EntityColumn.CAPTION.ordinal(), 256 * 30);
		sheet.setColumnWidth(EntityColumn.DESCRIPTION.ordinal(), 256 * 60);
		sheet.setColumnWidth(EntityColumn.REQUIRED.ordinal(), 256 * 10);
		sheet.setColumnWidth(EntityColumn.NEW_DISEASE.ordinal(), 256 * 8);
		sheet.setColumnWidth(EntityColumn.DISEASES.ordinal(), 256 * 45);
		sheet.setColumnWidth(EntityColumn.OUTBREAKS.ordinal(), 256 * 10);
		sheet.setColumnWidth(EntityColumn.IGNORED_COUNTRIES.ordinal(), 256 * 20);
		sheet.setColumnWidth(EntityColumn.EXCLUSIVE_COUNTRIES.ordinal(), 256 * 20);

		CellStyle defaultCellStyle = workbook.createCellStyle();
		defaultCellStyle.setWrapText(true);

		List<Class<Enum<?>>> usedEnums = new ArrayList<>();
		boolean usesFacilityReference = false;

		for (Field field : entityClass.getDeclaredFields()) {
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers()))
				continue;
			XSSFRow row = sheet.createRow(rowNumber++);

			// field name
			XSSFCell fieldNameCell = row.createCell(EntityColumn.FIELD.ordinal());
			fieldNameCell.setCellValue(field.getName());

			// value range
			XSSFCell fieldValueCell = row.createCell(EntityColumn.TYPE.ordinal());
			fieldValueCell.setCellStyle(defaultCellStyle);
			Class<?> fieldType = field.getType();
			if (fieldType.isEnum()) {
				// use enum type name - values are added below
				fieldValueCell.setCellValue(fieldType.getSimpleName());
				if (!usedEnums.contains(fieldType)) {
					usedEnums.add((Class<Enum<?>>) fieldType);
				}
			} else if (EntityDto.class.isAssignableFrom(fieldType)) {
				fieldValueCell.setCellValue(fieldType.getSimpleName().replaceAll("Dto", ""));
			} else if (ReferenceDto.class.isAssignableFrom(fieldType)) {
				fieldValueCell.setCellValue(fieldType.getSimpleName().replaceAll("Dto", ""));
				if (FacilityReferenceDto.class.isAssignableFrom(fieldType)) {
					usesFacilityReference = true;
				}
			} else if (String.class.isAssignableFrom(fieldType)) {
				fieldValueCell.setCellValue(I18nProperties.getCaption("text"));
			} else if (Date.class.isAssignableFrom(fieldType)) {
				fieldValueCell.setCellValue(I18nProperties.getCaption("date"));
			} else if (Number.class.isAssignableFrom(fieldType)) {
				fieldValueCell.setCellValue(I18nProperties.getCaption("number"));
			} else if (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType)) {
				fieldValueCell.setCellValue(Boolean.TRUE.toString() + ", " + Boolean.FALSE.toString());
			}

			//sensitive data
			XSSFCell dataProtectionCell = row.createCell(EntityColumn.DATA_PROTECTION.ordinal());
			if (field.getAnnotation(PersonalData.class) != null) {
				dataProtectionCell.setCellValue("personal");
			} else {
				if (field.getAnnotation(SensitiveData.class) != null)
					dataProtectionCell.setCellValue("sensitive");
			}

			// caption
			XSSFCell captionCell = row.createCell(EntityColumn.CAPTION.ordinal());
			captionCell.setCellValue(I18nProperties.getPrefixCaption(i18nPrefix, field.getName(), ""));

			// description
			XSSFCell descriptionCell = row.createCell(EntityColumn.DESCRIPTION.ordinal());
			descriptionCell.setCellStyle(defaultCellStyle);
			descriptionCell.setCellValue(I18nProperties.getPrefixDescription(i18nPrefix, field.getName(), ""));

			// required
			XSSFCell requiredCell = row.createCell(EntityColumn.REQUIRED.ordinal());
			if (field.getAnnotation(NotNull.class) != null)
				requiredCell.setCellValue(true);

			// diseases
			XSSFCell diseasesCell = row.createCell(EntityColumn.DISEASES.ordinal());
			diseasesCell.setCellStyle(defaultCellStyle);
			Diseases diseases = field.getAnnotation(Diseases.class);
			if (diseases != null) {
				StringBuilder diseasesString = new StringBuilder();
				for (Disease disease : diseases.value()) {
					if (diseasesString.length() > 0)
						diseasesString.append(", ");
					diseasesString.append(disease.toShortString());
				}
				diseasesCell.setCellValue(diseasesString.toString());
			} else {
				diseasesCell.setCellValue("All");
			}

			// outbreak
			XSSFCell outbreakCell = row.createCell(EntityColumn.OUTBREAKS.ordinal());
			if (field.getAnnotation(Outbreaks.class) != null)
				outbreakCell.setCellValue(true);

			//ignored countries
			XSSFCell ignoreForCountriesCell = row.createCell(EntityColumn.IGNORED_COUNTRIES.ordinal());
			HideForCountries hideForCountries = field.getAnnotation(HideForCountries.class);
			if (hideForCountries != null) {
				StringBuilder hideForCountriesString = new StringBuilder();
				for (String country : hideForCountries.countries()) {
					if (hideForCountriesString.length() > 0)
						hideForCountriesString.append(", ");
					hideForCountriesString.append(country);
				}
				ignoreForCountriesCell.setCellValue(hideForCountriesString.toString());
			}

			//exclusive countries
			XSSFCell exclusiveCountriesCell = row.createCell(EntityColumn.EXCLUSIVE_COUNTRIES.ordinal());
			HideForCountriesExcept hideForCountriesExcept = field.getAnnotation(HideForCountriesExcept.class);
			if (hideForCountriesExcept != null) {
				StringBuilder hideForCountriesExceptString = new StringBuilder();
				for (String exceptCountry : hideForCountriesExcept.countries()) {
					if (hideForCountriesExceptString.length() > 0)
						hideForCountriesExceptString.append(", ");
					hideForCountriesExceptString.append(exceptCountry);
				}
				exclusiveCountriesCell.setCellValue(hideForCountriesExceptString.toString());
			}
		}

		AreaReference reference =
			workbook.getCreationHelper().createAreaReference(new CellReference(0, 0), new CellReference(rowNumber - 1, columnCount - 1));
		table.setCellReferences(reference);
		table.getCTTable().addNewAutoFilter();

		// constant facilities
		if (usesFacilityReference) {
			rowNumber = createFacilityTable(sheet, rowNumber + 1, defaultCellStyle);
		}

		// enums
		for (Class<Enum<?>> usedEnum : usedEnums) {
			rowNumber = createEnumTable(sheet, rowNumber + 1, usedEnum);
		}

		return sheet;
	}

	private int createFacilityTable(XSSFSheet sheet, int startRow, CellStyle defaultCellStyle) {

		// Create
		XSSFTable table = sheet.createTable();
		String safeTableName = (sheet.getSheetName() + FacilityReferenceDto.class.getSimpleName().replaceAll("Dto", "")).replaceAll("\\s", "_");
		table.setName(safeTableName);
		table.setDisplayName(safeTableName);
		XssfHelper.styleTable(table, 2);

		int columnCount = EnumColumn.values().length - 1;
		int rowNumber = startRow;

		// header
		XSSFRow headerRow = sheet.createRow(rowNumber++);
		for (EnumColumn column : EnumColumn.values()) {
			if (EnumColumn.SHORT.equals(column)) {
				continue;
			}
			table.addColumn();
			String columnCaption = column.toString();
			columnCaption = columnCaption.charAt(0) + columnCaption.substring(1).toLowerCase();
			headerRow.createCell(column.ordinal()).setCellValue(columnCaption);
		}

		List<String> constantFacilities = Arrays.asList(FacilityDto.OTHER_FACILITY, FacilityDto.NO_FACILITY, FacilityDto.CONFIGURED_FACILITY);
		for (String constantFacility : constantFacilities) {
			XSSFRow row = sheet.createRow(rowNumber++);
			XSSFCell cell;

			cell = row.createCell(EnumColumn.TYPE.ordinal());
			if (constantFacility.equals(constantFacilities.get(0))) {
				cell.setCellValue(FacilityReferenceDto.class.getSimpleName().replaceAll("Dto", ""));
			}

			cell = row.createCell(EnumColumn.VALUE.ordinal());
			cell.setCellValue(FacilityDto.CONFIGURED_FACILITY.equals(constantFacility) ? "<text>" : constantFacility);

			cell = row.createCell(EnumColumn.CAPTION.ordinal());
			String caption = I18nProperties.getPrefixCaption(FacilityDto.I18N_PREFIX, constantFacility);
			cell.setCellValue(caption);

			cell = row.createCell(EnumColumn.DESCRIPTION.ordinal());
			cell.setCellStyle(defaultCellStyle);
			String desc = I18nProperties.getPrefixDescription(FacilityDto.I18N_PREFIX, constantFacility);
			cell.setCellValue(DataHelper.equal(caption, desc) ? "" : desc);
		}

		AreaReference reference =
			new AreaReference(new CellReference(startRow, 0), new CellReference(rowNumber - 1, columnCount - 1), SpreadsheetVersion.EXCEL2007);
		table.setCellReferences(reference);
		table.getCTTable().addNewAutoFilter();

		return rowNumber;
	}

	private enum EnumColumn {
		TYPE,
		VALUE,
		CAPTION,
		DESCRIPTION,
		SHORT
	}

	private int createEnumTable(XSSFSheet sheet, int startRow, Class<Enum<?>> enumType) {

		// Create
		XSSFTable table = sheet.createTable();
		String safeTableName = (sheet.getSheetName() + enumType.getSimpleName()).replaceAll("\\s", "_");
		table.setName(safeTableName);
		table.setDisplayName(safeTableName);
		XssfHelper.styleTable(table, 2);

		int columnCount = EnumColumn.values().length;
		int rowNumber = startRow;

		// header
		XSSFRow headerRow = sheet.createRow(rowNumber++);
		for (EnumColumn column : EnumColumn.values()) {
			table.addColumn();
			String columnCaption = column.toString();
			columnCaption = columnCaption.charAt(0) + columnCaption.substring(1).toLowerCase();
			headerRow.createCell(column.ordinal()).setCellValue(columnCaption);
		}

		Object[] enumValues = enumType.getEnumConstants();
		for (Object enumValueObject : enumValues) {
			XSSFRow row = sheet.createRow(rowNumber++);
			XSSFCell cell;
			Enum<?> enumValue = ((Enum<?>) enumValueObject);

			cell = row.createCell(EnumColumn.TYPE.ordinal());
			if (enumValueObject == enumValues[0]) {
				cell.setCellValue(enumType.getSimpleName());
			}

			cell = row.createCell(EnumColumn.VALUE.ordinal());
			cell.setCellValue(enumValue.name());

			cell = row.createCell(EnumColumn.CAPTION.ordinal());
			String caption = enumValue.toString();
			cell.setCellValue(caption);

			cell = row.createCell(EnumColumn.DESCRIPTION.ordinal());
			String desc = I18nProperties.getEnumDescription(enumValue);
			cell.setCellValue(DataHelper.equal(caption, desc) ? "" : desc);

			cell = row.createCell(EnumColumn.SHORT.ordinal());
			String shortCaption = I18nProperties.getEnumCaptionShort(enumValue);
			cell.setCellValue(DataHelper.equal(caption, shortCaption) ? "" : shortCaption);
		}

		AreaReference reference =
			new AreaReference(new CellReference(startRow, 0), new CellReference(rowNumber - 1, columnCount - 1), SpreadsheetVersion.EXCEL2007);
		table.setCellReferences(reference);
		table.getCTTable().addNewAutoFilter();

		return rowNumber;
	}
}
