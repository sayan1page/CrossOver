package com.aurea.testgenerator.generation.merge

import com.github.javaparser.ast.CompilationUnit
import groovy.transform.Immutable

@Immutable
class UnitTestMergeResult {
    CompilationUnit unit
    List<UnitTestMergeConflict> conflicts
}
