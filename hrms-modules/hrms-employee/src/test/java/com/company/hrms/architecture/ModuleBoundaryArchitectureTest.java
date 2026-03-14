package com.company.hrms.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.company.hrms",
        importOptions = {
                ImportOption.DoNotIncludeTests.class,
                ImportOption.DoNotIncludeJars.class
        }
)
class ModuleBoundaryArchitectureTest {

    @ArchTest
    static final ArchRule applicationApiDomainShouldNotDependOnInfrastructure = noClasses()
            .that().resideInAnyPackage("..api..", "..application..", "..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..");

    @ArchTest
    static final ArchRule modulesShouldNotDependOnAppPackages = noClasses()
            .that().resideInAnyPackage("com.company.hrms..")
            .and().resideOutsideOfPackage("com.company.hrms.app..")
            .should().dependOnClassesThat().resideInAnyPackage("com.company.hrms.app..");
}
