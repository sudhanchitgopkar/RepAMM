package mm;

import infra.Agent;

abstract public class AMM {
    
    public abstract boolean buy(Agent buyer, double amt, int outcome);
    public abstract boolean sell(Agent seller, double amt, int outcome);
    public abstract double getPrice(int outcome);
    public abstract double buyTillPrice(Agent a, int outcome, double price) throws Exception;
    public abstract double sellTillPrice(Agent a, int outcome, double price);
    public abstract String get_MM_type();

    public abstract double get_state(int outcome);
} //AMM
