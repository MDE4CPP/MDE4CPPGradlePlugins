package tui.sse.mde4cpp;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

/**
 * Gradle task class for generating C++ projects of Ecore and UML models using MDE4CPP Code Generator<br>
 * Configuration options:<br>
 * 1. using parameter on task call<br>
 * - PModel={path} - model path<br>
 * - PStructureOnly=1 / -PSO=1 to use UML4CPP on UML models, otherwise fUML4CPP is used for UML models, no effect on Ecore models<br>
 * <br>
 * 2. inside task declaration:<br>
 * - essential parameters:<br>
 * \t- String modelFilePath - model path<br>
 * \t- Boolean structureOnly - if set to 1, UML4CPP is used on UML models, otherwise fUML4CPP is used for UML models, no effect on Ecore models<br>
 * <br>
 * - optional parameters (will be calculated if not configured) 
 * \t\tIf generator is not configured, ecore4CPP generator is used for .ecore models and UML4CPP is used for .uml models
 * \t- String targetFolder - target folder for generated source code. Default folder is {model folder}/../src_gen.<br><br>
 * Further information:<br>
 * - Environment variable 'MDE4CPP_HOME' has to be set to calculate generator path ($MDE4CPP_HOME/application/generator/*.jar)<br>
 * - Working directory is the folder containing the model file.<br>
 * - Assumed folder structure:<br>
 * \troot project folder<br>
 * \t\tmodel folder<br>
 * \t\tgenerated source code folder<br><br>
 * More information can be found under https://github.com/MDE4CPP/gradlePlugins and http://sse.tu-ilmenau.de/mde4cpp
 *
 */
public class MDE4CPPGenerate extends DefaultTask
{
	private String m_modelFilePath = null;
	private boolean m_structureOnly = false;

	private String m_generatorPath = null;
	private String m_targetFolder = null;

	private GENERATOR m_generator = GENERATOR.ECORE4CPP;
	private String m_workingDirectory = null;
	private String m_modelFileName = null;
	
	/**
	 * Set model file path
	 * 
	 * @param modelFilePath : String - specify model file path
	 */
	public void setModelFilePath(String modelFilePath)
	{
		this.m_modelFilePath = modelFilePath;
	}
	
	/**
	 * indicates, that UML4CPP should be used to generate only the structure of the model, not the activity execution part
	 * 
	 * @param structureOnly : Boolean - true to generate structure only, false to generate structure + execution part 
	 */
	public void setStructureOnly(boolean structureOnly)
	{
		m_structureOnly = structureOnly;
	}
	

	/**
	 * Set target folder
	 * 
	 * @param generatorPath : String - specify target folder for generated code
	 */
	public void setGeneratorPath(String generatorPath)
	{
		m_generatorPath = generatorPath;
	}
	
	/**
	 * Set target folder
	 * 
	 * @param targetFolder : String - specify target folder for generated code
	 */
	public void setTargetFolder(String targetFolder)
	{
		m_targetFolder = targetFolder;
	}

	
	/**
	 * check and complete configuration
	 * 
	 */
	private void configure()
	{
		// check model file - path configured and existing
		if (m_modelFilePath == null)
		{
			m_modelFilePath = GradlePropertyAnalyser.getModelParameter(getProject());
			if (m_modelFilePath == null)
			{
				throw new GradleException("Property 'modelFilePath' is not set!\r\n" + "Configure the path to the model inside the gradle task or use parameter 'Model' (-PModel=<path>).");
			}
		}

		File file = new File(m_modelFilePath);
		if (!file.isFile())
		{
			throw new GradleException("The file '" + file.getAbsolutePath() + "' does not exists!");
		}
		if (m_workingDirectory == null)
		{
			m_workingDirectory = file.getParent();
		}
		if (m_modelFileName == null)
		{
			m_modelFileName = file.getName();
		}

		if (GradlePropertyAnalyser.isStructuredOnlyRequested(getProject()))
		{
			m_structureOnly = true;
		}
		
		int index = m_modelFilePath.lastIndexOf('.');
		if (index == -1) 
		{
			throw new GradleException("The file '" + file.getAbsolutePath() + "' is not supported! Only '.ecore' and '.uml' files are supported!");
		}
		String extension = m_modelFilePath.substring(index+1);
		
		if (extension.compareTo("ecore") == 0)
		{
			m_generator = GENERATOR.ECORE4CPP;
		}
		else if (extension.compareTo("uml") == 0 && !m_structureOnly)
		{
			m_generator = GENERATOR.FUML4CPP;
		}
		else if (extension.compareTo("uml") == 0 && m_structureOnly)
		{
			m_generator = GENERATOR.UML4CPP;
		}
		else
		{
			throw new GradleException("The file extension '" + extension + "' is not supported! Only '.ecore' and '.uml' models are supported!");			
		}
		
		if (m_generatorPath != null)
		{
			m_generator.setPath(m_generatorPath);
		}
		
		// check generator
		file = new File(m_generator.getPath());
		if (!file.isFile())
		{
			if (m_generatorPath == null)
			{
				throw new GradleException("Generator '" + m_generator.getName() + "' can not be found!" + 
											System.lineSeparator() + "Expected path: '" + m_generator.getPath() +"'." +
											System.lineSeparator() + "Please set 'MDE4CPP_HOME' correctly or use property 'generatorPath' for manual configuration.");
			}
			else
			{
				throw new GradleException("User specified generator path '" + m_generatorPath + "' is not a file!");
			}
		}
		
		if (m_targetFolder == null)
		{
			m_targetFolder = "../src_gen";
		}
		else 
		{
			file = new File(m_workingDirectory + File.separator + m_targetFolder);
			if (!file.isDirectory())
			{
				file.mkdirs();
				if (!file.exists())
				{
					throw new GradleException("Could not create target folder '" + file.getAbsolutePath() + "'");
				}
			}
		}
	}

	/**
	 * generate C++ code for configured model model
	 */
	private void generateModel()
	{		
		List<String> command = new LinkedList<String>();
		command.add("java");
		command.add("-jar");
		command.add(m_generator.getPath());
		command.add(m_modelFileName);
		command.add(m_targetFolder);
		String startingMessage = "Generating model " + m_modelFileName + " using generator " + m_generator.getName();
		
		executeGenerateProcess(command, m_workingDirectory, startingMessage);		
	}

	/**
	 * execute generation
	 * 
	 * @param List<String> commands
	 * @param String working directory
	 * @param String starting message
	 */
	private void executeGenerateProcess(List<String> commandList, String workingDir, String startingMessage)
	{
		try
		{
			ProcessBuilder processBuilder = new ProcessBuilder(commandList);
			processBuilder.directory(new File(workingDir));

			Process process = processBuilder.start();
			ProcessInputStreamThread inputThread = new ProcessInputStreamThread(process.getInputStream(), false);
			inputThread.setStartingMessage(startingMessage);
			ProcessInputStreamThread errorThread = new ProcessInputStreamThread(process.getErrorStream(), true);
			inputThread.start();
			errorThread.start();

			int code = process.waitFor();
			if (code != 0)
			{
				throw new GradleException("Generator execution failed!");	
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new GradleException("Generator execution failed!" + e.getMessage());
		}
	}

	/**
	 * entry point for this task
	 */
	@TaskAction
	void executeGenerate()
	{
		configure();
		generateModel();
	}
}