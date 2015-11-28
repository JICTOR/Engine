package com.fasoo.parser.visitor;

import com.fasoo.parser.data.*;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;

/**
 * Created by hwan on 2015-07-10.
 */
public class VisitorEnumDec extends SuperVisitor {

    public static void findRelation(DataAll allData, DataClassInfo classInfo, EnumDeclaration node, DataVisit visitData, CompilationUnit cu){
        setSuperAndPackage(allData, classInfo, node, visitData, cu);
        putInEnum(allData, classInfo, node, visitData, cu);
    }

    public static void putInEnum(final DataAll allData, final DataClassInfo classInfo, final EnumDeclaration node, final DataVisit visitData,final CompilationUnit cu){
        visitData.expandScope(node.getName().toString());
        visitData.getInnerClassFQN().add(visitData.getScope());

        if(!node.enumConstants().isEmpty()){
            for(Object enumConstant : node.enumConstants()){
                putInConstants(allData, classInfo, (EnumConstantDeclaration) enumConstant, visitData);
            }
        }

        node.accept(new ASTVisitor() {
            public boolean visit(TypeDeclaration subNode){
                VisitorTypeDec.putInClass(allData, classInfo, subNode, visitData, cu);
                return true;
            }
            public boolean visit(EnumDeclaration subNode){
                if(!node.equals(subNode)) {
                    putInEnum(allData, classInfo, subNode, visitData, cu);
                }
                return true;
            }

            public boolean visit(FieldDeclaration subNode){
                putInField(allData, classInfo, subNode, visitData);
                return true;
            }
            public boolean visit(MethodDeclaration subNode){
                putInMethod(allData, classInfo, subNode, visitData, null, cu);
                return true;
            }

            public boolean visit(QualifiedName subNode){
                String qualifier = subNode.getQualifier().toString();
                String firstQualifier;

                if(qualifier.contains(".")){
                    firstQualifier = qualifier.substring(0, qualifier.indexOf("."));
                }else{
                    firstQualifier = qualifier;
                }

                String userTypeFQN = visitData.findFQNbySimpleName(firstQualifier);

                if(allData.containsFQN(userTypeFQN)){
                    classInfo.addRelationStatic(userTypeFQN, subNode.toString().indexOf(firstQualifier) + subNode.getStartPosition(), firstQualifier.length());
                }
                return false;
            }

            public boolean visit(MethodInvocation subNode){
                String qualifier;
                String userTypeFQN;

                if(subNode.getExpression() != null){
                    qualifier = subNode.getExpression().toString();
                    userTypeFQN = visitData.findFQNbySimpleName(qualifier);
                }else{
                    return false;
                }

                if(allData.containsFQN(userTypeFQN)){
                    classInfo.addRelationStatic(userTypeFQN, subNode.getStartPosition() + subNode.toString().indexOf(qualifier), qualifier.length());
                }
                return true;
            }

            public boolean visit(MarkerAnnotation subNode){
                putInAnnotation(allData, classInfo, subNode, visitData);
                return true;
            }

            public boolean visit(NormalAnnotation subNode){
                putInAnnotation(allData, classInfo, subNode, visitData);
                return true;
            }

            public boolean visit(SingleMemberAnnotation subNode){
                putInAnnotation(allData, classInfo, subNode, visitData);
                return true;
            }
        });

        visitData.reduceScope();
    }

    private static void putInConstants(DataAll allData, DataClassInfo classInfo, EnumConstantDeclaration node, DataVisit visitData){
        DataVariable var = new DataVariable();
        ArrayList<String> modifiers = var.getModifiers();
        modifiers.add("public");
        modifiers.add("static");
        modifiers.add("final");

        var.setName(node.getName().toString());
        var.setTypeString("int");
        var.setClassFQName(classInfo.getFQName());

        classInfo.getListFieldVariable().add(var);
    }

    private static void setSuperAndPackage(DataAll allData, DataClassInfo classInfo, EnumDeclaration node, DataVisit visitData, CompilationUnit cu){
        if(!node.superInterfaceTypes().isEmpty()){
            for(Object superInterface : node.superInterfaceTypes()){
                Type superInterfaceType = (Type) superInterface;
                String tempFQN = visitData.findFQNbySimpleName(superInterfaceType.toString());

                if(allData.containsFQN(tempFQN)) {
                    classInfo.addSuperInterface(tempFQN, superInterfaceType.getStartPosition(), superInterfaceType.getLength());
                }
            }
        }

//        if(visitData.getListInSamePackage() != null) {
//            for (String className : visitData.getListInSamePackage()) {
//                classInfo.getClassInSamePackage().add(allData.findKeyByFQN(className));
//            }
//        }
    }
}