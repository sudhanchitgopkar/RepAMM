package infra;

import java.util.Random;

public class Agent {
    protected double rep, belief, budget;
    protected int participations, opportunities, correctPreds;
    protected double holdings;

    private final double CORRECTNESS_WEIGHT = 0.8;
    private final double REP_CAP = 0.8;
    private Random rand = new Random();
    
    /**
       Initializes a new Agent with some reputation and belief.
       Belief is a function of reputation, reputation is a function of participation + correct prediction.
       
       @param p number of prediction markets agent has participated in (1+ trade)
       @param o number of prediction markets agent *could have* participated in
       @param c number of correct predictions made by the agent
       @param outcome correct outcome of current market
       @param budget amount of money agent has to play with
       
       @return Agent object
     */
    public Agent(int p, int o, int c, int outcome, double budget) {
	this.participations = p;
	this.opportunities = o;
	this.correctPreds = c;

	this.rep = calcRep();
	this.belief = calcBelief(outcome);

	this.budget = budget;
	holdings = 0;
    } //Agent
    
    /**
       Initializes a new Agent with no reputation and random belief.
    
       @param outcome correct outcome of current market
       @param budget amount of money agent has to play with
       
       @return Agent object
     */
    public Agent(int outcome, int numOutcomes, double budget) {
	this.participations = 0;
	this.opportunities = 0;
	this.correctPreds = 0;

	this.rep = 0;
	this.belief = calcBelief(outcome);

	this.budget = budget;
	holdings = 0;
    } //Agent

    /**
       Calculates an agent's reputation.
       Reputation is a weighted combination of correctPreds + opportunities taken.
       Players start with 0 reputation and cannot have negative participation.
       
       @return agent's reputation
    */
    private double calcRep() {
	double rep = CORRECTNESS_WEIGHT * (correctPreds * 1.0)/participations;
	rep += (1 - CORRECTNESS_WEIGHT) * (participations * 1.0)/opportunities;
	return rep;
    } //private
    
    /**
       Calculates an agent's belief about the likelihood of the outcome.
       Should be a function of rep such that higher rep = closer to outcome.
       When reputation = 0, belief is randomly distributed in [0,1].

       @return agent's belief that the outcome will occur
      */
    private double calcBelief(int outcome) {
	//ToDo: Figure out this function
	return rand.nextDouble();
    } //calcBelief
    
    /**
       Getter for agent's budget.

       @return agent's budget
     */
    public double getBudget() {
	return budget;
    } //getBudget


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
    public double getHoldings() {
	return holdings;
    } //getHolding
    
    /**
       Setter for agent's holdings.

       @param amt amount of contracts to add to agent's holdings
     */
    public void addHoldings(double amt) {
	holdings += amt;
    } //addHoldings

    /**
       Setter for agent's holdings.
       
       @param amt amount of contracts to remove from agent's holdings
     */
    public void subHoldings(double amt) {
	holdings -= amt;
    } //subHoldings
} //Agent
