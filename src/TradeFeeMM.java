package mm;

import infra.Agent;

public class TradeFeeMM extends mm.AMM{
    private double [] state;
    private final double BETA = 1.0;
    private final boolean LOG = true;
    private final double BASE_FEE = 0.05;
    private final double STEP_SIZE = 0.01;

    /**
     * Initialize new Trade Fee MM
     *
     * @param numOutcomes the number of outcomes
     * @return TradeFeeMM obj
     */
    public TradeFeeMM(int numOutcomes) {
        state = new double[numOutcomes];
        if (numOutcomes == 1) {
            state[0] = 0;
        } else {
            for (int i = 0; i < numOutcomes; ++i) {
                state[i] = 0;
            }
        }
    }
    /**
     *  Returns the cost  (positive if buying, negative
     *  if selling) to trade a given amount of a contract.
     *
     * @param outcome  the contract to buy
     * @param amt  the amount being traded (neg if selling)
     * @return cost
     */
    public double trade_cost(int outcome, double amt) {
        double current_C = 0;
        double new_C = 0;

        for (int i = 0; i < state.length; ++i) {
            current_C += Math.exp(state[i] / BETA);
            if (i == outcome) {
                new_C += Math.exp((state[i] + amt) / BETA);
            } else {
                new_C += Math.exp(state[i] / BETA);
            }
        }

        current_C = BETA * Math.log(current_C);
        new_C = BETA * Math.log(new_C);

        return new_C - current_C;
    }
    /**
     * Calculates the trading fee for an agent, based on their reputation
     * and how large their transaction is.
     *
     * @param agent the transactor
     * @param cost the transaction cost
     * @return fee
     */
    public double trading_fee(Agent agent, double cost) {
        return cost * BASE_FEE * (1 - agent.getRep());
    }

    /**
     Facilitates an agent buying some amount of contracts on some outcome.
     Deducts the payment from the buyer, if valid, and changes the market state.
     Does nothing if the call is invalid.

     @param buyer the agent buying the contracts
     @param amt the amount of contracts being bought
     @param outcome the buyer is buying contracts for
     @return whether the operation was valid (buyer had enough money for purchase)
     */
    public boolean buy(Agent buyer, double amt, int outcome) {
        double cost = trade_cost(outcome, amt);
        double fee = trading_fee(buyer, cost);
        //update market state
        state[outcome] += amt;

        if (cost > buyer.getBudget()) {
            //reset state
            state[outcome] -= amt;
            if (LOG) System.out.println("AGENT " + buyer.getID() + " FAILED TO BUY " + amt +
                    " CONTRACTS FOR " + cost + " (AGENT BUDGET: " + buyer.getBudget() + ")");
            return false;
        } else {
            buyer.subMoney(cost + fee);
            buyer.addHoldings(amt, outcome);
            if (LOG) System.out.println("AGENT " + buyer.getID() + " BOUGHT " + amt + " CONTRACTS FOR " + cost);
            return true;
        } //if
    }
    /**
     Facilitates an agent selling some amount of contracts on some outcome.
     Gives some payment to the buyer, if valid, and changes the market state.
     Does nothing if the call is invalid.

     @param seller the agent selling the contracts
     @param amt the amount of contracts being sold
     @param outcome the seller is selling contracts for
     @return whether the operation was valid (seller has the contracts they intend to sell)
     */
    public boolean sell(Agent seller, double amt, int outcome) {
        if (LOG) System.out.println("INITIATED SELL ON OUTCOME " + outcome + " OF " + amt + " CONTRACTS BY " + seller.getID());
        if (seller.getHolding(outcome) < amt) {
            if (LOG) System.out.println("AGENT " + seller.getID() + " FAILED TO SELL " + amt + " (ONLY HAS " + seller.getHoldings() + ")");
            return false;
        } //if
        amt *= -1;

        //update market state
        state[outcome] += amt;
        double cost = trade_cost(outcome, amt);
        double fee = trading_fee(seller, cost);

        seller.addMoney(cost - fee);
        seller.subHoldings(amt, outcome);
        if (LOG) System.out.println("AGENT " + seller.getID() + " SOLD " + amt + " CONTRACTS FOR " + cost);
        return true;
    } //sell

