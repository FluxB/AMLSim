package amlsim;

import java.io.*;
import java.nio.file.*;
import org.json.*;


/**
 * Simulation properties and global parameters loaded from the configuration JSON file
 */
public class SimProperties {

    private static final String separator = File.separator;
    private JSONObject generalProp;
    private JSONObject simProp;
    private JSONObject inputProp;
    private JSONObject outputProp;
    private JSONObject cashInProp;
    private JSONObject cashOutProp;
    private String workDir;
    private float marginRatio;  // Ratio of margin for AML typology transactions
    private float scatterVariance;  // Scatter transactions shouldn't be exactly identical
    private float gatherVariance;  // Gather transactions shouldn't be exactly identical

    private float sarRoundAmountAlpha;  // the alpha value of the round amount BetaDistribution for SAR typologies
    private float sarRoundAmountBeta;  // the beta value of the round amount BetaDistribution for SAR typologies
    private float normalRoundAmountAlpha;  // the alpha value of the round amount BetaDistribution for normal actors
    private float normalRoundAmountBeta;  // the beta value of the round amount BetaDistribution for normal actors

    private float sarRoundAmountProbability;  // Suspicious transactions are more likely to be round
    private float normalRoundAmountProbability;  // Round amount probability of 'normal' transactions
    private int seed;  // Seed of randomness
    private String simName;  // Simulation name

    private int normalTxInterval;
//    private int sarTxInterval;
    private float minTxAmount;  // Minimum base (normal) transaction amount
    private float maxTxAmount;  // Maximum base (suspicious) transaction amount

    SimProperties(String jsonName) throws IOException{
        String jsonStr = loadTextFile(jsonName);
        JSONObject jsonObject = new JSONObject(jsonStr);
        JSONObject defaultProp = jsonObject.getJSONObject("default");

        generalProp = jsonObject.getJSONObject("general");
        simProp = jsonObject.getJSONObject("simulator");
        inputProp = jsonObject.getJSONObject("temporal");  // Input directory of this simulator is temporal directory
        outputProp = jsonObject.getJSONObject("output");

        normalTxInterval = simProp.getInt("transaction_interval");
        minTxAmount = defaultProp.getFloat("min_amount");
        maxTxAmount = defaultProp.getFloat("max_amount");

        System.out.printf("General transaction interval: %d\n", normalTxInterval);
        System.out.printf("Base transaction amount: Normal = %f, Suspicious= %f\n", minTxAmount, maxTxAmount);
        
        cashInProp = defaultProp.getJSONObject("cash_in");
        cashOutProp = defaultProp.getJSONObject("cash_out");
        marginRatio = defaultProp.getFloat("margin_ratio");
        scatterVariance = defaultProp.getFloat("scatter_variance");
        gatherVariance = defaultProp.getFloat("gather_variance");

        sarRoundAmountAlpha = defaultProp.getFloat("sar_round_amount_alpha");
        sarRoundAmountBeta = defaultProp.getFloat("sar_round_amount_beta");
        normalRoundAmountAlpha = defaultProp.getFloat("normal_round_amount_alpha");
        normalRoundAmountBeta = defaultProp.getFloat("normal_round_amount_beta");

        String envSeed = System.getenv("RANDOM_SEED");
        seed = envSeed != null ? Integer.parseInt(envSeed) : generalProp.getInt("random_seed");
        System.out.println("Random seed: " + seed);

        simName = System.getProperty("simulation_name");
        if(simName == null){
            simName = generalProp.getString("simulation_name");
        }
        System.out.println("Simulation name: " + simName);

        String simName = getSimName();
        workDir = inputProp.getString("directory") + separator + simName + separator;
        System.out.println("Working directory: " + workDir);
    }

    private static String loadTextFile(String jsonName) throws IOException{
        Path file = Paths.get(jsonName);
        byte[] bytes = Files.readAllBytes(file);
        return new String(bytes);
    }

    String getSimName(){
        return simName;
    }

    public int getSeed(){
        return seed;
    }

    public int getSteps(){
        return generalProp.getInt("total_steps");
    }

    boolean isComputeDiameter(){
        return simProp.getBoolean("compute_diameter");
    }

    int getTransactionLimit(){
        return simProp.getInt("transaction_limit");
    }

    int getNormalTransactionInterval(){
        return normalTxInterval;
    }

    public float getNormalBaseTxAmount(){
//        return minTxAmount;
        return minTxAmount + AMLSim.getRandom().nextFloat() * (maxTxAmount - minTxAmount);
    }

    public static float getRandom(float min, float max) {
        return min + AMLSim.getRandom().nextFloat() * (max - min);
    }

    public static float makeTransactionMoreRealistic(float amount, float variance, float roundAmountProbability) {
        // add a certain amount of variance
        float new_amount = amount * getRandom( (float) 1.0 - variance, (float) 1.0 + variance);

        // with a certain probability, make the transaction round
        if (getRandom(0, 1) < roundAmountProbability) {
            new_amount = (float) (Math.floor(new_amount / 100) * 100.0);
        }

        return new_amount;
    }

//    public float getSuspiciousTxAmount(){
//        return maxTxAmount;
//    }

//    int getSarTransactionInterval(){
//        return sarTxInterval;
//    }

//    float getSatBalanceRatio(){
//        return simProp.getFloat("sar_balance_ratio");
//    }

    public float getMarginRatio() {
        return marginRatio;
    }

    public float getScatterVariance() {
        return scatterVariance;
    }

    public float getGatherVariance() {
        return gatherVariance;
    }

    public float getSarRoundAmountAlpha() {
        return sarRoundAmountAlpha;
    }

    public float getSarRoundAmountBeta() {
        return sarRoundAmountBeta;
    }

    public float getNormalRoundAmountAlpha() {
        return normalRoundAmountAlpha;
    }

    public float getNormalRoundAmountBeta() {
        return normalRoundAmountBeta;
    }

    int getNumBranches(){
        return simProp.getInt("numBranches");
    }

    String getInputAcctFile(){
        return workDir + inputProp.getString("accounts");
    }

    String getInputTxFile(){
        return workDir + inputProp.getString("transactions");
    }

    String getInputAlertMemberFile(){
        return workDir + inputProp.getString("alert_members");
    }

    String getOutputTxLogFile(){
        return workDir + outputProp.getString("transaction_log");
    }

//    public String getOutputAlertMemberFile(){
//        return workDir + outputProp.getString("alert_members");
//    }

//    public String getOutputAlertTxFile(){
//        return workDir + outputProp.getString("alert_transactions");
//    }

    String getOutputDir(){
        return workDir;
    }

    String getCounterLogFile(){
        return workDir + outputProp.getString("counter_log");
    }

    String getDiameterLogFile(){
        return workDir + outputProp.getString("diameter_log");
    }

    int getCashTxInterval(boolean isCashIn, boolean isSAR){
        String key = isSAR ? "fraud_interval" : "normal_interval";
        return isCashIn ? cashInProp.getInt(key) : cashOutProp.getInt(key);
    }

    float getCashTxMinAmount(boolean isCashIn, boolean isSAR){
        String key = isSAR ? "fraud_min_amount" : "normal_min_amount";
        return isCashIn ? cashInProp.getFloat(key) : cashOutProp.getFloat(key);
    }

    float getCashTxMaxAmount(boolean isCashIn, boolean isSAR){
        String key = isSAR ? "fraud_max_amount" : "normal_max_amount";
        return isCashIn ? cashInProp.getFloat(key) : cashOutProp.getFloat(key);
    }
}


