package com.fasoo.parser.data;

import java.util.ArrayList;

public class DataMethod {
	private String classFQName;
	private String returnType;
	private String methodName;
	private ArrayList<DataParameter> parameters = new ArrayList<DataParameter>();
	private ArrayList<String> modifiers = new ArrayList<String>();

	public String getFullDeclaration(){
		String full = returnType + " " + methodName;

		if(!modifiers.isEmpty()) {
			for (String modifier : modifiers) {
				full = modifier + " " + full;
			}
		}

		full += "(";
		if(!parameters.isEmpty()) {
			for (DataParameter parameter : parameters) {
				full += (parameter.getFullDeclaration() + ", ");
			}
			full = full.substring(0, full.lastIndexOf(" ") - 1);
		}
		full +=")";

		return full;
	}

	public String getClassFQName() {
		return classFQName;
	}
	public void setClassFQName(String classFQName) {
		this.classFQName = classFQName;
	}

	public String getReturnType() {
		return returnType;
	}
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public ArrayList<DataParameter> getParameters() {
		return parameters;
	}

	public ArrayList<String> getModifiers() {
		return modifiers;
	}
}
