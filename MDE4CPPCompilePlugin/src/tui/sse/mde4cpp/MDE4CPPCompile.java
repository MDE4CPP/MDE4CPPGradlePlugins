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

	public String getPathToCMakeList()
	{
		return pathToCMakeList;
	}

	public void setPathToCMakeList(String pathToCMakeList)
	{
		this.pathToCMakeList = pathToCMakeList;
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

	@TaskAction
	void executeCompile()
	{
		Project project = getProject();
		if (GradlePropertyAnalyser.isDebugBuildModeRequestet(project))
		{
			compileBuildMode(BUILD_MODE.DEBUG);
		}
		if (GradlePropertyAnalyser.isReleaseBuildModeRequested(project))
		{
			compileBuildMode(BUILD_MODE.RELEASE);
		}
	}
}