package com.aurea.testgenerator.coverage;

import com.aurea.coverage.unit.ClassCoverage;
import com.aurea.coverage.unit.ClassCoverageImpl;
import com.aurea.coverage.unit.MethodCoverage;
import com.github.javaparser.ast.body.CallableDeclaration;
import one.util.streamex.StreamEx;

import java.util.List;

public class NoCoverageService implements CoverageService {
    @Override
    public MethodCoverage getMethodCoverage(MethodCoverageQuery methodCoverageQuery) {
        return getMethodCoverage(methodCoverageQuery.getMethod());
    }

    private MethodCoverage getMethodCoverage(CallableDeclaration methodDeclaration) {
        long count = NodeLocCounter.count(methodDeclaration);
        assert count < Integer.MAX_VALUE;
        return new MethodCoverage(methodDeclaration.getNameAsString(), 0, 0, 0, (int) count);
    }

    @Override
    public ClassCoverage getTypeCoverage(ClassCoverageQuery classCoverageQuery) {
        List<CallableDeclaration> methodDeclarations = classCoverageQuery.getType().findAll(CallableDeclaration.class);
        List<MethodCoverage> methodCoverages = StreamEx.of(methodDeclarations)
                .map(this::getMethodCoverage)
                .toList();

        return new ClassCoverageImpl(classCoverageQuery.getType().getNameAsString(), methodCoverages);
    }
}
