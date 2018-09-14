package tui.sse.mde4cpp;

import java.io.File;

/**
 * This enumeration represents build modes provided by the MDE4CPP framework for
 * the compilation of C++ projects.<br>
 * Two build modes are existing:
 * <ul>
 * <li>DEBUG - compile project with debug options</li>
 * <li>RELEASE - compile project with release options</li>
 * </ul>
 * Each literal includes the conform string representation, which should be used
 * for configuring MDE4CPP compilations.<br>
 */
enum GENERATOR
{
	/**
	 * This literal indicated to use ecore4CPP generator.
	 */
	ECORE4CPP("ecore4CPP"),
	/**
	 * This literal indicated to use UML4CPP generator.
	 */
	UML4CPP("UML4CPP"),
	/**
	 * This literal indicated to use fUML4CPP generator.
	 */
	FUML4CPP("fUML4CPP");
	
	/**
	 * generator name
	 */
	private String m_name;
	private String m_path;

	/**
	 * Constructor for BUILD_MODE literals configuring {@code name}
	 *
	 * @param name
	 *            conform string representation
	 */
	private GENERATOR(String name)
	{
		m_name = name;
		String mde4cppPath = System.getenv("MDE4CPP_HOME");
		if (mde4cppPath == null)
		{
			System.err.println("System environment variable 'MDE4CPP_HOME' is not set!");
			m_path = name + ".jar";
		}
		else
		{
			m_path = mde4cppPath + File.separator + "application" + File.separator + "generator" + File.separator + name + ".jar";			
		}
	}

	/**
	 * Returns string representation of a build mode, which can be directly used
	 * inside the MDE4CPP framework
	 *
	 * @return MDE4CPP conform string representation
	 */
	String getName()
	{
		return m_name;
	}
	
	/**
	 * @param path generator path
	 */
	void setPath(String path)
	{
		m_path = path;
	}
	
	/**
	 * @return generator path
	 */
	String getPath()
	{
		return m_path;
	}
}