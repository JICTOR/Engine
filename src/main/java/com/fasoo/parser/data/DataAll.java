package com.fasoo.parser.data;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

import java.io.File;
import java.util.*;

public class DataAll {

	private BidiMap classInfoList = new DualHashBidiMap();
	private BidiMap fqnList = new DualHashBidiMap();
	private ArrayList<String> addedDiffList = new ArrayList<>();
	
	public DualHashBidiMap getClassInfoList() {
		return (DualHashBidiMap) classInfoList;
	}

	public List<String> getAddedDiffList() {
		return addedDiffList;
	}

	private boolean putInDiffList(String key){
		if(key == null){
			System.out.println("Error : key parameter is null - putInDiffList in DataAll");
			return false;
		}

		if(!addedDiffList.contains(key)){
			addedDiffList.add(key);
			return true;
		}

		System.out.println("Error : key parameter is duplicated - putInDiffList in DataAll");
		return false;
	}

	public void setClassInfoList(BidiMap classInfoList) {
		this.classInfoList.clear();
		this.classInfoList.putAll(classInfoList);
	}

	public DataClassInfo findClassInfoFQN(String FQName){
		String key = findKeyByFQN(FQName);
		if(key == null ){
			return null;
		}else{
			return ((DataClassInfo) classInfoList.get(key));
		}
	}

	public DataClassInfo findClassInfo(File javaFile, String className){
		String javaFilePath = javaFile.getAbsolutePath();

		for(String key : addedDiffList){
			DataClassInfo classInfo = (DataClassInfo) classInfoList.get(key);
			if(classInfo == null) {
				System.out.println("Error : classInfo is null - findClassInfo in DataAll");
				System.out.println("Key : " + key + ", className : " + className + ", javaFile Path : " + javaFilePath);
			}else {
				if (javaFilePath.equals(classInfo.getJavaFilePath()) && className.equals(classInfo.getClassName())) {
					return classInfo;
				}
			}
		}

		for (Object value : classInfoList.values()) {
			DataClassInfo classInfo = (DataClassInfo) value;
			if (javaFilePath.equals(classInfo.getJavaFilePath()) && className.equals(classInfo.getClassName())) {
				return classInfo;
			}
		}

		return null;
	}

	public boolean containsFQN(String fqn){
		return fqnList.containsValue(fqn);
	}
	
	public ArrayList<String> findFQNamesInSamePackage(String packageName){
		ArrayList<String> classInSamePackage = new ArrayList<String>();
		for(Object value : fqnList.values()){
			String fqn = value.toString();
			int lastIndex = fqn.lastIndexOf(".");

			if(packageName == null && lastIndex == -1){
				classInSamePackage.add(fqn);
			}else if(packageName != null && lastIndex != -1){
				if(packageName.equals(fqn.substring(0, lastIndex))){
					classInSamePackage.add(fqn);
				}
			}
		}
		return classInSamePackage;
	}

	public String findFQNByKey(String key){
		String fqn = null;

		if(fqnList.containsKey(key)){
			fqn = fqnList.get(key).toString();
		}

		return fqn;
	}

	public String findKeyByFQN(String FQName){
		if(FQName == null){
			return null;
		}

		Object result = fqnList.getKey(FQName);

		if(result != null){
			return result.toString();
		}else{
			return null;
		}
	}

	public DualHashBidiMap getFqnList(){
		return (DualHashBidiMap) fqnList;
	}

	public void setFQNList(){
		fqnList.clear();
		DataClassInfo tempClassInfo;
		for(Object key : classInfoList.keySet()){
			tempClassInfo = (DataClassInfo) classInfoList.get(key);
			fqnList.put(key, tempClassInfo.getFQName());
		}
	}

	public boolean addClassInfo(String sha1HashCode, DataClassInfo classInfo){
		if(!classInfoList.containsKey(sha1HashCode)) {
			String isDupKey = findKeyByFQN(classInfo.getFQName());
			if(isDupKey != null){
				System.out.println("Error : Duplicate Fully Qualified Name, so delete origin - addClassInfo in DataAll");
				classInfoList.remove(isDupKey);
			}
			classInfoList.put(sha1HashCode, classInfo);
			putInDiffList(sha1HashCode);
			return true;
		}else{
			System.out.println("Error : fail add classInfo due to key duplication - addClassInfo in DataAll");
			return false;
		}
	}

	public boolean putAllinClassInfoList(Map list){
		if(list == null || list.isEmpty()){
			System.out.println("Error : list parameter is null or empty - putAllinClassInfoList in DataAll");
			return false;
		}
		classInfoList.putAll(list);
		for(Object key : list.keySet()){
			putInDiffList(key.toString());
//			fqnList.put(key, classInfoList.get(key));
		}
		return true;
	}
}