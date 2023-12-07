package infra;

import mm.AMM;
import mm.SRMM;
import mm.TradeFeeMM;
import infra.Sim;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;

public class Runner {
	private static final int NUM_MARKETS = 1;
	private static String output_directory;
    public static void main (String [] args) {
		output_directory = make_output_dir();
		new File(output_directory).mkdirs();
	//AMM srmm = new SRMM(2);
		for (int i = 0; i < NUM_MARKETS; ++i) {
			AMM tfmm = new TradeFeeMM(2);
			Sim s = new Sim(tfmm, i, output_directory);
			s.run();
		}//for
    } //main

	public static String make_output_dir() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_uuuu_HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
} //class
