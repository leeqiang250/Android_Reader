package cn.com.pyc.reader;

public interface IPlayer
{
	boolean isPlaying();

	int getCurPos();

	int getDuration();

	void seekTo(int pos);

	void play(PlayFile playFile);

	void start();

	void pause();
	
	void startOrPause();

	void release();
}
