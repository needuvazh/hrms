package com.company.hrms.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
        packages = "com.company.hrms",
        importOptions = {
                ImportOption.DoNotIncludeTests.class
        }
)
class ModuleApiDependencyArchitectureTest {

    @ArchTest
    static final ArchRule crossModuleAccessMustGoThroughApiPackages = classes()
            .that().resideInAnyPackage(
                    "com.company.hrms..api..",
                    "com.company.hrms..application..",
                    "com.company.hrms..infrastructure..")
            .and().resideOutsideOfPackages("com.company.hrms.app..", "com.company.hrms.platform..")
            .should(new CrossModuleApiOnlyCondition());

    private static final class CrossModuleApiOnlyCondition extends ArchCondition<JavaClass> {
        private CrossModuleApiOnlyCondition() {
            super("depend on other modules through api packages only");
        }

        @Override
        public void check(JavaClass origin, ConditionEvents events) {
            String originModule = moduleName(origin.getPackageName());
            for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                String targetPackage = target.getPackageName();
                if (!targetPackage.startsWith("com.company.hrms.")) {
                    continue;
                }

                String targetModule = moduleName(targetPackage);
                if (originModule.equals(targetModule)) {
                    continue;
                }
                if (targetPackage.contains(".api.") || targetPackage.endsWith(".api")) {
                    continue;
                }
                if (targetPackage.startsWith("com.company.hrms.platform.")) {
                    continue;
                }

                events.add(SimpleConditionEvent.violated(
                        dependency,
                        origin.getName() + " depends on non-api type " + target.getName()));
            }
        }

        private String moduleName(String packageName) {
            String prefix = "com.company.hrms.";
            int from = prefix.length();
            int nextDot = packageName.indexOf('.', from);
            if (nextDot < 0) {
                return packageName.substring(from);
            }
            return packageName.substring(from, nextDot);
        }
    }
}
