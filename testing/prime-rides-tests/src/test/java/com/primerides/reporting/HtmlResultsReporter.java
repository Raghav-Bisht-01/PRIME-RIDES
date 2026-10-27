package com.primerides.reporting;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class HtmlResultsReporter implements IReporter {
    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        List<String> caseRows = new ArrayList<>();

        for (ISuite suite : suites) {
            for (ISuiteResult result : suite.getResults().values()) {
                ITestContext context = result.getTestContext();
                passed += context.getPassedTests().getAllResults().size();
                failed += context.getFailedTests().getAllResults().size();
                skipped += context.getSkippedTests().getAllResults().size();
                appendRows(caseRows, context.getPassedTests().getAllResults(), "Passed");
                appendRows(caseRows, context.getFailedTests().getAllResults(), "Failed");
                appendRows(caseRows, context.getSkippedTests().getAllResults(), "Skipped");
            }
        }

        int total = passed + failed + skipped;
        int evaluated = passed + failed;
        double passRate = evaluated == 0 ? 0 : (passed * 100.0) / evaluated;
        int chartMaximum = Math.max(total, 1);
        String conclusion;
        if (failed == 0 && skipped == 0) {
            conclusion = "All " + total + " executed automated cases passed.";
        } else if (failed == 0) {
            conclusion = passed + " cases passed, none failed, and " + skipped
                    + " were skipped because their prerequisites were unavailable.";
        } else {
            conclusion = passed + " cases passed, " + failed + " failed, and " + skipped
                    + " were skipped; review the failed cases below.";
        }

        String html = """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Prime Rides Test Results</title>
                  <style>
                    :root { color-scheme: light; font-family: Arial, Helvetica, sans-serif; color: #17243b; background: #f2f5fa; }
                    * { box-sizing: border-box; }
                    body { margin: 0; padding: 48px 20px; }
                    main { max-width: 980px; margin: 0 auto; }
                    h1 { margin: 0 0 26px; font-size: clamp(28px, 4vw, 42px); letter-spacing: -.7px; }
                    .panel { padding: 26px 34px; color: white; text-align: center; border-radius: 8px; background: linear-gradient(120deg, #0759ad, #1678d2); box-shadow: 0 12px 30px #164e8b22; }
                    .panel h2 { margin: 0 0 20px; font-size: 23px; }
                    .metrics { display: flex; justify-content: center; gap: 0; }
                    .metric { min-width: 175px; padding: 0 24px; }
                    .metric + .metric { border-left: 1px solid #ffffff66; }
                    .metric span { display: block; font-size: 15px; opacity: .9; }
                    .metric strong { display: block; margin-top: 8px; font-size: 34px; }
                    .conclusion { margin: 20px 0 4px; color: #455268; font-size: 17px; line-height: 1.5; }
                    .timestamp { margin: 4px 0 0; color: #718096; font-size: 13px; }
                    .chart-card { margin-top: 34px; padding: 26px 30px 18px; border: 1px solid #e1e7f0; border-radius: 12px; background: white; }
                    .legend { display: flex; justify-content: center; gap: 8px; margin: 0 0 20px; font-size: 14px; }
                    .dot { width: 12px; height: 12px; margin-top: 2px; border-radius: 3px; background: #132440; }
                    .chart { height: 300px; display: flex; align-items: stretch; gap: 30px; padding: 12px 10px 0; border-bottom: 1px solid #dce3ec; }
                    .bar-column { flex: 1; display: flex; min-width: 0; flex-direction: column; align-items: center; justify-content: flex-end; }
                    .bar-value { margin-bottom: 7px; color: #34445c; font-size: 14px; }
                    .bar { width: min(100%%, 125px); height: max(var(--height), 3px); border-radius: 5px 5px 0 0; background: #132440; }
                    .bar.failed { background: #d64b4b; }
                    .bar.skipped { background: #8492a6; }
                    .bar-label { min-height: 40px; padding-top: 9px; color: #58677d; text-align: center; font-size: 14px; }
                    details { margin-top: 28px; overflow: hidden; border: 1px solid #e1e7f0; border-radius: 12px; background: white; }
                    summary { padding: 18px 22px; color: #17243b; cursor: pointer; font-weight: 700; }
                    .table-wrap { max-height: 520px; overflow: auto; }
                    table { width: 100%%; border-collapse: collapse; font-size: 13px; }
                    th, td { padding: 11px 14px; border-top: 1px solid #e8edf4; text-align: left; vertical-align: top; }
                    th { position: sticky; top: 0; background: #f7f9fc; color: #53627a; }
                    .status { font-weight: 700; }
                    .status.Passed { color: #14804a; }
                    .status.Failed { color: #c33333; }
                    .status.Skipped { color: #8792a3; }
                    .failure { color: #a52c2c; white-space: pre-wrap; }
                    .footer { margin-top: 20px; color: #7b8798; text-align: center; font-size: 12px; }
                    @media (max-width: 560px) { body { padding: 28px 12px; } .panel { padding: 24px 12px; } .metric { min-width: 0; flex: 1; padding: 0 10px; } .metric strong { font-size: 28px; } .chart-card { padding: 22px 14px 14px; } .chart { gap: 12px; height: 240px; } .bar-label { font-size: 12px; } }
                  </style>
                </head>
                <body>
                  <main>
                    <h1>Results and Conclusion</h1>
                    <section class="panel" aria-label="Execution summary">
                      <h2>Execution Results</h2>
                      <div class="metrics">
                        <div class="metric"><span>Total Tests</span><strong>%d</strong></div>
                        <div class="metric"><span>Passed</span><strong>%d</strong></div>
                        <div class="metric"><span>Pass Rate</span><strong>%.1f%%</strong></div>
                      </div>
                    </section>
                    <p class="conclusion">%s</p>
                    <p class="timestamp">Generated %s</p>
                    <section class="chart-card" aria-label="Test execution chart">
                      <div class="legend"><span class="dot"></span><span>Execution Results</span></div>
                      <div class="chart">
                        <div class="bar-column"><span class="bar-value">%d</span><div class="bar" style="--height:%d%%"></div><span class="bar-label">Total Tests</span></div>
                        <div class="bar-column"><span class="bar-value">%d</span><div class="bar" style="--height:%d%%"></div><span class="bar-label">Passed Tests</span></div>
                        <div class="bar-column"><span class="bar-value">%d</span><div class="bar failed" style="--height:%d%%"></div><span class="bar-label">Failed Tests</span></div>
                        <div class="bar-column"><span class="bar-value">%d</span><div class="bar skipped" style="--height:%d%%"></div><span class="bar-label">Skipped Tests</span></div>
                      </div>
                    </section>
                    <details>
                      <summary>Individual case results (%d cases)</summary>
                      <div class="table-wrap">
                        <table>
                          <thead><tr><th>Status</th><th>Test case</th><th>Failure detail</th></tr></thead>
                          <tbody>%s</tbody>
                        </table>
                      </div>
                    </details>
                    <p class="footer">Prime Rides · Selenium WebDriver · TestNG</p>
                  </main>
                </body>
                </html>
                """.formatted(
                total, passed, passRate, conclusion,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss")),
                total, (total * 100) / chartMaximum,
                passed, (passed * 100) / chartMaximum,
                failed, (failed * 100) / chartMaximum,
                skipped, (skipped * 100) / chartMaximum,
                total, String.join("", caseRows)
        );

        Path reportPath = Path.of("target", "prime-rides-test-report.html");
        try {
            Files.createDirectories(reportPath.getParent());
            Files.writeString(reportPath, html);
            System.out.println("HTML test dashboard: " + reportPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not write HTML test dashboard", e);
        }
    }

    private void appendRows(List<String> rows, Iterable<ITestResult> results, String status) {
        for (ITestResult result : results) {
            Object[] parameters = result.getParameters();
            String caseName = parameters.length > 0 && parameters[0] instanceof String
                    ? (String) parameters[0]
                    : result.getMethod().getDescription();
            if (caseName == null || caseName.isBlank()) {
                caseName = result.getName();
            }

            String failure = result.getThrowable() == null ? "" : result.getThrowable().getMessage();
            String parameterSummary = parameters.length > 1
                    ? "<br><small>Input: " + escape(Arrays.deepToString(parameters)) + "</small>"
                    : "";
            rows.add("<tr><td class=\"status " + status + "\">" + status + "</td><td>"
                    + escape(caseName) + parameterSummary + "</td><td class=\"failure\">"
                    + escape(failure == null ? "" : failure) + "</td></tr>");
        }
    }

    private String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
