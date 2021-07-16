package de.symeda.sormas.backend.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.utils.Experimental;

public class PatchHelper {

	private static ObjectMapper objectMapper = new ObjectMapper();
	private static Map<Class, List<Field>> classFieldsMap = new HashMap<Class, List<Field>>();

	/**
	 * This method is used to partially update an existing object. It updates an existingObject with the values received in the jsonObject.
	 * The existingObject has to be an object that contains a field UUID - thus an object instance of a class that implements HasUuid
	 * interface
	 * The jsonObject has to contain a field uuid and the existingObject uuid has to match with the uuid field in json.
	 * The jsonObject can contains only the fields that are intended to be updated and the uuid of the object.
	 * If the existingObject contains inner objects:
	 * - a) of type HasUuid - those can be either updated or replaced.
	 * In order to be updated the uuid of the inner object has to be provided and has to be the same as the existing one.
	 * If the uuid differs the inner object is replaced.
	 * - b) not of type HasUuid - are replaced.
	 * !The logic if any inner object or value is allowed to be replaced or not relies entirely on the application!
	 * 
	 * THIS METHOD IS EXPERIMENTAL!!!!
	 * 
	 * @param jsonObject
	 *            - the json payload containing the fields that are to be replaced. The structure of the json matches the structure of the
	 *            Dto given by the existingObject
	 * @param existingObject
	 *            - the existing object whose fields are to be updated
	 * @param <T>
	 *            - the type of the object
	 */
	@Experimental
	public static <T extends HasUuid> void postUpdate(@NotNull JsonNode jsonObject, @NotNull T existingObject) {

		if (!existingObject.getUuid().equals(jsonObject.get(EntityDto.UUID).textValue())) {
			throw new RuntimeException("The updated object must have a Uuid");
		}
		postUpdateOrReplace(jsonObject, existingObject);
	}

	private static <T extends HasUuid> void postUpdateOrReplace(@NotNull JsonNode jsonObject, @NotNull T existingObject) {

		Iterator<Map.Entry<String, JsonNode>> jsonObjectFieldMap = jsonObject.fields();
		while (jsonObjectFieldMap.hasNext()) {
			Map.Entry<String, JsonNode> jsonObjectFieldMapEntry = jsonObjectFieldMap.next();
			JsonNode jsonObjectFieldNode = jsonObjectFieldMapEntry.getValue();
			Field existingObjectField = getClassFieldByName(jsonObjectFieldMapEntry.getKey(), existingObject.getClass());
			existingObjectField.setAccessible(true);

			if (jsonObjectFieldNode.isObject()) {
				updateExistingObject(existingObject, jsonObjectFieldNode, existingObjectField);

			} else if (jsonObjectFieldNode.isArray()) {
				updateObjectList(existingObject, jsonObjectFieldNode, existingObjectField);

			} else {
				setFieldValue(existingObject, existingObjectField, jsonObjectFieldNode);
			}
		}
	}

	/**
	 * This method updates the fields of type collections (lists or array).
	 * 
	 * @param existingObject
	 * @param jsonObjectFieldNode
	 * @param existingObjectField
	 * @param <T>
	 * @throws JsonProcessingException
	 * @throws IllegalAccessException
	 */
	private static <T extends HasUuid> void updateObjectList(T existingObject, JsonNode jsonObjectFieldNode, Field existingObjectField) {
		Class listElementClass = getParameterizedType(existingObjectField);
		List tempNewElementList = new ArrayList();

		for (JsonNode listElement : jsonObjectFieldNode) {
			T updatedObjectList = addOrReplaceListElement(existingObject, existingObjectField, listElementClass, listElement);
			if (updatedObjectList != null) {
				tempNewElementList.add(updatedObjectList);
			}
		}

		if (existingObjectField.getType().isAssignableFrom(List.class)) {
			Object existingObjectFieldInstance = null;
			try {
				existingObjectFieldInstance = existingObjectField.get(existingObject);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(
					"Cannot access field " + existingObjectField.getName() + " on the object of type: " + existingObject.getClass().getName());
			}
			if (existingObjectFieldInstance != null) {
				Collection existingElementList = (Collection) existingObjectFieldInstance;
				existingElementList.clear();
				existingElementList.addAll(tempNewElementList);
			} else {
				try {
					existingObjectField.set(existingObject, tempNewElementList);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					throw new RuntimeException(
						"Cannot access field " + existingObjectField.getName() + " on the object of type: " + existingObject.getClass().getName());
				}
			}
		} else if (existingObjectField.getType().isArray()) {
			try {
				existingObjectField.set(existingObject, tempNewElementList.toArray());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException(
					"Cannot access field " + existingObjectField.getName() + " on the object of type: " + existingObject.getClass().getName());
			}
		} else {
			throw new RuntimeException(
				"The list of objects expect to have a type that inherits from Collection or to be an array! The current list object field "
					+ existingObjectField.getName() + " has a type of: " + existingObjectField.getType().getName());
		}
	}

	/**
	 * This method update an existing object field. If the existingObject field is NULL a new instance is created.
	 * If the existing object field is not NULL the postUpdate method is called on the object field
	 * 
	 * @param existingObject
	 * @param jsonObjectFieldNode
	 * @param existingObjectField
	 * @param <T>
	 * @throws IllegalAccessException
	 * @throws JsonProcessingException
	 */
	private static <T extends HasUuid> void updateExistingObject(T existingObject, JsonNode jsonObjectFieldNode, Field existingObjectField) {
		T existingFieldValue = null;
		try {
			existingFieldValue = (T) existingObjectField.get(existingObject);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(
				"Cannot access field " + existingObjectField.getName() + " on the object of type: " + existingObject.getClass().getName());
		}
		if (existingFieldValue == null) {
			setFieldValue(existingObject, existingObjectField, jsonObjectFieldNode);
		} else {
			postUpdateOrReplace(jsonObjectFieldNode, existingFieldValue);
		}
	}

