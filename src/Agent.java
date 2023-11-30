package infra;

import java.util.Random;

class Agent {
    protected double rep, belief;
    protected double budget;
    protected int participations, opportunities, correctPreds;
    private final double CORRECTNESS_WEIGHT = 0.8;
    private final double REP_CAP = 0.8;
    private Random rand = new Random();

    public Agent(int p, int o, int c, int outcome, double budget) {
	this.participations = p;
	this.opportunities = o;
	this.correctPreds = c;

	this.rep = calcRep();
	this.belief = calcBelief(outcome);

	this.budget = budget;
    } //Agent

    public Agent(int outcome, double budget) {
	this.participations = 0;
	this.opportunities = 0;
	this.correctPreds = 0;

	this.rep = 0;
	this.belief = calcBelief(outcome);

	this.budget = budget;
    } //Agent

    private double calcRep() {
	/*
	  Rep should be weighted combination of correctPreds + opportunities taken.
	  When 0 participations, should be 0
	*/
	double rep = CORRECTNESS_WEIGHT * (correctPreds * 1.0)/participations;
	rep += (1 - CORRECTNESS_WEIGHT) * (participations * 1.0)/opportunities;
	return rep;
    } //private

    private double calcBelief(int outcome) {
	/*
	  Should be a function of rep s.t. + rep = closer to outcome.
	  When rep = 0, randomly distr. in [0,1]
	  //ToDo: Figure out this function, Negative rep?
	 */
	return rand.nextDouble();
    } //calcBelief
} //Agent
