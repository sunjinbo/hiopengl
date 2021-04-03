package com.hiopengl.practices;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Particle {
    private float initX;
    private float initY;
    private float x;
    private float y;
    private float radius;
    private float angle; // 弧度
    private float speed;
    private int color;
    private int life;

    public Particle(float x, float y, float radius, float angle, float speed, int color) {
        this.initX = x;
        this.initY = y;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.angle = angle;
        this.speed = speed;
        this.color = color;
        this.life = 255;
    }

    public void step() {
        float offsetX = (float)(Math.cos(angle) * speed);
        float offsetY = (float)(Math.sin(angle) * speed);
        x += offsetX;
        y += offsetY;
        life -= 5;
        if (life <= 0) {
            life = 255;
            x = initX;
            y = initY;
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(color);
        paint.setAlpha(life);
        canvas.drawOval(x - radius, y - radius, x + radius, y + radius, paint);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getLife() {
        return life;
    }

    public float getColor() {
        return color;
    }
}
