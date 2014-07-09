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
		pdialog.setTitle("��ʾ");
		pdialog.setMessage("���Ժ�");
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
			// ����һ��ͼƬ

			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(image_path);
			HttpResponse httpResponse = null;

			try {

				httpResponse = httpClient.execute(httpGet);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					byte[] data = EntityUtils.toByteArray(httpResponse
							.getEntity());
					// �õ�һ��Bitmap���󣬲���Ϊ��ʹ����post�ڲ����Է��ʣ���������Ϊfinal
					final Bitmap bmp = BitmapFactory.decodeByteArray(data, 0,
							data.length);

					handler.post(new Runnable() {
						@Override
						public void run() {
							// ��Post�в���UI���ImageView
							iv.setImageBitmap(bmp);
							iv.setOnTouchListener(new TouchListener());

						}
					});
					// ���ضԻ���
					pdialog.dismiss();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	
	private class TouchListener implements OnTouchListener {
        
        /** ��¼��������Ƭģʽ���ǷŴ���С��Ƭģʽ */
        private int mode = 0;// ��ʼ״̬ 
        /** ������Ƭģʽ */
        private static final int MODE_DRAG = 1;
        /** �Ŵ���С��Ƭģʽ */
        private static final int MODE_ZOOM = 2;
          
        /** ���ڼ�¼��ʼʱ�������λ�� */
        private PointF startPoint = new PointF();
        /** ���ڼ�¼����ͼƬ�ƶ�������λ�� */
        private Matrix matrix = new Matrix();
        /** ���ڼ�¼ͼƬҪ��������ʱ�������λ�� */
        private Matrix currentMatrix = new Matrix();
      
        /** ������ָ�Ŀ�ʼ���� */
        private float startDis;
        /** ������ָ���м�� */
        private PointF midPoint;
  
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            /** ͨ�������㱣������λ MotionEvent.ACTION_MASK = 255 */
        	System.out.println("this is  in onTouch-------------------");
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // ��ָѹ����Ļ
            case MotionEvent.ACTION_DOWN:
                mode = MODE_DRAG;
                // ��¼ImageView��ǰ���ƶ�λ��
                currentMatrix.set(iv.getImageMatrix());
                startPoint.set(event.getX(), event.getY());
                break;
            // ��ָ����Ļ���ƶ������¼��ᱻ���ϴ���
            case MotionEvent.ACTION_MOVE:
                // ����ͼƬ
                if (mode == MODE_DRAG) {
                	System.out.println("this is  in Drag-------------------");
                    float dx = event.getX() - startPoint.x; // �õ�x����ƶ�����
                    float dy = event.getY() - startPoint.y; // �õ�x����ƶ�����
                    // ��û���ƶ�֮ǰ��λ���Ͻ����ƶ�
                    matrix.set(currentMatrix);
                    matrix.postTranslate(dx, dy);
                }
                // �Ŵ���СͼƬ
                else if (mode == MODE_ZOOM) {
                    float endDis = distance(event);// ��������
                    if (endDis > 10f) { // ������ָ��£��һ���ʱ�����ش���10
                        float scale = endDis / startDis;// �õ����ű���
                        matrix.set(currentMatrix);
                        matrix.postScale(scale, scale,midPoint.x,midPoint.y);
                    }
                }
                break;
            // ��ָ�뿪��Ļ
            case MotionEvent.ACTION_UP:
                // �������뿪��Ļ��������Ļ�ϻ��д���(��ָ)
            case MotionEvent.ACTION_POINTER_UP:
                mode = 0;
                break;
            // ����Ļ���Ѿ��д���(��ָ)������һ������ѹ����Ļ
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = MODE_ZOOM;
                /** ����������ָ��ľ��� */
                startDis = distance(event);
                /** ����������ָ����м�� */
                if (startDis > 10f) { // ������ָ��£��һ���ʱ�����ش���10
                    midPoint = mid(event);
                    //��¼��ǰImageView�����ű���
                    currentMatrix.set(iv.getImageMatrix());
                }
                break;
            }
            iv.setImageMatrix(matrix);
            return true;
        }
  
        /** ����������ָ��ľ��� */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** ʹ�ù��ɶ���������֮��ľ��� */
            return FloatMath.sqrt(dx * dx + dy * dy);
        }
  
        /** ����������ָ����м�� */
        private PointF mid(MotionEvent event) {
            float midX = (event.getX(1) + event.getX(0)) / 2;
            float midY = (event.getY(1) + event.getY(0)) / 2;
            return new PointF(midX, midY);
        }
  
    }
  
}

