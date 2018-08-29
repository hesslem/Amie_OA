package amie.embedding;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.logging.Logger;


import org.json.simple.*;
import org.json.simple.parser.JSONParser;

/**
 * Created by hovinhthinh on 11/13/17.
 */

// Support L1 norm only.
public class TransEClient extends EmbeddingClient {
    public static final Logger LOGGER = Logger.getLogger(TransEClient.class.getName());
    private String norm;
    private DoubleVector[] entitiesEmbedding, relationsEmbedding;

    public TransEClient(String workspace, String norm) {
        super(workspace);
        LOGGER.info("Loading embedding TransE client from '" + workspace + "' with norm " + norm + ".");
        this.norm = norm;
        /*if (!norm.equals("L1")) {
            throw new RuntimeException("Support L1 norm only.");
        }*/
        /*try {
            // Read embeddings.
            DataInputStream eIn = new DataInputStream(new FileInputStream(
                    new File(workspace+"/TransE.json")));
            System.out.println("Embedding eingelesen");
            eLength = 100;
            System.out.println("eLength: "+eLength);

            entitiesEmbedding = new DoubleVector[nEntities];
            for (int i = 0; i < nEntities; ++i) {
                entitiesEmbedding[i] = new DoubleVector(eLength);
                for (int j = 0; j < eLength; ++j) {
                    entitiesEmbedding[i].value[j] = eIn.readDouble();
                    if(i==0){
                        System.out.println(entitiesEmbedding[i].value[j]);
                    }
                }
            }



            System.out.println("Entities done");
            relationsEmbedding = new DoubleVector[nRelations];
            for (int i = 0; i < nRelations; ++i) {
                relationsEmbedding[i] = new DoubleVector(eLength);
                for (int j = 0; j < eLength; ++j) {
                    relationsEmbedding[i].value[j] = eIn.readDouble();
                }
            }
            System.out.println("Relations done");
            eIn.close();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        try{
            jsonObject = (JSONObject) parser.parse(new FileReader("/home/kalo/notebooks/OpenKE/hessler/transe.vec.json"));
            System.out.println("Embedding eingelesen");
            this.eLength = 200;
            System.out.println("eLength: "+eLength);
            JSONArray entityArray = (JSONArray) jsonObject.get("ent_embeddings");
            System.out.println("No entities: "+entityArray.size());
            entitiesEmbedding = new DoubleVector[nEntities];

            for (int i = 0; i < nEntities; ++i) {
                entitiesEmbedding[i] = new DoubleVector(eLength);
                JSONArray vector = (JSONArray) entityArray.get(i);
                for (int j = 0; j < eLength; ++j) {
                    entitiesEmbedding[i].value[j] = Double.parseDouble(vector.get(j).toString());
                }
            }
            System.out.println("Saved entities");

            JSONArray relationArray = (JSONArray) jsonObject.get("rel_embeddings");

            relationsEmbedding = new DoubleVector[nRelations];

            for (int i = 0; i < nRelations; ++i) {
                relationsEmbedding[i] = new DoubleVector(eLength);
                JSONArray vector = (JSONArray) relationArray.get(i);
                for (int j = 0; j < eLength; ++j) {
                    relationsEmbedding[i].value[j] = Double.parseDouble(vector.get(j).toString());
                }
            }
            System.out.println("Saved relations");


        } catch (Exception e){
            System.out.println("Error: "+e);
        }

    }


    @Override
    public double getScore(int subject, int predicate, int object) {
            double score = 0;

        if (this.norm.equals("L1")){
            for (int i = 0; i < eLength; ++i) {
                score += Math.abs(entitiesEmbedding[subject].value[i] + relationsEmbedding[predicate].value[i] -
                        entitiesEmbedding[object].value[i]);
            }
            return score/100;
        } else {

            for (int i = 0; i < eLength; ++i) {
                //System.out.println(entitiesEmbedding[subject].value[i]);
                score += Math.pow((entitiesEmbedding[subject].value[i] + relationsEmbedding[predicate].value[i] -
                        entitiesEmbedding[object].value[i]),2);
            }
            return Math.sqrt(score)/10;

        }

    }

}
