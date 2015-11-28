package com.fasoo.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.fasoo.parser.astparser.JavaParser;
import com.fasoo.parser.astparser.PreJavaParser;
import com.fasoo.parser.data.*;
import com.fasoo.parser.gitTest.GitDiffVO;
import org.eclipse.jgit.diff.DiffEntry;

public class Parsing {

    public static DataAll parsing(List<File> javaFileList) throws IOException{
        JavaParser javaParser = new JavaParser();
        PreJavaParser preJavaParser = new PreJavaParser();
        DataAll allData = new DataAll();

        for(File javaFile : javaFileList){
            preJavaParser.preParse(fileToString(new FileInputStream(javaFile)), allData, javaFile);
        }

        allData.setFQNList();

        for(File javaFile : javaFileList){
            javaParser.parse(fileToString(new FileInputStream(javaFile)), allData, javaFile);
        }

        return allData;
    }

    public static String fileToString(InputStream inputStream) throws IOException {
        StringBuilder fileData = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine()) != null){
            fileData.append(line+"\n");
        }
        reader.close();
        return fileData.toString();
    }

    public static DataAll changeCommitInfo (DataAll beforeData, List<GitDiffVO> dataDiffs) throws IOException{
        if(beforeData == null){
            System.out.println("Error : DataAll is null - changeCommitInfo in Parsing");
            return null;
        }

        DataAll afterData = new DataAll();
        afterData.setClassInfoList(beforeData.getClassInfoList());
        afterData.setFQNList();

        if(dataDiffs == null || dataDiffs.isEmpty()){
            System.out.println("Error : DataDiff List is null or empty - changeCommitInfo in Parsing");
            return afterData;
        }

        PreJavaParser preParser = new PreJavaParser();
        JavaParser parser = new JavaParser();

        for(GitDiffVO diff : dataDiffs){
            preParser.commitParse(afterData, diff);
        }

        afterData.setFQNList();

        for(GitDiffVO diff : dataDiffs){
            if(diff.getType() == DiffEntry.ChangeType.ADD || diff.getType() == DiffEntry.ChangeType.MODIFY){
                parser.parse(fileToString(new FileInputStream(diff.getNewFile())), afterData, diff.getNewFile());
            }
        }

        for(Object key : afterData.getClassInfoList().keySet()){
            if(!afterData.getFqnList().containsKey(key)){
                DataClassInfo classInfo = (DataClassInfo) afterData.getClassInfoList().get(key);
                System.out.println("Error : Not Equal both classInfoList and fqnList(only classInfoList exist) - " + key.toString() + " " + classInfo.getFQName());
                System.out.println("      : " + afterData.getFqnList().getKey(classInfo.getFQName()) + " " + classInfo.getFQName());
            }
        }

        for(Object key : afterData.getFqnList().keySet()){
            if(!afterData.getClassInfoList().containsKey(key)){
                System.out.println("Error : Not Equal both classInfo List and fqnList(only fqnList exist)");
            }
        }

        return afterData;
    }
}