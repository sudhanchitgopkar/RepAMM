package infra;

import mm.AMM;
import mm.SRMM;
import mm.TradeFeeMM;
import infra.Sim;

public class Runner {
    public static void main (String [] args) {
	//AMM srmm = new SRMM(2);
	AMM tfmm = new TradeFeeMM(2); 
	Sim s = new Sim(tfmm);
	s.run();
    } //main
} //class
