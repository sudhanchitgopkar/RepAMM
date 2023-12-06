package infra;

import mm.AMM;
import mm.SRMM;
import infra.Sim;

public class Runner {
    public static void main (String [] args) {
	AMM srmm = new SRMM(2);
	Sim s = new Sim(srmm);
	s.run();
    } //main
} //class
