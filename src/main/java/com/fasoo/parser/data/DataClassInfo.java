package com.fasoo.parser.data;

import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class DataClassInfo {

	public static final int CLASS = 1;
	public static final int INTERFACE = 2;
	public static final int ENUM = 3;
	public static final int ANNOTATION = 4;

	public static final int SUPER_CLASS = 11;
	public static final int SUPER_INTERFACE = 12;
	public static final int RELATION_FIELD_VAR = 13;
	public static final int RELATION_METHOD_RETURN = 14;
	public static final int RELATION_METHOD_PARAMETER = 15;
	public static final int RELATION_METHOD_LOCAL_VAR = 16;
	public static final int RELATION_STATIC = 17;
	public static final int RELATION_ANNOTATION = 18;

	ArrayList<DataPosition> callPositions = new ArrayList<DataPosition>();

	private String packageName;
	private String className;

	private int lineCount;

	private ArrayList<DataVariable> listFieldVariable = new ArrayList<DataVariable>();
	private ArrayList<DataMethod> listMethod = new ArrayList<DataMethod>();

	private String javaFileName;
	private String javaFilePath;

//	private ArrayList<String> classInSamePackage = new ArrayList<>();

	private String superClass = null;
	private ArrayList<String> superInterfaces = new ArrayList<>();

	private int classType;

	private ArrayList<String> relationFieldVar = new ArrayList<>();
	private ArrayList<String> relationMethodReturn = new ArrayList<>();
	private ArrayList<String> relationMethodLocalVar = new ArrayList<>();
	private ArrayList<String> relationStatic = new ArrayList<>();
	private ArrayList<String> relationAnnotation = new ArrayList<>();
	private ArrayList<String> relationMethodParameter = new ArrayList<>();

	public DataVariable findFieldVar(String varName){
		for(DataVariable var : listFieldVariable){
			if(varName.equals(var.getName())){
				return var;
			}
		}
		return null;
	}

	public String getFQName(){
		if(className != null && packageName != null){
			return (packageName + "." + className);
		}else if(className != null && packageName == null){
			return className;
		}else{
			return null;
		}
	}

	public ArrayList<DataVariable> getListFieldVariable() {
		return listFieldVariable;
	}
	public ArrayList<DataMethod> getListMethod() {
		return listMethod;
	}

//	public ArrayList<String> getClassInSamePackage() {
//		return classInSamePackage;
//	}

	public String getSuperClass() {
		return superClass;
	}

	public ArrayList<String> getSuperInterfaces() {
		return superInterfaces;
	}

	public ArrayList<String> getRelationFieldVar() {
		return relationFieldVar;
	}

	public ArrayList<String> getRelationMethodReturn() {
		return relationMethodReturn;
	}

	public ArrayList<String> getRelationMethodLocalVar() {
		return relationMethodLocalVar;
	}

	public ArrayList<String> getRelationStatic() {
		return relationStatic;
	}

	public ArrayList<String> getRelationAnnotation() {
		return relationAnnotation;
	}

	public ArrayList<String> getRelationMethodParameter() {
		return relationMethodParameter;
	}

	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getJavaFileName() {
		return javaFileName;
	}
	public void setJavaFileName(String javaFileName) {
		this.javaFileName = javaFileName;
	}
	public String getJavaFilePath() {
		return javaFilePath;
	}

	public void setJavaFilePath(String javaFilePath) {
		this.javaFilePath = javaFilePath;
	}

	public int getClassType() {
		return classType;
	}
	public void setClassType(int classType) {
		this.classType = classType;
	}

	private boolean addCallPosition(String fqn, int callPosition, int lengthSimpleName, int relationType){
		for(DataPosition position : callPositions){
			if(position.getCallPosition() == callPosition){
				return false;
			}
		}
		DataPosition newPosition = new DataPosition();
		newPosition.setRelationType(relationType);
		newPosition.setFqn(fqn);
		newPosition.setSimpleNamelength(lengthSimpleName);
		newPosition.setCallPosition(callPosition);

		callPositions.add(newPosition);

		return true;
	}

	public void setSuperClass(String fqn, int callPosition, int lengthSimpleName) {
		if(fqn == null){
			return;
		}
		addCallPosition(fqn, callPosition, lengthSimpleName, SUPER_CLASS);

		this.superClass = fqn;
	}

	public boolean addSuperInterface(String fqn, int callPosition, int lengthSimpleName){
		return addRelation(fqn, callPosition, lengthSimpleName, superInterfaces, SUPER_INTERFACE);
	}

	public boolean addRelationFieldVar(String fqn, int callPosition, int lengthSimpleName){
		return addRelation(fqn, callPosition, lengthSimpleName, relationFieldVar, RELATION_FIELD_VAR);
	}

	public boolean addRelationMethodLocalVar(String fqn, int callPosition,  int lengthSimpleName){
		return addRelation(fqn, callPosition, lengthSimpleName, relationMethodLocalVar, RELATION_METHOD_LOCAL_VAR);
	}

	public boolean addRelationStatic(String fqn, int callPosition, int lengthSimpleName){
		return addRelation(fqn, callPosition, lengthSimpleName, relationStatic, RELATION_STATIC);
	}

	public boolean addRelationMethodReturn(String fqn, int callPosition, int lengthSimpleName){
		return addRelation(fqn, callPosition, lengthSimpleName, relationMethodReturn, RELATION_METHOD_RETURN);
	}

	public boolean addRelationAnnotation(String fqn, int callPosition, int lengthSimpleName){
		return addRelation(fqn, callPosition, lengthSimpleName, relationAnnotation, RELATION_ANNOTATION);
	}

	public boolean addRelationParameter(String fqn, int callPosition, int lengthSimpleName){
		return addRelation(fqn, callPosition, lengthSimpleName, relationMethodParameter, RELATION_METHOD_PARAMETER);
	}

	private boolean addRelation(String fqn, int callPosition, int lengthSimpleName, ArrayList<String> relationList, int relationType){
		if(fqn == null){
			return false;
		}
		if(addCallPosition(fqn, callPosition, lengthSimpleName, relationType) && !relationList.contains(fqn)) {
			relationList.add(fqn);
			return true;
		}else{
			return false;
		}
	}

	public int getLineCount() {
		return lineCount;
	}
	public void setLineCount(int lineCount) {
		this.lineCount = lineCount;
	}

	public void sortCallPosition(){
		Collections.sort(callPositions);
	}

	public ArrayList<DataPosition> getCallPositions() {
		return callPositions;
	}

	public void sortRelationList(){
		Collections.sort(relationAnnotation);
		Collections.sort(relationFieldVar);
		Collections.sort(relationMethodLocalVar);
		Collections.sort(relationMethodParameter);
		Collections.sort(relationMethodReturn);
		Collections.sort(relationStatic);
//		Collections.sort(classInSamePackage);
	}

	public void initRelation(){
		callPositions.clear();
		listFieldVariable.clear();
		listMethod.clear();
		superClass = null;
		superInterfaces.clear();
		relationAnnotation.clear();
		relationStatic.clear();
		relationFieldVar.clear();
		relationMethodReturn.clear();
		relationMethodParameter.clear();
		relationMethodLocalVar.clear();
	}
}