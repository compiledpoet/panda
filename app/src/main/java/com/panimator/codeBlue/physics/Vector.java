package com.panimator.codeBlue.physics;

/**
 * Created by ASANDA on 2018/04/23.
 * for Pandaphic
 */
public final class Vector {
    private float X, Y;

    public Vector(float x, float y){
        this.X = x;
        this.Y = y;
    }

    public void add(Vector vector){
        this.X += vector.X;
        this.Y += vector.Y;
    }
    public static Vector add(Vector vector1, Vector vector2){
        return new Vector(vector1.X + vector2.X, vector1.Y + vector2.Y);
    }

    public void sub(Vector vector){
        this.X -= vector.X;
        this.Y -= vector.Y;
    }
    public static Vector sub(Vector vector1, Vector vector2){
         return new Vector(vector1.X - vector2.X, vector1.Y - vector2.Y);
    }

    public Vector getUnitVector(){
        float magnitude = this.getMagnitude();
        return new Vector(this.X / magnitude, this.Y / magnitude);
    }

    public void setAngle(double radAngle){
        float r = this.getMagnitude();
        this.X = (float)(r * Math.cos(radAngle));
        this.Y = (float)(r * Math.sin(radAngle));
    }


    public void setMagnitude(float magnitude){
        Vector unitVector = this.getUnitVector();
        this.X = unitVector.X * magnitude;
        this.Y = unitVector.Y * magnitude;
    }

    public void setLimit(float limit){
        if(this.getMagnitude() > limit){
            this.setMagnitude(limit);
        }
    }


    public float getX() {
        return X;
    }

    public float getY() {
        return Y;
    }

    public float getMagnitude(){
        return (float)Math.sqrt((X*X) + (Y*Y));
    }

    public double getAngle(){ return Math.atan(this.Y / this.X); }

    public void reset(){
        this.X = this.Y = 0;
    }
}
