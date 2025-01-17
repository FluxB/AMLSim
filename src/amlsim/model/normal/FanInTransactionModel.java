package amlsim.model.normal;

import amlsim.*;
import amlsim.model.AbstractTransactionModel;
import java.util.*;

/**
 * Receive money from one of the senders (fan-in)
 */
public class FanInTransactionModel extends AbstractTransactionModel {

    private int index = 0;

    public void setParameters(int interval, float balance, long start, long end){
        super.setParameters(interval, balance, start, end);
        if(this.startStep < 0){  // decentralize the first transaction step
            this.startStep = generateStartStep(interval);
        }
    }


    @Override
    public String getModelName() {
        return "FanIn";
    }

    private boolean isValidStep(long step){
        return (step - startStep) % interval == 0;
    }

    @Override
    public void makeTransaction(long step) {
        List<Account> origs = this.account.getOrigList();  // Sender accounts
        int numOrigs = origs.size();
        if(!isValidStep(step) || numOrigs == 0){
            return;
        }
        if(index >= numOrigs){
            index = 0;
        }

        Account orig = origs.get(index);
        float amount = orig.getModel().getTransactionAmount(maxTxAmount);
        makeTransaction(step, AMLSim.getSimProp().makeTransactionMoreRealistic(amount, (float) 1.0, roundAmountProbability),
                        orig, this.account);
        index++;
    }
}
