package tui.sse.mde4cpp;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MDE4CPPCompilePlugin implements Plugin<Project>
{
	@Override
	public void apply(Project project)
	{
		project.getTasks().create("compile", MDE4CPPCompile.class, (task) ->
		{
			task.setPathToCMakeList(new File(".").getAbsolutePath());
		});
	}
}