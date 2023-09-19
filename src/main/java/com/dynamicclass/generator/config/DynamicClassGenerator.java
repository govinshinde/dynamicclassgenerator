package com.dynamicclass.generator.config;

import javassist.*;

import org.springframework.stereotype.Component;

import com.dynamicclass.generator.model.ClassName;
import com.dynamicclass.generator.model.ClassProperty;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class DynamicClassGenerator {
	Map<String, CtClass> fieldTypeMapping = new HashMap<>();
	private static final String PACKAGE = "com.dynamicclass.generator.model";
	private static final String CLASS = "Test";

    public Object generateDynamicClass(List<ClassName> cnList, Map<String, Object> dynamicObjects) {
        Object dynamicObject = null;
        try {
            String packageName = PACKAGE;
            String className = null;
            List<FieldDefinition> fieldDefinitions = new ArrayList<>();

            for (ClassName cn : cnList) {
                className = cn.getClassName();

                for (ClassProperty cp : cn.getClassProperty()) {
                    FieldDefinition fd = new FieldDefinition(cp.getFieldName(), null, cp.getFieldType());
                    fd.setTypeFromString(cp.getFieldType());
                    fieldDefinitions.add(fd);
                }

                dynamicObject = getOrCreateClass(packageName, className, fieldDefinitions);
                dynamicObjects.put(className, dynamicObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dynamicObject;
    }

    private String generateToStringMethod(String className, List<FieldDefinition> fieldDefinitions) {
        StringBuilder toStringMethodBody = new StringBuilder();
        toStringMethodBody.append("public String toString() { return \"")
                .append(className)
                .append("(");

        for (int i = 0; i < fieldDefinitions.size(); i++) {
            FieldDefinition fd = fieldDefinitions.get(i);
            toStringMethodBody.append(fd.getName())
                    .append("=\" + ")
                    .append(fd.getName());

            if (i < fieldDefinitions.size() - 1) {
                toStringMethodBody.append(" + \", \" + \"");
            }
        }

        toStringMethodBody.append("+\")\"; }");
        return toStringMethodBody.toString();
    }

    private Object getOrCreateClass(String packageName, String className, List<FieldDefinition> fieldDefinitions) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass dynamicClass;

            try {
                dynamicClass = pool.get(packageName + "." + className);
            } catch (NotFoundException notFoundEx) {
                dynamicClass = pool.makeClass(packageName + "." + className);
            }
            fieldTypeMapping.put("java.lang.Integer",  pool.get("java.lang.Integer"));
        	fieldTypeMapping.put("java.lang.Boolean", pool.get("java.lang.Boolean"));
        	fieldTypeMapping.put("java.lang.Double", pool.get("java.lang.Double"));
        	fieldTypeMapping.put("java.lang.String", pool.get("java.lang.String"));

            for (FieldDefinition fieldDef : fieldDefinitions) {
                if (!fieldExists(dynamicClass, fieldDef.getName())) {
                    // Add the new field to the class
                    dynamicClass.defrost();
                    CtField field = new CtField(fieldTypeMapping.get(fieldDef.getFieldType()), fieldDef.getName(), dynamicClass);
                    dynamicClass.addField(field);
                    String fieldName = fieldDef.getName();
                    CtMethod getter = CtNewMethod.make("public " + fieldDef.getType().getName() + " get" + capitalize(fieldName) + "() { return " + fieldName + "; }", dynamicClass);
                    CtMethod setter = CtNewMethod.make("public void set" + capitalize(fieldName) + "(" + fieldDef.getType().getName() + " value) { " + fieldName + " = value; }", dynamicClass);
                    dynamicClass.addMethod(getter);
                    dynamicClass.addMethod(setter);
                }
            }

            // Generate the toString method using Javassist's API
            String toStringMethodBody = generateToStringMethod(className, fieldDefinitions);
            try {
            	CtMethod toStringMethod = CtNewMethod.make(toStringMethodBody, dynamicClass);
                dynamicClass.addMethod(toStringMethod);
            } catch(Exception e) {
            	
            }            

            ClassLoader classLoader2 = Thread.currentThread().getContextClassLoader(); // Get the current class loader
            // Set the class loader for the ClassPool
            pool.insertClassPath(new LoaderClassPath(classLoader2));
            
            CtClass dynamicClass3 = pool.get(PACKAGE + ".Parent");
            try {
            	dynamicClass.setSuperclass(dynamicClass3);
            } catch(Exception e) {
            	
            }
            
            try {
            	 for (FieldDefinition fieldDef : fieldDefinitions) {
                     if (!fieldExists(dynamicClass3, fieldDef.getName())) {
                         // Add the new field to the class
                         dynamicClass3.defrost();
                         CtField field = new CtField(fieldTypeMapping.get(fieldDef.getFieldType()), fieldDef.getName(), dynamicClass3);
                         dynamicClass3.addField(field);
                         String fieldName = fieldDef.getName();
                         CtMethod getter = CtNewMethod.make("public " + fieldDef.getType().getName() + " get" + capitalize(fieldName) + "() { return " + fieldName + "; }", dynamicClass3);
                         CtMethod setter = CtNewMethod.make("public void set" + capitalize(fieldName) + "(" + fieldDef.getType().getName() + " value) { " + fieldName + " = value; }", dynamicClass3);
                         dynamicClass3.addMethod(getter);
                         dynamicClass3.addMethod(setter);
                     }
                 }
            } catch (Exception e) {
            	System.err.println("Exception occurred while adding getters setters");
            }
            
            try {
            	toStringMethodBody = generateToStringMethod(CLASS, fieldDefinitions);
            	CtMethod toStringMethod = CtNewMethod.make(toStringMethodBody, dynamicClass3);
                dynamicClass3.addMethod(toStringMethod);
            } catch(Exception e) {
            	
            }   
           


            if (true) {
                // Save the newly generated class to the class file
                byte[] bytecode = dynamicClass.toBytecode();
                saveToClassFile(className, bytecode);
                byte[] bytecode2 = dynamicClass3.toBytecode();
                saveToClassFile("Test", bytecode2);
            }

            DynamicClassLoaderLocal classLoader = new DynamicClassLoaderLocal(classLoader2);
            Class<?> cClass = classLoader.loadClass(packageName + "." + className, dynamicClass.toBytecode());

            // Create an instance of the dynamically generated class
            Constructor<?> constructor = cClass.getDeclaredConstructor();
            Object dynamicObject = constructor.newInstance();

            return dynamicObject;
        } catch (Exception e) {
            System.err.println("Exception occurred before returning dynamic object");
            return null;
        }
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static boolean fieldExists(CtClass ctClass, String fieldName) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();
            for (CtField field : fields) {
                if (field.getName().equals(fieldName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void saveToClassFile(String className, byte[] bytecode) {
        try (FileOutputStream fos = new FileOutputStream("target\\classes\\com\\dynamicclass\\generator\\model\\" + className + ".class")) {
            fos.write(bytecode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class DynamicClassLoaderLocal extends ClassLoader {
        public DynamicClassLoaderLocal(ClassLoader parent) {
            super(parent);
        }

        public Class<?> loadClass(String name, byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }

    class FieldDefinition {
        private String name;
        private Class<?> type;
        private String fieldType;

        public FieldDefinition(String name, Class<?> type, String fieldType) {
            this.name = name;
            this.type = type;
            this.fieldType = fieldType;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }

        public String getFieldType() {
            return fieldType;
        }

        public void setTypeFromString(String typeName) throws ClassNotFoundException {
            this.type = Class.forName(typeName);
        }
    }

    private void updateInterfaceMethods(Object dynamicObject, String interfaceClassName) {
        try {
            Class<?> dynamicClass = dynamicObject.getClass();
            Class<?> interfaceClass = Class.forName(interfaceClassName);

            // Get methods from the dynamic class
            Method[] dynamicMethods = dynamicClass.getDeclaredMethods();

            for (Method dynamicMethod : dynamicMethods) {
                // Check if the method is not already present in the interface
                if (!methodExists(interfaceClass, dynamicMethod.getName(), dynamicMethod.getParameterTypes())) {
                    // Add the method to the interface
                    CtClass ctInterface = ClassPool.getDefault().get(interfaceClassName);
                    String methodReturnType = dynamicMethod.getReturnType().getName();
                    Class<?>[] parameterTypes = dynamicMethod.getParameterTypes();

                    // Build the method signature without a body
                    StringBuilder methodSignature = new StringBuilder();
                    methodSignature.append("public abstract ")
                            .append(methodReturnType)
                            .append(" ")
                            .append(dynamicMethod.getName())
                            .append("(");
                    for (int i = 0; i < parameterTypes.length; i++) {
                        CtClass ctParamType = ClassPool.getDefault().get(parameterTypes[i].getName());
                        methodSignature.append(ctParamType.getName());
                        methodSignature.append(" arg").append(i);
                        if (i < parameterTypes.length - 1) {
                            methodSignature.append(", ");
                        }
                    }
                    methodSignature.append(");");

                    CtMethod ctMethod = CtMethod.make(methodSignature.toString(), ctInterface);
                    ctInterface.addMethod(ctMethod);

                    // Print a message indicating that the method was added
                    System.out.println("Added method to interface: " + dynamicMethod.getName());
                }
            }
            try {
            	ClassPool pool = ClassPool.getDefault();
                CtClass ct = pool.get("com.ict.model" + "." + interfaceClassName);
                byte[] bytecode = ct.toBytecode();
                saveToClassFile(interfaceClassName, bytecode);
            } catch (NotFoundException notFoundEx) {   
            	System.err.println("exception occurred");
            }
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean methodExists(Class<?> interfaceClass, String methodName, Class<?>[] parameterTypes) {
        try {
            Method method = interfaceClass.getMethod(methodName, parameterTypes);
            return method != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}