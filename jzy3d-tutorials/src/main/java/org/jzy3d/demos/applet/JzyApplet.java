/*package org.jzy3d.demos.applet;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.mouse.ChartMouseController;
import org.jzy3d.chart.controllers.thread.ChartThreadController;
import org.jzy3d.plot3d.rendering.canvas.ICanvas;


@SuppressWarnings("serial")
public class JzyApplet extends JApplet implements ActionListener{

private Chart chart = null;
private ICanvas canvas = null;

public void init(){
    try{
        SwingUtilities.invokeAndWait(new Runnable(){
            public void run(){
                chart = ScatterCanvas.generateCanvas(); //this just returns a chart object with a scatter object
                canvas = chart.getCanvas();

                add((Component) canvas);                
            }
        });     
    }
    catch(Exception e){
        System.out.println(e);
    }   

    ChartMouseController mouse   = new ChartMouseController();

    chart.addController(mouse);


    ChartThreadController thread = new ChartThreadController();
    mouse.addSlaveThreadController(thread);
    chart.addController(thread);

    thread.start();

}

@Override
public void actionPerformed(ActionEvent e) {}

public void zoom(){
    float value = canvas.getView().getBounds().getXmax()-10;
    canvas.getView().getBounds().setXmax(value);
    canvas.getView().shoot();
}


}*/