package com.aurea.testgenerator.generation.assertions

import com.aurea.testgenerator.TestUnitSpec
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.source.Imports
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.type.PrimitiveType
import one.util.streamex.StreamEx
import spock.lang.Unroll

import static com.github.javaparser.JavaParser.parseExpression
import static com.github.javaparser.JavaParser.parseType
import static org.assertj.core.api.Assertions.assertThat

class SimpleAssertionBuilderSpec extends TestUnitSpec {

    SimpleAssertionBuilder builder = new SimpleAssertionBuilder()

    @Unroll
    def "correctly builds assertions for char/byte/long/short/int primitives"() {
        when:
        List<DependableNode<Statement>> statements = builder.with(wrapWithCompilationUnit(type),
                parseExpression(actual),
                parseExpression(expected)
        ).statements

        then:
        statements.size() == 1
        statements.first().node.toString() == "assertThat(${actual}).isEqualTo(${expected});"
        statements.first().dependency.imports.contains Imports.ASSERTJ_ASSERTTHAT

        where:
        actual | expected | type
        "5"    | "3 + 2"  | PrimitiveType.intType()
        "5"    | "3 + 2"  | parseType("Integer")
        "5"    | "3 + 2"  | parseType("java.lang.Integer")
        "c"    | "c"      | PrimitiveType.charType()
        "c"    | "c"      | parseType("Character")
        "c"    | "c"      | parseType("java.lang.Character")
        "5"    | "3 + 2"  | PrimitiveType.byteType()
        "5"    | "3 + 2"  | parseType("Byte")
        "5"    | "3 + 2"  | parseType("java.lang.Byte")
        "5"    | "3 + 2"  | PrimitiveType.longType()
        "5"    | "3 + 2"  | parseType("Long")
        "5"    | "3 + 2"  | parseType("java.lang.Long")
        "5"    | "3 + 2"  | PrimitiveType.shortType()
        "5"    | "3 + 2"  | parseType("Short")
        "5"    | "3 + 2"  | parseType("java.lang.Short")
    }

    @Unroll
    def "correctly build for booleans"() {
        when:
        List<DependableNode<Statement>> statements = builder.with(
                wrapWithCompilationUnit(PrimitiveType.booleanType()),
                parseExpression(actual),
                parseExpression(expected)
        ).statements

        then:
        statements.size() == 1
        statements.first().node.toString() == "assertThat(${actual}).${expectedAssertion};"
        statements.first().dependency.imports.contains Imports.ASSERTJ_ASSERTTHAT

        where:
        actual  | expected       | expectedAssertion
        "true"  | "false"        | "isFalse()"
        "false" | "true"         | "isTrue()"
        "true"  | "Boolean.TRUE" | "isEqualTo(Boolean.TRUE)"
    }

    @Unroll
    def "correctly build for boxed booleans"() {
        when:
        List<DependableNode<Statement>> statements = builder.with(
                wrapWithCompilationUnit(parseType('Boolean')),
                parseExpression(actual),
                parseExpression(expected)
        ).statements

        then:
        statements.size() == 1
        statements.first().node.toString() == "assertThat(${actual}).${expectedAssertion};"
        statements.first().dependency.imports.contains Imports.ASSERTJ_ASSERTTHAT

        where:
        actual  | expected       | expectedAssertion
        "true"  | "false"        | "isFalse()"
        "false" | "true"         | "isTrue()"
        "true"  | "Boolean.TRUE" | "isEqualTo(Boolean.TRUE)"
    }

    @Unroll
    def "correctly build for floating numbers"() {
        when:
        List<DependableNode<Statement>> statements = builder.with(wrapWithCompilationUnit(type),
                parseExpression(actual),
                parseExpression(expected)
        ).statements

        then:
        statements.size() == 1
        statements.first().node.toString() ==
                "assertThat(${actual}).isCloseTo($expected, Offset.offset($offset));"
        statements.first().dependency.imports.contains Imports.ASSERTJ_OFFSET

        where:
        actual   | expected | offset                                              | type
        "0.1f"   | "0.2f"   | SimpleAssertionBuilder.FLOATING_POINT_OFFSET_FLOAT  | PrimitiveType.floatType()
        "0.001f" | "2.3f"   | SimpleAssertionBuilder.FLOATING_POINT_OFFSET_FLOAT  | parseType('Float')
        "0.001f" | "2.3f"   | SimpleAssertionBuilder.FLOATING_POINT_OFFSET_FLOAT  | parseType('java.lang.Float')
        "0.001"  | "2.3"    | SimpleAssertionBuilder.FLOATING_POINT_OFFSET_DOUBLE | PrimitiveType.doubleType()
        "0.001"  | "2.3"    | SimpleAssertionBuilder.FLOATING_POINT_OFFSET_DOUBLE | parseType('Double')
        "0.001"  | "2.3"    | SimpleAssertionBuilder.FLOATING_POINT_OFFSET_DOUBLE | parseType('java.lang.Double')
    }

    def "soft assertions are properly grouped"() {
        when:
        List<DependableNode<Statement>> statements = builder
                .with(
                wrapWithCompilationUnit(PrimitiveType.intType()),
                parseExpression("3"),
                parseExpression("2 + 1"))
                .with(
                wrapWithCompilationUnit(PrimitiveType.floatType()),
                parseExpression("3.4"),
                parseExpression("3.4"))
                .statements

        then:
        statements.size() == 2
        Set<ImportDeclaration> imports = StreamEx.of(statements)
                                                 .flatMap { it.dependency.imports.stream() }
                                                 .toSet()
        imports.containsAll([Imports.ASSERTJ_OFFSET,
                             Imports.ASSERTJ_ASSERTTHAT])
        assertThat(statements.collect { it.node }
                             .join(System.lineSeparator()))
                .isEqualToIgnoringWhitespace("""
            assertThat(3).isEqualTo(2 + 1);
            assertThat(3.4).isCloseTo(3.4, Offset.offset(${SimpleAssertionBuilder.FLOATING_POINT_OFFSET_FLOAT}));
        """)
    }

    def "asserting strings work"() {
        when:
        List<DependableNode<Statement>> statements = builder
                .with(
                    wrapWithCompilationUnit(parseType("String")),
                    parseExpression("\"ABC\""),
                    parseExpression("\"AAB\""))
                .statements

        then:
        statements.size() == 1
        statements.first().node.toString() == 'assertThat("ABC").isEqualTo("AAB");'
        statements.first().dependency.imports.contains Imports.ASSERTJ_ASSERTTHAT
    }

    @Unroll
    def "known comparable types are being asserted by isEqualByComparingTo"() {
        when:
        builder.with(wrapWithCompilationUnit(type),
                parseExpression(value),
                parseExpression(value))
        List<DependableNode<Statement>> statements = builder.statements

        then:
        statements.size() == 1
        statements.first().node.toString() == "assertThat($value).isEqualByComparingTo($value);"
        statements.first().dependency.imports.contains Imports.ASSERTJ_ASSERTTHAT

        where:
        value            | type
        "BigDecimal.ONE" | parseType("java.math.BigDecimal")
    }
}
