package com.dynamicclass.generator.model;

import lombok.Data;

@Data
public class ClassProperty {
	private Integer cpid;
	private String fieldName;
	private String fieldType; 
	private ClassName className;
}
