package org.jzy3d.demos.animation;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;


public class AnimatedSample extends AbstractAnalysis{
	public static void main(String[] args) throws Exception {
		AnalysisLauncher.open(new AnimatedSample());
	}
		
	public void init(){
		 // Define a function to plot
		
        Mapper mapper = new Mapper() {
        	int count =0 ;
            @Override
            public double f(double x, double y) {
            	System.out.println("im x : "+x+" :  im y  : "+y);
            	System.out.println("im sin (x * y) : "+x* Math.sin(x * y));
                return x * Math.sin(x * y);
            }
        };

        // Define range and precision for the function to plot
        Range range = new Range(0, 3);
        Range range1 = new Range(0, 2);
        int steps = 80;

        // Create the object to represent the function over the given range.
        final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range1, steps), mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, 0.5f)));
//        surface.setFaceDisplayed(true);
        surface.setDetailedToString(true);
//        surface.setWireframeDisplayed(false);

        // Create a chart
        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
        chart.getScene().getGraph().add(surface);
    }
}
