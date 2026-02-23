package src.tests;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("   STARTING SECURITY SUITE       ");
        System.out.println("=================================\n");

        try {
            AccessControlTests.run();
            RequestHandlerTests.run();
            
            System.out.println("=================================");
            System.out.println("   ALL TESTS PASSED SUCCESSFULLY ");
            System.out.println("=================================");
        } catch (Exception e) {
            System.err.println("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("   TEST SUITE FAILED             ");
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
