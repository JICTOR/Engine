package com.fasoo.parser.data;

import java.util.ArrayList;

public class DataVariable {
	private String classFQName;
	private String name;
	private ArrayList<String> modifiers = new ArrayList<String>();
	private String typeString;

	public String getFullDeclaration(){
		String full = typeString + " " + name;

		if(!modifiers.isEmpty()) {
			for (String modifier : modifiers) {
				full = modifier + " " + full;
			}
		}

		return full;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<String> getModifiers() {
		return modifiers;
	}
	public String getTypeString() {
		return typeString;
	}
	public void setTypeString(String typeString) {
		this.typeString = typeString;
	}
	public String getClassFQName() {
		return classFQName;
	}
	public void setClassFQName(String classFQName) {
		this.classFQName = classFQName;
	}
}