import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("               JAVA LOG ANALYZER - JUNIT TEST SUITE RUNNER                     ");
        System.out.println("================================================================================\n");

        Class<?>[] testClasses = new Class<?>[] {
            LogParserTest.class,
            LogAnalyzerTest.class,
            AlertDetectorTest.class,
            ReportGeneratorTest.class,
            EdgeCaseTest.class,
            IntegrationTest.class
        };

        int totalRun = 0;
        int totalFailures = 0;

        for (Class<?> testClass : testClasses) {
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("RUNNING TEST SUITE: " + testClass.getSimpleName());
            System.out.println("--------------------------------------------------------------------------------");

            Result result = JUnitCore.runClasses(testClass);
            totalRun += result.getRunCount();
            totalFailures += result.getFailureCount();

            System.out.println("Tests Executed : " + result.getRunCount());
            System.out.println("Tests Passed   : " + (result.getRunCount() - result.getFailureCount()));
            System.out.println("Tests Failed   : " + result.getFailureCount());
            System.out.println("Execution Time : " + result.getRunTime() + " ms");

            if (!result.wasSuccessful()) {
                System.out.println("\n[FAILURES DETECTED]");
                for (Failure failure : result.getFailures()) {
                    System.out.println("  ❌ " + failure.getTestHeader() + ": " + failure.getMessage());
                }
            } else {
                System.out.println("STATUS         : 100% PASSED ✅");
            }
            System.out.println();
        }

        System.out.println("================================================================================");
        System.out.println("                       FINAL TEST EXECUTION SUMMARY                            ");
        System.out.println("================================================================================");
        System.out.println("Total Test Classes  : " + testClasses.length);
        System.out.println("Total Tests Run     : " + totalRun);
        System.out.println("Total Tests Passed  : " + (totalRun - totalFailures));
        System.out.println("Total Tests Failed  : " + totalFailures);
        System.out.println("OVERALL SUITE STATUS: " + (totalFailures == 0 ? "ALL TESTS PASSED ✅" : "SOME TESTS FAILED ❌"));
        System.out.println("================================================================================\n");
    }
}
