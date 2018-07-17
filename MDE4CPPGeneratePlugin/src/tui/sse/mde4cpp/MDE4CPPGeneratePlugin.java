package tui.sse.mde4cpp;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * This class represents the MDE4CPPCompile-Plugin extension to Gradle.
 *
 */
public class MDE4CPPGeneratePlugin implements Plugin<Project>
{
	@Override
	public void apply(Project project)
	{
//		project.getTasks().create("generate", MDE4CPPGenerate.class, (task) ->
//		{
//			task.setProjectFolder(new File(".").getAbsolutePath());
//		});
	}
}