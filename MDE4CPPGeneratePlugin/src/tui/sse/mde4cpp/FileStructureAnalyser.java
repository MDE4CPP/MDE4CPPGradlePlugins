package tui.sse.mde4cpp;

import java.io.File;

import javax.swing.JOptionPane;

import org.gradle.api.GradleException;


/**
 * This class analyzed the file structure around the model file.<br>
 * Following file structure is necessary to use the MDE4CPP generators without additional manual changes:<br>
 * 
 * Project root folder<br>
 * <ul>
 * 	<li>model
 * 	<ul>
 * 	<li>model.uml / model.ecore</li>
 * 	<li>...</li>
 * 	</ul></li>
 * 	<li>src_gen
 *  <ul>
 * 		<li>generated source code</li>
 *  </ul></li>
 * </ul>
 *
 */
public class FileStructureAnalyser 
{
	/**
	 * check, if the model file is 
	 * 
	 * @param modelFile - the model file
	 * @return true, if file structure is fine, otherwise false
	 */
	static public boolean checkFileStructure(File modelFile)
	{
		if (isParentFolderCalledModel(modelFile))
		{
			return true;
		}
		
		if (!requestFileStructureAdaption(modelFile))
		{
			return false;
		}
		
		return moveModelIntoSubfolder(modelFile);
	}
	
	
	/**
	 * checks, if model folder is called 'model'
	 */
	static private boolean isParentFolderCalledModel(File file)
	{
		File parentFile = file.getParentFile();
		return parentFile.isDirectory() && (parentFile.getName().compareTo("model") == 0);
	}
	
	/**
	 * inform the user, that the file structure is not well defined and ask, if the file structure should be changed
	 * @param modelFile - the model file
	 * @return true for change model structure, otherwise false
	 */
	static private boolean requestFileStructureAdaption(File modelFile)
	{
		String whiteSpace = "    ";
		String message = "To use the MDE4CPP generators without additional manual modification, the following file structure is necessary:" + System.lineSeparator()
				+ whiteSpace + modelFile.getParent() + System.lineSeparator()
				+ whiteSpace + whiteSpace + "model" + System.lineSeparator()
				+ whiteSpace + whiteSpace + whiteSpace + modelFile.getName() + System.lineSeparator();
		
		for (File file : modelFile.getParentFile().listFiles()) 
		{
			if (file.getName().compareTo(".project") == 0)
			{
				message += whiteSpace + whiteSpace + whiteSpace + file.getName() + System.lineSeparator();
			}
			else if (file.getName().endsWith(".aird"))
			{
				message += whiteSpace + whiteSpace + whiteSpace + file.getName() + System.lineSeparator();			
			}
		}
		
		message	+= whiteSpace + whiteSpace + "src_gen" + System.lineSeparator()
				+ System.lineSeparator()
				+ "Do you want to move the model file to the subfolder 'model'?";
			
		int result = JOptionPane.showConfirmDialog(null, message, "File structure issue", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (result == JOptionPane.YES_OPTION)
		{
			return true;
		}
		else if (result == JOptionPane.NO_OPTION)
		{
			return false;
		}
		else
		{
			throw new GradleException("Generation canceld by user.");
		}
	}
	
	static private boolean moveModelIntoSubfolder(File modelFile)
	{
		File parentFolder = modelFile.getParentFile();

		String modelFolderPath = parentFolder.getAbsolutePath() + File.separator + "model"; 
		File modelFolder = new File(modelFolderPath);
		if (!modelFolder.exists())
		{
			modelFolder.mkdir();
		}
		moveFile(modelFile, modelFolderPath);
		
		for (File file : parentFolder.listFiles()) 
		{
			if (file.getName().compareTo(".project") == 0)
			{
				moveFile(file, modelFolderPath);
			}
			else if (file.getName().endsWith(".aird"))
			{
				moveFile(file, modelFolderPath);				
			}
		}	
				
		return true;
	}
	
	static private void moveFile(File file, String targetPath)
	{

		file.renameTo(new File(targetPath + File.separator + file.getName()));
	}
}
