package com.aurea.testgenerator.generation.patterns.bootcamp

import com.aurea.testgenerator.generation.AbstractMethodTestGenerator
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.reporting.CoverageReporter
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.source.Unit

import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.ast.body.MethodDeclaration

import groovy.util.logging.Log4j2

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component


import javax.print.DocFlavor


@Component
@Profile("manual")
@Log4j2
class AssignentOne extends  AbstractMethodTestGenerator {

    NomenclatureFactory nomenclatures

    @Autowired
    AssignmentOne(JavaParserFacade solver, TestGeneratorResultReporter reporter,
                                  CoverageReporter visitReporter, NomenclatureFactory nomenclatures) {
        //super(solver, reporter, visitReporter, nomenclatures)
        this.nomenclatures = nomenclatures
    }



    @Override
    protected TestGeneratorResult generate(MethodDeclaration method, Unit unitUnderTest) {
        TestGeneratorResult result = new TestGeneratorResult()

        DependableNode<MethodDeclaration> testMethod = new DependableNode<>()

        TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unitUnderTest.javaClass)
        String testName = testMethodNomenclature.requestTestMethodName(AssignmentTypes.ASSIGNMENT_ONE_TYPE, method)
        ClassOrInterfaceDeclaration parentClass = method.getAncestorOfType(ClassOrInterfaceDeclaration).get()

        List<String> typeNames = parentClass.extendedTypes[0].findAll(ClassOrInterfaceType).nameAsString
        //TODO:Improve entity type detection  https://github.com/trilogy-group/GeneralPatterns/issues/48
        def entityTypeName = typeNames.find { !it.contains(REPOSITORY) && !it.contains(DocFlavor.STRING) && !it.contains(LONG) }
        //if (entityTypeName) { fillTestMethod(entityTypeName, testName, method, testMethod, result) }

        result
    }

    @Override
    protected TestType getType() {
        AssignmentTypes.ASSIGNMENT_ONE_TYPE
    }

    @Override
    protected boolean shouldBeVisited(Unit unit, MethodDeclaration method) {
        //super.shouldBeVisited(unit, method) && hasSingletonSignature(method) && isEager(method)
    }


}
