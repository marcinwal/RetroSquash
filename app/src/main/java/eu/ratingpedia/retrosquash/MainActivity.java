package eu.ratingpedia.retrosquash;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ourFrame = (ImageView)findViewById(R.id.imageView);
        Bitmap ourBitmap = Bitmap.createBitmap(300,600, Bitmap.Config.ARGB_8888);
        Canvas ourCanvas = new Canvas(ourBitmap);

        Paint paint = new Paint();

        ourCanvas.drawColor(Color.BLACK);
        paint.setColor(Color.argb(255,255,255,255));

        ourCanvas.drawText("Score: 42 Lives:3 Hi: 97",10,10,paint);
        ourCanvas.drawLine(10,50,200,50,paint);
        ourCanvas.drawCircle(110,160,100,paint);
        ourCanvas.drawPoint(1,260,paint);

        ourFrame.setImageBitmap(ourBitmap);


    }


}
