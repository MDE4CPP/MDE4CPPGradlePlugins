package tui.sse.mde4cpp;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
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

	private void executeProcess(List<String> commandList, File workingDir)
	{
		try
		{
			ProcessBuilder processBuilder = new ProcessBuilder(commandList);
			processBuilder.directory(workingDir);

			Process process = processBuilder.start();
			ProcessInputStreamThread inputThread = new ProcessInputStreamThread(process.getInputStream(), false);
			ProcessInputStreamThread errorThread = new ProcessInputStreamThread(process.getErrorStream(), true);
			inputThread.start();
			errorThread.start();

			process.waitFor();
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
		commandList.add("cmake -G \"" + getCMakeGenerator() + "\" -D CMAKE_BUILD_TYPE=" + buildMode.getName() + " "
				+ new File(pathToCMakeList).getAbsolutePath());
		executeProcess(commandList, folder);

		commandList.set(commandList.size() - 1, getMakeCommand());
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

	private class ProcessInputStreamThread extends Thread
	{
		private InputStream m_stream;
		private boolean m_isErrorSteam = false;

		private ProcessInputStreamThread(InputStream steam, boolean isErrorStream)
		{
			m_stream = steam;
			m_isErrorSteam = isErrorStream;
		}

		@Override
		public void run()
		{
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(m_stream));
				String line;
				while ((line = reader.readLine()) != null)
				{
					if (m_isErrorSteam)
					{
						System.err.println(line);
					}
					else
					{
						System.out.println(line);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}