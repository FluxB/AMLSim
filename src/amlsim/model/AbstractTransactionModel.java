package amlsim.model;

import amlsim.Account;
import amlsim.AMLSim;

import org.apache.commons.math3.distribution.BetaDistribution;


/**
 * Base class of transaction models
 */
public abstract class AbstractTransactionModel {

    // Transaction model ID
    public static final int SINGLE = 0;  // Make a single transaction to each neighbor account
    public static final int FAN_OUT = 1;  // Send money to all neighbor accounts
    public static final int FAN_IN = 2;  // Receive money from neighbor accounts
    public static final int MUTUAL = 3;
    public static final int FORWARD = 4;
    public static final int PERIODICAL = 5;

//    protected static Random rand = new Random(AMLSim.getSeed());

    protected Account account;  // Account object
    protected int interval = 1; // Default transaction interval
    protected float balance;  // Current balance
    protected long startStep = -1;  // The first step of transactions
    protected long endStep = -1;  // The end step of transactions
    protected boolean isSAR = false;

    protected float roundAmountProbability;

    final private static float roundAmountAlpha = AMLSim.getSimProp().getNormalRoundAmountAlpha();
    final private static float roundAmountBeta = AMLSim.getSimProp().getNormalRoundAmountBeta();

    protected float maxTxAmount;

    public AbstractTransactionModel() {
        // a beta distribution is used to model the round amount affinity of the actor
        BetaDistribution betaDistribution = new BetaDistribution(roundAmountAlpha, roundAmountBeta);
        roundAmountProbability = (float) betaDistribution.inverseCumulativeProbability(AMLSim.getRandom().nextDouble());

        maxTxAmount = AMLSim.getSimProp().getMaxTxAmount() +
                       AMLSim.getSimProp().getMaxTxAmountRange() * AMLSim.getRandom().nextFloat();
    }

    /**
     * Get the assumed number of transactions in this simulation
     * @return Number of total transactions
     */
    public int getNumberOfTransactions(){
        return (int)AMLSim.getNumOfSteps() / interval;
    }

    /**
     * Generate the assumed amount of a normal transaction
     * @return Normal transaction amount
     */
    public float getTransactionAmount(float maxTxAmount){
        // Each transaction amount should be independent of the current balance
        return AMLSim.getSimProp().getNormalBaseTxAmount(maxTxAmount);
    }
    
    /**
     * Set an account object which has this model
     * @param account Account object
     */
    public void setAccount(Account account){
        this.account = account;
        this.isSAR = account.isSAR();
    }

    /**
     * Get the simulation step range as the period when this model is valid
     * If "startStep" and/or "endStep" is undefined (negative), it returns the largest range
     * @return The total number of simulation steps
     */
    public int getStepRange(){
        long st = startStep >= 0 ? startStep : 0;
        long ed = endStep > 0 ? endStep : AMLSim.getNumOfSteps();
        return (int)(ed - st + 1);
    }

    /**
     * Get transaction model name
     * @return Transaction model name
     */
    public abstract String getModelName();

    /**
     * Make a transaction
     * @param step Current simulation step
     */
    public abstract void makeTransaction(long step);

    /**
     * Generate the start transaction step (to decentralize transaction distribution)
     * @param range Simulation step range
     * @return random int value [0, range-1]
     */
    protected static int generateStartStep(int range){
//        return rand.nextInt(range);
        return AMLSim.getRandom().nextInt(range);
    }

    /**
     * Set initial parameters
     * This method will be called when the account is initialized
     * @param interval Transaction interval
     * @param balance Initial balance of the account
     * @param start Start simulation step (It never makes any transactions before this step)
     * @param end End simulation step (It never makes any transactions after this step)
     */
    public void setParameters(int interval, float balance, long start, long end){
        this.interval = interval;
        setParameters(balance, start, end);
    }
    
    /**
     * Set initial parameters of the transaction model (for AML typology models)
     * @param balance Initial balance of the account
     * @param start Start simulation step
     * @param end End simulation step
     */
    public void setParameters(float balance, long start, long end){
        this.balance = balance;
        this.startStep = start;
        this.endStep = end;
    }

    /**
     * Generate and register a transaction (for alert transactions)
     * @param step Current simulation step
     * @param amount Transaction amount
     * @param orig Origin account
     * @param dest Destination account
     * @param isSAR Whether this transaction is SAR
     * @param alertID Alert ID
     */
    protected void makeTransaction(long step, float amount, Account orig, Account dest, boolean isSAR, long alertID){
        if(amount <= 0){  // Invalid transaction amount
            AMLSim.getLogger().warning("Warning: invalid transaction amount: " + amount);
            return;
        }
        String ttype = orig.getTxType(dest);
        if(isSAR) {
            AMLSim.getLogger().fine("Handle transaction: " + orig.getID() + " -> " + dest.getID());
        }
        AMLSim.handleTransaction(step, ttype, amount, orig, dest, isSAR, alertID);
    }

    /**
     * Generate and register a transaction (for cash transactions)
     * @param step Current simulation step
     * @param amount Transaction amount
     * @param orig Origin account
     * @param dest Destination account
     * @param ttype Transaction type
     */
    protected void makeTransaction(long step, float amount, Account orig, Account dest, String ttype){
        AMLSim.handleTransaction(step, ttype, amount, orig, dest, false, -1);
    }

    /**
     * Generate and register a transaction (for normal transactions)
     * @param step Current simulation step
     * @param amount Transaction amount
     * @param orig Origin account
     * @param dest Destination account
     */
    protected void makeTransaction(long step, float amount, Account orig, Account dest){
        makeTransaction(step, amount, orig, dest, false, -1);
    }

    /**
     * Generate and register a transaction (for normal transactions)
     * @param step Current simulation step
     * @param amount Transaction amount
     * @param dest Destination account
     */
    protected void makeTransaction(long step, float amount, Account dest){
        this.makeTransaction(step, amount, this.account, dest);
    }

}
