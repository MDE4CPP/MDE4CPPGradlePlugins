package tui.sse.mde4cpp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

class ProcessInputStreamThread extends Thread
{
	private InputStream m_stream;
	private boolean m_isErrorSteam = false;
	private String m_message;

	public ProcessInputStreamThread(InputStream steam, boolean isErrorStream, String message)
	{
		m_stream = steam;
		m_isErrorSteam = isErrorStream;
		m_message = message;
	}

	@Override
	public void run()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(m_stream));
			String line;
			if (m_message != null)
			{
				String highlighting = new String(new char[m_message.length()+4]).replace('\0', '#');
				System.out.println(highlighting);
				System.out.println("§ " + m_message + " #");
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
