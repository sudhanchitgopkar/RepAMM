package infra;

import java.util.Random;
import java.util.ArrayList;
import infra.Agent;
import mm.AMM;
import mm.SRMM;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

public class Sim {
    //Params
    private final long SEED = 4092002;
    private Random rand = new Random(SEED);
    private final int NUM_OUTCOMES = 2;
    private final int OUTCOME = rand.nextInt(2); //returns int 0 or 1
    private final int N = 10; //number of agents
    private final int ROUNDS = 5;
    private final boolean LOG = true;

    private AMM amm;
    private Agent [] agents;
	private int market_num;

	private String METADATA;
	private FileOutputStream METADATA_IO;
	private String PRICE_HISTORY;
	private FileOutputStream PRICE_HISTORY_IO;
	private String MARKET_HISTORY;
	private FileOutputStream MARKET_HISTORY_IO;

    public Sim(AMM amm, int market_num, String output_directory) {
		this.amm = amm;//new SRMM(NUM_OUTCOMES);
		this.market_num = market_num;
		this.METADATA = output_directory + "/metadata_" + market_num + ".txt";
		this.PRICE_HISTORY = output_directory + "/price_history_" + market_num + ".csv";
		this.MARKET_HISTORY = output_directory + "/market_history_" + market_num + ".csv";
		try {
			// create new files
			File metadata_file = new File(this.METADATA);
			metadata_file.createNewFile();
			this.METADATA_IO = new FileOutputStream(metadata_file, false);

			File price_hist_file = new File(this.PRICE_HISTORY);
			price_hist_file.createNewFile();
			this.PRICE_HISTORY_IO = new FileOutputStream(price_hist_file, false);

			File market_hist_file = new File(this.MARKET_HISTORY);
			market_hist_file.createNewFile();
			this.MARKET_HISTORY_IO = new FileOutputStream(market_hist_file, false);
		} catch (IOException e) {
			System.out.println("Error creating files...");
		}
	
	//Init Agents
	agents  = new Agent [N];
	for (int i = 0; i < N; i++) {
	    agents[i] = new Agent(i, OUTCOME, NUM_OUTCOMES, 5);
	    //agents[i] new Agent(i, 10, 10, 10, OUTCOME, NUM_OUTCOMES, 5);
	} //for

	if (LOG) System.out.println("MADE NEW SIM:\nNum Outcomes: " + NUM_OUTCOMES + 
				    "\nOutcome: " + OUTCOME + "\nROUNDS: " + ROUNDS + "\nAGENTS: " + N);
	if (LOG) {
		// initialize file info
		try {
			METADATA_IO.write(("Num agents: " + String.valueOf(agents.length)).getBytes());
			METADATA_IO.write(10);
			METADATA_IO.write(("Market Maker: " + amm.get_MM_type()).getBytes());
			METADATA_IO.write(10);
			METADATA_IO.close();

			PRICE_HISTORY_IO.write("Contract_0,Contract_1".getBytes());
			PRICE_HISTORY_IO.write(10);
			MARKET_HISTORY_IO.write("Contract_0,Contract_1".getBytes());
			MARKET_HISTORY_IO.write(10);
		} catch (IOException e) {
			System.out.println("Error creating metadata file...");
		}
	}
    } //Sim

	public void logger() {
		try {
			PRICE_HISTORY_IO.write((
							String.valueOf(amm.getPrice(0)) + "," + String.valueOf(amm.getPrice(1))
					).getBytes()
			);
			PRICE_HISTORY_IO.write(10);
			MARKET_HISTORY_IO.write((
							String.valueOf(amm.get_state(0)) + "," + String.valueOf(amm.get_state(1))
					).getBytes()
			);
			MARKET_HISTORY_IO.write(10);
		} catch (IOException e) { System.out.println("whoopsie"); };
	}

    public void run() {
		logger();
	for (int i = 0; i < ROUNDS; i++) {
	    if (LOG) System.out.println("ROUND: " + i + "\n" + amm + "\n----\n");
	    
	    for (Agent a : agents) {
		if (LOG) System.out.println("NOW PLAYING: " + a);
		if (LOG) System.out.println("TRADING OUTCOME 0");

		if (a.getBelief() < amm.getPrice(0)) {
		    amm.sellTillPrice(a, 0, a.getBelief());
		} else if (a.getBelief() > amm.getPrice(0)) {
		    try {
			amm.buyTillPrice(a, 0, a.getBelief());
		    } catch (Exception e) {
			System.out.println(e);
		    } //catch
		} //if
		if (LOG) System.out.println("Updated Agent\n" + a);
		if (LOG) System.out.println("Updated Market State\n" + amm);
		if (LOG) System.out.println("TRADING OUTCOME 1");
		
		if (1 - a.getBelief() < amm.getPrice(1)) {
		    amm.sellTillPrice(a, 1, 1 - a.getBelief());
		} else if (1 - a.getBelief() > amm.getPrice(1)) {
		    try {
			amm.buyTillPrice(a, 1, 1 - a.getBelief());
		    } catch (Exception e) {
			System.out.println(e);
		    } //catch
		} //if
		if (LOG) System.out.println("Updated Agent\n" + a);
		if (LOG) System.out.println("Updated Market State\n" + amm);
		logger();
	    } //for
	    
	} //for
		// close out files
		try {
			PRICE_HISTORY_IO.close();
			MARKET_HISTORY_IO.close();
		} catch (IOException e) {
			System.out.println("Error closing files?!!?");
		}
    } //run

} //class
