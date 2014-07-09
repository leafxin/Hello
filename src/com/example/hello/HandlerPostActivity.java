package com.example.hello;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HandlerPostActivity extends Activity {
	private Button btn;
	private ImageView iv;
	private static Handler handler = new Handler();
	private static String image_path = "http://ww4.sinaimg.cn/bmiddle/786013a5jw1e7akotp4bcj20c80i3aao.jpg";
	private ProgressDialog pdialog;
	private TextView tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// btn = (Button) findViewById(R.id.button);
		setContentView(R.layout.activity_main);
		iv = (ImageView) findViewById(R.id.imageView1);

		pdialog = new ProgressDialog(this);
		pdialog.setTitle("提示");
		pdialog.setMessage("请稍后");
		pdialog.setCancelable(false);
		btn = (Button) findViewById(R.id.button);

		// if (btn1 == null) return;
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pdialog.show();
				new Thread(new MyThread()).start();

			}
		});
		

	}

	class MyThread implements Runnable {

		@Override
		public void run() {
			// 锟斤拷锟斤拷一锟斤拷图片

			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(image_path);
			HttpResponse httpResponse = null;

			try {

				httpResponse = httpClient.execute(httpGet);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					byte[] data = EntityUtils.toByteArray(httpResponse
							.getEntity());
					// 锟矫碉拷一锟斤拷Bitmap锟斤拷锟襟，诧拷锟斤拷为锟斤拷使锟斤拷锟斤拷post锟节诧拷锟斤拷锟皆凤拷锟绞ｏ拷锟斤拷锟斤拷锟斤拷锟斤拷为final
					final Bitmap bmp = BitmapFactory.decodeByteArray(data, 0,
							data.length);

					handler.post(new Runnable() {
						@Override
						public void run() {
							// 锟斤拷Post锟叫诧拷锟斤拷UI锟斤拷锟絀mageView
							iv.setImageBitmap(bmp);
							iv.setOnTouchListener(new TouchListener());

						}
					});
					// 锟斤拷锟截对伙拷锟斤拷
					pdialog.dismiss();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	
	private class TouchListener implements OnTouchListener {
        
        /** 记录是拖拉照片模式还是放大缩小照片模式 */
        private int mode = 0;// 初始状态 
        /** 拖拉照片模式 */
        private static final int MODE_DRAG = 1;
        /** 放大缩小照片模式 */
        private static final int MODE_ZOOM = 2;
          
        /** 用于记录开始时候的坐标位置 */
        private PointF startPoint = new PointF();
        /** 用于记录拖拉图片移动的坐标位置 */
        private Matrix matrix = new Matrix();
        /** 用于记录图片要进行拖拉时候的坐标位置 */
        private Matrix currentMatrix = new Matrix();
      
        /** 两个手指的开始距离 */
        private float startDis;
        /** 两个手指的中间点 */
        private PointF midPoint;
  
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
        	System.out.println("this is  in onTouch-------------------");
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 手指压下屏幕
            case MotionEvent.ACTION_DOWN:
                mode = MODE_DRAG;
                // 记录ImageView当前的移动位置
                currentMatrix.set(iv.getImageMatrix());
                startPoint.set(event.getX(), event.getY());
                break;
            // 手指在屏幕上移动，改事件会被不断触发
            case MotionEvent.ACTION_MOVE:
                // 拖拉图片
                if (mode == MODE_DRAG) {
                	System.out.println("this is  in Drag-------------------");
                    float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                    float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
                    // 在没有移动之前的位置上进行移动
                    matrix.set(currentMatrix);
                    matrix.postTranslate(dx, dy);
                }
                // 放大缩小图片
                else if (mode == MODE_ZOOM) {
                    float endDis = distance(event);// 结束距离
                    if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                        float scale = endDis / startDis;// 得到缩放倍数
                        matrix.set(currentMatrix);
                        matrix.postScale(scale, scale,midPoint.x,midPoint.y);
                    }
                }
                break;
            // 手指离开屏幕
            case MotionEvent.ACTION_UP:
                // 当触点离开屏幕，但是屏幕上还有触点(手指)
            case MotionEvent.ACTION_POINTER_UP:
                mode = 0;
                break;
            // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = MODE_ZOOM;
                /** 计算两个手指间的距离 */
                startDis = distance(event);
                /** 计算两个手指间的中间点 */
                if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                    midPoint = mid(event);
                    //记录当前ImageView的缩放倍数
                    currentMatrix.set(iv.getImageMatrix());
                }
                break;
            }
            iv.setImageMatrix(matrix);
            return true;
        }
  
        /** 计算两个手指间的距离 */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 使用勾股定理返回两点之间的距离 */
            return FloatMath.sqrt(dx * dx + dy * dy);
        }
  
        /** 计算两个手指间的中间点 */
        private PointF mid(MotionEvent event) {
            float midX = (event.getX(1) + event.getX(0)) / 2;
            float midY = (event.getY(1) + event.getY(0)) / 2;
            return new PointF(midX, midY);
        }
  
    }
  
}

