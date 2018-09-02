package com.example.project.gogame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class GoView extends View {
    private float width, height;
    private float length;
    private float radius;
    private Paint black, white, empty, grey;
    private ArrayList<Position> positions;
    private ArrayList<Position> previous_position, screen_shot;
    private ArrayList<Integer> chain, territory_chain;
    private ArrayList<Integer> black_territories, white_territories;
    private boolean game_over, territory, last_move_pass, suicide_move;
    private float touchX, touchY;
    boolean turn_black;
    public MyCountDownTimer black_timer, white_timer;
    private long black_start_time, white_start_time;
    private int prisoners_of_white, prisoners_of_black;
    private int white_score, black_score;

    public GoView(Context c){
        super(c);
        init();
    }
    public GoView(Context c, AttributeSet as){
        super(c, as);
        init();
    }
    public GoView(Context c, AttributeSet as, int default_style){
        super(c, as, default_style);
        init();
    }

    public class MyCountDownTimer extends CountDownTimer {
        public long time_left;
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
            time_left = startTime;
        }
        @Override
        public void onFinish() {
            endGame();
            time_left = 0;
            invalidate();
        }
        @Override
        public void onTick(long millisUntilFinished) {
            time_left = millisUntilFinished;
            invalidate();
        }
    }


    private void init()
    {
        int i,j;
        black=new Paint(Paint.ANTI_ALIAS_FLAG);
        black.setColor(Color.BLACK);
        black.setStrokeWidth(4);
        white=new Paint(Paint.ANTI_ALIAS_FLAG);
        white.setColor(0xFFFFFFFF);
        white.setStrokeWidth(4);
        empty=new Paint(Paint.ANTI_ALIAS_FLAG);
        empty.setColor(Color.GRAY);
        grey=new Paint(Paint.ANTI_ALIAS_FLAG);
        grey.setColor(Color.LTGRAY);
        positions=new ArrayList<Position>();
        previous_position=new ArrayList<Position>();
        screen_shot = new ArrayList<Position>();
        for(i=0;i<81;i++)
        {
            positions.add(new Position());
        }
        for(i=0;i<81;i++)
        {
            previous_position.add(new Position());
        }
        for(i=0;i<81;i++)
        {
            screen_shot.add(new Position());
        }
        newGame();
    }
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        int i;
        for(i=0;i<81;i=i+9)
        {
            canvas.drawLine(positions.get(i).getX(),positions.get(i).getY(),positions.get(i + 8).getX(),positions.get(i + 8).getY(),black);
        }
        for(i = 0; i < 9; i++)
        {
            canvas.drawLine(positions.get(i).getX(),positions.get(i).getY(),positions.get(72 + i).getX(),positions.get(72 + i).getY(),black);
        }


        for(i=0;i<81;i++)
        {
            if(i==20 || i==22 || i==24 || i==38 || i==40 || i==42 || i==56 || i==58 || i==60)// reference points
            {
                canvas.drawCircle(positions.get(i).getX(),positions.get(i).getY(),radius/4,black);
            }
            if(!positions.get(i).getColor().equals(empty))
            {
                canvas.drawCircle(positions.get(i).getX(),positions.get(i).getY(),radius,positions.get(i).getColor());
            }
            if(territory)
            {
                if(black_territories.contains(i))
                {
                    canvas.drawRect(positions.get(i).getX()-radius/2,positions.get(i).getY()-radius/2,positions.get(i).getX()+radius/2,positions.get(i).getY()+radius/2,black);
                }
                else if(white_territories.contains(i))
                {
                    canvas.drawRect(positions.get(i).getX()-radius/2,positions.get(i).getY()-radius/2,positions.get(i).getX()+radius/2,positions.get(i).getY()+radius/2,white);
                }
            }

        }
        if(turn_black)
        {
            canvas.drawCircle(0.50f*length,0.65f*height,radius*1.5f,black);
        }
        else
        {
            canvas.drawCircle(0.50f*length,0.65f*height,radius*1.5f,white);
        }
        canvas.drawRect(0.10f*length,0.62f*height,0.40f*length,0.70f*height,grey);
        canvas.drawText("Pass",0.15f*length,0.68f*height,black);
        canvas.drawRect(0.60f*length,0.62f*height,0.90f*length,0.70f*height,grey);
        canvas.drawText("Territory",0.62f*length,0.68f*height,black);
        canvas.drawRect(0.10f*length,0.72f*height,0.40f*length,0.80f*height,grey);
        canvas.drawText("Reset",0.15f*length,0.78f*height,black);
        canvas.drawRect(0.60f*length,0.72f*height,0.90f*length,0.80f*height,grey);
        canvas.drawText("End",0.70f*length,0.78f*height,black);
        int seconds=(int)(black_timer.time_left/1000);
        int minutes=seconds/60;
        seconds=seconds%60;
        canvas.drawText("Black "+String.format("%d:%02d",minutes,seconds),0.10f*length,0.85f*height,black);
        seconds=(int)(white_timer.time_left/1000);
        minutes=seconds/60;
        seconds=seconds%60;
        canvas.drawText("White "+String.format("%d:%02d",minutes,seconds),0.60f*length,0.85f*height,white);
        canvas.drawText("Captured: "+prisoners_of_black,0.10f*length,0.90f*height,black);
        canvas.drawText("Captured: "+prisoners_of_white,0.60f*length,0.90f*height,white);
        if(game_over)
        {
            if(black_score>white_score)
            {
                canvas.drawText("Winner: Black by "+(black_score-white_score)+" points",0.20f*length,0.59f*height,black);
            }
            else if(black_score<white_score)
            {
                canvas.drawText("Winner: White by "+(white_score - black_score)+" points",0.20f*length,0.59f*height,white);
            }
            else
            {
                canvas.drawText("Tie",0.47f*length,0.59f*height,black);
            }
        }
    }
    public void addItem(int pos, Paint color){
        for(int i = 0; i < 81; i++){
            screen_shot.get(i).setColor(positions.get(i).getColor());
        }
        positions.get(pos).setColor(color);
        chain = new ArrayList<Integer>();
        ArrayList<Integer> neighbours = getNeighboours(pos);
        boolean remove_successfull = true;
        for(int i = 0; i < neighbours.size(); i++){
            if (!color.equals(positions.get(neighbours.get(i)).getColor()) && !positions.get(neighbours.get(i)).getColor().equals(empty)) {
                if(checkForSurrounded(neighbours.get(i)) && chain.size() > 0){
                    remove_successfull = !removeCapturedItems();
                }
                chain = new ArrayList<Integer>();
            }

        }
        if(remove_successfull){
            if(checkForSurrounded(pos) && chain.size() > 0){ // check for suicide
                suicide_move = true;
                removeCapturedItems();
            }
            for(int i = 0; i < 81; i++){
                previous_position.get(i).setColor(screen_shot.get(i).getColor());
            }
            switchPlayer();
        }
        else{
            positions.get(pos).setColor(empty);
        }
    }
    public boolean removeCapturedItems(){
        for(int i = 0; i < chain.size(); i++){
            positions.get(chain.get(i)).setColor(empty);
        }

        boolean repeated_moves = true;
        for(int i = 0; i < 81; i++){
            if(!previous_position.get(i).getColor().equals(positions.get(i).getColor())){
                repeated_moves = false;
                break;
            }
        }
        if(repeated_moves){  // dont allow
            for(int i = 0; i < 81; i++){
                positions.get(i).setColor(screen_shot.get(i).getColor());
            }
            return true;
        }
        if(suicide_move){
            if (positions.get(chain.get(0)).getColor().equals(empty)) {
                if (turn_black) {
                    prisoners_of_white = prisoners_of_white + chain.size();
                } else {
                    prisoners_of_black = prisoners_of_black + chain.size();
                }
            }
            suicide_move = false;
        }
        else {
            if (positions.get(chain.get(0)).getColor().equals(empty)) {
                if (turn_black) {
                    prisoners_of_black = prisoners_of_black + chain.size();
                } else {
                    prisoners_of_white = prisoners_of_white + chain.size();
                }
            }
        }
        return false;
    }

    public void switchPlayer(){
        if(turn_black)
        {
            black_start_time = black_timer.time_left;
            black_timer.cancel();
            white_timer = new MyCountDownTimer(white_start_time, 1000);
            white_timer.start();
        }
        else
        {
            white_start_time = white_timer.time_left;
            white_timer.cancel();
            black_timer = new MyCountDownTimer(black_start_time, 1000);
            black_timer.start();
        }
        turn_black = !turn_black;

    }

    public void endGame()
    {
        calculateTerritory();
        game_over=true;
        black_score=prisoners_of_black+black_territories.size();
        white_score=prisoners_of_white+white_territories.size();
        white_timer.cancel();
        black_timer.cancel();
        invalidate();
    }
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getActionMasked()==MotionEvent.ACTION_DOWN)
        {
            touchX=event.getX();
            touchY=event.getY();
            if(touchX>0 || touchY>0)
            {
                territory=false;
                if(!game_over)
                {
                    for (int i=0;i<81;i++)
                    {
                        float distance=(float)Math.sqrt(((touchX-positions.get(i).getX())*(touchX-positions.get(i).getX()))+((touchY-positions.get(i).getY())*(touchY-positions.get(i).getY())));
                        if (distance<=radius && positions.get(i).getColor().equals(empty))
                        {
                            if (turn_black)
                            {
                                addItem(i, black);
                            }
                            else
                            {
                                addItem(i, white);
                            }

                            invalidate();
                        }
                    }
                }
                if(touchX > 0.10*length && touchX<0.40*length && touchY>0.62*height && touchY<0.70*height)
                {
                    if(last_move_pass)
                    {
                        endGame();
                    }
                    else
                    {
                        last_move_pass=true;
                        switchPlayer();
                    }
                }
                else if(touchX>0.60*length && touchX<0.90*length && touchY>0.62*height && touchY<0.70*height)
                {
                    calculateTerritory();
                }
                else if(touchX>0.10*length && touchX<0.40*length && touchY>0.72*height && touchY<0.80*height)
                {
                    newGame();
                }
                else if(touchX>0.60*length && touchX<0.90*length && touchY>0.72*height && touchY<0.80*height)
                {
                    endGame();
                }
            }
            return true;
        }
        return  super.onTouchEvent(event);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width=MeasureSpec.getSize(widthMeasureSpec);
        height=MeasureSpec.getSize(heightMeasureSpec);
        length=width;
        length=width>height*0.60f?height*0.60f:width;
        radius=length/27;

        float i,j;
        int count=0;
        for(i =0.10f;i<1.0;i=i+0.10f)
        {
            for(j=0.10f;j<1.0;j=j+0.10f)
            {
                positions.get(count).setLocation(Math.round(length*j),Math.round(length*i));
                count++;
            }
        }
        black.setTextSize(length/17);
        white.setTextSize(length/17);
        invalidate();
    }
    public void calculateTerritory(){
            black_territories = new ArrayList<>();
            white_territories = new ArrayList<>();
            for (int i = 0; i < 81; i++) {
                if (positions.get(i).getColor().equals(empty)) {
                    territory_chain = new ArrayList<>();
                    if (findTerritory(i, black)) {
                        for (int j = 0; j < territory_chain.size(); j++) {
                            if (!black_territories.contains(territory_chain.get(j))) {
                                black_territories.add(territory_chain.get(j));
                            }
                        }
                    }
                    territory_chain = new ArrayList<>();
                    if (findTerritory(i, white)) {
                        for (int j = 0; j < territory_chain.size(); j++) {
                            if (!white_territories.contains(territory_chain.get(j))) {
                                white_territories.add(territory_chain.get(j));
                            }
                        }
                    }
                }

            }
        territory = true;
    }

    public boolean findTerritory(int pos, Paint color){
        territory_chain.add(pos);
        ArrayList<Integer> neighbours = getNeighboours(pos);
        for(int i = 0; i < neighbours.size(); i++){
            if(positions.get(neighbours.get(i)).getColor().equals(positions.get(pos).getColor()) && !territory_chain.contains(neighbours.get(i))){
                if(!findTerritory(neighbours.get(i), color)){
                    return false;
                }
            }
            if(!positions.get(neighbours.get(i)).getColor().equals(color) && !positions.get(neighbours.get(i)).getColor().equals(empty)){
                return false;
            }
        }
        return  true;

    }

    public boolean checkForSurrounded(int pos) {
        int i;
        chain.add(pos);
        ArrayList<Integer> neighbours = new ArrayList<Integer>();
        neighbours = getNeighboours(pos);
        for(i = 0; i < neighbours.size(); i++){
            if (positions.get(pos).getColor().equals(positions.get(neighbours.get(i)).getColor()) && !chain.contains(neighbours.get(i))) {
                if(!checkForSurrounded(neighbours.get(i))){
                    return false;
                }
            }
            if (positions.get(neighbours.get(i)).getColor().equals(empty)) {
                return false;
            }
        }
        return true;
    }
    public ArrayList<Integer> getNeighboours(int pos)
    {
        ArrayList<Integer> neighbours=new ArrayList<Integer>();
        if(pos<72)
        {
            neighbours.add(pos+9);
        }
        if(pos>8)
        {
            neighbours.add(pos-9);
        }
        if(((pos+1)%9)!=0)
        {
            neighbours.add(pos+1);
        }
        if((pos%9)!=0)
        {
            neighbours.add(pos-1);
        }
        return neighbours;
    }

    public void newGame()
    {
        int i;
        for(i=0;i<81;i++)
        {
            positions.get(i).setColor(empty);
        }
        for(i=0;i<81;i++)
        {
            previous_position.get(i).setColor(empty);
        }
        turn_black=true;
        game_over=false;
        last_move_pass=false;
        prisoners_of_black=prisoners_of_white=0;
        black_start_time = white_start_time = 900000;
        black_timer = new MyCountDownTimer(black_start_time, 1000);
        black_timer.start();
        white_timer = new MyCountDownTimer(white_start_time, 1000);
        black_territories = new ArrayList<>();
        white_territories = new ArrayList<>();
        territory = false;
        suicide_move = false;

    }

}
