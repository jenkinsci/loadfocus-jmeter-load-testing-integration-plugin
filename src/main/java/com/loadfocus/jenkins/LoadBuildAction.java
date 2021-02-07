package com.loadfocus.jenkins;

import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.AbstractBuild;
import hudson.util.Secret;

public class LoadBuildAction implements HealthReportingAction {
	private final AbstractBuild<?, ?> build;
	
	private String testrunid = null;
	private String testrunname = null;
	private Secret apikey = null;

	public LoadBuildAction(AbstractBuild<?, ?> build, String testrunname, String testrunid, Secret apikey) {
		this.build = build;
		this.testrunname = testrunname;
		this.testrunid = testrunid;
		this.apikey = apikey;
	}
	
	public AbstractBuild<?, ?> getOwner() {
        return build;
    }
	
	public String getIconFileName() {
		return "/plugin/jmeter-load-test-loadfocus/images/icon48.png";
	}

	public String getDisplayName() {
		return "LoadFocus.com Results";
	}

	public String getUrlName() {
		return "loadfocus";
	}

	public HealthReport getBuildHealth() {
		return null;
	}

	public String getTestrunid() {
		return testrunid;
	}

	public void setTestrunid(String testrunid) {
		this.testrunid = testrunid;
	}

	public String getTestrunname() {
		return testrunname;
	}

	public void setTestrunname(String testrunname) {
		this.testrunname = testrunname;
	}

	public String getApikey() {
		return apikey.getPlainText();
	}

	public void setApikey(Secret apikey) {
		this.apikey = apikey;
	}
}
