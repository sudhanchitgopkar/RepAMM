package infra;

import mm.AMM;
import mm.SRMM;
import mm.TradeFeeMM;
import infra.Sim;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;

public class Runner {
	private static final int NUM_MARKETS = 50;
	private static final int NUM_AGENTS = 10;
	private static final int NUM_OUTCOMES = 2;
	private static String output_directory;
	private static Agent [] agents;
    public static void main (String [] args) {
		String mm_type = "TradeFee";
		output_directory = make_output_dir(mm_type);
		new File(output_directory).mkdirs();

		// initialize agents
		agents  = new Agent [NUM_AGENTS];
		for (int i = 0; i < NUM_AGENTS; i++) {
			agents[i] = new Agent(i, NUM_OUTCOMES, 5);
		} //for
		for (int i = 0; i < NUM_MARKETS; ++i) {
			AMM amm = new TradeFeeMM(2);
			Sim s = new Sim(amm, agents, i, output_directory);
			s.run();
		}//for
    } //main

	public static String make_output_dir(String mm_type) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM_dd_HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		return mm_type + "_" +  dtf.format(now);
	}
} //class
