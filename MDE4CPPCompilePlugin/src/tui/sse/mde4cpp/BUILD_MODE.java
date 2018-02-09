package tui.sse.mde4cpp;

enum BUILD_MODE
{
	DEBUG("Debug"),
	RELEASE("Release");

	private final String name;

	private BUILD_MODE(String name)
	{
		this.name = name;
	}

	String getName()
	{
		return name;
	}
}