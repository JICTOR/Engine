package com.fasoo.parser.visitor;

import com.fasoo.parser.data.DataAll;
import com.fasoo.parser.data.DataClassInfo;
import com.fasoo.parser.data.DataMethod;
import com.fasoo.parser.data.DataVisit;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;

/**
 * Created by hwan on 2015-07-10.
 */
public class VisitorAnnotaionDec extends SuperVisitor{
    public static void findRelation(final DataAll allData, final DataClassInfo classInfo, AnnotationTypeDeclaration node,final DataVisit visitData){
        node.accept(new ASTVisitor() {
            public boolean visit(AnnotationTypeMemberDeclaration subNode){
                DataMethod member = new DataMethod();
                ArrayList<String> modifiers = null;

                if(!subNode.modifiers().isEmpty()) {
                    modifiers = member.getModifiers();
                    for(Object modifier : subNode.modifiers()){
                        modifiers.add(modifier.toString());
                    }
                }
                member.setClassFQName(classInfo.getFQName());
                member.setMethodName(subNode.getName().toString());
                member.setReturnType(subNode.getType().toString());
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
    }
}