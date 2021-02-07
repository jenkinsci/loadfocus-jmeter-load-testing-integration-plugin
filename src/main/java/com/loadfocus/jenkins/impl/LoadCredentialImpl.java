package com.loadfocus.jenkins.impl;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.loadfocus.jenkins.AbstractCredential;
import com.loadfocus.jenkins.api.LoadAPI;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;

public class LoadCredentialImpl extends AbstractCredential {
	private static final long serialVersionUID = 1L;
	private final Secret apiKey;
    private final String description;

    @DataBoundConstructor
    public LoadCredentialImpl(String apiKey, String description) {
        this.apiKey = Secret.fromString(apiKey);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getApiKey() {
        return apiKey.getPlainText();
    }

    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.LoadCredential_DisplayName();
        }

        @POST
        public FormValidation doTestConnection(@QueryParameter("apiKey") final Secret apiKey) throws MessagingException, IOException, JSONException, ServletException {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return FormValidation.ok();
            }

        	return checkLoadKey(apiKey);
        }

        @POST
        public FormValidation doTestExistingConnection(@QueryParameter("apiKey") final Secret apiKey) throws MessagingException, IOException, JSONException, ServletException {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return FormValidation.ok();
            }

            return checkLoadKey(apiKey);
        }
        
        private FormValidation checkLoadKey(final Secret apiKey) throws JSONException, IOException, ServletException {
        	LoadAPI ldr = new LoadAPI(apiKey);
            if (ldr.isValidApiKey()) {
                return FormValidation.okWithMarkup("Valid API Key");
            } else {
                return FormValidation.errorWithMarkup("Invalid API Key");
            }
        }

    }
    
}
