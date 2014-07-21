package test.camerapreview;

import java.io.IOException;

import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.hardware.Camera;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.content.Context;


class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;
	
	Preview(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
		mHolder = null;
	}
	
	public void surfaceChanged(SurfaceHolder holder,int format,int w,int h){
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(720,480);
		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}
	
}

public class CameraPreview extends Activity {
	private static final String TAG="TTT";
	private Preview mPreview;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        mPreview = new Preview(this);
        setContentView(mPreview);
        
        Log.v(TAG,"onCreate");
    }
    
}
