package com.dynamicclass.generator.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dynamicclass.generator.model.ClassName;
import com.dynamicclass.generator.service.DynamicClassGeneratorService;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@CrossOrigin
public class DynamicClassGeneratorController {
	@Autowired
	private DynamicClassGeneratorService dynamicClassGeneratorService;
	
	private String generateDynamicClass(ClassName cn) {
		try {
			List<ClassName> dynamicObjectsList = new ArrayList<>();
			dynamicObjectsList.add(cn);
			Object obj = dynamicClassGeneratorService.generateDynamicClasses(dynamicObjectsList);
			Class<?> dynamicClass = obj.getClass();
			Field[] fields = dynamicClass.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true); // Make the field accessible (even if it's private)
				String fieldName = field.getName();
				Object fieldValue = field.get(obj); // Get the field value for the instance
				field.set(obj, 45.8);
				System.out.println("Field Name: " + fieldName);
				System.out.println("Field Value: " + fieldValue);
			}
			ObjectMapper objectMapper = new ObjectMapper();
			String json = objectMapper.writeValueAsString(obj);
			return json;
		} catch (Exception e) {
			System.err.println("Exception occured while generating dynamic class");
		}
		return "failure";
	}
	
	@PostMapping("/addclass")
	public ResponseEntity<ClassName> addClassDetails(@RequestBody ClassName obj) {
		ClassName objRes = dynamicClassGeneratorService.addClassDetails(obj);
		try {
			generateDynamicClass(obj);
		} catch(Exception e) {
			System.err.println("Exception occured while adding class properties " + e);
		}
		return new ResponseEntity<ClassName>(objRes, HttpStatus.OK);
	}

}
