package nationGen.misc;

public class TestResult {
    public final Boolean failed;
    public final String reason;

    private TestResult(Boolean failed) {
        this.failed = failed;
        this.reason = null;
    }

    private TestResult(Boolean failed, String failReason) {
        this.failed = failed;
        this.reason = failReason;
    }

    public static TestResult fail(String failReason)  {
        return new TestResult(true, failReason);
    }

    public static TestResult pass() {
        return new TestResult(false);
    }

    public static TestResult check(Boolean checkResult, String potentialReason) {
        return new TestResult(checkResult, potentialReason);
    }
}
