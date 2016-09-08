package org.jzy3d.demos.applet;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

public class SimpleApplet extends JApplet {

  public void start(){
      try{
          SwingUtilities.invokeAndWait(new Runnable(){
              public void run(){
            	   add(ScatterCanvas.generateCanvas());
              }
          });
      }
      catch(Exception e){
          System.out.println(e);
      }
  }
  
  	@Override
	public String getAppletInfo() {
		return super.getAppletInfo();
	}
}