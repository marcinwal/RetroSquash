package eu.ratingpedia.retrosquash;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;
import android.app.Activity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;


public class MainActivity extends Activity {

    Canvas canvas;
    SquashCourtView squashCourtView;
    //sound
    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    //for display details
    Display display;
    Point size;
    int screenWidth;
    int screenHeight;

    //game
    int racketWidth;
    int racketHeigth;
    Point racketPosition;

    Point ballPosition;
    int ballWidth;

    //for ball moves
    boolean ballIsMovingLeft;
    boolean ballIsMovingRight;
    boolean ballIsMovingUp;
    boolean isBallIsMovingDown;

    //rocket move
    boolean racketIsMovingLeft;
    boolean racketIsMovingRight;

    //stats
    long lastFrameTime;
    int fps;
    int scores;
    int lives;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        squashCourtView = new SquashCourtView(this);
        setContentView(squashCourtView);

        //sound
        soundPool = new SoundPool(10,AudioManager.STREAM_MUSIC,0);
        try{
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("sample1.ogg");
            sample1 = soundPool.load(descriptor,0);

            descriptor = assetManager.openFd("sample2.ogg");
            sample2 = soundPool.load(descriptor,0);

            descriptor = assetManager.openFd("sample3.ogg");
            sample3 = soundPool.load(descriptor,0);

            descriptor = assetManager.openFd("sample4.ogg");
            sample4 = soundPool.load(descriptor,0);


        }catch (IOException e){
            Toast.makeText(getApplicationContext(),"MUSIC FILES PROBLEM",Toast.LENGTH_LONG).show();
        }

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        //game objects
        racketPosition = new Point();
        racketPosition.x = screenWidth / 2;
        racketPosition.y = screenHeight - 20;
        racketWidth = screenWidth / 8;
        racketHeigth = 10;

        ballWidth = screenWidth / 35;
        ballPosition = new Point();
        ballPosition.x = screenWidth / 2;
        ballPosition.y = 1 + ballWidth;

        lives = 3;


    }


    private class SquashCourtView extends SurfaceView implements Runnable{
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSquash;
        Paint paint;

        public SquashCourtView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            isBallIsMovingDown = true;

            //ball sent randomly
            Random randInt = new Random();
            int ballDirection = randInt.nextInt(3);
            switch(ballDirection){
                case 0:
                    ballIsMovingLeft = true;
                    ballIsMovingRight = false;
                    break;
                case 1:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = true;
                    break;
                case 2:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = false;
                    break;
            }
        }

        @Override
        public void run() {
            while(playingSquash){
                updateCourt();
                drawCourt();
                controlFPS();
            }
        }

        private void updateCourt() {
            if(racketIsMovingRight){
                racketPosition.x = racketPosition.x + 10;
            }

            if(racketIsMovingLeft){
                racketPosition.x = racketPosition.x -10;
            }
            //detection collision

            //hit right of the screen
            if(ballPosition.x + ballWidth > screenWidth){
                ballIsMovingLeft = true;
                ballIsMovingRight = false;
                soundPool.play(sample1,1,1,0,0,1);
            }

            //hit on the left
            if(ballPosition.x < 0){
                ballIsMovingLeft = false;
                ballIsMovingRight = true;
                soundPool.play(sample1,1,1,0,0,1);
            }
        }
    }
}
