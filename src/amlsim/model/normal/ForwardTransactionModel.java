package amlsim.model.normal;

import amlsim.Account;
import amlsim.model.AbstractTransactionModel;

import amlsim.*;

import java.util.*;

/**
 * Send money received from an account to another account in a similar way
 */
public class ForwardTransactionModel extends AbstractTransactionModel {
    private int index = 0;

    public void setParameters(int interval, float balance, long start, long end){
        super.setParameters(interval, balance, start, end);
        if(this.startStep < 0){  // decentralize the first transaction step
            this.startStep = generateStartStep(interval);
        }
    }

    @Override
    public String getModelName() {
        return "Forward";
    }

    @Override
    public void makeTransaction(long step) {

        float amount = getTransactionAmount(maxTxAmount);  // this.balance;
        List<Account> dests = this.account.getBeneList();
        int numDests = dests.size();
        if(numDests == 0){
            return;
        }
        if((step - startStep) % interval != 0){
            return;
        }

        if(index >= numDests){
            index = 0;
        }
        Account dest = dests.get(index);
        this.makeTransaction(
            step, AMLSim.getSimProp().makeTransactionMoreRealistic(amount, (float) 1.0, roundAmountProbability), dest
        );
        index++;
    }
}
