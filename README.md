# JMeter Load Testing in the Cloud CI/CD Plugin by LoadFocus
<p align="center">
<a href="https://loadfocus.com">
<img src="https://d2woeiihr4s5r6.cloudfront.net/loadfocus.png" align="right"
     alt="cloud testing tool" width="220"></a>
</p>

[JMeter Load Testing](https://loadfocus.com/jmeter-load-testing) in the Cloud Jenkins CI/CD plugin is a Jenkins plugin for running Apache JMeter load tests continuously for Websites and APIs
 provided by <a href="https://loadfocus.com">LoadFocus</a>. 
 
Helps you run [Apache JMeter load tests](https://loadfocus.com/jmeter-load-testing) as a Post-build Action marking the Build as Passed, Unstable or Failed based on:

* **error percentage** and **response times**.
* all URLs from the test are considered when marking the status of the build.

<p align="center">
<a href="https://loadfocus.com">
<img src="https://d2woeiihr4s5r6.cloudfront.net/jenkins/load-testing-ci-cd-plugin-configuration-loadfocus.jpeg"
  alt="JMeter Load Testing in the Cloud CI/CD Plugin configuration Jenkins"
 height="389"></a>
</p>

With **JMeter Load Testing in the Cloud CI/CD Plugin** you can run load test with thousands of parallel users periodically.

## How It Works

### Installation Steps
1. Create your JMeter load testing account on [LoadFocus](https://loadfocus.com)
2. Copy your **LoadFocus.com API key** from https://loadfocus.com/account
3. Go to **Manage Jenkins > Plugins > Available plugins**
4. Search for and install **JMeter Load Testing in the Cloud for CI/CD by LoadFocus**
5. Go to **Manage Jenkins > Credentials** and add a credential of kind **LoadFocus.com API key** (or a standard **Secret text** holding the key)
<p align="center">
<img src="https://d2woeiihr4s5r6.cloudfront.net/jenkins/load-testing-ci-cd-plugin-add-credentials-loadfocus.png"
  alt="JMeter Load Testing Add Credentials API key"
 height="189">
</p>
6. Click Test LoadFocus API key button to make sure the API key is working properly.
<p align="center">
<img src="https://d2woeiihr4s5r6.cloudfront.net/jenkins/load-testing-ci-cd-plugin-API-key-loadfocus.png"
  alt="JMeterLoad Testing API key Test"
 height="189"></p>

### Usage
How to use JMeter Load Testing in the Cloud CI/CD Plugin for Post-build load tests:
* Note: All Completed load tests from your [LoadFocus](https://loadfocus.com) account will be available in the plugin.

1. Create a New Job or Configure an exiting one. 
2. In the Post-build Section, look for the **JMeter Load Testing in Cloud by LoadFocus** option and select the checkbox. See the screenshot below:
<p align="center">
<img src="https://d2woeiihr4s5r6.cloudfront.net/jenkins/load-testing-ci-cd-plugin-add-load-testing-test-loadfocus.png"
  alt="Load Testing Add Post Build Action"
 height="289"></p>
3. Choose the JMeter load test and how the build should be judged (any combination; the worst result wins):
   * **Error percentage** and **average response time** thresholds, checked per request. Leave a field empty to skip it.
   * **Use LoadFocus verdict**: fail the build when the run misses the pass/fail thresholds configured for the test on loadfocus.com (P95/P99, error rate, throughput).

   Then click Save.
<p align="center"><img src="https://d2woeiihr4s5r6.cloudfront.net/jenkins/load-testing-ci-cd-plugin-configuration-loadfocus.jpeg"
  alt="JMeter Load Testing in the Cloud CI/CD Plugin Configuration LoadFocus"
 height="289"></p>
4. Run the Job and View JMeter Load Test Results in the job log
<p align="center"><img src="https://d2woeiihr4s5r6.cloudfront.net/jenkins/load-testing-ci-cd-plugin-console-log-success-loadfocus.jpeg"
  alt="JMeter Load Testing in the Cloud CI/CD Plugin Job Log Results"
 height="289"></p>
    * View the Console output and monitor the progress of your running JMeter load tests during job's Post build actions.
    * View the complete JMeter load test report of the LoadFocus.com when the job has finished.
    ```
    loadfocus.com: Test: checkout
    loadfocus.com: Config: build UNSTABLE if error percentage is greater than 3%
    loadfocus.com: Config: build FAILURE if the LoadFocus verdict (thresholds set on loadfocus.com) fails
    loadfocus.com: Run #42 started: https://loadfocus.com/jmetertests?testrunname=checkout&testrunid=42
    loadfocus.com: Run #42 tagged "Jenkins checkout-pipeline #17"
    loadfocus.com: Run state: running (35s)
    loadfocus.com: Run #42 finished
    loadfocus.com: Result: GET /api/cart: average response time 59.7 ms, errors 0.0%
    loadfocus.com: Verdict check PASS: P95 response time 310 ms (target <= 500 ms)
    loadfocus.com: Verdict: PASS
    ```
 
### Pipeline

The step `loadfocusJMeterTest` runs a JMeter test in the LoadFocus cloud, gates the build on it and returns the result. It does not need a `node` block.

```groovy
pipeline {
  agent any
  stages {
    stage('Load test') {
      steps {
        script {
          // Thresholds live in the Jenkinsfile: saved to the test on loadfocus.com, then checked after the run
          def lt = loadfocusJMeterTest testId: 'checkout', credentialsId: 'loadfocus-api-key',
                                     p95Ms: 500, errorRatePct: 1, releaseTag: "${env.GIT_COMMIT?.take(8)}"
          echo "LoadFocus run #${lt.testrunid}: ${lt.verdict}, p95 ${lt.metrics.p95Ms} ms, report ${lt.reportUrl}"
        }
      }
    }
  }
}
```

The step returns a map: `testrunname`, `testrunid`, `result` (`SUCCESS`/`UNSTABLE`), `verdict` (`pass`/`fail`/`none`), `reportUrl` and `metrics` (`p95Ms`, `p99Ms`, `errorRatePct`, `rps`, plus `meanMs` when per-request thresholds are used).

All options:

| Option | Default | Meaning |
|---|---|---|
| `testId` | (required) | Name of the LoadFocus JMeter test |
| `credentialsId` | the default key | ID of a **LoadFocus.com API key** credential or of a standard **Secret text** credential holding the key |
| `p95Ms`, `p99Ms`, `errorRatePct`, `minRps` | not set | Pass/fail thresholds for the whole run. When any is set, they replace the test's thresholds on loadfocus.com before the run (unset ones are cleared) and the verdict is checked |
| `useVerdict` | `false` | Check the LoadFocus verdict against the thresholds configured for the test on loadfocus.com. No thresholds enabled marks the build UNSTABLE; a threshold that could not be evaluated fails it |
| `errorUnstableThreshold`, `errorFailedThreshold` | not set | Per-request error percentage (0-100) above which the build is UNSTABLE / FAILURE |
| `responseTimeUnstableThreshold`, `responseTimeFailedThreshold` | not set | Per-request average response time in ms above which the build is UNSTABLE / FAILURE |
| `tagRun` | `true` | Label the LoadFocus run with this build (shown on its results and trend pages) |
| `releaseTag` | `Jenkins <job> #<build>` | Custom label for the run, e.g. a version or commit (max 64 characters) |
| `timeoutMinutes` | `120` | Fail if the run has not finished in time |
| `shareReport` | `false` | Create a public share link for the run (anyone with the link can view it) |

At least one threshold or `useVerdict` must be set.

How the step ends:
* **FAILURE** (a failed threshold or verdict, a run that fails, a timeout, or a test that cannot be started) fails the step, so later stages do not run. Wrap it in `catchError` to continue anyway.
* **UNSTABLE** marks the build and the stage, and the pipeline continues.
* The step never reports on an older run. If another build starts the same test at the same moment, it fails rather than guess which run is its own.
* Short LoadFocus API outages (for example during a LoadFocus deploy) are retried for about 5 minutes.
* Aborting the Jenkins build does not stop the cloud run; the log prints its link.

### JMeter Load Test Results & Reports  
1. View the JMeter load test report
<p align="center"><img src="https://d2woeiihr4s5r6.cloudfront.net/jenkins/whitelabel-reports-test-presets-loadfocus.jpeg"
  alt="JMeter Load Testing in the Cloud CI/CD Plugin Whitelabel Results"
 height="389"></p>
2. Print the JMeter load test report to a PDF file
<p align="center">
<img src="https://d2woeiihr4s5r6.cloudfront.net/jenkins/whitelabel-reports-test-print.jpeg"
  alt="JMeter Load Testing in the Cloud CI/CD Plugin PDF report"
 height="389"></p>