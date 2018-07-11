package amie.mining.assistant;

import amie.data.KB;
import amie.mining.assistant.MiningAssistant;
//import amie.mining.assistant.MiningOperator;
import amie.rules.Rule;
//import javatools.database.Virtuoso;
import javatools.datatypes.ByteString;
import javatools.datatypes.IntHashMap;

import java.util.*;

import amie.embedding.TransEClient;

public class OldSchemaAttributeMiningAssistant extends MiningAssistant {

    ByteString concept;
    //static KB complete;
    //static Virtuoso virtuoso = new Virtuoso();
    static Object myLock = new Object();
    double classSize;

    boolean withEmbedding;
    TransEClient embedding;

    /*
    public SchemaAttributeMiningAssistant(KB dataSource, KB completeKB) {
        super(dataSource);
        //virtuoso = new Virtuoso();
        //bodyExcludedRelations = Arrays.asList(ByteString.of("<rdf:type>"));
        super.maxDepth = 8;
        super.setEnablePerfectRules(true);
        super.minPcaConfidence = 0.05;
        super.minStdConfidence = 0.001;
        //Class in Head Relationship that should be mined
        this.concept = ByteString.of("<http://dbpedia.org/ontology/City>");
        super.bodyExcludedRelations = Arrays.asList(ByteString.of("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"));
        //this.complete = completeKB;

    }
    */

