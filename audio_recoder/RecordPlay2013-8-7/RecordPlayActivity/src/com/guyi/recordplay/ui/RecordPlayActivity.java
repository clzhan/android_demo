package com.guyi.recordplay.ui;

import java.util.LinkedList;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * <p>
 * 文件名称 ：RecordPlayActivity.java
 * <p>
 * 内容摘要 ：
 * <p>
 * 作者 ：吴辰彪 创建时间 ：2012-7-25 下午01:35:48 描述 :边录音边播放的例子
 */
public class RecordPlayActivity extends Activity implements OnClickListener
{

	private static final String TAG = "RecordPlayActivity";
	/**
	 * 按钮
	 */
	private Button bt_exit;
	/**
	 * AudioRecord 写入缓冲区大小
	 */
	protected int m_in_buf_size;
	/**
	 * 录制音频对象
	 */
	private AudioRecord m_in_rec;
	/**
	 * 录入的字节数组
	 */
	private byte[] m_in_bytes;
	/**
	 * 存放录入字节数组的大小
	 */
	private LinkedList<byte[]> m_in_q;
	/**
	 * AudioTrack 播放缓冲大小
	 */
	private int m_out_buf_size;
	/**
	 * 播放音频对象
	 */
	private AudioTrack m_out_trk;
	/**
	 * 播放的字节数组
	 */
	private byte[] m_out_bytes;
	/**
	 * 录制音频线程
	 */
	private Thread record;
	/**
	 * 播放音频线程
	 */
	private Thread play;
	/**
	 * 让线程停止的标志
	 */
	private boolean flag = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.setTitle("音频回路");
		
		Log.d("dfdfd", "333333333333");
		
		init();
		record = new Thread(new recordSound());
		play = new Thread(new playRecord());
		// 启动录制线程
		record.start();
		// 启动播放线程
		play.start();
	}

	private void init()
	{
		bt_exit = (Button) findViewById(R.id.bt_yinpinhuilu_testing_exit);
		Log.i(TAG, "bt_exit====" + bt_exit);
		
		bt_exit.setOnClickListener(this);
		// AudioRecord 得到录制最小缓冲区的大小
		m_in_buf_size = AudioRecord.getMinBufferSize(8000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		// 实例化播放音频对象
		m_in_rec = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, m_in_buf_size);
		// 实例化一个字节数组，长度为最小缓冲区的长度
		m_in_bytes = new byte[m_in_buf_size];
		// 实例化一个链表，用来存放字节组数
		m_in_q = new LinkedList<byte[]>();

		// AudioTrack 得到播放最小缓冲区的大小
		m_out_buf_size = AudioTrack.getMinBufferSize(8000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		// 实例化播放音频对象
		m_out_trk = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, m_out_buf_size,
				AudioTrack.MODE_STREAM);
		// 实例化一个长度为播放最小缓冲大小的字节数组
		m_out_bytes = new byte[m_out_buf_size];
	}

	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{

		case R.id.bt_yinpinhuilu_testing_exit:
			flag = false;
			m_in_rec.stop();
			m_in_rec = null;
			m_out_trk.stop();
			m_out_trk = null;
			this.finish();
		}
	}

	/**
	 * <p>
	 * 文件名称 ：RecordPlayActivity.java
	 * <p>
	 * 内容摘要 ：
	 * <p>
	 * 作者 ：吴辰彪 创建时间 ：2012-7-25 下午01:57:09 描述 :录音线程
	 */
	class recordSound implements Runnable
	{
		@Override
		public void run()
		{
			Log.i(TAG, "........recordSound run()......");
			byte[] bytes_pkg;
			// 开始录音
			m_in_rec.startRecording();

			while (flag)
			{
				m_in_rec.read(m_in_bytes, 0, m_in_buf_size);
				bytes_pkg = m_in_bytes.clone();
				Log.i(TAG, "........recordSound bytes_pkg==" + bytes_pkg.length);
				if (m_in_q.size() >= 2)
				{
					m_in_q.removeFirst();
				}
				m_in_q.add(bytes_pkg);
			}
		}

	}

	/**
	 * <p>
	 * 文件名称 ：RecordPlayActivity.java
	 * <p>
	 * 内容摘要 ：
	 * <p>
	 * 作者 ：吴辰彪 创建时间 ：2012-7-25 下午01:57:34 描述 :播放线程
	 */
	class playRecord implements Runnable
	{
		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			Log.i(TAG, "........playRecord run()......");
			byte[] bytes_pkg = null;
			// 开始播放
			m_out_trk.play();

			while (flag)
			{
				try
				{
					m_out_bytes = m_in_q.getFirst();
					bytes_pkg = m_out_bytes.clone();
					m_out_trk.write(bytes_pkg, 0, bytes_pkg.length);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

}