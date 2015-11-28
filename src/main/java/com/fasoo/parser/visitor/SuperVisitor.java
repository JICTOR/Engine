package com.fasoo.parser.visitor;


import com.fasoo.parser.data.*;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

class SuperVisitor {
    public static void putInAnnotation(DataAll allData, DataClassInfo classInfo, Annotation node, DataVisit visitData){
        String annotationName = node.getTypeName().toString();
        String annotationFQN = visitData.findFQNbySimpleName(annotationName);
        if(allData.containsFQN(annotationFQN)){
            classInfo.addRelationAnnotation(annotationFQN, node.getStartPosition() + node.toString().indexOf(annotationName), annotationName.length());
        }
    }
    public static void putInField(DataAll allData, DataClassInfo classInfo, FieldDeclaration node, DataVisit visitData){
        executeVar(allData, classInfo, node, visitData, node.fragments());
    }

    public static void putInMethod(final DataAll allData, final DataClassInfo classInfo, MethodDeclaration node, final DataVisit visitData, final TypeDeclaration superNode,final CompilationUnit cu){
        ArrayList<String> userTypesFQN = new ArrayList<String>();
        String classScope = visitData.getScope();
        visitData.expandScope(node.getName().toString());
        DataMethod method = null;
        Type returnType = node.getReturnType2();

        // Return Type 찾기
        if(isUserTypes(userTypesFQN, returnType, visitData)){
            for(String userTypeFQN : userTypesFQN){
                String userType = userTypeFQN.substring(userTypeFQN.lastIndexOf(".")+1);

                //todo 명확한 return type의 시작위치를 찾아야함
                if(allData.containsFQN(userTypeFQN)) {
                    classInfo.addRelationMethodReturn(userTypeFQN, returnType.getStartPosition() + returnType.toString().indexOf(userType), userType.length());
                }
            }
        }

        if(!node.isConstructor()){
            method = new DataMethod();
            method.setReturnType(node.getReturnType2().toString());
            method.setClassFQName(classInfo.getFQName());
            method.setMethodName(node.getName().toString());

            if(!node.modifiers().isEmpty()){
                ArrayList<String> modifiers = method.getModifiers();
                for(Object modifier : node.modifiers()){
                    if(((IExtendedModifier) modifier).isModifier()){
                        modifiers.add(modifier.toString());
                    }
                }
            }
        }

        // Parameters 찾기
        for(Object o : node.parameters()){
            userTypesFQN.clear();
            SingleVariableDeclaration para = (SingleVariableDeclaration) o;
            if(method != null){
                DataParameter parameter = new DataParameter();
                parameter.setName(para.getName().toString());
                parameter.setType(para.getType().toString());

                method.getParameters().add(parameter);
            }
            if(isUserTypes(userTypesFQN, para.getType(), visitData)){
                for(String userTypeFQN : userTypesFQN){
                    String userType = userTypeFQN.substring(userTypeFQN.lastIndexOf(".") + 1);

                    if(allData.containsFQN(userTypeFQN)) {
                        classInfo.addRelationParameter(userTypeFQN, para.getStartPosition() + para.toString().indexOf(userType), userType.length());
                    }
                }
            }
        }

        if(method != null && classScope.equals(classInfo.getFQName())){
            classInfo.getListMethod().add(method);
        }

        if(node.getBody() == null){
            visitData.reduceScope();
            return;
        }

        node.getBody().accept(new ASTVisitor(){

            public boolean visit(TypeDeclarationStatement subNode) {
                TypeDeclaration tempType = null;

                if(subNode.getDeclaration().getNodeType() == AbstractTypeDeclaration.TYPE_DECLARATION){
                    tempType = (TypeDeclaration) subNode.getDeclaration();
                }

                if(tempType != null && !tempType.equals(superNode)) {
                    VisitorTypeDec.putInClass(allData, classInfo, tempType, visitData, cu);
                }
                return false;
            }

            public boolean visit(VariableDeclarationStatement subNode){
                executeVar(allData, classInfo, subNode, visitData, subNode.fragments());
                return false;
            }

            public boolean visit(Assignment subNode){
                ArrayList<String> userTypesFQN = new ArrayList<String>();
                Type userType = null;
                if(subNode.getRightHandSide().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION){
                    ClassInstanceCreation tempCreation = (ClassInstanceCreation) subNode.getRightHandSide();
                    userType = tempCreation.getType();
                    isUserTypes(userTypesFQN, userType, visitData);
                }else if(subNode.getRightHandSide().getNodeType() == ASTNode.ARRAY_CREATION){
                    ArrayCreation tempCreation = (ArrayCreation) subNode.getRightHandSide();
                    userType = tempCreation.getType();
                    isUserTypes(userTypesFQN, userType, visitData);
                }else if(subNode.getRightHandSide().getNodeType() == ASTNode.SIMPLE_NAME){
                    Expression rightHand = subNode.getRightHandSide();
//                    Type userType = null;
                    if(visitData.findLocalBySimpleName(rightHand.toString()) != null){
                        userType = visitData.findLocalBySimpleName(rightHand.toString()).getType();
                    }else if(visitData.findFieldBySimpleName(rightHand.toString()) != null){
                        userType = visitData.findFieldBySimpleName(rightHand.toString()).getType();
                    }else if(visitData.findFQNbySimpleName(rightHand.toString()) != null){
                        userTypesFQN.add(visitData.findFQNbySimpleName(rightHand.toString()));
                    }

                    if(userType != null){
                        isUserTypes(userTypesFQN, userType, visitData);
                    }
                }

                for(String userTypeFQN : userTypesFQN){
//                    DataRelation localRel = new DataRelation();
//                    localRel.setFQName(userTypeFQN);
//                    localRel.setIndex(allData.findKeyByFQN(userTypeFQN));
                    String simpleName = userTypeFQN.substring(userTypeFQN.lastIndexOf(".")+1);

                    //todo 명확한 위치 확인
                    //todo Field var 이름을 할당하고, 실제 할당문에 type의 이름이 없고 Field에 있는 경우 시작 위치 정하기
                    if(subNode.toString().indexOf(simpleName) != -1 && allData.containsFQN(userTypeFQN)) {
                        classInfo.addRelationMethodLocalVar(userTypeFQN, userType.getStartPosition() + userType.toString().indexOf(simpleName), simpleName.length());
                    }
                }
                return false;
            }
        });
        visitData.reduceScope();
    }

