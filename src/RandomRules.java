import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class RandomRules {



    public static void main(String[] args){

        try{
            ArrayList<String> ruleList = new ArrayList<>();
            ArrayList<String> resultList = new ArrayList<>();
            Random rand = new Random();


            BufferedReader resultRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/allRulesTransHL1Hybrid.txt"))));
            String line = "";

            ruleList.co


            while ((line = resultRead.readLine()) != null) {
                String[] rLine = line.split("\\s++");
                if (Double.parseDouble(rLine[12]) < (0.9)) {
                    continue;
                }

                ruleList.add(rLine[0]+" "+rLine[1]+" "+rLine[2]);
            }

            while(resultList.size()<100){
                resultList.add(ruleList.get(rand.nextInt(ruleList.size())));
            }

            for (String rule: resultList) {
                System.out.println(rule);

            }

        } catch (Exception e){
            System.out.println(e);
        }


    }
}
