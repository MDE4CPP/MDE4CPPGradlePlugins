package tui.sse.mde4cpp;

import java.io.File;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

public class MDE4CPPCompile extends DefaultTask
{
	private String projectFolder = null;

	public String getProjectFolder()
	{
		return projectFolder;
	}

	public void setProjectFolder(String projectFolder)
	{
		this.projectFolder = projectFolder;
	}

	private void checkInput()
	{
		if (projectFolder == null)
		{
			throw new GradleException("Property 'projectFolder' is not set!\r\nConfigure the project folder containing 'CMakeLists.txt' with project build instructions.");
		}
		
		File folder = new File(projectFolder);
		if (!folder.isDirectory())
		{
			throw new GradleException("The folder '" + folder.getAbsolutePath() + "' does not exists!");
		}

		File cmakeFile = new File(projectFolder + File.separator + "CMakeLists.txt");
		if (!cmakeFile.isFile())
		{
			throw new GradleException("The folder '" + folder.getAbsolutePath() + "' does not contain a 'CMakeLists.txt' file!");
		}
	}
	
	private void compileBuildMode(BUILD_MODE buildMode)
	{
		String buildPath = projectFolder + File.separator + ".cmake" + File.separator + buildMode.getName();
		File projectFolderFile = new File(projectFolder);
		File folder = new File(buildPath);
		if (!folder.exists())
		{
			folder.mkdirs();
		}

		List<String> command = CommandBuilder.getCMakeCommand(buildMode, projectFolderFile);
		String startingMessage = "Compiling " + projectFolderFile.getName() + " with " + buildMode.getName() + " options";
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
		checkInput();
		
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