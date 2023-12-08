package infra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Agent {
    protected double rep, belief, budget, initial_budget;
    protected int participations;
    protected int opportunities;
    protected ArrayList<Integer> correctPreds;
    protected double [] holdings;

    private final double CORRECTNESS_WEIGHT = 0.8;
    private final double REP_CAP = 1.0;
    private final int LAST_N_MATCHES = 10; // matches to include in a rolling window
    private Random rand = new Random();
    private int id;
    protected double PnL; // profits and losses
    
    /**
       Initializes a new Agent with some reputation and belief.
       Belief is a function of reputation, reputation is a function of participation + correct prediction.
       
       @param p number of prediction markets agent has participated in (1+ trade)
       @param o number of prediction markets agent *could have* participated in
       @param c number of correct predictions made by the agent
       @param outcome correct outcome of current market
       @param numOutcomes number of possible outcomes for the market
       @param budget amount of money agent has to play with
       
       @return Agent object
     */
    public Agent(int id, int p, int o, int c, int outcome, int numOutcomes, double budget) {
	this.id = id;
	this.participations = p;
	this.opportunities = o;
	this.correctPreds = new ArrayList<Integer>();
    this.PnL = 0.0;

    for (int i = 0; i < c; ++i) {
        this.correctPreds.add(1);
    }
    for (int i = 0; i < c - this.LAST_N_MATCHES; ++i) {
        this.correctPreds.add(0);
    }

	this.rep = calcRep();
	this.belief = calcBelief(outcome);

	this.budget = budget;
	this.initial_budget = budget;

	this.holdings = new double[numOutcomes];
	for (int i = 0; i < numOutcomes; i++) {
	    holdings[i] = 0;
	} //for
    } //Agent
    
    /**
       Initializes a new Agent with no reputation and random belief.
    
       @param outcome correct outcome of current market
       @param numOutcomes number of possible outcomes for the market
       @param budget amount of money agent has to play with
       
       @return Agent object
     */
    public Agent(int id, int outcome, int numOutcomes, double budget) {
	this.id = id;
	this.participations = 0;
	this.opportunities = 0;
	this.correctPreds = new ArrayList<Integer>();

	this.rep = calcRep();
	this.belief = calcBelief(outcome);
	this.initial_budget = budget;
	this.PnL = 0.0;

	this.budget = budget;
	
	this.holdings = new double[numOutcomes];
	for (int i = 0; i < numOutcomes; i++) {
	    holdings[i] = 0;
	} //for
    } //Agent

    /**
     * This constructor for the agent assumes that they will have to
     * re-calculate their beliefs for every market that they participate in.
     * Likewise, the repuation of the agent has to be re-calced after every
     * market.
     * @param id agent id
     * @param numOutcomes number of outcomes in the market
     * @param budget trading budget for the market
     */
    public Agent(int id, int numOutcomes, double budget) {
        this.id = id;
        this.budget = budget;
        this.rep = calcRep();
        this.participations = 0;
        this.opportunities = 0;
        this.correctPreds = new ArrayList<Integer>();
        this.PnL = 0.0;
        this.initial_budget = budget;

        this.holdings = new double[numOutcomes];
        for (int i = 0; i < numOutcomes; ++i) {
            holdings[i] = 0;
        }
    }

    /**
     * Sigmoid helper function for belief calculation:
     * sig(x)  = 1 / (1 + exp(-x))
     *
     * @param x real number
     * @return sig(x)
     */
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    } //sigmoid

    /**
       Calculates an agent's reputation.
       Reputation is a weighted combination of correctPreds + opportunities taken.
       Players start with 0 reputation and cannot have negative participation.
       
       @return agent's reputation
    */
    protected double calcRep() {
	return rand.nextFloat(); //tester for RepMM
        /*
	if (participations == 0) return 0;
        int lower_val = Math.min(participations, LAST_N_MATCHES);
        double sig = sigmoid(lower_val);
        int num_correct = correctPreds.subList(0,lower_val).stream().mapToInt(Integer::intValue).sum();
        return sig * num_correct / lower_val;
	*/
    } //private
    
    /**
       Calculates an agent's belief about the likelihood of the outcome.
       Should be a function of rep such that higher rep = closer to outcome.
       When reputation = 0, belief is randomly distributed in [0,1].

       @return agent's belief that the outcome will occur
      */
    protected double calcBelief(int outcome) {
	double noise = (1 - rep) * rand.nextDouble();
	int dir = rand.nextInt(2);

	if (dir == 0) {
	    this.belief = outcome - noise > 0 ? outcome - noise : outcome + noise;
	} else {
	    this.belief = outcome + noise < 1 ? outcome + noise : outcome - noise;
	} //if
	
	return this.belief;
    } //calcBelief
    
    /**
       Getter for agent's budget.

       @return agent's budget
     */
    public double getBudget() {
	return budget;
    } //getBudget

    /**
     * Budget Setter.
     * @param new_budget
     */
    void setBudget(double new_budget) { budget = new_budget; }

    /**
     * Getter for agent's initial budget.
     * @return initial budget
     */
    public double getInitial_budget() { return initial_budget; } //getInitialBudget
    
    /**
       Getter for agent's belief.

       @return agent's belief
     */
    public double getBelief() {
	return belief;
    } //getBelief
    
    
    /**
       Getter for agent's reputation.

       @return agent's reputation
     */
    public double getRep() {
	return rep;
    } //getRep

    /**
       Setter for agent's budget.

       @param amt amount of money to add to player's budget
       @return player's budget after adding money
     */
    public double addMoney(double amt) {
	budget += amt;
	return budget;
    } //giveMoney

    /**
       Setter for agent's budget.

       @param amt amount of money to remove from player's budget
       @return player's budget after removing money
     */
    public double subMoney(double amt) {
	return this.addMoney(-1 * amt);
    } //takeMoney
    
    /**
       Getter for agent's holdings.
       
       @return number of contracts on outcome happening the agent holds
     */
    public double [] getHoldings() {
	return holdings;
    } //getHolding
    
    /**
       Returns the number of contracts of a certain outcome the agent has
       
       @param outcome the outcome to get number of contracts held for
       @return the number of contracts of outcome held
     */
    public double getHolding(int outcome) {
	return holdings[outcome];
    } //getHolding

    /**
     * Reset agent holdings after a market.
     */
    void reset_holdings() {
        Arrays.fill(holdings, 0.0);
    }
    
    /**
       Setter for agent's holdings.

       @param amt amount of contracts to add to agent's holdings
       @param outcome to add holdings to
     */
    public void addHoldings(double amt, int outcome) {
	holdings[outcome] += amt;
    } //addHoldings

    /**
       Setter for agent's holdings.
       
       @param amt amount of contracts to remove from agent's holdings
       @param outcome the outcome to remove holdings from
     */
    public void subHoldings(double amt, int outcome) {
	holdings[outcome] -= amt;
    } //subHoldings
    
    /**
       Getter for Agent's ID.
       
       @return agent's ID
     */
    public int getID() {
	return id;
    } //getID

    /**
     * Increment the agent's participations
     */
    void add_participation() { ++participations; }

    /**
     * Increment agent opportunity.
     */
    void add_opportunity() { ++opportunities; }

    /**
     * PnL getter for the agent
     * @return
     */
    double getPnL() { return PnL; }

    /**
     * PnL setter for the agent
     * @param new_PnL
     */
    void setPnL(double new_PnL) { PnL = new_PnL; }

    @Override
    public String toString() {
	String s = "ID: " + id;
	s += "\tREP: " + rep + "\n";
	s += "\tBELIEF: " + belief + "\n";
	s += "\tHOLDINGS: (" + holdings[0] + "," + holdings[1] + ")" + "\n";
	s += "\tBUDGET: " + budget + "\n";

	return s;
    } //toString
} //Agent
