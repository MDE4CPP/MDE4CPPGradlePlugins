package tui.sse.mde4cpp;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import java.io.File;

public class MDE4CPPCompilePlugin implements Plugin<Project>
{
	public void apply(Project project)
	{
		project.getTasks().create("compile", MDE4CPPCompile.class, (task) -> {
			task.setPathToCMakeList(new File(".").getAbsolutePath());
		});
	}
}