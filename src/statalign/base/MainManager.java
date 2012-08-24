package statalign.base;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.SwingUtilities;

import mpi.MPI;
import statalign.MPIUtils;
import statalign.base.thread.MainThread;
import statalign.base.thread.StoppedException;
import statalign.model.subst.SubstitutionModel;
import statalign.postprocess.Postprocess;
import statalign.postprocess.PostprocessManager;
import statalign.postprocess.gui.InputGUI;
import statalign.ui.MainFrame;

/**
 * 
 * This is the central class in the information flow amongst classes.
 * 
 * This class also manages the main thread that runs the MCMC.
 * 
 * @author miklos, novak
 * 
 */
public class MainManager {

	/**
	 * Reference to all input data required to run StatAlign
	 */
	public InputData inputData = new InputData();

	/**
	 * This is the postprocess-manager of our program, which handles the
	 * postprocesses applied on the MCMC run.
	 */
	public PostprocessManager postProcMan;

	/**
	 * Array of substitution model classes that can be selected for an analysis.
	 */
	public Class<? extends SubstitutionModel>[] substModels;

	/**
	 * The main window of the program.
	 */
	public MainFrame frame;

	/**
	 * The graphical interface of the Input panel
	 */
	public InputGUI inputgui;

	/**
	 * Main (background) calculation thread of the application
	 */
	public MainThread thread;

	/**
	 * The full path of the input file from which we read the sequences.
	 */
	public String fullPath;

	/**
	 * Alignment formats in which StatAlign can generate output
	 * 
	 * Implemented formats currently are <tt>StatAlign</tt> (our own format),
	 * <tt>Clustal</tt>, <tt>Fasta</tt>, <tt>Phylip</tt>, <tt>Nexus</tt>
	 */
	public static String[] alignmentTypes = new String[] { "StatAlign",
			"Clustal", "Fasta", "Phylip", "Nexus" };

	/**
	 * A trivial constructor that only sets <tt>MainFrame</tt> and substModels.
	 * 
	 * The MainFrame creates its MainManager, and the MainManager knows who is
	 * its owner MainFrame, so it can access GUIs on the MainFrame
	 * 
	 * @param frame
	 *            The owner of the MainManager, the main window of the graphical
	 *            interface.
	 */
	public MainManager(MainFrame frame) {
		this.frame = frame;
		postProcMan = new PostprocessManager(this);
	}

	/**
	 * This function starts a new MCMC run.
	 * 
	 * It asks files for writing outputs, and it launches a <tt>MainThread</tt>
	 * that handles the MCMC run.
	 */
	public void start() {

		try {
			postProcMan.logFile = new FileWriter(fullPath + ".log");

			for (Postprocess p : postProcMan.plugins) {
				if (p.postprocessWrite) {
					String name = fullPath + "." + p.getFileExtension();
					System.out.println("Output file for " + p.getTabName()
							+ ": " + name);
					p.outputFile = new FileWriter(name);
				}
			}

			thread = new MainThread(this);
			thread.start();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void startParallel(int noOfProcesses, int rank) {

		// TODO: Parallel GUI.
		if (frame != null) {
			frame.statusText.setText(" Generating initial tree and alignment...");
		}

		// Sets up the logging for the master process.
		if (MPIUtils.isMaster(rank)) {
			MPIUtils.println(rank, "Setting up log files for the plugins:");
			try {
				postProcMan.logFile = new FileWriter(fullPath + ".log");

				for (Postprocess p : postProcMan.plugins) {
					if (p.postprocessWrite) {
						String name = fullPath + "." + p.getFileExtension();
						MPIUtils.println(rank, 
								"Output file for " + p.getTabName() + ": " + name);
						p.outputFile = new FileWriter(name);
					}
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
			MPIUtils.println(rank, "Generating initial tree and alignment...");
		}

		// Determines the heat of the chain.
		double heat = 1.0d / (1.0d + ((double) rank / noOfProcesses));;

		try {
			inputData.title = new File(fullPath).getName();
			Tree tree = new Tree(inputData.seqs.sequences.toArray(new String[inputData.seqs.sequences
					.size()]), inputData.seqs.seqNames.toArray(new String[inputData.seqs.seqNames
					.size()]), inputData.model, inputData.model.attachedScoringScheme, new File(
					fullPath).getName());
			Mcmc mcmc = new Mcmc(tree, inputData.pars, postProcMan, noOfProcesses, rank, heat);
			mcmc.doMCMC();

			// Sets up a barrier.
			MPI.COMM_WORLD.Barrier();

			// Retrieve the log-likelihood ratio from all of the workers
			double[] logLikelihood = new double[] { tree.getLogLike() };
			double[] maxLogLikelihood = new double[1];
			MPI.COMM_WORLD.Reduce(logLikelihood, 0, maxLogLikelihood, 0, 1,
					MPI.DOUBLE, MPI.MAX, 0);

			MPIUtils.println(
					rank,
					"My loglikelihood ratio: "
							+ Double.toString(logLikelihood[0]));
			if (MPIUtils.isMaster(rank)) {
				MPIUtils.println(rank, "Max loglikelihood ratio: "
						+ Double.toString(maxLogLikelihood[0]));
			}

			System.out.println(mcmc.getInfoString() + " Heat: " + mcmc.tree.heat);

			finished();
			System.out.println("Ready.");

		} catch (StoppedException stex) {
			//stex.printStackTrace(System.err);
			finished();
			frame.statusText.setText(frame.IDLE_STATUS_MESSAGE);
			//System.out.println("IS THE EXCEPTION HERE?");
		}
	}

	/**
	 * Called when the MCMC thread terminates, signals end of the process back
	 * to MainFrame.
	 */
	public void finished() {
		if (frame != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					frame.finished();
				}
			});
		}
	}

	public void deactivateRNA() {
		frame.deactivateRNA();
	}

}