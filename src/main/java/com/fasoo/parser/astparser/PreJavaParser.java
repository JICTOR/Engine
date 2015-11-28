package com.fasoo.parser.astparser;

import com.fasoo.parser.Parsing;
import com.fasoo.parser.data.DataAll;
import com.fasoo.parser.data.DataClassInfo;
import com.fasoo.parser.gitTest.GitDiffVO;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by hwan on 2015-07-09.
 */
public class PreJavaParser {
    public void preParse(String str, DataAll allData, File javaFile) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(str.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        settingParser(parser);
        if(!allData.putAllinClassInfoList(createDataClassInfo(parser, javaFile))){
            System.out.println("@@ Error : This File doesn't have Class - " + javaFile.getAbsolutePath());
        }
    }

    public void commitParse(DataAll allData, GitDiffVO diff) throws IOException{

        if( (diff.getOldFile() == null) && (diff.getNewFile() == null) ){
            System.out.println("Error : Not Exist both Old and New Files - changeCommitInfo in Parsing");
            return;
        }

        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        settingParser(parser);

        File javaFile;

        if(diff.getType() == DiffEntry.ChangeType.ADD){
            javaFile = diff.getNewFile();
            parser.setSource(Parsing.fileToString(new FileInputStream(javaFile)).toCharArray());
            allData.putAllinClassInfoList(createDataClassInfo(parser, javaFile));
        }else if(diff.getType() == DiffEntry.ChangeType.DELETE){
            javaFile = diff.getOldFile();
            parser.setSource(Parsing.fileToString(new FileInputStream(javaFile)).toCharArray());
            for(Object key : createDataClassInfo(parser, javaFile).keySet()) {
                allData.getClassInfoList().remove(key);
//                allData.getFqnList().remove(key);
            }
        }else if(diff.getType() == DiffEntry.ChangeType.MODIFY){
            DualHashBidiMap oldList;
            DualHashBidiMap newList;

            javaFile = diff.getNewFile();
            parser.setSource(Parsing.fileToString(new FileInputStream(javaFile)).toCharArray());
            newList = createDataClassInfo(parser, javaFile);

            javaFile = diff.getOldFile();
            parser.setSource(Parsing.fileToString(new FileInputStream(javaFile)).toCharArray());
            oldList = createDataClassInfo(parser, javaFile);

//            for(Object key : oldList.keySet()){
//                allData.getClassInfoList().remove(key);
//                allData.getFqnList().remove(key);
//            }
//
//            for(Object key : newList.keySet()){
//                allData.addClassInfo(key.toString(), (DataClassInfo) newList.get(key));
//            }

            for(Object key : oldList.keySet()){
                if(!newList.containsKey(key)){
                    allData.getClassInfoList().remove(key);
                    allData.getFqnList().remove(key);
                }
            }

            for(Object key : newList.keySet()){
                if(!oldList.containsKey(key)){
                    allData.addClassInfo(key.toString(), (DataClassInfo) newList.get(key));
                }
            }
        }else{
            System.out.println("Error : GitDiffVO Type incorrect - commitParse in PreJavaParser");
        }
    }

    private DualHashBidiMap createDataClassInfo(ASTParser parser, File javaFile){
        DualHashBidiMap createData = new DualHashBidiMap();
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        String packageName = null;
        DataClassInfo classInfo;

        if(cu.getPackage() != null){
            packageName = cu.getPackage().getName().toString();
        }

        String importList = cu.imports().toString();

        for(Object classType : cu.types()) {
            AbstractTypeDeclaration tempClassType = (AbstractTypeDeclaration) classType;
            int startPosition = tempClassType.getStartPosition();
            int endPosition = startPosition + tempClassType.getLength() - 1;
            int lineCount = cu.getLineNumber(endPosition) - cu.getLineNumber(startPosition) + 1;
            int commentLineCount = 0;
            String className = tempClassType.getName().toString();

            classInfo = new DataClassInfo();

            for (Object co : cu.getCommentList()) {
                Comment comment = (Comment) co;
                int startCommentPosition = comment.getStartPosition();

                if (startPosition < startCommentPosition) {
                    if (endPosition > startCommentPosition) {
                        commentLineCount += cu.getLineNumber(startCommentPosition + comment.getLength() - 1) - cu.getLineNumber(startCommentPosition) + 1;
                    } else {
                        continue;
                    }
                }
            }

            classInfo.setClassName(className);
            classInfo.setPackageName(packageName);
            classInfo.setClassType(getClassTypeNum(tempClassType));
            classInfo.setJavaFilePath(javaFile.getAbsolutePath());
            classInfo.setJavaFileName(javaFile.getName());
            classInfo.setLineCount(lineCount - commentLineCount);

            createData.put(DigestUtils.sha1Hex(packageName + importList + tempClassType.toString()), classInfo);
        }

        return createData;
    }

    private void settingParser(ASTParser parser){
        Map options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
        parser.setCompilerOptions(options);
    }

    private int getClassTypeNum(AbstractTypeDeclaration classType){
        int classTypeNum;

        if(classType.getNodeType() == AbstractTypeDeclaration.TYPE_DECLARATION){
            TypeDeclaration tempType = (TypeDeclaration) classType;
            if(tempType.isInterface()){
                classTypeNum = DataClassInfo.INTERFACE;
            }else{
                classTypeNum = DataClassInfo.CLASS;
            }
        }else if(classType.getNodeType() == AbstractTypeDeclaration.ENUM_DECLARATION){
            classTypeNum = DataClassInfo.ENUM;
        }else{
            classTypeNum = DataClassInfo.ANNOTATION;
        }

        return classTypeNum;
    }
}