package com.aurea.testgenerator.generation.patterns.pojos

import com.aurea.testgenerator.value.Types
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import com.jasongoodwin.monads.Try
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

import static com.aurea.testgenerator.generation.patterns.pojos.PojoFieldFinder.validPrefix
import static com.aurea.testgenerator.generation.patterns.pojos.Pojos.isGetterSignature
import static com.aurea.testgenerator.generation.patterns.pojos.Pojos.isSetterSignature
import static com.aurea.testgenerator.value.Types.areSameOrBoxedSame

@Log4j2
class PojoMethodsFinder {

    static Optional<ResolvedMethodDeclaration> findGetterMethod(ResolvedFieldDeclaration fieldDeclaration) {
        Try.ofFailable {
            if (Types.isBooleanType(fieldDeclaration.getType())) {
                def expectedGetterName = fieldDeclaration.name
                if (!validPrefix(expectedGetterName, 'is')) {
                    expectedGetterName = 'is' + expectedGetterName.capitalize()
                }

                Optional<ResolvedMethodDeclaration> withIsName = findGetterWithName(fieldDeclaration, expectedGetterName)
                if (withIsName.present) {
                    return withIsName
                }
            }

            findGetterWithName(fieldDeclaration, 'get' + fieldDeclaration.name.capitalize())
        }.orElse(Optional.empty())
    }

    static Optional<ResolvedMethodDeclaration> findSetterMethod(ResolvedFieldDeclaration fieldDeclaration) {
        Try.ofFailable {
            String fieldName = fieldDeclaration.name
            if (Types.isBooleanType(fieldDeclaration.getType()) && validPrefix(fieldName, 'is')) {

                String expectedName = fieldName.replaceFirst('is', 'set')
                Optional<ResolvedMethodDeclaration> withIsName = findSetterWithName(fieldDeclaration, expectedName)
                if (withIsName.present) {
                    return withIsName
                }
            }

            findSetterWithName(fieldDeclaration, 'set' + fieldName.capitalize())
        }.onFailure {
            log.warn('error', it.toString())
        }.orElse(Optional.empty())
    }

    private static Optional<ResolvedMethodDeclaration> findGetterWithName(ResolvedFieldDeclaration fieldDeclaration,
                                                                          String expectedName) {
        ResolvedTypeDeclaration rtd = fieldDeclaration.declaringType()
        if (rtd.class || rtd.anonymousClass) {
            return StreamEx.of(rtd.asClass().declaredMethods).findFirst {
                it.name == expectedName && isGetter(fieldDeclaration, it)
            }
        }

        if (rtd.enum) {
            //TODO: Add enum support — https://github.com/trilogy-group/GeneralPatterns/issues/24
        }

        return Optional.empty()
    }

    private static Optional<ResolvedMethodDeclaration> findSetterWithName(ResolvedFieldDeclaration fieldDeclaration,
                                                                          String name) {
        ResolvedTypeDeclaration rtd = fieldDeclaration.declaringType()

        if (rtd.class || rtd.anonymousClass) {
            return StreamEx.of(rtd.asClass().declaredMethods).findFirst {
                it.name == name && isSetter(fieldDeclaration, it)
            }
        }

        if (rtd.enum) {
            //TODO: Add enum support — https://github.com/trilogy-group/GeneralPatterns/issues/24
        }

        return Optional.empty()
    }

    private static boolean isGetter(ResolvedFieldDeclaration fieldDeclaration, ResolvedMethodDeclaration methodDeclaration) {
        methodDeclaration.static == fieldDeclaration.static &&
                isGetterSignature(methodDeclaration) &&
                areSameOrBoxedSame(methodDeclaration.returnType, fieldDeclaration.getType())
    }

    private static boolean isSetter(ResolvedFieldDeclaration fieldDeclaration, ResolvedMethodDeclaration methodDeclaration) {
        methodDeclaration.static == fieldDeclaration.static &&
                isSetterSignature(methodDeclaration) &&
                areSameOrBoxedSame(methodDeclaration.getParam(0).getType(), fieldDeclaration.getType())
    }
}
