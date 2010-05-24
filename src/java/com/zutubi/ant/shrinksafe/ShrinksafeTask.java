package com.zutubi.ant.shrinksafe;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.dojotoolkit.shrinksafe.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ResourceBundle;

/**
 * An ant task wrapper for the shrinksafe javascript compressor.
 */
public class ShrinksafeTask extends MatchingTask
{
    private final ResourceBundle I18N;

    /**
     * Force overwrite of existing files.
     */
    private boolean force;
    /**
     * Suffix for compacted files. Optional
     */
    private String suffix;
    /**
     * Directory into which the compacted files are written.
     */
    private File outputDir;
    /**
     * Directory from which the javascript files to be compacted are taken.
     */
    private File sourceDir;

    public void setForce(boolean force)
    {
        this.force = force;
    }

    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    public void setOutputDir(File outputDir)
    {
        this.outputDir = outputDir;
    }

    public void setSourceDir(File sourceDir)
    {
        this.sourceDir = sourceDir;
    }

    public ShrinksafeTask() throws IOException
    {
        I18N = ResourceBundle.getBundle("com.zutubi.ant.shrinksafe.messages");
    }

    @Override
    public void execute() throws BuildException
    {
        if (sourceDir == null)
        {
            throw new BuildException(I18N.getString("src.dir.required"));
        }

        if (outputDir == null)
        {
            throw new BuildException(I18N.getString("out.dir.required"));
        }

        try
        {
            DirectoryScanner scanner = super.getDirectoryScanner(sourceDir);

            String[] files = scanner.getIncludedFiles();

            for (String file : files)
            {
                File sourceFile = new File(sourceDir, file);

                String outputFileName = file;
                if (suffix != null)
                {
                    // add the suffix prior ot the file extension.
                    int i = outputFileName.lastIndexOf('.');
                    if (i != -1)
                    {
                        outputFileName = outputFileName.substring(0, i) + "-" + suffix + outputFileName.substring(i);
                    }
                    else
                    {
                        outputFileName = outputFileName + "-" + suffix;
                    }
                }
                File outputFile = new File(outputDir, outputFileName);

                if (outputFile.exists() && !force)
                {
                    throw new BuildException(I18N.getString("out.file.exists"));
                }

                if ((outputFile.exists() && !outputFile.delete()) || !outputFile.createNewFile())
                {
                    throw new BuildException(I18N.getString("out.file.create.failed"));
                }

                PrintStream originalSystemOut = System.out;
                try
                {
                    // capture the output from the shrinksafe processing into the output file.
                    System.setOut(new PrintStream(new FileOutputStream(outputFile), true));

                    // run shrinksafe.
                    Main.main(args(sourceFile));
                }
                finally
                {
                    // stop capturing the output.
                    System.setOut(originalSystemOut);
                }
            }
        }
        catch (IOException ioe)
        {
            throw new BuildException(ioe);
        }
    }

    // generate the arguments for the shrinksafe package.
    private String[] args(File sourceFile) throws IOException
    {
        return new String[]{sourceFile.getCanonicalPath()};
    }
}
