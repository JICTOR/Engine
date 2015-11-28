package com.fasoo.parser.visitor;

import com.fasoo.parser.data.DataAll;
import com.fasoo.parser.data.DataClassInfo;
import com.fasoo.parser.data.DataVisit;
import org.eclipse.jdt.core.dom.*;

/**
 * Created by hwan on 2015-07-10.
 */
public class VisitorTypeDec extends SuperVisitor {
    public static void findRelation(DataAll allData, DataClassInfo classInfo, TypeDeclaration node, DataVisit visitData, CompilationUnit cu){
        setSuperAndPackage(allData, classInfo, node, visitData, cu);
        putInClass(allData, classInfo, node, visitData, cu);
    }

    public static void putInClass(final DataAll allData, final DataClassInfo classInfo, TypeDeclaration node, final DataVisit visitData, final CompilationUnit cu){
        visitData.expandScope(node.getName().toString());
        visitData.getInnerClassFQN().add(visitData.getScope());

        if(node.getTypes().length > 0){
            for(TypeDeclaration type : node.getTypes()){
                putInClass(allData, classInfo, type, visitData, cu);
            }
        }

        if(node.getFields().length > 0){
            for(FieldDeclaration field : node.getFields()) {
                putInField(allData, classInfo, field, visitData);
            }
        }

        if(node.getMethods().length > 0){
            for(MethodDeclaration method : node.getMethods()) {
                putInMethod(allData, classInfo, method, visitData, node, cu);
            }
        }

        node.accept(new ASTVisitor() {

            public boolean visit(EnumDeclaration subNode){
                VisitorEnumDec.putInEnum(allData, classInfo, subNode, visitData, cu);
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
                    classInfo.addRelationStatic(userTypeFQN, subNode.getStartPosition() + subNode.toString().indexOf(firstQualifier), firstQualifier.length());
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
                    classInfo.addRelationStatic(userTypeFQN, subNode.toString().indexOf(qualifier) + subNode.getStartPosition(), qualifier.length());
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

    private static void setSuperAndPackage(DataAll allData, DataClassInfo classInfo, TypeDeclaration node, DataVisit visitData, CompilationUnit cu){
        Type superClassType = node.getSuperclassType();
        String tempFQN;

        if(!node.superInterfaceTypes().isEmpty()){
            for(Object superInterface : node.superInterfaceTypes()){
                Type superInterfaceType = (Type) superInterface;
                tempFQN = visitData.findFQNbySimpleName(superInterfaceType.toString());

                if(allData.containsFQN(tempFQN)) {
                    classInfo.addSuperInterface(tempFQN, superInterfaceType.getStartPosition(), superInterfaceType.getLength());
                }
            }
        }

        if(superClassType != null) {
            tempFQN = visitData.findFQNbySimpleName(superClassType.toString());
            if(tempFQN != null) {
                String simpleName = tempFQN.substring(tempFQN.lastIndexOf(".") + 1);

                classInfo.setSuperClass(tempFQN, superClassType.getStartPosition(), simpleName.length());
            }
        }

//        if(visitData.getListInSamePackage() != null) {
//            for (String className : visitData.getListInSamePackage()) {
//                classInfo.getClassInSamePackage().add(allData.findKeyByFQN(className));
//            }
//        }
    }
}