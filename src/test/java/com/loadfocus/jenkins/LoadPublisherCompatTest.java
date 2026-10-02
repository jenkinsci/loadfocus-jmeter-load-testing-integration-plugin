package com.loadfocus.jenkins;

import com.loadfocus.jenkins.api.LoadAPI;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Job config, build record and global config as 1.0.0 wrote them (the API key itself stored in the job). */
@WithJenkins
class LoadPublisherCompatTest {

    @Test
    @LocalData
    void oldDataLoads(JenkinsRule j) throws Exception {
        FreeStyleProject p = j.jenkins.getItemByFullName("old", FreeStyleProject.class);
        assertNotNull(p, "1.0.0 job must load");
        LoadPublisher pub = p.getPublishersList().get(LoadPublisher.class);
        assertNotNull(pub);
        assertEquals("checkout", pub.getTestId());
        assertEquals(5, pub.getErrorFailedThreshold());
        assertEquals(0, pub.getErrorUnstableThreshold(), "0 stays an active threshold");
        assertEquals(1000, pub.getResponseTimeFailedThreshold());
        assertEquals(500, pub.getResponseTimeUnstableThreshold());
        assertEquals(120, pub.getTimeoutMinutes());
        assertNull(pub.getCredentialsId());

        FreeStyleBuild old = p.getBuildByNumber(1);
        LoadBuildAction a = old.getAction(LoadBuildAction.class);
        assertNotNull(a, "1.0.0 build action must load");
        assertSame(old, a.getOwner());
        assertEquals(LoadAPI.DEFAULT_BASE_URL + "jmetertests?testrunname=checkout&testrunid=5", a.getReportUrl());

        String page = j.createWebClient().getPage(old, "loadfocus").getWebResponse().getContentAsString();
        assertTrue(page.contains("testrunid=5"));
        assertFalse(page.contains(FakeLoadFocus.KEY), "old stored key must not be rendered");

        old.save(); // what Manage Old Data > Upgrade does
        String xml = Files.readString(new File(old.getRootDir(), "build.xml").toPath(), StandardCharsets.UTF_8);
        assertFalse(xml.contains(FakeLoadFocus.KEY), "re-saving an old build scrubs the stored key");
    }

    @Test
    @LocalData("oldDataLoads")
    void oldJobStillRunsWithTheKeyItStored(JenkinsRule j) throws Exception {
        // 1.0.0 used the key stored in the job directly, even with no credential configured: keep that working.
        try (FakeLoadFocus lf = new FakeLoadFocus()) {
            LoadPublisher.baseUrl = lf.baseUrl();
            LoadPublisher.pollMillis = 10;
            FreeStyleProject p = j.jenkins.getItemByFullName("old", FreeStyleProject.class);
            FreeStyleBuild b = j.buildAndAssertSuccess(p);
            j.assertLogContains("Run #8 started", b);
            j.assertLogNotContains(FakeLoadFocus.KEY, b);
            j.assertLogContains("Config: build UNSTABLE if error percentage is greater than 0%", b);
        } finally {
            LoadPublisher.baseUrl = LoadAPI.DEFAULT_BASE_URL;
        }
    }

    @Test
    @LocalData("oldDataLoads")
    void oldGlobalDefaultKeyMapsToItsCredential(JenkinsRule j) throws Exception {
        com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getCredentials().add(
                new com.loadfocus.jenkins.impl.LoadCredentialImpl("other-key-999999", "other"));
        com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getCredentials().add(
                new com.loadfocus.jenkins.impl.LoadCredentialImpl(FakeLoadFocus.KEY, "stored default"));
        LoadPublisher.LoadPerformancePublisherDescriptor d = j.jenkins.getDescriptorByType(LoadPublisher.LoadPerformancePublisherDescriptor.class);
        assertEquals("lfke...abcdef", d.getDefaultCredentialsId(), "the key 1.0.0 stored globally selects its credential, not the first one");
    }
}
