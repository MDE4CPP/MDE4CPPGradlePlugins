package tui.sse.mde4cpp;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.gradle.api.Project;

class CommandBuilder
{
	private static String getCMakeGenerator()
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

	static private List<String> initialCommandList()
	{
		List<String> commandList = new LinkedList<>();
		if (isWindowsSystem())
		{
			commandList.add("cmd");
			commandList.add("/c");
		}
		return commandList;
	}

	private static boolean isWindowsSystem()
	{
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

	static List<String> getCMakeCommand(BUILD_MODE buildMode, File projectFolder)
	{
		List<String> commandList = CommandBuilder.initialCommandList();
		commandList.add("cmake -G \"" + getCMakeGenerator() + "\" -D CMAKE_BUILD_TYPE=" + buildMode.getName() + " " + projectFolder.getAbsolutePath());
		return commandList;
	}

	static List<String> getMakeCommand(Project project)
	{
		List<String> commandList = CommandBuilder.initialCommandList();
		commandList.add(getMakeTool() + " install" + GradlePropertyAnalyser.getParallelJobsFlag(project));
		return commandList;
	}

	static String getMakeTool()
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
}
