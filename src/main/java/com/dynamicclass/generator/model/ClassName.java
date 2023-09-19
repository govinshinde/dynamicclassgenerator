package com.dynamicclass.generator.model;

import java.util.List;

import lombok.Data;

@Data
public class ClassName {
	private Integer cnid;
	private String className;
	private List<ClassProperty> classProperty;
}
