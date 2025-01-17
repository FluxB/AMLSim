package amlsim.model.normal;

import amlsim.AMLSim;
import amlsim.Account;
import amlsim.model.AbstractTransactionModel;

import java.util.List;
import java.util.Random;

/**
 * Send money only for once to one of the neighboring accounts regardless the transaction interval parameter
 */
public class SingleTransactionModel extends AbstractTransactionModel {

//    private static Random rand = new Random();
    private static Random rand = AMLSim.getRandom();
    
    /**
     * Simulation step when this transaction is done
     */
    private long txStep = -1;
    
    public String getModelName(){
        return "Single";
    }

    public void setParameters(int interval, float balance, long start, long end){
        super.setParameters(interval, balance, start, end);
        if(this.startStep < 0){  // Unlimited start step
            this.startStep = 0;
        }
        if(this.endStep < 0){  // Unlimited end step
            this.endStep = AMLSim.getNumOfSteps();
        }
        // The transaction step is determined randomly within the given range of steps
        this.txStep = this.startStep + rand.nextInt((int)(endStep - startStep + 1));
    }
    
    public void makeTransaction(long step){
        List<Account> beneList = this.account.getBeneList();
        int numBene = beneList.size();
        if(step != this.txStep || numBene == 0){
            return;
        }

        float amount = getTransactionAmount(maxTxAmount);
        int index = rand.nextInt(numBene);
        Account dest = beneList.get(index);
        this.makeTransaction(
            step, AMLSim.getSimProp().makeTransactionMoreRealistic(amount, (float) 1.0, roundAmountProbability), dest
        );

        // allow a SingleTransactionModel to make another transaction
        this.txStep = this.startStep + rand.nextInt((int)(endStep - startStep + 1));
    }
}
