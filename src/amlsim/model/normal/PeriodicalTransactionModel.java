package amlsim.model.normal;

import amlsim.AMLSim;
import amlsim.Account;
import amlsim.model.AbstractTransactionModel;

import java.util.Random;


/**
 * Send money to neighbors periodically
 */
public class PeriodicalTransactionModel extends AbstractTransactionModel {

    private static Random rand = AMLSim.getRandom();

    private int index = 0;

    public void setParameters(int interval, float balance, long start, long end){
        super.setParameters(interval, balance, start, end);
        if(this.startStep < 0){  // decentralize the first transaction step
            this.startStep = generateStartStep(interval);
        }
    }

    @Override
    public String getModelName() {
        return "Periodical";
    }

    private boolean isValidStep(long step){
        return (step - startStep) % interval == 0;
    }

    @Override
    public void makeTransaction(long step) {
        if(!isValidStep(step) || this.account.getBeneList().isEmpty()){
            return;
        }
        int numDests = this.account.getBeneList().size();
        if(index >= numDests){
            index = 0;
        }

        int totalCount = getNumberOfTransactions();  // Total number of transactions
        // int eachCount = (numDests < totalCount) ? 1 : numDests / totalCount;

        int eachCount = rand.nextInt(numDests) + 1;

        for(int i=0; i<eachCount; i++) {
            float amount = getTransactionAmount(maxTxAmount);  // this.balance;
            Account dest = this.account.getBeneList().get(index);
            this.makeTransaction(
                step, AMLSim.getSimProp().makeTransactionMoreRealistic(amount, (float) 1.0, roundAmountProbability), dest
            );
            index++;
            if(index >= numDests) break;
        }
        index = 0;
    }
}
