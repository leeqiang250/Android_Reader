package cn.com.pyc.pbbonline.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorManager
{

	private static volatile ExecutorService sExecutor = null;

	private ExecutorManager()
	{
	}

	public static ExecutorService getInstance()
	{
		return createExecutor();
	}

	private static ExecutorService createExecutor()
	{
		if (sExecutor == null)
			sExecutor = Executors.newCachedThreadPool();
		return sExecutor;
	}

	public static void shutdownNow()
	{
		if (sExecutor != null)
		{
			sExecutor.shutdownNow();
			sExecutor = null;
		}
	}

}
