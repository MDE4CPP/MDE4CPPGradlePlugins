package tui.sse.mde4cpp;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * This class represents the MDE4CPPCompile-Plugin extension to Gradle.
 *
 */
public class MDE4CPPCompilePlugin implements Plugin<Project>
{
	@Override
	public void apply(Project project)
	{
		project.getTasks().create("compile", MDE4CPPCompile.class, (task) ->
		{
			task.setProjectFolder(new File(".").getAbsolutePath());
		});
	}
}