package com.aurea.testgenerator.generation

import com.aurea.testgenerator.generation.ast.DependableNode
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.Canonical


@Canonical
class TestGeneratorResult implements TestGeneratorErrorContainer {
    List<TestGeneratorError> errors = []
    List<DependableNode<MethodDeclaration>> tests = []
    TestType type
}
