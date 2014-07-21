package com.compal.bsp.Audio_abs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;



@SuppressLint("NewApi")
public class RecorderTrack extends Activity {
		private String TAG = "session";
		
		private RadioGroup mRadioGroup1,mRadioGroup2,mRadioGroup3;
		private RadioButton mRadio1,mRadio2,mRadio3,mRadio4,mRadio5,mRadio5_2,mRadio6,mRadio7; 	
	
        private static final int RECORDER_BPP = 16;
        //private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
        private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
        private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
        private static int frequency = 44100;
        private static int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
        private static int EncodingBitRate = AudioFormat.ENCODING_PCM_16BIT;
        
        private AudioRecord audioRecord = null;
        private AudioTrack audioTrack = null;
        private int recBufSize = 0;
        private int playBufSize = 0;
        private Thread recordingThread = null;
        private boolean isRecording = false;
        private boolean isTracking = false;
        private boolean m_keep_running;
        
        protected PCMAudioTrack m_player;
        SeekBar skbVolume;//调节音量
        
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        
	        setButtonHandlers();
	        enableButton1(false);
	   
	    }

        private void setButtonHandlers() {
                ((Button)findViewById(R.id.btnRecord)).setOnClickListener(btnClick);
                ((Button)findViewById(R.id.btnStop)).setOnClickListener(btnClick);
                ((Button)findViewById(R.id.btnTrack)).setOnClickListener(btnClick);
                ((Button)findViewById(R.id.btnStop2)).setOnClickListener(btnClick);
                //((Button)findViewById(R.id.btnExit)).setOnClickListener(btnClick);
                mRadioGroup1 = (RadioGroup) findViewById(R.id.myRadioGroup1); 
                mRadioGroup2 = (RadioGroup) findViewById(R.id.myRadioGroup2); 
                mRadioGroup3 = (RadioGroup) findViewById(R.id.myRadioGroup3); 
                mRadio1 = (RadioButton) findViewById(R.id.myRadio1Button1);
                mRadio2 = (RadioButton) findViewById(R.id.myRadio1Button2);
                mRadio3 = (RadioButton) findViewById(R.id.myRadio2Button1);
                mRadio4 = (RadioButton) findViewById(R.id.myRadio2Button2);
                mRadio5 = (RadioButton) findViewById(R.id.myRadio2Button3);
                mRadio5_2 = (RadioButton) findViewById(R.id.myRadio2Button4);
                mRadio6 = (RadioButton) findViewById(R.id.myRadio3Button1);
                mRadio7 = (RadioButton) findViewById(R.id.myRadio3Button2);
              
                
                mRadioGroup1.setOnCheckedChangeListener(mChangeRadio1);
                mRadioGroup2.setOnCheckedChangeListener(mChangeRadio2);
                mRadioGroup3.setOnCheckedChangeListener(mChangeRadio3);
        }
        
        private void enableButton(int id,boolean isEnable){
                ((Button)findViewById(id)).setEnabled(isEnable);
        }
        
        private void enableButton0(boolean isRecording) {
            enableButton(R.id.btnRecord,!isRecording);
            enableButton(R.id.btnTrack,!isRecording);
            enableButton(R.id.btnStop2,!isRecording);
            enableButton(R.id.btnStop,isRecording);
        }
        
        private void enableButton1(boolean isRecording) {
                enableButton(R.id.btnRecord,!isRecording);
                enableButton(R.id.btnTrack,isRecording);
                enableButton(R.id.btnStop2,isRecording);
                enableButton(R.id.btnStop,isRecording);
        }
        
        private void enableButton2(boolean isRecording) {
            enableButton(R.id.btnRecord,!isRecording);
            enableButton(R.id.btnTrack,!isRecording);
            enableButton(R.id.btnStop2,isRecording);
            enableButton(R.id.btnStop,isRecording);
        }
        
        private void enableButton3(boolean isTracking) {
            enableButton(R.id.btnRecord,!isTracking);
            enableButton(R.id.btnStop,!isTracking);
            enableButton(R.id.btnTrack,!isTracking);
            enableButton(R.id.btnStop2,isTracking);
        }
        
        private void enableButton4(boolean isTracking) {
            enableButton(R.id.btnRecord,!isTracking);
            enableButton(R.id.btnStop,isTracking);
            enableButton(R.id.btnTrack,!isTracking);
            enableButton(R.id.btnStop2,isTracking);
        }
              
        private String getFilename(){
                String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
                File file = new File(filepath,AUDIO_RECORDER_FOLDER);
                
                if(file.exists()){
                	 file.delete();
                }
                                
                return (file.getAbsolutePath() + "/session.wav" );
        }
        
        private String getTempFilename(){
                String filepath = Environment.getExternalStorageDirectory().getPath();
                File file = new File(filepath,AUDIO_RECORDER_FOLDER);
                
                if(!file.exists()){
                        file.mkdirs();
                }
                
                File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);
                
                if(tempFile.exists())
                        tempFile.delete();
                
                return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
        }
        
        private void startRecording(){
                
        		createAudioRecord();
        	
        		audioRecord.startRecording();
                
                isRecording = true;
                
                recordingThread = new Thread(new Runnable() {
                        
                        @Override
                        public void run() {
                                writeAudioDataToFile();
                        }
                },"AudioRecorder Thread");
                
                recordingThread.start();
        }
        
        private void writeAudioDataToFile(){
                byte data[] = new byte[recBufSize];
                String filename = getTempFilename();
                FileOutputStream os = null;
                
                try {
                        os = new FileOutputStream(filename);
                } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                
                int read = 0;
                
                if(null != os){
                        while(isRecording){
                                read = audioRecord.read(data, 0, recBufSize);
                                
                                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                                        try {
                                                os.write(data);
                                        } catch (IOException e) {
                                                e.printStackTrace();
                                        }
                                }
                        }
                        
                        try {
                                os.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }
        }
        
        private void stopRecording(){
                if(null != audioRecord){
                        isRecording = false;
                        
                        audioRecord.stop();
                        audioRecord.release();
                        
                        audioRecord = null;
                        recordingThread = null;
                }
                
                copyWaveFile(getTempFilename(),getFilename());
                deleteTempFile();
        }

        private void deleteTempFile() {
                File file = new File(getTempFilename());
                
                file.delete();
        }
        
        private void copyWaveFile(String inFilename,String outFilename){
                FileInputStream in = null;
                FileOutputStream out = null;
                long totalAudioLen = 0;
                long totalDataLen = totalAudioLen + 36;
                long longSampleRate = frequency;
                int channels = 2;
                long byteRate = RECORDER_BPP * frequency * channels/8;
                
                byte[] data = new byte[recBufSize];
                
                try {
                        in = new FileInputStream(inFilename);
                        out = new FileOutputStream(outFilename);
                        totalAudioLen = in.getChannel().size();
                        totalDataLen = totalAudioLen + 36;
                        
                        AppLog.logString("File size: " + totalDataLen);
                        
                        WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                                        longSampleRate, channels, byteRate);
                        
                        while(in.read(data) != -1){
                                out.write(data);
                        }
                        
                        in.close();
                        out.close();
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        private void WriteWaveFileHeader(
                        FileOutputStream out, long totalAudioLen,
                        long totalDataLen, long longSampleRate, int channels,
                        long byteRate) throws IOException {
                
                byte[] header = new byte[44];
                
                header[0] = 'R';  // RIFF/WAVE header
                header[1] = 'I';
                header[2] = 'F';
                header[3] = 'F';
                header[4] = (byte) (totalDataLen & 0xff);
                header[5] = (byte) ((totalDataLen >> 8) & 0xff);
                header[6] = (byte) ((totalDataLen >> 16) & 0xff);
                header[7] = (byte) ((totalDataLen >> 24) & 0xff);
                header[8] = 'W';
                header[9] = 'A';
                header[10] = 'V';
                header[11] = 'E';
                header[12] = 'f';  // 'fmt ' chunk
                header[13] = 'm';
                header[14] = 't';
                header[15] = ' ';
                header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
                header[17] = 0;
                header[18] = 0;
                header[19] = 0;
                header[20] = 1;  // format = 1
                header[21] = 0;
                header[22] = (byte) channels;
                header[23] = 0;
                header[24] = (byte) (longSampleRate & 0xff);
                header[25] = (byte) ((longSampleRate >> 8) & 0xff);
                header[26] = (byte) ((longSampleRate >> 16) & 0xff);
                header[27] = (byte) ((longSampleRate >> 24) & 0xff);
                header[28] = (byte) (byteRate & 0xff);
                header[29] = (byte) ((byteRate >> 8) & 0xff);
                header[30] = (byte) ((byteRate >> 16) & 0xff);
                header[31] = (byte) ((byteRate >> 24) & 0xff);
                header[32] = (byte) (2 * 16 / 8);  // block align
                header[33] = 0;
                header[34] = RECORDER_BPP;  // bits per sample
                header[35] = 0;
                header[36] = 'd';
                header[37] = 'a';
                header[38] = 't';
                header[39] = 'a';
                header[40] = (byte) (totalAudioLen & 0xff);
                header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
                header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
                header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

                out.write(header, 0, 44);
        }
        
        private View.OnClickListener btnClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        switch(v.getId()){
                                case R.id.btnRecord:{
                                        AppLog.logString("Start Recording");
                                        
                                        enableButton0(true);
                                        startRecording();
                                                        
                                        break;
                                }
                                case R.id.btnStop:{
                                        AppLog.logString("Start Recording");
                                        
                                        enableButton2(false);
                                        stopRecording();
                                        
                                        break;
                                }
                                case R.id.btnTrack:{
                                    AppLog.logString("Start Tracking");
                                    
                                    enableButton3(true);
                                    m_player = new PCMAudioTrack();
                        			Log.i(TAG,"xxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                        			m_player.init();
                        			m_player.start();
                                    
                                    break;
                                }
                                case R.id.btnStop2:{
                                    AppLog.logString("Stop Tracking");
                                    enableButton4(false);
                                    m_player.free();
                        			m_player = null;
                                    
                                    break;
                                }
                                
                                
                        }
                }
        }; 
        
        @SuppressLint("NewApi")
		public void createAudioRecord(){
    		recBufSize = AudioRecord.getMinBufferSize(frequency,
    				AudioFormat.CHANNEL_IN_MONO, EncodingBitRate);

    	
    		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
    				AudioFormat.CHANNEL_IN_MONO, EncodingBitRate, recBufSize);    	
    	}
        
        @SuppressLint({ "NewApi", "InlinedApi" })
		public void createAudioTrack(){
        	playBufSize=AudioTrack.getMinBufferSize(frequency,
        			AudioFormat.CHANNEL_OUT_MONO, EncodingBitRate);

    	
        	audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
        			AudioFormat.CHANNEL_OUT_MONO, EncodingBitRate,
    				playBufSize, AudioTrack.MODE_STREAM);	
    	}
        
        @SuppressLint("NewApi")
		class PCMAudioTrack extends Thread {
        	
        	protected byte[] m_out_bytes;
        	

        	final String FILE_PATH = "/sdcard/AudioRecorder/";
        	final String FILE_NAME = "session.wav";

        	File file;
        	FileInputStream in;
        	
        	public void init() {
        		try {		
        			file = new File(FILE_PATH , FILE_NAME);
        			file.createNewFile();			
        			in = new FileInputStream(file);

//        			in.read(temp, 0, length);
        			
        			m_keep_running = true;

        			createAudioTrack();

        			m_out_bytes = new byte[playBufSize];

        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}

        	public void free() {
        		m_keep_running = false;
        		try {
        			Thread.sleep(1000);
        		} catch (Exception e) {
        			Log.d("sleep exceptions...\n", "");
        		}
        	}

        	public void run() {
        		byte[] bytes_pkg = null;
        		audioTrack.play();
        		while (m_keep_running) {
        			try {
        				in.read(m_out_bytes);
        				bytes_pkg = m_out_bytes.clone();
        				audioTrack.write(bytes_pkg, 0, bytes_pkg.length);
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        		}

        		audioTrack.stop();
        		audioTrack = null;
        		try {
        			in.close();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	}
        }
        
        RadioGroup.OnCheckedChangeListener mChangeRadio1 =
    	    new RadioGroup.OnCheckedChangeListener() 
    	  { 
    	    @Override public void onCheckedChanged(RadioGroup group, int checkedId)
    	    { 
    	      // TODO Auto-generated method stub 
    	      if(checkedId==mRadio1.getId()) 
    	      { 
    	       
    	    	  channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    	    	  
    	        } 
    	      else if(checkedId==mRadio2.getId())
    	      {
    	       
    	    	  channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    	      }
    	    } 
    	};
    	    
    	    RadioGroup.OnCheckedChangeListener mChangeRadio2 =
    		    new RadioGroup.OnCheckedChangeListener() 
    		  { 
    		    @Override public void onCheckedChanged(RadioGroup group, int checkedId)
    		    { 
    		      // TODO Auto-generated method stub 
    		      if(checkedId==mRadio3.getId()) 
    		      { 
    		       
    		    	  frequency = 44100;
    		        } 
    		      else if(checkedId==mRadio4.getId())
    		      {
    		       
    		    	  frequency = 22050;
    		      }
    		      else if(checkedId==mRadio5.getId())
    		      {
    		       
    		    	  frequency = 11025;
    		      }
    		      else if(checkedId==mRadio5_2.getId())
    		      {
    		       
    		    	  frequency = 16000;
    		      }
    		    } 
    		 };

    		 RadioGroup.OnCheckedChangeListener mChangeRadio3 =
    			    new RadioGroup.OnCheckedChangeListener() 
    			  { 
    			    @Override public void onCheckedChanged(RadioGroup group, int checkedId)
    			    { 
    			      // TODO Auto-generated method stub 
    			      if(checkedId==mRadio6.getId()) 
    			      { 
    			       
    			    	  EncodingBitRate = AudioFormat.ENCODING_PCM_16BIT;
    			        } 
    			      else if(checkedId==mRadio7.getId())
    			      {
    			       
    			    	  EncodingBitRate = AudioFormat.ENCODING_PCM_8BIT;
    			      }
    			    } 
    			};
    			
    			@Override
    			protected void onDestroy() {
    				super.onDestroy();
    				android.os.Process.killProcess(android.os.Process.myPid());
    			}
}