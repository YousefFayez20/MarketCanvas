package org.workshop.marketcanvas;


import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
@AnalyzeClasses(packages = "org.workshop.marketcanvas")
public class ArchitectureEnforcementTest {
    @ArchTest
    static final ArchRule moduleShouldBeIndependent = layeredArchitecture()
            .consideringAllDependencies()
            .layer("User").definedBy("..user..")
        .layer("Watchlist").definedBy("..watchlist..")
        .layer("MarketData").definedBy("..marketdata..")
        .layer("SharedKernel").definedBy("..sharedkernel..")
        .whereLayer("Watchlist").mayOnlyAccessLayers("SharedKernel")
        .whereLayer("User").mayOnlyAccessLayers("SharedKernel")
        .whereLayer("MarketData").mayOnlyAccessLayers("SharedKernel").withOptionalLayers(true);;

}
