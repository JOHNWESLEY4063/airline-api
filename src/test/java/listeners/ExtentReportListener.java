package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtentReportListener implements ITestListener {
    private ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testLocal = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        System.out.println("✅ ExtentReportListener: Initializing report at target/ExtentReport.html");
        ExtentSparkReporter spark = new ExtentSparkReporter("target/ExtentReport.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        testLocal.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = testLocal.get();
        if (test != null) {
            test.pass("Test passed");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = testLocal.get();
        if (test != null) {
            test.fail("Test failed: " + result.getThrowable().getMessage());
            test.fail(result.getThrowable());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            try {
                extent.flush();
                System.out.println("✅ ExtentReportListener: Report successfully written to target/ExtentReport.html");
            } catch (Exception e) {
                System.err.println("❌ Failed to flush Extent Report: " + e.getMessage());
            }
        }
    }
}