package weblab4.model;
import java.io.Serializable;

public class Point implements Serializable {

    private double x;
    private double y;
    private double r = 1;
    private String res;
    private String owner;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }


    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", r=" + r +
                ", res='" + res + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }

    public void check() {
        if ((x * x + y * y <= (r/2) * (r/2)  && x <= 0 && y <= 0) ||
                (y <= r + 2 * x  && x <= 0 && y >= 0 && y <= r && x >= -r/2) ||
                (y <= r && x >= 0 &&  x <= r  && y >= 0)) {
            res = "True";
        } else {
            res = "False";
        }
    }
}