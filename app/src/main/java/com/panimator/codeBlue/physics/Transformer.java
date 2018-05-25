package com.panimator.codeBlue.physics;

/**
 * Created by ASANDA on 2018/05/04.
 * for Pandaphic
 */
public class Transformer {
    private double x, y, z, tetha;

    public Transformer(double oX, double oY, double oZ, double oTetha){
        this.x = oX;
        this.y = oY;
        this.z = oZ;
        this.tetha = oTetha;
    }
    public double tranformX(double tx){
        return this.x + tx;
    }

    public double transformY(double ty){
        return this.y + ty;
    }

    public double transformZ(double tz){
        return this.z = tz;
    }

    public double transformTetha(double tTetha){ return this.tetha + tTetha; }

    public void setOriginX(double originX){
        this.x = originX;
    }

    public void setOriginY(double originY){
        this.y = originY;
    }
    public void setOriginZ(double originZ){
        this.z = originZ;
    }
    public void setOriginTetha(double originTetha){
        this.tetha = originTetha;
    }
}
