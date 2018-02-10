package tui.sse.mde4cpp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

class ProcessInputStreamThread extends Thread
{
	private boolean m_isErrorSteam = false;
	private String m_startingMessage = null;
	private String m_warningExecPluginExisting = null;


	private InputStream m_stream;

	public ProcessInputStreamThread(InputStream steam, boolean isErrorStream)
	{
		m_stream = steam;
		m_isErrorSteam = isErrorStream;
	}
	
	void setStartingMessage(String startingMessage)
	{
		m_startingMessage = startingMessage;
	}
	
	public void setWarningExecPluginExisting(String warningExecPluginExisting)
	{
		m_warningExecPluginExisting = warningExecPluginExisting;
	}

	@Override
	public void run()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(m_stream));
			String line;
			if (m_warningExecPluginExisting != null)
			{
				System.out.println(m_warningExecPluginExisting);
			}
			if (m_startingMessage != null)
			{
				String highlighting = new String(new char[m_startingMessage.length() + 4]).replace('\0', '#');
				System.out.println(highlighting);
				System.out.println("# " + m_startingMessage + " #");
				System.out.println(highlighting);
			}
			while ((line = reader.readLine()) != null)
			{
				if (m_isErrorSteam)
				{
					System.err.println(line);
				}
				else
				{
					System.out.println(line);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
