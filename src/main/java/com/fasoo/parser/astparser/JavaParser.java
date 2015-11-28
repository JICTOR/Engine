package com.fasoo.parser.astparser;

import com.fasoo.parser.data.DataClassInfo;
import com.fasoo.parser.visitor.VisitorAnnotaionDec;
import com.fasoo.parser.visitor.VisitorEnumDec;
import com.fasoo.parser.visitor.VisitorTypeDec;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import com.fasoo.parser.data.DataAll;
import com.fasoo.parser.data.DataVisit;

import java.io.File;
import java.util.*;

public class JavaParser {
	public void parse(String str, DataAll allData, File javaFile){
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		settingParser(parser);
		parserNodeVisit(parser, allData, javaFile);
	}
	
	private void parserNodeVisit(ASTParser parser, DataAll allData, File javaFile){
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		DataVisit visitData = new DataVisit();

		settingDataVisit(visitData, cu, allData);

		DataClassInfo classInfo;
		AbstractTypeDeclaration absType;

		for(Object classType : cu.types()) {
			absType = (AbstractTypeDeclaration) classType;
			classInfo = allData.findClassInfo(javaFile, absType.getName().toString());

			if (classInfo != null) {

				classInfo.initRelation();

				if (absType.getNodeType() == AbstractTypeDeclaration.TYPE_DECLARATION) {
					VisitorTypeDec.findRelation(allData, classInfo, (TypeDeclaration) absType, visitData, cu);
				} else if (absType.getNodeType() == AbstractTypeDeclaration.ENUM_DECLARATION) {
					VisitorEnumDec.findRelation(allData, classInfo, (EnumDeclaration) absType, visitData, cu);
				} else {
					VisitorAnnotaionDec.findRelation(allData, classInfo, (AnnotationTypeDeclaration) absType, visitData);
				}

				visitData.getInnerClassFQN().clear();
				visitData.getSimpleField().clear();
				visitData.getSimpleLocal().clear();

				classInfo.sortCallPosition();
				classInfo.sortRelationList();
			}
		}
	}

	private void settingDataVisit(DataVisit visitData, CompilationUnit cu, DataAll allData){
		String packageName = null;
		ArrayList<String> importFQNames = new ArrayList<String>();
		ArrayList<String> importStaticFQNames = new ArrayList<String>();
		ArrayList<String> listInSamePackage;
		ImportDeclaration tempImport;

		if(cu.getPackage() != null){
			packageName = cu.getPackage().getName().toString();
		}

		listInSamePackage = allData.findFQNamesInSamePackage(packageName);

		if(!cu.imports().isEmpty()) {
			for (Object importDec : cu.imports()) {
				tempImport = (ImportDeclaration) importDec;
				String importName = tempImport.getName().toString();
				if (tempImport.isOnDemand()) {
					if(tempImport.isStatic()) {
						importStaticFQNames.addAll(allData.findFQNamesInSamePackage(importName));
					}else {
						importFQNames.addAll(allData.findFQNamesInSamePackage(importName));
					}
				} else {
					if(tempImport.isStatic()){
						importStaticFQNames.add(importName);
					}else {
						if (allData.findClassInfoFQN(tempImport.getName().toString()) != null) {
							importFQNames.add(importName);
						}
					}
				}
			}
		}
		visitData.setImportFQNames(importFQNames);
		visitData.setImportStaticFQNames(importStaticFQNames);
		visitData.setListInSamePackage(listInSamePackage);
		visitData.setScope(packageName);
	}

	private void settingParser(ASTParser parser){
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		parser.setCompilerOptions(options);
	}
}