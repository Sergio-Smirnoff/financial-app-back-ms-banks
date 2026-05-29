package com.financialapp.banks.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Enforces DDD / hexagonal layer boundaries. Dependencies point inward toward the domain:
 * web -> application -> domain, infrastructure -> (application, domain). The domain depends
 * on nothing else in the codebase and is free of framework/persistence imports.
 *
 * If a rule fails, the fix is to remove the offending dependency, not to weaken the rule.
 */
@AnalyzeClasses(
        packages = "com.financialapp.banks",
        importOptions = ImportOption.DoNotIncludeTests.class)
class LayeredArchitectureTest {

    @ArchTest
    static final ArchRule layers_respect_inward_dependency_flow = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..banks.domain..")
            .layer("Application").definedBy("..banks.application..")
            .layer("Web").definedBy("..banks.web..")
            .layer("Infrastructure").definedBy("..banks.infrastructure..")
            // Domain is the core: accessed by everyone, depends on no other layer.
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Web", "Infrastructure")
            // Application orchestrates domain; only adapters (web/infra) drive it.
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Web", "Infrastructure")
            // Web and Infrastructure are outermost: nothing else imports them.
            .whereLayer("Web").mayNotBeAccessedByAnyLayer()
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();

    @ArchTest
    static final ArchRule domain_is_free_of_framework_and_outer_layers = noClasses()
            .that().resideInAPackage("..banks.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..banks.web..",
                    "..banks.infrastructure..",
                    "org.springframework..",
                    "jakarta.persistence..")
            .as("Domain must not depend on web, infrastructure, Spring, or JPA");

    @ArchTest
    static final ArchRule application_does_not_depend_on_web = noClasses()
            .that().resideInAPackage("..banks.application..")
            .should().dependOnClassesThat().resideInAPackage("..banks.web..")
            .as("Application must not depend on the web layer");

    @ArchTest
    static final ArchRule domain_events_are_recorded_by_aggregates_not_the_application = noClasses()
            .that().resideInAPackage("..banks.application..")
            .should().dependOnClassesThat().resideInAPackage("..banks.domain.event..")
            .as("Domain events must be recorded by aggregates in the domain, not constructed in the "
                    + "application layer (use cases drain them via DomainEventPublisher.publishAll)");

    @ArchTest
    static final ArchRule only_aggregate_roots_have_repositories = classes()
            .that().resideInAPackage("..banks.domain.repository..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("Repository")
            .andShould().haveSimpleNameNotContaining("Installment")
            .as("Only aggregate roots (Bank, Account, Card, Loan) may have a domain repository; "
                    + "installments are owned by their root and have no repository");
}
