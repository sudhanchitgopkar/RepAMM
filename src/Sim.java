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
    private final int N = 2; //number of agents
    private final int ROUNDS = 5;
    private final boolean LOG = true;

    private AMM amm;
    private final Agent [] agents;
	private final int market_num;

	private final String METADATA;
	private FileOutputStream METADATA_IO;
	private final String PRICE_HISTORY;
	private FileOutputStream PRICE_HISTORY_IO;
	private final String MARKET_HISTORY;
	private FileOutputStream MARKET_HISTORY_IO;
	private final String AGENT_PNL_HISTORY;
	private FileOutputStream AGENT_PNL_IO;
	private final String MM_PNL_HISTORY;
	private FileOutputStream MM_PNL_IO;
	private final String AGENT_REP;
	private FileOutputStream AGENT_REP_IO;

    public Sim(AMM amm, int market_num, String output_directory) {
		this.amm = amm;//new SRMM(NUM_OUTCOMES);
		this.market_num = market_num;
		this.METADATA = output_directory + "/metadata_" + market_num + ".txt";
		this.PRICE_HISTORY = output_directory + "/price_history_" + market_num + ".csv";
		this.MARKET_HISTORY = output_directory + "/market_history_" + market_num + ".csv";
		this.AGENT_PNL_HISTORY = output_directory + "/agent_pnl_history_" + market_num + ".csv";
		this.MM_PNL_HISTORY = output_directory + "/MM_pnl_history_" + market_num + ".csv";
		this.AGENT_REP = output_directory + "/agent_rep_history_" + market_num + ".csv";
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

			File agent_pnl_file = new File(this.AGENT_PNL_HISTORY);
			agent_pnl_file.createNewFile();
			this.AGENT_PNL_IO = new FileOutputStream(agent_pnl_file, false);

			File mm_pnl_file = new File(this.MM_PNL_HISTORY);
			agent_pnl_file.createNewFile();
			this.MM_PNL_IO = new FileOutputStream(mm_pnl_file, false);

			File agent_rep_file = new File(this.AGENT_REP);
			agent_rep_file.createNewFile();
			this.AGENT_REP_IO = new FileOutputStream(agent_rep_file, false);

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

	public Sim(AMM amm, Agent[] agents, int market_num, String output_directory) {
		this.amm = amm;//new SRMM(NUM_OUTCOMES);
		this.market_num = market_num;
		this.METADATA = output_directory + "/metadata_" + market_num + ".txt";
		this.PRICE_HISTORY = output_directory + "/price_history_" + market_num + ".csv";
		this.MARKET_HISTORY = output_directory + "/market_history_" + market_num + ".csv";
		this.AGENT_PNL_HISTORY = output_directory + "/agent_pnl_history_" + market_num + ".csv";
		this.MM_PNL_HISTORY = output_directory + "/MM_pnl_history_" + market_num + ".csv";
		this.AGENT_REP = output_directory + "/agent_rep_history_" + market_num + ".csv";
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

			File agent_pnl_file = new File(this.AGENT_PNL_HISTORY);
			agent_pnl_file.createNewFile();
			this.AGENT_PNL_IO = new FileOutputStream(agent_pnl_file, false);

			File mm_pnl_file = new File(this.MM_PNL_HISTORY);
			agent_pnl_file.createNewFile();
			this.MM_PNL_IO = new FileOutputStream(mm_pnl_file, false);

			File agent_rep_file = new File(this.AGENT_REP);
			agent_rep_file.createNewFile();
			this.AGENT_REP_IO = new FileOutputStream(agent_rep_file, false);

		} catch (IOException e) {
			System.out.println("Error creating files...");
		}

		// load in the set of agents to be used...
		this.agents = agents;
		// calculate their beliefs for the current market, and determine
		// whether they have correctly predicted the outcome.
		for (Agent agent : agents) {
			agent.calcBelief(OUTCOME);
			if (Math.abs(agent.getBelief() - OUTCOME) < 0.5) {
				agent.correctPreds.add(0, 1);
			} else {
				agent.correctPreds.add(0, 0);
			}
		}

		if (LOG) System.out.println("MADE NEW SIM:\nNum Outcomes: " + NUM_OUTCOMES +
				"\nOutcome: " + OUTCOME + "\nROUNDS: " + ROUNDS + "\nAGENTS: " + N);
		if (LOG) {
			// initialize file info
			try {
				METADATA_IO.write(("Num agents: " + String.valueOf(agents.length)).getBytes());
				METADATA_IO.write(10);
				METADATA_IO.write(("Market Maker: " + amm.get_MM_type()).getBytes());
				METADATA_IO.write(10);
				METADATA_IO.write(("Outcome: " + String.valueOf(OUTCOME)).getBytes());
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
		} catch (IOException e) { System.out.println("whoopsie"); }
	}

	/**
	 * Log PNL information for an agent after a market
	 * @param agent the agent being tracked.
	 */
	public void agent_PnL_logger(Agent agent) {
		try {
			AGENT_PNL_IO.write(String.valueOf(agent.getPnL()).getBytes());
			AGENT_PNL_IO.write(10);
		} catch (IOException e) {
			System.out.println("LOG ERROR AGENT PNL");
		}
	}

	/**
	 * MM PNL information logger.
	 * @param mm_PNL
	 */
	public void mm_PnL_logger(double mm_PNL) {
		try {
			MM_PNL_IO.write(String.valueOf(mm_PNL).getBytes());
			MM_PNL_IO.write(10);
		} catch (IOException e) {
			System.out.println("LOG ERROR MM PNL");
		}
	}

	public void agent_rep_logger(Agent agent) {
		try {
			AGENT_REP_IO.write(String.valueOf(agent.getRep()).getBytes());
			AGENT_REP_IO.write(10);
		} catch (IOException e) {
			System.out.println("Couldn't record agent reputation");
		}
	}

    public void run() {
		logger();
		if (LOG) System.out.println("MARKET NUM: " + market_num);
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

		// Track agent performance and reset per-market values.
		double mm_PNL = 0;
		for (Agent agent : agents) {
			agent.calcRep();
			agent_rep_logger(agent);
			agent.add_opportunity();
			// update participation
			if (agent.getHolding(0) + agent.getHolding(1) > 0.0) {
				agent.add_participation();
			}
			// payout agent and calculate PnL
			double payout = 0;
			if (OUTCOME == 0) {
				payout = agent.getHoldings()[0];
			} else if (OUTCOME == 1) {
				payout = agent.getHoldings()[1];
			}
			double agent_PNL = payout - (agent.getInitial_budget() - agent.getBudget());
			agent.setPnL(agent_PNL);
			mm_PNL -= agent_PNL;
			// reset holdings
			agent.reset_holdings();
			// reset budget
			agent.setBudget(agent.getInitial_budget());

			agent_PnL_logger(agent);
		}
		// log MM PNL
		mm_PnL_logger(mm_PNL);
		// close last files
		try {
			MM_PNL_IO.close();
			AGENT_PNL_IO.close();
			AGENT_REP_IO.close();
		} catch (IOException e) {
			System.out.println("couldnt get them files closed, sorry");
		}

    } //run
} //class
