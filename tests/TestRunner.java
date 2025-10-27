import ij.gui.Roi;

/**
 * Minimal sanity checks that can run headlessly in CI.
 */
public class TestRunner {
    public static void main(String[] args) {
        testMeasurementString();
        System.out.println("All tests passed.");
    }

    private static void testMeasurementString() {
        Comet comet = new Comet(new Roi(0, 0, 5, 5));
        comet.id = 42;

        comet.status = Comet.INVALID;
        if (!comet.getMeasurementString(",").isEmpty()) {
            throw new AssertionError("Invalid comet should not produce output");
        }

        comet.status = Comet.VALID;
        comet.cometArea = 10.0;
        comet.cometIntensity = 20.0;
        comet.cometLength = 30.0;
        comet.cometDNA = 40.0;
        comet.headArea = 50.0;
        comet.headIntensity = 60.0;
        comet.headLength = 70.0;
        comet.headDNA = 80.0;
        comet.headDNAPercent = 90.0;
        comet.tailArea = 12.0;
        comet.tailIntensity = 22.0;
        comet.tailLength = 32.0;
        comet.tailDNA = 42.0;
        comet.tailDNApercent = 52.0;
        comet.tailMoment = 62.0;
        comet.tailOliveMoment = 72.0;

        String expected = String.join(",",
                "42",
                "normal",
                "10.0",
                "20.0",
                "30.0",
                "40.0",
                "50.0",
                "60.0",
                "70.0",
                "80.0",
                "90.0",
                "12.0",
                "22.0",
                "32.0",
                "42.0",
                "52.0",
                "62.0",
                "72.0");

        String actual = comet.getMeasurementString(",");
        if (!expected.equals(actual)) {
            throw new AssertionError("Unexpected measurement string:\nexpected: "
                    + expected + "\nactual: " + actual);
        }
    }
}