    public static boolean isUserTypes(ArrayList<String> userTypesFQN, Type type, DataVisit visitData){
        if(type == null){
            return false;
        }

        boolean isSimple = false;

        if(type.isSimpleType()){
            isSimple = isTypes(userTypesFQN, type.toString(), visitData);
        }else if(type.isArrayType()){
            ArrayType tempArray = (ArrayType) type;
            isSimple = isTypes(userTypesFQN, tempArray.getElementType().toString(), visitData);
        }else if(type.isParameterizedType()){
            ParameterizedType tempParaType = (ParameterizedType) type;
            for(Object o : tempParaType.typeArguments()){
                Type tempType = (Type) o;
                isSimple = isTypes(userTypesFQN, tempType.toString(), visitData);
            }
        }

        return isSimple;
    }

    private static boolean isTypes(ArrayList<String> userTypesFQN, String typeString, DataVisit visitData){
        String tempFQN;
        boolean isSimple;

        tempFQN = visitData.findFQNbySimpleName(typeString);

        if(tempFQN == null){
            isSimple = false;
        }else if(userTypesFQN == null){
            isSimple = true;
        }else{
            userTypesFQN.add(tempFQN);
            isSimple = true;
        }
        return isSimple;
    }

    private static void executeVar(DataAll allData, DataClassInfo classInfo, ASTNode node, DataVisit visitData, List<VariableDeclarationFragment> fragments){
        ArrayList<String> userTypesFQN = new ArrayList<String>();
        FieldDeclaration fieldNode = null;
        VariableDeclarationStatement localNode = null;
        boolean isLocal;
        if(node.getNodeType() == BodyDeclaration.FIELD_DECLARATION){
            fieldNode = (FieldDeclaration) node;
            isLocal = false;
        }else if(node.getNodeType() == Statement.VARIABLE_DECLARATION_STATEMENT){
            localNode = (VariableDeclarationStatement) node;
            isLocal = true;
        }else{
            return;
        }

        for(Object o : fragments){
            VariableDeclarationFragment varFrag = (VariableDeclarationFragment) o;
            Expression tempEx = varFrag.getInitializer();
            Type tempType = null;

            if(tempEx != null){
                switch(tempEx.getNodeType()) {
                    case ASTNode.CLASS_INSTANCE_CREATION:
                        ClassInstanceCreation cic = (ClassInstanceCreation) tempEx;
                        tempType = cic.getType();
                        break;
                    case ASTNode.ARRAY_CREATION:
                        ArrayCreation ac = (ArrayCreation) tempEx;
                        tempType = ac.getType().getElementType();
                        break;
                    case ASTNode.SIMPLE_NAME:
                        SimpleName sn = (SimpleName) tempEx;
                        FieldDeclaration tempField = visitData.findFieldBySimpleName(sn.toString());
                        if (tempField != null) {
                            tempType = tempField.getType();
                        }
                        break;
                }
                if (isUserTypes(userTypesFQN, tempType, visitData)) {
                    for(String userTypeFQN : userTypesFQN) {
                        if (allData.containsFQN(userTypeFQN)) {
                            String simpleName = userTypeFQN.substring(userTypeFQN.lastIndexOf(".") + 1);
                            if (isLocal) {
                                classInfo.addRelationMethodLocalVar(userTypeFQN, tempEx.getStartPosition() + tempEx.toString().indexOf(simpleName), simpleName.length());
                            } else {
                                classInfo.addRelationFieldVar(userTypeFQN, tempEx.getStartPosition() + tempEx.toString().indexOf(simpleName), simpleName.length());
                            }
                        }
                    }
                }
            }else{
                if(isLocal){
                    if(isUserTypes(userTypesFQN, localNode.getType(), visitData)){
                        visitData.getSimpleLocal().add(localNode);
                    }
                }else{
                    if(isUserTypes(userTypesFQN, fieldNode.getType(), visitData)){
                        visitData.getSimpleField().add(fieldNode);
                    }
                }
            }

            if(!isLocal && visitData.getScope().equals(classInfo.getFQName())){
                addVariable(classInfo, fieldNode, varFrag.getName().toString());
            }
            userTypesFQN.clear();
        }
    }

    private static void addVariable(DataClassInfo classInfo, FieldDeclaration fieldNode, String fragName){
        DataVariable var = new DataVariable();
        var.setName(fragName);
        var.setClassFQName(classInfo.getFQName());
        var.setTypeString(fieldNode.getType().toString());
        if(!fieldNode.modifiers().isEmpty()) {
            ArrayList<String> modifiers = var.getModifiers();
            for (Object modifier : fieldNode.modifiers()) {
                if (((IExtendedModifier) modifier).isModifier()) {
                    modifiers.add(modifier.toString());
                }
            }
        }
        classInfo.getListFieldVariable().add(var);
    }
}