    public OldSchemaAttributeMiningAssistant(KB dataSource, String type, boolean withEmbedding) {
        super(dataSource);
        //virtuoso = new Virtuoso();
        //bodyExcludedRelations = Arrays.asList(ByteString.of("<rdf:type>"));
        super.maxDepth = 2;
        super.setEnablePerfectRules(true);
        super.minPcaConfidence = 0.05;
        super.minStdConfidence = 0.001;
        this.withEmbedding = withEmbedding;
        //Class in Head Relationship that should be mined
        this.concept = ByteString.of(type);
        super.bodyExcludedRelations = Arrays.asList(ByteString.of("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"), ByteString.of("<http://purl.org/dc/terms/subject>"));
        ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of("?y"));
        this.classSize = kb.count(query);
        if (withEmbedding){
            this.embedding = new TransEClient("/home/","L1");
        }
        kb.setEntitiyList();


    }

    @Override
    public String getDescription() {
        return "Rules of the form r(x,y) r(x,z) => type(x, C) or r(x,c1) r(x,c2) => type(x, C)";
    }


    @Override
    public void getInitialAtomsFromSeeds(Collection<ByteString> relations,
                                                     double minSupportThreshold, Collection<Rule> output) {
        ByteString relation = relations.iterator().next();

        //Collection<Rule> output = new ArrayList<>();
        Rule emptyQuery = new Rule();
        ByteString[] newEdge = emptyQuery.fullyUnboundTriplePattern();
        emptyQuery.getTriples().add(newEdge);

        newEdge[1] = relation;
        int countVarPos = countAlwaysOnSubject ? 0 : findCountingVariable(newEdge);
        ByteString countingVariable = newEdge[countVarPos];
        long cardinality = kb.countDistinct(countingVariable, emptyQuery.getTriples());

        ByteString[] succedent = newEdge.clone();
        Rule candidate = new Rule(succedent, cardinality);
        candidate.setFunctionalVariablePosition(countVarPos);
        registerHeadRelation(candidate);

        ByteString[] danglingEdge = candidate.getTriples().get(0);
        IntHashMap<ByteString> constants = kb.frequentBindingsOf(danglingEdge[2], candidate.getTriples().get(0)[0], candidate.getTriples());
        //System.out.println(constants);
        for(ByteString constant : constants){


            Rule newCandidate = candidate.instantiateConstant(2, constant, cardinality);
            output.add(newCandidate);
            //System.out.println(newCandidate);

        }

        //System.out.println("got initial Atoms");

    }


    //@MiningOperator(name = "dangling")
    public void getDanglingAtoms(Rule rule, double minSupportThreshold, Collection<Rule> output) {
        //System.out.println("Getting dangling atoms");
        ByteString[] newEdge = rule.fullyUnboundTriplePattern();

        List<ByteString> joinVariables = null;


        //Bind all Atoms to central entity variable
        joinVariables = rule.getVariables();
        ByteString joinVariable = joinVariables.get(0);

        int nPatterns = rule.getTriples().size();
        ByteString originalRelationVariable = newEdge[1];

        for (int joinPosition = 0; joinPosition <= 2; joinPosition += 2) {
            ByteString originalFreshVariable = newEdge[joinPosition];


            //System.out.println("JOIN VARIABLES " + joinVariable);
            newEdge[joinPosition] = joinVariable;
            //newEdge[1] = ByteString.of("<testRelation>");
            rule.getTriples().add(newEdge);
            //System.out.println("JOIN VARIABLE " + joinVariable);
            //System.out.println("NEWEDGE " + newEdge[1]);
            //System.out.println("Functional Variable " + rule.getFunctionalVariable());

            //System.out.println("CurrentRule " + rule.toString());

            IntHashMap<ByteString> promisingRelations = kb.frequentBindingsOf(newEdge[1],
                    rule.getTriples().get(0)[0], rule.getTriples());
            //System.out.println("HASHMAPSIZE " + promisingRelations.size());
            //System.out.println(promisingRelations);
            rule.getTriples().remove(nPatterns);

            int danglingPosition = (joinPosition == 0 ? 2 : 0);

            //System.out.println("Promising relations: "+promisingRelations);

            //System.out.println("Number of Promising Relations " + promisingRelations.size());
            for (ByteString relation : promisingRelations) {
                if (this.bodyExcludedRelations != null &&
                        this.bodyExcludedRelations.contains(relation))
                    continue;
                //Here we still have to make a redundancy check
                int cardinality = promisingRelations.get(relation);
                //System.out.println("Cardinality: "+cardinality + "minSupportThreshold: "+minSupportThreshold);

                //check weather the cardinality of the joined relationship is above the minimum Threshold
                //Problems: Isnt the Support between 0 and 1, and the cardinality always above 1?
                if (cardinality >= minSupportThreshold) {
                    if (!rule.containsRelation(relation)) {
                        newEdge[1] = relation;
                        Rule candidate = rule.addAtom(newEdge, cardinality);
                        //System.out.println("New candidate rule: " + candidate);
                        /*if (candidate.containsUnifiablePatterns()) {
                            //Verify whether dangling variable unifies to a single value (I do not like this hack)
                            if (kb.countDistinct(newEdge[danglingPosition], candidate.getTriples()) < 2)
                                continue;
                        }*/

                        /*candidate.setHeadCoverage((double) candidate.getSupport()
                                / headCardinalities.get(candidate.getHeadRelation()));
                        candidate.setSupportRatio((double) candidate.getSupport()
                                / (double) getTotalCount(candidate));*/
                        candidate.setParent(rule);
                        //candidate.setClassConfidence(getClassConfidence(candidate));
                        //candidate.setFrequency(cardinality/this.classSize);

                        //Here we add the generated candidate to the output collection
                        output.add(candidate);
                        //System.out.println("Output: "+output);
                        //System.out.println("output Dangling Atom");
                    }
                }
                //}

                newEdge[1] = originalRelationVariable;
            }
            newEdge[joinPosition] = originalFreshVariable;

        }
    }

    @Override
    public boolean testConfidenceThresholds(Rule candidate) {
        //System.out.println("Testing Confidence Thresholds");
        if(this.withEmbedding){
            return (getOAScore(candidate) > 0);
        }
        getOAScore(candidate);
        if(candidate.getClassConfidence() > 0.05){
            return true;
        } else {
            return false;
        }


        /*if (getFrequency(candidate) > 0.01) {
            System.out.println("Frequency tested");
            if (getClassConfidence(candidate) > 0.01) {
                return true;
            }
            else{
                return true;
            }

        } else {
            return true;
        }*/
    }

    @Override
    public void getClosingAtoms(Rule query, double minSupportThreshold, Collection<Rule> output) {
        return;
    }

    @Override
    public void getInstantiatedAtoms(Rule rule, double minSupportThreshold,
                                     Collection<Rule> danglingEdges, Collection<Rule> output) {
        return;

//        List<ByteString> queryFreshVariables = rule.getOpenVariables();
//        if (queryFreshVariables.size() < 2) {
//            for (Rule candidate : danglingEdges) {
//
//                // Find the dangling position of the query
//                int lastTriplePatternIndex = candidate.getLastRealTriplePatternIndex();
//                ByteString[] lastTriplePattern = candidate.getTriples().get(lastTriplePatternIndex);
//
//                List<ByteString> candidateFreshVariables = candidate.getOpenVariables();
//                int danglingPosition = 0;
//                if (candidateFreshVariables.contains(lastTriplePattern[0])) {
//                    danglingPosition = 0;
//                } else if (candidateFreshVariables.contains(lastTriplePattern[2])) {
//                    danglingPosition = 2;
//                } else {
//                    throw new IllegalArgumentException("The query " + rule.getRuleString() +
//                            " does not contain fresh variables in the last triple pattern.");
//                }
//                getInstantiatedAtoms(candidate, candidate,
//                        lastTriplePatternIndex, danglingPosition,
//                        minSupportThreshold, output);
//            }
//        }
    }

    @Override
    protected void getInstantiatedAtoms(Rule queryWithDanglingEdge, Rule parentQuery,
                                        int danglingAtomPosition, int danglingPositionInEdge, double minSupportThreshold, Collection<Rule> output) {

        ByteString[] danglingEdge = queryWithDanglingEdge.getTriples().get(danglingAtomPosition);
        IntHashMap<ByteString> constants = kb.frequentBindingsOf(danglingEdge[danglingPositionInEdge],
                queryWithDanglingEdge.getFunctionalVariable(), queryWithDanglingEdge.getTriples());
        for (ByteString constant: constants){
            int cardinality = constants.get(constant);
            if(cardinality >= minSupportThreshold){
                //System.out.println("Cardinality " + cardinality);
                //System.out.println("minsup" +  minSupportThreshold);
                ByteString[] lastPatternCopy = queryWithDanglingEdge.getLastTriplePattern().clone();
                lastPatternCopy[danglingPositionInEdge] = constant;
                Rule candidate = queryWithDanglingEdge.instantiateConstant(danglingPositionInEdge,
                        constant, cardinality);

                if(candidate.getRedundantAtoms().isEmpty()){
                    candidate.setHeadCoverage((double)cardinality / headCardinalities.get(candidate.getHeadRelation()));
                    candidate.setSupportRatio((double)cardinality / (double)getTotalCount(candidate));
                    //candidate.setClassConfidence(this.getClassConfidence(candidate));

                    //candidate.setFrequency(cardinality/(double) this.classSize);
                    candidate.setParent(parentQuery);
                    output.add(candidate);
                    //System.out.println("output Instantiated Atom");
                }
            }
        }
    }



    public double getFrequency(Rule r){
        double frequency = 0.0;
        ByteString[] head = r.getHead();
        ByteString countVariable = null;
        countVariable = head[0];
        long support = kb.countDistinct(countVariable, r.getTriples());

        frequency = support/(double) this.classSize;
        return frequency;
    }



    /**
     * A method that computes the specificity of a rule regarding the input class.
     *
     * @param r
     * @return
     */

    public void setClassConfidence(Rule r){

        ByteString[] head = r.getHead();
        ByteString countVariable = null;
        countVariable = head[0];
        long support = kb.countDistinct(countVariable, r.getTriples());
        //System.out.println("Class Support: "+ support);

        ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of("?y"));
        double classSize = kb.count(query);
        //System.out.println("Class size: " + classSize);

        //System.out.println("Class confidence: " + (support/classSize));
        r.setClassConfidence((support/classSize));


        /*double classConfidence = 0.0;

        ByteString[] head = r.getHead();
        ByteString countVariable = null;
        countVariable = head[0];
        //r.setClassConfidence(this.getClassConfidence(r));
        long support = kb.countDistinct(countVariable, r.getTriples());
        //String query = r.bodyToSparql();
        //System.out.println(query);
        List<ByteString[]> headList = null;
        headList.add(head);
        long supportComplete = kb.countDistinct(countVariable, headList);
        System.out.println("SUPPORT " + support);
        System.out.println("ALL " + supportComplete);


        classConfidence = support/(double) supportComplete;
        System.out.println("CONFIDENCE " + classConfidence);
        return classConfidence;
        */
    }

    @Override
    public void calculateConfidenceMetrics(Rule r){
        setClassConfidence(r);
    }

    public double getOAScore(Rule r){

        List<ByteString[]> body = r.getBody();
        ByteString[] head = r.getHead();
        //List<ByteString[]> headList = new ArrayList<>();
        //System.out.println(fact.get(0)[0]);
        //System.out.println(head[0]);

        int relId = getId(body.get(0)[1]);

        for (int varPos = 0; varPos <= 2; varPos += 2){

            if (!(KB.isVariable(body.get(0)[varPos])) && !(head[0].equals(body.get(0)[varPos]))){
                continue;
            }

            IntHashMap<ByteString> subjects = kb.resultsOneVariable(head);

            double totalSize = subjects.size();

            double scoreSum = 0.0;



            for (ByteString subject: subjects) {
                int subjId = getId(subject);

                double maxScore = 0.0;
                double currentScore;
 
                for (ByteString object: kb.getAllEntities()){

                    int objId = getId(object);

                    if (varPos == 0){
                        currentScore = embedding.getScore(subjId, relId, objId);
                        if (maxScore < currentScore){
                            maxScore = currentScore;
                        }
                    } else {
                        currentScore = embedding.getScore(objId, relId, subjId);
                        if (maxScore < currentScore){
                            maxScore = currentScore;
                        }
                    }
                }
                scoreSum = scoreSum + maxScore;

            }

            System.out.println(subjects);


            return (scoreSum/totalSize);

        }




        //double embeddingScore = embedding.getScore(0,0,0);


        return 0.0;
    }

    public int getId(ByteString entity){
        //datei mit entity und id
        return 0;
    }
}
