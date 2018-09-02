package com.example.project.gogame;

import android.graphics.Paint;



public class Position {

    private float x, y;
    private Paint color;
    public Position(){

    }
    public Position( float x, float y, Paint color){
        this.x = x;
        this.y = y;
        this.color = color;
    }
    public Position(Paint color){
        this.color = color;
    }
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Paint getColor() {
        return color;
    }

    public void setColor(Paint color) {
        this.color = color;
    }
    public void setLocation(float x, float y){
        this.x = x;
        this.y = y;
    }
}
