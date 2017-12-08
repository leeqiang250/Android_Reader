package cn.com.pyc.reader;

public interface IPlayerStateListener
{
	void onError(int what);

	void onComplete();

	void onStateChanged(boolean isPlaying);

	void onProgressChanged(int progress, int duration);
}
