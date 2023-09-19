package com.dynamicclass.generator.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dynamicclass.generator.config.DynamicClassGenerator;
import com.dynamicclass.generator.model.ClassName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DynamicClassGeneratorService {
	@Autowired
	private DynamicClassGenerator dynamicClassGenerator;
	private Map<String, Object> dynamicObjects = new HashMap<>();
	
	public Object generateDynamicClasses(List<ClassName> dynamicObjectsList) {
		
		List<ClassName> cnList = dynamicObjectsList;
		return dynamicClassGenerator.generateDynamicClass(cnList, dynamicObjects);
	}
	
	
	public ClassName addClassDetails(ClassName obj) {
		ClassName dbObj = obj;	
		List<ClassName> dynamicObjectsList = new ArrayList<>();
		dynamicObjectsList.add(dbObj);
		try {
			generateDynamicClassGlobal(dynamicObjectsList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dbObj;
	}
	
	
	public void generateDynamicClassGlobal(List<ClassName> dynamicObjectsList)
			throws JsonProcessingException, IllegalArgumentException, IllegalAccessException {
		Object obj = generateDynamicClasses(dynamicObjectsList);
		Class<?> dynamicClass = obj.getClass();
		Field[] fields = dynamicClass.getDeclaredFields();

		for (Field field : fields) {
			field.setAccessible(true); // Make the field accessible (even if it's private)
			String fieldName = field.getName();
			Object fieldValue = field.get(obj); // Get the field value for the instance
		}
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(obj);
	}


}
