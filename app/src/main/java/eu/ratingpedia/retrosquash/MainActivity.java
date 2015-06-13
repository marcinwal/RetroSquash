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
    boolean ballIsMovingDown;

    //rocket move
    boolean racketIsMovingLeft;
    boolean racketIsMovingRight;

    //stats
    long lastFrameTime;
    int fps;
    int score;
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
            ballIsMovingDown = true;

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
        public boolean onTouchEvent(MotionEvent motionEvent){
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    if(motionEvent.getX() >= screenWidth / 2){
                        racketIsMovingRight = true;
                        racketIsMovingLeft =false;
                    } else{
                        racketIsMovingRight = false;
                        racketIsMovingLeft = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    racketIsMovingLeft = false;
                    racketIsMovingLeft = false;
                    break;
            }
            return true;
        }

        @Override
        public void run() {
            while(playingSquash){
                updateCourt();
                drawCourt();
                controlFPS();
            }
        }

        private void controlFPS() {
            long timeThisFrame = (System.currentTimeMillis()-lastFrameTime);
            long timeToSleep = 15 - timeThisFrame;
            if(timeThisFrame > 0){
                fps = (int)(1000/timeThisFrame);
            }
            if(timeToSleep>0){
                try{
                    ourThread.sleep(timeToSleep);

                }catch (InterruptedException e){

                }
            }
            lastFrameTime = System.currentTimeMillis();
        }



        private void drawCourt() {
            if (ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.BLACK);//background
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(45);
                canvas.drawText("Score:"+score+" Lives:"+lives+" fps:"+fps,20,40,paint);

                //drawing the racket
                canvas.drawRect(racketPosition.x - (racketWidth/2),racketPosition.y-(racketHeigth/2),
                                racketPosition.x + (racketWidth/2),racketPosition.y+racketHeigth,paint);
                //ball
                canvas.drawRect(ballPosition.x,ballPosition.y,
                                ballPosition.x + ballWidth,ballPosition.y+ballWidth,paint);

                ourHolder.unlockCanvasAndPost(canvas);
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

            //ball hit the boottom meaning player lost life
            if(ballPosition.y > screenHeight - ballWidth){
                lives--;
                if(lives == 0){
                    lives = 3;
                    score = 0;
                    soundPool.play(sample4,1,1,0,0,1);
                }
                ballPosition.y = 1 + ballWidth; //top of the screen again
                Random randInt = new Random();
                int startX = randInt.nextInt(screenWidth - ballWidth) + 1;
                ballPosition.x = startX + ballWidth;
                int ballDirection = randInt.nextInt(3);
                switch (ballDirection){
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

            //hitting te top
            if(ballPosition.y < 0){
                ballIsMovingDown = true;
                ballIsMovingUp = false;
                ballPosition.y = 1;
                soundPool.play(sample2,1,1,0,0,1);
            }

            //adjusting position depending on direction
            if(ballIsMovingDown){
                ballPosition.y += 6;
            }

            if(ballIsMovingUp){
                ballPosition.y -= 10;
            }

            if(ballIsMovingLeft){
                ballPosition.x -= 12;
            }

            if(ballIsMovingRight){
                ballPosition.x += 12;
            }

            //ball hitting the racket
            if(ballPosition.y + ballWidth >= (racketPosition.y - racketHeigth /2 )){
                int halfRacket = racketWidth / 2 ;
                if(ballPosition.x + ballWidth > (racketPosition.x - halfRacket)&& ballPosition.x - ballWidth < (racketPosition.x + halfRacket)){
                    soundPool.play(sample3,1,1,0,0,1);
                    score++;
                    ballIsMovingUp = true;
                    ballIsMovingDown = false;
                    if (ballPosition.x > racketPosition.x){
                        ballIsMovingRight = true;
                        ballIsMovingLeft = false;
                    }else {
                        ballIsMovingRight = false;
                        ballIsMovingLeft = true;
                    }
                }
            }

        }

        public void pause(){
            playingSquash = false;
            try{
                ourThread.join();
            }catch (InterruptedException e){

            }
        }

        public void resume(){
            playingSquash = true;
            ourThread = new Thread(this);
            ourThread.start();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        while(true){
            squashCourtView.pause();
            break;
        }

        finish();
    }

    @Override
    protected void onPause(){
        super.onPause();
        squashCourtView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        squashCourtView.resume();
    }

    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            squashCourtView.pause();
            finish();
            return true;
        }
        return true;
    }
}