	/**
	 * Add or replace the element in the list. The search is based on the UUID of the object. If an object with the given UUID is find in
	 * the list it is patched as all the other objects.
	 * If there is no element with that UUID in the list the new element is added. This method is specific to the project, For the object
	 * which re not instances of HasUuid (dont have an uuid),
	 * all the elements are replaced with the values form json
	 * 
	 * @param existingObject
	 * @param existingObjectField
	 * @param listElementClass
	 * @param listElement
	 * @param <T>
	 * @throws JsonProcessingException
	 */
	private static <T extends HasUuid> T addOrReplaceListElement(
		T existingObject,
		Field existingObjectField,
		Class listElementClass,
		JsonNode listElement) {
		if (HasUuid.class.isAssignableFrom(listElementClass) && listElement.hasNonNull(EntityDto.UUID)) {
			T existingListElement = (T) getListElementByUuid(existingObject, existingObjectField, listElement.get(EntityDto.UUID).textValue());
			if (existingListElement != null) {
				postUpdate(listElement, existingListElement);
				//tempNewElementList.add(existingListElement);
				return existingListElement;
			} else {
				//addObjectToList(tempNewElementList, listElement, listElementClass);
				return (T) getObjectFromJsonNode(listElement, listElementClass);
			}

		} else {
			//addObjectToList(tempNewElementList, listElement, listElementClass);
			return (T) getObjectFromJsonNode(listElement, listElementClass);
		}
	}

	/**
	 * Replace the field value of the existing with the value from the json
	 * 
	 * @param existingObject
	 * @param existingField
	 * @param fieldJson
	 * @param <T>
	 * @throws IllegalAccessException
	 * @throws JsonProcessingException
	 */
	private static <T> void setFieldValue(@NotNull T existingObject, @NotNull Field existingField, JsonNode fieldJson) {
		Object fieldValue = null;

		fieldValue = getObjectFromJsonNode(fieldJson, existingField.getType());
		try {
			existingField.set(existingObject, fieldValue);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(
				"Cannot access field " + existingField.getName() + " on the object of type: " + existingObject.getClass().getName());
		}

	}

	/**
	 * Deserialized the json to the java object. The method treats the special case of enumeration and also the case when the json is null
	 * 
	 * @param fieldJson
	 * @param objectClass
	 * @return
	 * @throws JsonProcessingException
	 */
	private static Object getObjectFromJsonNode(JsonNode fieldJson, @NotNull Class objectClass) {
		Object object = null;
		if (!fieldJson.isNull()) {

			if (objectClass.isEnum()) {
				object = Enum.valueOf((Class<Enum>) objectClass, fieldJson.textValue());
			} else {
				try {
					object = objectMapper.treeToValue(fieldJson, objectClass);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					throw new RuntimeException("Cannot covert JSon" + fieldJson.asText() + " to object of class: " + objectClass.getName());
				}
			}
		}
		return object;
	}

	/**
	 * 
	 * @param list
	 * @param objectJson
	 * @param objectClass
	 * @throws JsonProcessingException
	 */
	private static void addObjectToList(@NotNull List list, JsonNode objectJson, @NotNull Class objectClass) {
		Object object = getObjectFromJsonNode(objectJson, objectClass);
		if (object != null) {
			list.add(object);
		}
	}

	private static @NotNull Field getClassFieldByName(@NotNull String fieldName, @NotNull Class objectClass) {
		if (classFieldsMap.get(objectClass) == null) {
			getAllClassFields(objectClass);
		}
		Field requiredField = classFieldsMap.get(objectClass).stream().filter(field -> field.getName().equals(fieldName)).findAny().orElse(null);
		if (requiredField == null) {
			throw new RuntimeException("No such field exception! " + fieldName + " in the class:" + objectClass.getName());
		}

		return requiredField;
	}

	private static void getAllClassFields(@NotNull Class objectClass) {
		Class<?> currentClass = objectClass;
		List<Field> fieldList = new ArrayList<Field>();
		while (currentClass.getSuperclass() != null) {
			fieldList.addAll(Arrays.asList(currentClass.getDeclaredFields()));
			currentClass = currentClass.getSuperclass();
		}
		classFieldsMap.put(objectClass, fieldList);
	}

	/**
	 * Returns the parameterized type of the field
	 * 
	 * @param field
	 * @return
	 */
	private static Class getParameterizedType(@NotNull Field field) {
		Type genericFieldType = field.getGenericType();
		Class listElementClass = Object.class;
		if (genericFieldType instanceof ParameterizedType) {
			ParameterizedType aType = (ParameterizedType) genericFieldType;
			Type[] fieldArgTypes = aType.getActualTypeArguments();
			listElementClass = (Class) fieldArgTypes[0];
		}

		return listElementClass;
	}

	/**
	 * Returns the element from the arrayField that have the given uuid
	 * 
	 * @param obj
	 *            - the object that contains the arrayField
	 * @param arrayField
	 *            - the arrayField
	 * @param uuid
	 *            - the given uuid
	 * @return
	 */
	private static <T extends HasUuid> T getListElementByUuid(@NotNull Object obj, @NotNull Field arrayField, @NotNull String uuid) {
		try {
			return (T) ((Collection) arrayField.get(obj)).stream()
				.filter(listElement -> ((HasUuid) listElement).getUuid().equals(uuid))
				.findFirst()
				.orElse(null);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

}
