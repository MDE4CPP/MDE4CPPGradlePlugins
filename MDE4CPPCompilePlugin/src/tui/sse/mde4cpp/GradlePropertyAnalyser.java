package tui.sse.mde4cpp;

import org.gradle.api.Project;

class GradlePropertyAnalyser
{
	static String getParallelJobsFlag(Project project)
	{
		if (project.hasProperty("make_parallel_jobs"))
		{
			return " -j" + project.property("make_parallel_jobs");
		}
		else
		{
			return "";
		}
	}

	static boolean isDebugBuildModeRequestet(Project project)
	{
		return (project.hasProperty("DEBUG") && project.property("DEBUG") != "0") || (project.hasProperty("D") && project.property("D") != "0")
				|| (!project.hasProperty("RELEASE") && !project.hasProperty("R") && !project.hasProperty("DEBUG") && !project.hasProperty("D"));
	}

	static boolean isReleaseBuildModeRequested(Project project)
	{
		return (project.hasProperty("RELEASE") && project.property("RELEASE") != "0") || (project.hasProperty("R") && project.property("R") != "0")
				|| (!project.hasProperty("RELEASE") && !project.hasProperty("R") && !project.hasProperty("DEBUG") && !project.hasProperty("D"));
	}
}
