package com.fasoo.parser.data;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DataVisit {
	private ArrayList<String> importFQNames;
	private ArrayList<String> importStaticFQNames;
	private ArrayList<String> listInSamePackage;
	private ArrayList<FieldDeclaration> simpleField = new ArrayList<FieldDeclaration>();
	private ArrayList<VariableDeclarationStatement> simpleLocal = new ArrayList<VariableDeclarationStatement>();
	private Set<String> innerClassFQN = new HashSet<String>();

	private String scope;

	public String expandScope(String unitName){
		if(scope != null) {
			scope += "." + unitName;
		}else{
			scope = unitName;
		}
		return scope;
	}

	public String reduceScope(){
		if(scope.contains(".")) {
			scope = scope.substring(0, scope.lastIndexOf("."));
		}else{
			scope = null;
		}
		return scope;
	}

	public String findFQNbySimpleName(String simpleName){
		if(importFQNames == null || simpleName == null){
			return null;
		}

		String simple;

		for(String fqn : innerClassFQN){
			simple = fqn.substring(fqn.lastIndexOf(".") + 1, fqn.length());

			if(simpleName.equals(simple)){
				return fqn;
			}
		}

		for(String fqn : importFQNames){
			simple = fqn.substring(fqn.lastIndexOf(".") + 1, fqn.length());

			if(simpleName.equals(simple)){
				return fqn;
			}
		}

		for(String fqn : listInSamePackage){
			simple = fqn.substring(fqn.lastIndexOf(".") + 1, fqn.length());

			if(simpleName.equals(simple)){
				return fqn;
			}
		}

		for(String fqn : importStaticFQNames){
			simple = fqn.substring(fqn.lastIndexOf(".") + 1, fqn.length());

			if(simpleName.equals(simple)){
				return fqn;
			}
		}

		return null;
	}
	public FieldDeclaration findFieldBySimpleName(String simpleName){
		for(FieldDeclaration field : simpleField){
			for(Object o : field.fragments()){
				VariableDeclarationFragment frag = (VariableDeclarationFragment) o;
				if(simpleName.equals(frag.getName().toString())){
					return field;
				}
			}
		}
		return null;
	}
	public VariableDeclarationStatement findLocalBySimpleName(String simpleName){
		for(VariableDeclarationStatement local : simpleLocal){
			for(Object o : local.fragments()){
				VariableDeclarationFragment frag = (VariableDeclarationFragment) o;
				if(simpleName.equals(frag.getName().toString())){
					return local;
				}
			}
		}
		return null;
	}

	public ArrayList<String> getImportFQNames() {
		return importFQNames;
	}
	public void setImportFQNames(ArrayList<String> importFQNames) {
		this.importFQNames = importFQNames;
	}

	public ArrayList<String> getImportStaticFQNames() {
		return importStaticFQNames;
	}
	public void setImportStaticFQNames(ArrayList<String> importStaticFQNames) {
		this.importStaticFQNames = importStaticFQNames;
	}

	public ArrayList<String> getListInSamePackage() {
		return listInSamePackage;
	}
	public void setListInSamePackage(ArrayList<String> listInSamePackage) {
		this.listInSamePackage = listInSamePackage;
	}

	public ArrayList<FieldDeclaration> getSimpleField() {
		return simpleField;
	}

	public ArrayList<VariableDeclarationStatement> getSimpleLocal() {
		return simpleLocal;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getScope() {
		return scope;
	}

	public Set<String> getInnerClassFQN() {
		return innerClassFQN;
	}
}
