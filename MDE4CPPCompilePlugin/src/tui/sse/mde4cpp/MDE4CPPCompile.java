package tui.sse.mde4cpp;

import java.io.File;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

public class MDE4CPPCompile extends DefaultTask
{
	private String pathToCMakeList;

	public MDE4CPPCompile()
	{
		pathToCMakeList = new File(".").getAbsolutePath();
	}

	private void compileBuildMode(BUILD_MODE buildMode)
	{
		String buildPath = pathToCMakeList + File.separator + ".cmake" + File.separator + buildMode.getName();
		File projectFolder = new File(pathToCMakeList);
		File folder = new File(buildPath);
		if (!folder.exists())
		{
			folder.mkdirs();
		}
		
		List<String> command = CommandBuilder.getCMakeCommand(buildMode, projectFolder);
		String startingMessage = "Compiling " + projectFolder.getName() + " with " + buildMode.getName() + " options";				
		if (!executeProcess(command, folder, startingMessage))
		{
			throw new GradleException("Compilation failed during cmake execution!");
		}

		command = CommandBuilder.getMakeCommand(getProject());
		if (!executeProcess(command, folder, null))
		{
			throw new GradleException("Compilation failed during " + CommandBuilder.getMakeTool() + " execution!");
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

	private boolean executeProcess(List<String> commandList, File workingDir, String message)
	{
		try
		{
			ProcessBuilder processBuilder = new ProcessBuilder(commandList);
			processBuilder.directory(workingDir);

			Process process = processBuilder.start();
			ProcessInputStreamThread inputThread = new ProcessInputStreamThread(process.getInputStream(), false,
					message);
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

	public String getPathToCMakeList()
	{
		return pathToCMakeList;
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


	public void setPathToCMakeList(String pathToCMakeList)
	{
		this.pathToCMakeList = pathToCMakeList;
	}
}