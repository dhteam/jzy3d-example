package org.jzy3d.demos.animation.histogram;


import java.util.List;
import java.util.Random;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.analysis.IRunnableAnalysis;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartScene;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.TicToc;
import org.jzy3d.maths.Utils;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.HistogramBar;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;


public class AnimatedSurfaceDemo
extends AbstractAnalysis
implements IRunnableAnalysis
{
	public static void main(String[] args) throws Exception{

		 AnimatedSurfaceDemo paramArrayOfString;
		 AnalysisLauncher.open(paramArrayOfString = new AnimatedSurfaceDemo());
		 paramArrayOfString.start();
	}
	
	public static int ii=1;
	public static HistogramBar localHistogramBar[] = new HistogramBar[20];
	@Override
	public void init(){
		/*mapper = new ParametrizedMapper(0.9){
			public double f(double x, double y) {
				return 10*Math.sin(x*p)*Math.cos(y*p)*x;
			}
		};
		Range range = new Range(-150,150);
		int steps   = 50;
		
		// Create the object to represent the function over the given range.
		surface = (Shape)Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1,1,1,.5f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeDisplayed(true);
		surface.setWireframeColor(Color.BLACK);
		
		// Create a chart 
		chart = initializeChart(Quality.Intermediate);
		surface.setLegend(new AWTColorbarLegend(surface, 
							chart.getView().getAxe().getLayout().getZTickProvider(), 
							chart.getView().getAxe().getLayout().getZTickRenderer()));
		chart.getScene().getGraph().add(surface);*/
		
		
		this.chart = initializeChart(Quality.Advanced);
		replot();

		

		
	}
	
	public Chart getChart(){
		return chart;
	}
	
	public void start(){
		fpsText = "";
		t = new Thread(){
			TicToc tt = new TicToc();
			@Override
			public void run() {
				while(true){
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					tt.tic();
					//mapper.setParam( mapper.getParam() + 0.0001 );
					//remap(surface, mapper);
					//chart.render();
					
					replot();
					tt.toc();
					fpsText = Utils.num2str(1/tt.elapsedSecond(), 4) + " FPS";
				}
			}
		};
		t.start();
	}
	
	public void stop(){
		if(t!=null)
			t.interrupt();
	}
	
	
	
	public void replot(){
		int index =0;
		ChartScene localChartScene = this.chart.getScene();
		Random localRandom;
		(localRandom = new Random()).setSeed(0L);
		for (int i = 0; i < 200; i += 50) {
			for (int j = 0; j < 200; j += 50) {
				
				double d3 = localRandom.nextDouble() * Math.random()*10* 2.0D;
				double d2 = j;
				double d1 = i;
				Color.rng.setSeed(new Double( Math.random()*10).longValue());
				Color localColor;
				(localColor = Color.random()).a = 1F;
				if(localHistogramBar[index]!=null){
					localChartScene.remove(localHistogramBar[index]);
				}
				(localHistogramBar[index] = new HistogramBar()).setData(new Coord3d(
						d1, d2, 0.0D), (float) d3, 10.0F, localColor);
				localHistogramBar[index].setWireframeDisplayed(false);
				localHistogramBar[index].setWireframeColor(Color.BLACK);
				localChartScene.add(localHistogramBar[index]);
				index++;
			}
		}
	
		
		
	}
	protected void remap(Shape shape, Mapper mapper){
		List<AbstractDrawable> polygons = shape.getDrawables();		
		for(AbstractDrawable d: polygons){
			if(d instanceof Polygon){
				Polygon p = (Polygon) d;				
				for(int i=0; i<p.size(); i++){
					Point pt = p.get(i);
					Coord3d c = pt.xyz;
					c.z = (float) mapper.f(c.x, c.y);
				}
			}
		}
	}
	
	protected Chart chart;
	protected Shape surface;
	protected ParametrizedMapper mapper;	
	protected String fpsText;
	
	protected Thread t;
}
