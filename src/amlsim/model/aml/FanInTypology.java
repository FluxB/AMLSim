//
// Note: No specific bank models are used for this AML typology model class.
//

package amlsim.model.aml;

import amlsim.Account;
import amlsim.AMLSim;

import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.*;
import java.lang.*;

/**
 * Multiple accounts send money to the main account
 */
public class FanInTypology extends AMLTypology {

    // Originators and the main beneficiary
    private Account bene;  // The destination (beneficiary) account
    private List<Account> origList = new ArrayList<>();  // The origin (originator) accounts

    private long[] steps;
    private static final int SIMULTANEOUS = 1;
    private static final int FIXED_INTERVAL = 2;
    private static final int RANDOM_RANGE = 3;
    private static final int FIXED_INTERVAL_BIASED_START = 4;

    final private static float gatherVariance = AMLSim.getSimProp().getGatherVariance();
    final private static float startBiasRange = AMLSim.getSimProp().getStartBiasRange();
    final private static float roundAmountAlpha = AMLSim.getSimProp().getSarRoundAmountAlpha();
    final private static float roundAmountBeta = AMLSim.getSimProp().getSarRoundAmountBeta();

    private float roundAmountProbability;

    FanInTypology(float minAmount, float maxAmount, int start, int end){
        super(minAmount, maxAmount, start, end);

        // a beta distribution is used to model the round amount affinity of the actor
        BetaDistribution betaDistribution = new BetaDistribution(roundAmountAlpha, roundAmountBeta);
        roundAmountProbability = (float) betaDistribution.inverseCumulativeProbability(AMLSim.getRandom().nextDouble());
    }

    public void setParameters(int schedulingID){

        // Set members
        List<Account> members = alert.getMembers();
        Account mainAccount = alert.getMainAccount();
        bene = mainAccount != null ? mainAccount : members.get(0);  // The main account is the beneficiary
        for(Account orig : members){  // The rest of accounts are originators
            if(orig != bene) origList.add(orig);
        }

        // Set transaction schedule
        int numOrigs = origList.size();
        int totalStep = (int)(endStep - startStep + 1);
        int defaultInterval = Math.max(totalStep / numOrigs, 1);

        if (schedulingID == FIXED_INTERVAL_BIASED_START) {
            // we model p(x) = 1/d * exp(-1/d * x), with d = startBiasRange
            // the cumulative distribution is then: c(x) = 1 - exp(-1/d * x)
            double c = AMLSim.getRandom().nextDouble();
            this.startStep = (long) (-Math.log(1.0 - c) * startBiasRange);
            if (this.startStep > endStep) {
                this.startStep = endStep - 1;
            }
        } else {
            this.startStep = generateStartStep(defaultInterval);  //  decentralize the first transaction step
        }

        steps = new long[numOrigs];
        if(schedulingID == SIMULTANEOUS){
            long step = getRandomStep();
            Arrays.fill(steps, step);
        }else if(schedulingID == FIXED_INTERVAL || schedulingID == FIXED_INTERVAL_BIASED_START) {
            int range = (int)(endStep - startStep + 1);
            if(numOrigs < range){
                interval = range / numOrigs;
                for(int i=0; i<numOrigs; i++){
                    steps[i] = startStep + interval*i;
                    System.out.println("step " + this.steps[i]);
                }
            }else{
                long batch = numOrigs / range;
                for(int i=0; i<numOrigs; i++){
                    steps[i] = startStep + i/batch;
                    System.out.println("step " + this.steps[i]);
                }
            }
        }else if(schedulingID == RANDOM_RANGE){
            for(int i=0; i<numOrigs; i++){
                steps[i] = getRandomStep();
            }
        }
    }

    @Override
    public String getModelName() {
        return "FanInTypology";
    }

    public void sendTransactions(long step, Account acct){
        long alertID = alert.getAlertID();
        boolean isSAR = alert.isSAR();
        float amount = getRandomAmount();

        for(int i = 0; i< origList.size(); i++){
            if(steps[i] == step){
                Account orig = origList.get(i);
                makeTransaction(
                    step, AMLSim.getSimProp().makeTransactionMoreRealistic(amount, gatherVariance, roundAmountProbability),
                    orig, bene, isSAR, alertID
                );
            }
        }
    }
}
