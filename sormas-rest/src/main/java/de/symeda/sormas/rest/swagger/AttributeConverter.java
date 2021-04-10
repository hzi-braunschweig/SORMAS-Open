package de.symeda.sormas.rest.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.utils.*;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AttributeConverter extends ModelResolver {

	public static final String XPROP_PREFIX = "x-sormas-";
	public static final String XPROP_PERSONAL_DATA = XPROP_PREFIX + "personal-data";
	public static final String XPROP_FOR_COUNTRIES = XPROP_PREFIX + "countries-for";
	public static final String XPROP_EXCEPT_COUNTRIES = XPROP_PREFIX + "countries-except";
	public static final String XPROP_DEPENDS_ON = XPROP_PREFIX + "depends-on";
	public static final String XPROP_DISEASES = XPROP_PREFIX + "diseases";
	public static final String XPROP_OUTBREAKS = XPROP_PREFIX + "outbreaks";
	public static final String XPROP_COMPLICATIONS = XPROP_PREFIX + "complications";

	public AttributeConverter(ObjectMapper mapper) {
		super(mapper);
	}

	@Override
	public Schema<?> resolve(AnnotatedType annotatedType, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
		Schema schema = super.resolve(annotatedType, modelConverterContext, iterator);

		if (schema != null && annotatedType.getCtxAnnotations() != null && annotatedType.isSchemaProperty()) {

			for (Annotation a : annotatedType.getCtxAnnotations()) {
				if (a.annotationType() == PersonalData.class) {
					// Outbreak visibility documentation
					schema.addExtension(XPROP_PERSONAL_DATA, true);

				} else if (a.annotationType() == Outbreaks.class) {
					// Complications documentation
					schema.addExtension(XPROP_OUTBREAKS, true);

				} else if (a.annotationType() == Complication.class) {
					// Complications documentation
					schema.addExtension(XPROP_COMPLICATIONS, true);

				} else if (a.annotationType() == Diseases.class) {
					// Disease association documentation
					Disease[] diseases = ((Diseases) a).value();
					if (diseases.length > 0) {
						schema.addExtension(XPROP_DISEASES, diseases);
					}

				} else if (a.annotationType() == DependantOn.class) {
					// Field dependency documentation
					schema.addExtension(XPROP_DEPENDS_ON, ((DependantOn) a).value());

				} else if (a.annotationType() == HideForCountries.class) {
					// Documentation of countries to hide the field from
					if (schema.getExtensions() != null) {
						schema.getExtensions().remove(XPROP_FOR_COUNTRIES);
					}
					schema.addExtension(XPROP_EXCEPT_COUNTRIES, ((HideForCountries) a).countries());

				} else if (a.annotationType() == HideForCountriesExcept.class) {
					// Documentation of countries to show field for
					if (schema.getExtensions() == null || !schema.getExtensions().containsKey(XPROP_EXCEPT_COUNTRIES)) {
						schema.addExtension(XPROP_FOR_COUNTRIES, ((HideForCountriesExcept) a).countries());
					}
				}
			}
		}

		return schema;

	}
}
