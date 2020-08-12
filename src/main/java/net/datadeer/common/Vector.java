package net.datadeer.common;

public class Vector {
    float w;
    float x;
    float y;
    float z;

    public Vector(float x, float y, float z) {
        this(0, x, y, z);
    }
    public Vector(float w, float x, float y, float z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector scale(float magnitude) {
        w *= magnitude;
        x *= magnitude;
        y *= magnitude;
        z *= magnitude;
        return this;
    }

    @Override
    public String toString() {
        return "("+w+","+x+","+y+","+z+")";
    }
}