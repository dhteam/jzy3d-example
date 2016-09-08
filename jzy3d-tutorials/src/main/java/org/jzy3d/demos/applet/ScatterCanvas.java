package org.jzy3d.demos.applet;

import java.awt.Canvas;

import org.jzy3d.chart.Chart;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;


public class ScatterCanvas{

    public static Canvas generateCanvas(){
            // Create the dot cloud scene and fill with data
            int size = 2000;
            float x;
            float y;
            float z;
            float a;

            Coord3d[] points = new Coord3d[size];
            Color[]   colors = new Color[size];

            for(int i=0; i<size; i++){
                    x = (float)Math.random() - 0.5f;
                    y = (float)Math.random() - 0.5f;
                    z = (float)Math.random() - 0.5f;
                    points[i] = new Coord3d(x, y, z);
                    a = 0.25f + (float)(points[i].distance(Coord3d.ORIGIN)/Math.sqrt(1.3)) / 2;
                    colors[i] = new Color(x, y, z, a);
            }

            Scatter scatter = new Scatter(points, colors);
            chart = new Chart();
            chart.getScene().add(scatter);
            return (Canvas) chart.getCanvas();
    }
    protected static Chart chart;

    
}