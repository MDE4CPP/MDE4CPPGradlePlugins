package tui.sse.mde4cpp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.Task;
import org.gradle.api.Project;

public class MDE4CPPCompile extends DefaultTask 
{
	private enum BUILD_MODE
	{
		DEBUG("Debug"),
		RELEASE("Release");
		
		private final String name;
		
		private BUILD_MODE(String name)
		{
			this.name = name;
		}
		
		private String getName()
		{
			return name;
		}
	}
	
	private String projectName;
	private String sourceFolderName;
	private String rootFolder;
	
	
	public MDE4CPPCompile()
	{
		projectName = "";
		sourceFolderName = "src";
		rootFolder = new File(".").getAbsolutePath();
	}
	
	public String getSourceFolderName()
	{
		return sourceFolderName;
	}	
	public void setSourceFolderName(String sourceFolderName)
	{
		this.sourceFolderName = sourceFolderName;
	}

	public String getRootFolder()
	{
		return rootFolder;
	}	
	public void setRootFolder(String rootFolder)
	{
		this.rootFolder = rootFolder;
	}

	
	public String getProjectName()
	{
		return projectName;
	}	
	public void setProjectName(String projectName)
	{
		this.projectName = projectName;
	}
	
	private boolean isDebugModeActive()
	{
		Project project  = getProject();
		return (project.hasProperty("DEBUG") && project.property("DEBUG") != "0")
			|| (project.hasProperty("D") && project.property("D") != "0")
			|| (!project.hasProperty("RELEASE") && !project.hasProperty("R") && !project.hasProperty("DEBUG") && !project.hasProperty("D"));
	}
	private boolean isReleaseModeActive()
	{
		Project project  = getProject();
		return (project.hasProperty("RELEASE") && project.property("RELEASE") != "0")
			|| (project.hasProperty("R") && project.property("R") != "0")
			|| (!project.hasProperty("RELEASE") && !project.hasProperty("R") && !project.hasProperty("DEBUG") && !project.hasProperty("D"));
	}
	
	private boolean isWindowsSystem()
	{
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
	
	private String getCMakeGenerator()
	{
		if (isWindowsSystem())
		{
			return "MinGW Makefiles";
		}
		else
		{
			return "Unix Makefiles";
		}
	}
	
	private String getMakeCommand()
	{
		Project project  = getProject();
		String parallel = "";
		if (project.hasProperty("make_parallel_jobs"))
		{			
			parallel = " -j" + project.property("make_parallel_jobs");
		}
		
		if (isWindowsSystem())
		{
			return "mingw32-make install" + parallel;
		}
		else
		{
			return "make install" + parallel;
		}
	}
	
	
	private void executeProcess(List<String> commandList, File workingDir)
	{
		try
		{
			ProcessBuilder processBuilder = new ProcessBuilder(commandList);
			processBuilder.directory(workingDir);
			Process process = processBuilder.start();
	
			BufferedReader reader = new BufferedReader (new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) 
			{
				System.out.println(line);
			}
			
			process.waitFor();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void compileBuildMode(BUILD_MODE buildMode)
	{
		String projectPath = rootFolder + File.separator + sourceFolderName + File.separator + projectName;
		String buildPath = projectPath + File.separator + ".cmake" + File.separator + buildMode.getName();
		File folder = new File(buildPath);
		if (!folder.exists())
		{
			folder.mkdirs();
		}
		List<String> commandList = new LinkedList<String>();
		if (isWindowsSystem())
		{
			commandList.add("cmd");
			commandList.add("/c");
		}
		commandList.add("cmake -G \"" + getCMakeGenerator() + "\" -D CMAKE_BUILD_TYPE=" + buildMode + " " + new File(projectPath).getAbsolutePath());
		executeProcess(commandList, folder);
		
		commandList.set(commandList.size()-1, getMakeCommand());
		executeProcess(commandList, folder);
	}
	
	
	@TaskAction
	void executeCompile()
	{	
		if (isDebugModeActive())
		{
			compileBuildMode(BUILD_MODE.DEBUG);
		}
		if (isReleaseModeActive())
		{
			compileBuildMode(BUILD_MODE.RELEASE);
		}
	}
}