package statalign.postprocess.plugins;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import statalign.distance.Distance;

import statalign.base.InputData;
import statalign.base.State;
import statalign.postprocess.Postprocess;
import statalign.postprocess.gui.DistanceGUI;

public class VisualDistance extends statalign.postprocess.Postprocess {

	public String title;
	JPanel pan = new JPanel(new BorderLayout());
	DistanceGUI gui;
	PPFold ppFold;

	ArrayList<String> initialAlignment;
	ArrayList<String> currentAlignment;

	public ArrayList<Double> distances;
	double currentDistance = 0;

	public VisualDistance() {
		sampling = true;
		screenable = true;
		outputable = true;
		postprocessable = true;
		postprocessWrite = true;
		rnaAssociated = true;
	}

	@Override
	public String getTabName() {
		// TODO Auto-generated method stub
		return "Distance";
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPanel getJPanel() {
		// TODO Auto-generated method stub
		return pan;
	}

	@Override
	public void reloadPanel() {
		pan = new JPanel(new BorderLayout());
	}

	@Override
	public String getTip() {
		// TODO Auto-generated method stub
		return "Plots distance between alignment samples.";
	}

	@Override
    public double getTabOrder() {
        return 11.0d;
    }

	@Override
	public String[] getDependences() {
		return new String[] { "statalign.postprocess.plugins.PPFold" };
	}

	@Override
	public void refToDependences(Postprocess[] plugins) {
		ppFold = (PPFold)plugins[0];
	}

	@Override
	public void setSampling(boolean enabled) {
		sampling = enabled;	
	}

	@Override
	public void beforeFirstSample(InputData input) {
		if(show) {
			title = input.title;
			pan.removeAll();
			gui = new DistanceGUI(title, this);
			
			JScrollPane scroll = new JScrollPane();
			scroll.setViewportView(gui);

			pan.add(scroll);
			System.out.println("Distance parent: " + pan.getParent());
			pan.getParent().getParent().validate();
		}

		distances = new ArrayList<Double>();
	}

	@Override
	public void newSample(State state, int no, int total) {
		if(sampling) {
			//System.out.println("THESE ARE THE CURRENT ALIGNMENTS: " + ppFold.tempAlignment);}

			//System.out.println(ppFold.al);
			if(ppFold.alignments.size() == 0 || ppFold.tempAlignment == null) {return;}

			//System.out.println("INITIAL ALIGNMENT: " + ppFold.al.sequences);
			//System.out.println("TEMP ALIGNMENT: " + ppFold.tempAlignment);
			if(no > 0)
			{
				currentDistance = Distance.AMA(ppFold.al.sequences, ppFold.tempAlignment);
			}
			//currentDistance = Distance.sequenceSimilarityScore(ppFold.al.sequences);

			distances.add(currentDistance);


			if(show) {gui.repaint();}

		}

	}

	@Override
	public void afterLastSample() {

	}

}
