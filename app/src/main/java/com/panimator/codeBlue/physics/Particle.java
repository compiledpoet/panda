package com.panimator.codeBlue.physics;

/**
 * Created by ASANDA on 2018/04/23.
 * for Pandaphic
 */
public class Particle {
    private final Vector location, velocity, acceleration;
    private Vector Target;
    private float lastX, lastY, maxSpeed, maxForce, decelerationDistance;
    private boolean reactedTarget;


    public Particle(float x, float y){
        this(x, y, new Vector(0f,0f));
    }
    public Particle(float x, float y, Vector velocity){
        this.location = new Vector(x, y);
        this.velocity = velocity;
        this.acceleration = new Vector(0,0);
        this.reactedTarget = true;
        this.lastX = x;
        this.lastY = y;
        this.maxSpeed = 15f; // units per move();
        this.maxForce = 0.3f; // units per move();
        this.decelerationDistance = 50f; // units per move();

    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setMaxForce(float maxForce) {
        this.maxForce = maxForce;
    }

    public void setDecelerationDistance(float decelerationDistance) {
        this.decelerationDistance = decelerationDistance;
    }

    public void applyForce(Vector force){
        this.acceleration.add(force);
    }

    public void setTarget(Vector target) {
        this.Target = target;
        this.reactedTarget = false;
    }

    public boolean move(){
        this.lastX = this.location.getX();
        this.lastY = this.location.getY();
        if(this.Target == null){
            return false;
        }else{
            if(this.targetReached())
                return false;
            else{
                Vector desiredDirection = Vector.sub(this.Target, this.location);
                float distance = desiredDirection.getMagnitude();

                if(distance <= 1){
                    this.reactedTarget = true;
                    return false;
                }else{
                    if(distance < this.decelerationDistance){
                        float slowSpeed = (distance / this.decelerationDistance) * maxSpeed;
                        desiredDirection.setMagnitude(slowSpeed);
                    }else{
                        desiredDirection.setMagnitude(maxSpeed);
                    }


                    Vector steeringForce = Vector.sub(desiredDirection, this.velocity);
                    steeringForce.setLimit(maxForce);


                    this.applyForce(steeringForce);
                    this.velocity.add(this.acceleration);
                    this.velocity.setLimit(maxSpeed);
                    this.location.add(this.velocity);
                    this.acceleration.reset();
                }
            }
        }



        return true;
    }

    public float getX(){
        return this.location.getX();
    }

    public float getY(){
        return this.location.getY();
    }

    public float getLastX() {
        return lastX;
    }

    public float getLastY() {
        return lastY;
    }

    public boolean targetReached() {
        return reactedTarget;
    }
}
