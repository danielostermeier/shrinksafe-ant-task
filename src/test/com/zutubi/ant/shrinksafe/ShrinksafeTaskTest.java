package com.zutubi.ant.shrinksafe;

import org.apache.tools.ant.BuildFileTest;

import java.io.File;
import java.io.IOException;

public class ShrinksafeTaskTest extends BuildFileTest
{
    private File tmp;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        // load the local test build.xml.
        configureProject("src/test/com/zutubi/ant/shrinksafe/build.xml");

        tmp = createTempDirectory();
        setProperty("out.dir", tmp.getCanonicalPath());

    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testSourceDirRequired() throws IOException
    {
        expectSpecificBuildException("testSourceDirRequired", "source directory is missing", "a");
    }

    public void testOutputDirRequired()
    {
        expectSpecificBuildException("testOutputDirRequired", "output directory is missing", "b");
    }

    public void testForce()
    {
        setProperty("force", "true");
        executeTarget("testForce");
        executeTarget("testForce");
    }

    public void testNoForce()
    {
        setProperty("force", "false");
        executeTarget("testForce");
        expectSpecificBuildException("testForce", "target file already exists", "c");
    }

    public void testSuffix()
    {
        executeTarget("testSuffix");

        File output = new File(tmp, "sample-blah.js");
        assertTrue(output.isFile());
    }

    public void testEmptyFileset()
    {
        executeTarget("testEmptyFileset");
        assertEquals(0, tmp.list().length);
    }

    // sanity test.
    public void testShrinksafe() throws IOException
    {
        executeTarget("shrinksafe");

        File source = new File(getProjectDir(), "resources/ext-all-debug.js");
        File output = new File(tmp, "ext-all-debug.js");

        assertTrue(output.exists());
        assertTrue(source.length() > output.length());
    }

    protected void removeDirectory(File dir) throws IOException
    {
        if (dir != null)
        {
            if (dir.isDirectory())
            {
                for (File f : dir.listFiles())
                {
                    if (f.isDirectory())
                    {
                        removeDirectory(f);
                    }
                    else
                    {
                        if (!f.delete())
                        {
                            throw new IOException("Failed to delete file.");
                        }
                    }
                }
                if (!dir.delete())
                {
                    throw new IOException("Failed to delete directory.");
                }
            }
        }
    }

    protected File createTempDirectory() throws IOException
    {
        File f = File.createTempFile("temp", "dir");
        if (!f.delete())
        {
            throw new IOException("Failed to delete file.");
        }
        if (!f.mkdirs())
        {
            throw new IOException("Failed to create directory.");
        }
        return f;
    }

    protected void setProperty(String key, String value)
    {
        getProject().setNewProperty(key, value);
    }
}
