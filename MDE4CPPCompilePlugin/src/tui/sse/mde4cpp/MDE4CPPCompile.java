package tui.sse.mde4cpp;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.Project;

public class MDE4CPPCompile extends DefaultTask
{
	private String pathToCMakeList;

	public MDE4CPPCompile()
	{
		pathToCMakeList = new File(".").getAbsolutePath();
	}

	public String getPathToCMakeList()
	{
		return pathToCMakeList;
	}

	public void setPathToCMakeList(String pathToCMakeList)
	{
		this.pathToCMakeList = pathToCMakeList;
	}

	private boolean isDebugModeActive()
	{
		Project project = getProject();
		return (project.hasProperty("DEBUG") && project.property("DEBUG") != "0")
				|| (project.hasProperty("D") && project.property("D") != "0") || (!project.hasProperty("RELEASE")
						&& !project.hasProperty("R") && !project.hasProperty("DEBUG") && !project.hasProperty("D"));
	}

	private boolean isReleaseModeActive()
	{
		Project project = getProject();
		return (project.hasProperty("RELEASE") && project.property("RELEASE") != "0")
				|| (project.hasProperty("R") && project.property("R") != "0") || (!project.hasProperty("RELEASE")
						&& !project.hasProperty("R") && !project.hasProperty("DEBUG") && !project.hasProperty("D"));
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

	private String getMakeTool()
	{
		if (isWindowsSystem())
		{
			return "mingw32-make";
		}
		else
		{
			return "make";
		}
	}
	
	private String getMakeCommand()
	{
		Project project = getProject();
		String parallel = "";
		if (project.hasProperty("make_parallel_jobs"))
		{
			parallel = " -j" + project.property("make_parallel_jobs");
		}

		return getMakeTool() + " install" + parallel;
	}

	private boolean executeProcess(List<String> commandList, File workingDir, String message)
	{
		try
		{
			ProcessBuilder processBuilder = new ProcessBuilder(commandList);
			processBuilder.directory(workingDir);

			Process process = processBuilder.start();
			ProcessInputStreamThread inputThread = new ProcessInputStreamThread(process.getInputStream(), false, message);
			ProcessInputStreamThread errorThread = new ProcessInputStreamThread(process.getErrorStream(), true, null);
			inputThread.start();
			errorThread.start();

			int code = process.waitFor();
			
			return code == 0;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	private void compileBuildMode(BUILD_MODE buildMode)
	{
		String buildPath = pathToCMakeList + File.separator + ".cmake" + File.separator + buildMode.getName();
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
		File projectFolder = new File(pathToCMakeList);
		commandList.add("cmake -G \"" + getCMakeGenerator() + "\" -D CMAKE_BUILD_TYPE=" + buildMode.getName() + " "
				+ projectFolder.getAbsolutePath());
		
		String message = "Compiling " + projectFolder.getName() + " with " + buildMode.getName() + " options";
		if (!executeProcess(commandList, folder, message))
		{
			throw new GradleException("Compilation failed during cmake execution!");
		}

		commandList.set(commandList.size() - 1, getMakeCommand());
		if(!executeProcess(commandList, folder, null))
		{
			throw new GradleException("Compilation failed during " + getMakeTool() + " execution!");
		}
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