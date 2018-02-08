package tui.sse.mde4cpp;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import tui.sse.mde4cpp.Compile; 
import java.io.File;

public class CompilePlugin implements Plugin<Project>
{
	public void apply(Project project)
	{
		project.getTasks().create("compile", Compile.class, (task) -> {
			task.setProjectName("unknown");
			task.setSourceFolderName("src");
			task.setRootFolder(new File(".").getAbsolutePath());
		});
	}
}