    /**
     Gets the instantaneous price of the contract on some outcome.

     @param outcome the outcome to get the contract price of
     @return the price of the contract of the outcome
     */
    public double getPrice(int outcome) {
        double price = Math.exp(state[outcome]/BETA);
        double sum = 0;

        for (int i = 0; i < state.length; i++) {
            sum += Math.exp(state[i]/BETA);
        } //for

        return price/sum;
    } //getPrice

    /**
     * Root finding for a budget-constrained buy on a contract. The agent would like
     * to but more, but is constrained by the cost of the contract as well as the trading
     * fee associated.
     *
     * @param agent the purchashing agent
     * @param outcome the contract being bought
     * @return qty of contract to be bought (err lower rather than higher)
     */
    public double constrained_buy(Agent agent, int outcome) {
        double qty = 0;
        double transaction_cost;
        double total_cost = 0;

        while (total_cost < agent.getBudget()) {
            qty += STEP_SIZE;
            transaction_cost = trade_cost(outcome, qty);
            total_cost = transaction_cost + trading_fee(agent, transaction_cost);
        }
        return Math.max(0.0, qty - STEP_SIZE);
    }

    /**
     Returns the quantity of contract `outcome` bought by agent a till its price becomes `price`.

     @param a the purchasing agent
     @param outcome the index of the outcome to purchase in state
     @param price of the outcome after the purchase
     @return the quantity of contract `outcome` bought
     */
    public double buyTillPrice(Agent a, int outcome, double price) throws Exception {
        double qty = 0;
        double PHI = (price * Math.exp(state[outcome == 0 ? 1 : 0]))/(1 - price);
        qty = Math.log(PHI) - state[outcome];

        if (!this.buy(a, qty, outcome)) {
            if (LOG) System.out.println("COULDN'T BUY " + qty + " CONTRACTS! BUYING MAX POSSIBLE WITH BUDGET INSTEAD.");
            //Buy as much as possible with remaining budget. Specifically for binary outcome!
            // Need to take into account the trading fee! Just use a binary search here.
            qty = constrained_buy(a, outcome);
            if (qty == 0) return qty;
            if (!this.buy(a, qty, outcome)) {
                throw new Exception("BUY TILL PRICE FALLBACK ERROR, TRIED TO BUY " + qty + " CONTRACTS");
            } //if
        } //if

        return qty;
    } //buyTillPrice
    /**
     Returns the quantity of contract `outcome` bought by agent a till its price becomes `price`.

     @param a the purchasing agent
     @param outcome the index of the outcome to purchase in state
     @param price of the outcome after the purchase
     @return the quantity of contract `outcome` bought
     */
    public double sellTillPrice(Agent a, int outcome, double price) {
        double qty = 0;

        for (int i = 0; i < state.length; i++) {
            if (i != outcome) {
                qty += price * Math.exp(state[i]/BETA);
            } //if
        } //for

        qty /= (1 - price);
        qty = state[outcome] - qty;

        if (qty < 0) return qty;

        //if we don't have qty amount, sell all that we can
        this.sell(a, Math.min(qty, a.getHolding(outcome)), outcome);

        return qty;
    } //sellTillPrice

    /**
     * Get the current market state for a contract (outcome)
     *
     * @param outcome
     * @return state[outcome]
     */
    public double get_state(int outcome) {
        return state[outcome];
    }
    @Override
    public String toString() {
        String s = "CONTRACTS SOLD: (" + state[0] + "," + state[1] + ")\n";
        s += "CONTRACT PRICES: (" + getPrice(0) + "," + getPrice(1) + ")";
        return s;
    } //toString
    public String get_MM_type() {
        return "TradeFee";
    }
}
