/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration.buildSteps;

import hudson.model.AbstractBuild;
import hudson.model.BallColor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.projectConfiguration.BuildSummaryAction;
import org.jenkinsci.plugins.projectConfiguration.exceptions.ScriptPluginInteractionException;

/**
 *
 * @author gabi
 */
public final class BuildStepInfo {
    
    protected String status;
    protected String img;
    protected String details;
    
    public BuildStepInfo(AbstractBuild<?, ?> build, StepNameEnum stepName) throws ScriptPluginInteractionException
    {
        this.initInfo(build, stepName);
    }
    
    public String getStatus()
    {
        return this.status;
    }
    
    public String getImg()
    {
        return this.img;
    }
    
    public String getDetails()
    {
        return this.details;
    }
    
    public void initInfo(AbstractBuild<?, ?> build, StepNameEnum stepName) throws ScriptPluginInteractionException
    {
        try 
        {
            File buildWorkspacePath = new File(build.getWorkspace().toURI().getPath() + "/build_" + build.getNumber() + "/");
            if (buildWorkspacePath.isDirectory() == false)
            {
                throw new IOException("Build workspace path not found under: " + buildWorkspacePath.getPath());
            }
            
            File f_start = new File(buildWorkspacePath + stepName.name().toLowerCase() + "_start_" + build.getNumber() + ".info");
            File f_finish = new File(buildWorkspacePath + stepName.name().toLowerCase() + "_finish_" + build.getNumber() + ".info");
            
            if (f_start.exists() == true)
            {
                this.status = "In progress";
                this.img = BuildSummaryAction.statusPicsDir + BallColor.GREY_ANIME.getImage();
                this.details = null;
            }
            else if (f_finish.exists() == true)
            {
                InputStream fis;
                BufferedReader br;
                String line;

                fis = new FileInputStream(f_finish);
                br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
                
                while ((line = br.readLine()) != null) 
                {
                    if (line.startsWith("STATUS=") == true)
                    {
                        int indexOfEquels = line.indexOf('=') + 1;
                        this.status = line.substring(indexOfEquels);
                    }
                    else if (line.startsWith("DETAILS=") == true)
                    {
                        int indexOfEquels = line.indexOf('=') + 1;
                        this.details = line.substring(indexOfEquels);
                    }
                }
                
                if (this.details == null || this.status == null)
                {
                    throw new NullPointerException("this.details=" + this.details + "\n"
                            + "this.status=" + this.status + "\n");
                }
                
                if (this.status.compareTo("SUCCESS") == 0)
                {
                    this.img =  BuildSummaryAction.statusPicsDir + BallColor.BLUE.getImage();
                }
                else if(this.status.compareTo("ERROR") == 0)
                {
                    this.img =  BuildSummaryAction.statusPicsDir + BallColor.RED.getImage();
                }
                else 
                {
                    throw new ScriptPluginInteractionException("Unknown status value: " + this.status);
                }
                
                br.close();
            }
            else
            {
                
                this.status = "N/A";
                this.img = BuildSummaryAction.statusPicsDir + BallColor.GREY.getImage();
                this.details = null;
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(BuildStepInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
