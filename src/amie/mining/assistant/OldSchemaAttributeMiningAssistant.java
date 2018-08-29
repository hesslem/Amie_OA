package amie.mining.assistant;

import amie.data.KB;
import amie.embedding.EmbeddingClient;
import amie.embedding.HolEClient;
import amie.mining.assistant.MiningAssistant;
//import amie.mining.assistant.MiningOperator;
import amie.rules.Rule;
//import javatools.database.Virtuoso;
import javatools.datatypes.ByteString;
import javatools.datatypes.IntHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import amie.embedding.TransEClient;

import java.util.logging.Logger;

public class OldSchemaAttributeMiningAssistant extends MiningAssistant {

    ByteString concept;
    //static KB complete;
    //static Virtuoso virtuoso = new Virtuoso();
    static Object myLock = new Object();
    double classSize;
    double countThreshold;
    double hybidParameter;
    boolean withEmbedding, hybridFunction;
    public HashMap<ByteString, Set<ByteString>> benchmark = new HashMap<>();
    public HashMap<ByteString, Set<ByteString>> result = new HashMap<>();

    public static final Logger LOGGER = Logger.getLogger(OldSchemaAttributeMiningAssistant.class.getName());

    EmbeddingClient embedding;


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

    public OldSchemaAttributeMiningAssistant(KB dataSource, String type, boolean withEmbedding, boolean hybridFunction, double countThreshold, double hybridParameter) {
        super(dataSource);
        //virtuoso = new Virtuoso();
        //bodyExcludedRelations = Arrays.asList(ByteString.of("<rdf:type>"));
        super.maxDepth = 2;
        super.setEnablePerfectRules(true);
        super.minPcaConfidence = 0.05;
        super.minStdConfidence = 0.001;
        this.countThreshold = countThreshold;
        this.countAlwaysOnSubject = true;
        this.withEmbedding = withEmbedding;
        this.hybridFunction = hybridFunction;
        this.hybidParameter = hybridParameter;
        //Class in Head Relationship that should be mined
        this.concept = ByteString.of(type);
        super.bodyExcludedRelations = Arrays.asList(ByteString.of("<http://www.wikidata.org/prop/direct/P106>"));
        ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of("?y"));
        this.classSize = kb.count(query);
        System.out.println("Class Size: "+this.classSize);
        if (withEmbedding){
            this.embedding = new TransEClient("/home/kalo/notebooks/OpenKE/hessler", "L2");
            double score = 0.0;
            score += embedding.getScore(0,0,1);
            score += embedding.getScore(2,1,3);
            score += embedding.getScore(31696,46,101);
            score += embedding.getScore(1592628,12,2066);
            score += embedding.getScore(2952464,69,423054);
            double averageScore = score/5;
            System.out.println("Average Score"+averageScore);

            int subj = embedding.getEntityId(ByteString.of("<http://www.wikidata.org/entity/Q23>"));
            int obj = embedding.getEntityId(ByteString.of("<http://www.wikidata.org/entity/Q494413>"));
            int pred = embedding.getRelationId(ByteString.of("<http://www.wikidata.org/prop/direct/P19>"));
            int rand = embedding.getEntityId(ByteString.of("<http://www.wikidata.org/entity/Q82955>"));
            System.out.println("IDs: "+subj+","+pred+","+obj);

            System.out.println("Place of Birth random: "+embedding.getScore(subj,pred,rand));
            System.out.println("Place of Birth true: "+embedding.getScore(subj,pred,obj));

            //System.out.println("ID of wdt:Q1351858: "+embedding.getEntityId(ByteString.of("wdt:Q1351858")));
            //System.out.println("ID of wdt:P20: "+embedding.getRelationId(ByteString.of("wdt:P20")));
            LOGGER.info("Finished testing");

        }

        kb.setEntitiyList();
        kb.setClassList();
        //evaluate();



    }

    @Override
    public String getDescription() {
        return "Rules of the form r(x,y) r(x,z) => type(x, C) or r(x,c1) r(x,c2) => type(x, C)\nwith type: "+this.concept.toString();
    }


    @Override
    public void getInitialAtomsFromSeeds(Collection<ByteString> relations,
                                                     double minSupportThreshold, Collection<Rule> output) {
        ByteString relation = relations.iterator().next();
        //System.out.println("Initializing with relation: "+relation.toString());

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
        //System.out.println("Output: "+output);

        //System.out.println("got initial Atoms");

    }


    //@MiningOperator(name = "dangling")
    public void getDanglingAtoms(Rule rule, double minSupportThreshold, Collection<Rule> output) {
        //System.out.println("Getting dangling atoms");
        minSupportThreshold = 100;
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



            //System.out.println("Promising relations for class "+rule.getHead()[1].toString()+": "+promisingRelations);
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
        /*if(this.withEmbedding){
            return (getOAScore(candidate) > 0);
        }*/
        //System.out.println("Testing rule: "+candidate);
        /*if (kb.countDistinct(candidate.getHead()[0], candidate.getTriples()) < 100){
            System.out.println("support of "+candidate+" not big enough");
            return false;
        }*/
        if (this.withEmbedding == false) {
            //System.out.println("Testing");
            ByteString mainClass = candidate.getHead()[2];
            //System.out.println("Current Rule: "+candidate);
            int count = 0;

            long support = kb.countDistinct(candidate.getHead()[0], candidate.getTriples());

            if (support < countThreshold){
                return false;
            }
            //System.out.println(kb.getAllClasses());
            for (ByteString otherClass : kb.getAllClasses()) {

                if (mainClass.equals(otherClass)){
                    continue;
                }



                Set<ByteString> difference = getClassDiff(mainClass, otherClass);
                //System.out.println("Difference of "+mainClass.toString()+" and "+otherClass.toString()+": "+difference);
                Set<ByteString> intersection = getClassIntersect(mainClass, otherClass);

                Set<ByteString> differenceInverse = getClassDiff(otherClass, mainClass);
                Set<ByteString> intersectionInverse = getClassDiff(otherClass, mainClass);

                Set<ByteString> allEntities = kb.selectDistinct(candidate.getHead()[0], candidate.getTriples());


                Rule emptyRule = new Rule();
                ByteString[] newEdge = emptyRule.fullyUnboundTriplePattern();
                emptyRule.getTriples().add(newEdge);
                newEdge[1] = candidate.getHead()[1];
                newEdge[2] = otherClass;
                long cardinality = kb.countDistinct(newEdge[0], emptyRule.getTriples());
                Rule otherRule = new Rule(newEdge, cardinality);
                ByteString[] newBody = candidate.getBody().get(0);
                Rule otherClassRule = otherRule.addAtom(newBody, cardinality);
                //System.out.println("other Rule: "+otherClassRule);
                Set<ByteString> allEntitiesOtherClass = kb.selectDistinct(otherClassRule.getHead()[0], otherClassRule.getTriples());

                //System.out.println("Diff Size: "+difference.size()+"\tIntersect Size: "+intersection.size());

                if (getConfidence(candidate)*intersection.size() < 1 || getAttributeIntersectCount(intersection, candidate) < 1){
                    continue;
                }

                if (difference.size() <= 0||intersection.size() <= 0||differenceInverse.size() <= 0||intersectionInverse.size() <= 0) {
                    count++;
                    continue;
                }

                //System.out.println("Current Rule: "+candidate+"\tRatio: "+Math.abs(Math.log(getConfidenceRatio(candidate, difference, intersection, allEntities)))+"\tLog: "+Math.log(2));
                if (Math.abs(Math.log(getConfidenceRatio(candidate, difference, intersection, allEntities))) > Math.log(2)){
                    //System.out.println("Density too big: "+candidate);
                    return false;
                }

                if (Math.abs(Math.log(getConfidenceRatio(candidate, differenceInverse, intersectionInverse, allEntitiesOtherClass))) > Math.log(2)){
                    return false;
                }





            }
            //System.out.println("Count of "+candidate+" : "+count);
            if (count == kb.getAllClasses().size()){
                if (getConfidence(candidate) > 0.9) {
                    return true;
                } else {
                    return false;
                }

            }
            return true;

        } else if (this.hybridFunction) {
            double confidence = getConfidence(candidate);
            double oaScore = getOAScoreHybrid(candidate);
            double hybridScore = 0.0;
            for (double i = 0; i <= 10; i++){
                hybridScore = 0.0;
                //System.out.println("Calculating with "+i/10);
                hybridScore = ((i / 10) * confidence) + ((1 - (i / 10)) * oaScore);
                candidate.setHybridScore(hybridScore, (int) i);
            }

            if (true){
                return true;
            } else {
                return false;
            }


        } else {

            if (getOAScore(candidate) <= 50) {
                return true;
            } else {
                return false;
            }
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

    public double getConfidence(Rule r){
        ByteString[] head = r.getHead();
        ByteString countVariable = head[0];


        long support = kb.countDistinct(countVariable, r.getTriples());
        //System.out.println("Class Support: "+ support);

        Set<ByteString> kbClass = kb.resultsOneVariable(r.getHead());
        double classSize = kbClass.size();
        //ByteString[] query = KB.triple(ByteString.of("?x"), concept, r.getHead()[2]);
        //double classSize = kb.count(query);
        //System.out.println("Class size: " + classSize);

        //System.out.println("Class confidence: " + (support/classSize));
        r.setClassConfidence(support/classSize);
        return support/classSize;
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
        ByteString countVariable = head[0];






        long support = kb.countDistinct(countVariable, r.getTriples());
        //System.out.println("Class Support: "+ support);

        Set<ByteString> kbClass = kb.resultsOneVariable(r.getHead());
        double classSize = kbClass.size();

        //ByteString[] query = KB.triple(ByteString.of("?x"), concept, r.getHead()[2]);
        //double classSize = kb.count(query);
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

    public double getConfidenceRatio(Rule r, Set<ByteString> difference, Set<ByteString> intersection, Set<ByteString> allEntities){


        double diffCount = 0.0;

        for (ByteString entity : allEntities){
            if (difference.contains(entity)){
                diffCount++;
            }
        }
        //System.out.println("difference: "+difference);
        //System.out.println("intersection: "+intersection);
        //System.out.println("allEntities: "+allEntities);
        //System.out.println("diffCount: "+diffCount);

        double diffConf = diffCount/difference.size();


        double intersectCount = 0.0;
        for (ByteString entity : allEntities){
            if (intersection.contains(entity)){
                intersectCount++;
            }
        }
        //System.out.println("intersectCount: "+intersectCount);
        double intersectConf = intersectCount/intersection.size();

        return diffConf/intersectConf;

    }

    @Override
    public void calculateConfidenceMetrics(Rule r){
        setClassConfidence(r);
    }

    public double getOAScore(Rule r){
        //System.out.println(("Checking rule: "+r.toString()));
        List<ByteString[]> body = r.getBody();
        ByteString[] head = r.getHead();
        //List<ByteString[]> headList = new ArrayList<>();
        //System.out.println(fact.get(0)[0]);
        //System.out.println(head[0]);

        //int relId = Integer.parseInt(body.get(0)[1].toString());
        int relId = embedding.getRelationId(body.get(0)[1]);

        if (relId < 0){
            return 999999;
        }

        IntHashMap<ByteString> subjects = kb.resultsOneVariable(head);

        double totalSize = subjects.size();

        double scoreSum = 0.0;

        double oaScore = 0.0;

        int counter = 0;

        for (int varPos = 0; varPos <= 2; varPos += 2){

            if (!(head[0].equals(body.get(0)[varPos]))){
                continue;
            }


            for (ByteString subject: subjects) {
                //int subjId = Integer.parseInt(subject.toString());
                int subjId = embedding.getEntityId(subject);

                if (subjId < 0){
                    continue;
                }

                double maxScore = 0.0;
                double currentScore;
 
                //for (ByteString object: kb.getAllEntities()){
                for (int i = 0; i <= 1000; i++){

                    //int objId = Integer.parseInt(object.toString());
                    //int objId = embedding.getEntityId(object);
                    int objId = ThreadLocalRandom.current().nextInt(0, 2952465);
                    if (objId < 0 || objId > 2952465){
                        continue;
                    }
                    if (varPos == 0){
                        //System.out.println("subject: "+subject.toString()+"\trelation: "+body.get(0)[1].toString()+"\tobject: "+object.toString()+"\nsubj: "+subjId+"\trel: "+relId+"\tobj: "+objId);
                        currentScore = embedding.getScore(subjId, relId, objId);

                        if (i == 0){
                            maxScore = currentScore;
                            continue;
                        }
                        if (maxScore > currentScore){
                            maxScore = currentScore;
                        }
                    } else {

                        currentScore = embedding.getScore(objId, relId, subjId);
                        if (i == 0){
                            maxScore = currentScore;
                            continue;
                        }
                        if (maxScore > currentScore){
                            maxScore = currentScore;
                        }
                    }
                }
                scoreSum = scoreSum + maxScore;

            }

            //System.out.println(subjects);

            //System.out.println("Rule "+r.toString()+" with scoreSum "+scoreSum+" and totalSize "+totalSize);
            oaScore = (scoreSum/totalSize);
            /*
            if (oaScore < 30 && counter <= 5){
                scoreSum = 0.0;
                counter++;
                for (ByteString subject: subjects) {
                    //int subjId = Integer.parseInt(subject.toString());
                    int subjId = embedding.getEntityId(subject);

                    if (subjId < 0){
                        continue;
                    }

                    double maxScore = 0.0;
                    double currentScore;

                    boolean first = true;
                    for (ByteString object: kb.getAllEntities()){


                        //int objId = Integer.parseInt(object.toString());
                        int objId = embedding.getEntityId(object);

                        if (varPos == 0){
                            //System.out.println("subject: "+subject.toString()+"\trelation: "+body.get(0)[1].toString()+"\tobject: "+object.toString()+"\nsubj: "+subjId+"\trel: "+relId+"\tobj: "+objId);
                            currentScore = embedding.getScore(subjId, relId, objId);
                            if (first){
                                maxScore = currentScore;
                                first = false;
                                continue;
                            }
                            if (maxScore > currentScore){
                                maxScore = currentScore;
                            }
                        } else {
                            currentScore = embedding.getScore(objId, relId, subjId);
                            if (first){
                                maxScore = currentScore;
                                first = false;
                                continue;
                            }
                            if (maxScore > currentScore){
                                maxScore = currentScore;
                            }
                        }
                    }
                    scoreSum = scoreSum + maxScore;

                }

                double scoreWithAllEntities = scoreSum/totalSize;

                System.out.println("Rule: "+r.toString()+"with OAScore: "+oaScore+"and ScoreWAE: "+scoreWithAllEntities);


            }*/

        }
        //System.out.println("Checked Rule: "+r.toString()+"\tOAScore: "+oaScore+"\tTotal Size: "+totalSize+"\tScore Sum: "+scoreSum);
        r.setOAScore(oaScore);
        return oaScore;
    }

    public Set<ByteString> getClassDiff(ByteString mainClass, ByteString otherClass){

        List<ByteString[]> queryMainClass = new ArrayList<>();
        ByteString[] queryMain = KB.triple(ByteString.of("?x"), concept, mainClass);
        queryMainClass.add(queryMain);
        Set<ByteString> entitiesMain = kb.resultsOneVariable(queryMain);
        Set<ByteString> resultingSet = new HashSet<>(entitiesMain);

        List<ByteString[]> queryOtherClass = new ArrayList<>();
        ByteString[] queryOther = KB.triple(ByteString.of("?y"), concept, otherClass);
        queryOtherClass.add(queryMain);
        Set<ByteString> entitiesOther = kb.resultsOneVariable(queryOther);



        for (ByteString entity : entitiesMain){
            if (entitiesOther.contains(entity)){
                resultingSet.remove(entity);
            }
        }


        return resultingSet;
    }

    public double getClassDiffCount(ByteString mainClass, ByteString otherClass){return getClassDiff(mainClass ,otherClass).size();}

    public Set<ByteString> getClassIntersect(ByteString mainClass, ByteString otherClass){

        List<ByteString[]> queryMainClass = new ArrayList<>();
        ByteString[] queryMain = KB.triple(ByteString.of("?x"), concept, mainClass);
        queryMainClass.add(queryMain);
        Set<ByteString> entitiesMain = kb.resultsOneVariable(queryMain);
        Set<ByteString> resultingSet = new HashSet<>();


        List<ByteString[]> queryOtherClass = new ArrayList<>();
        ByteString[] queryOther = KB.triple(ByteString.of("?y"), concept, otherClass);
        queryOtherClass.add(queryMain);
        Set<ByteString> entitiesOther = kb.resultsOneVariable(queryOther);


        for (ByteString entity : entitiesMain){
            if (entitiesOther.contains(entity)){
                resultingSet.add(entity);
            }
        }


        return resultingSet;
    }

    public double getClassIntersectCount(ByteString mainClass, ByteString otherClass){return getClassIntersect(mainClass ,otherClass).size();}

    public Set<ByteString> getAttributeIntersect(Set<ByteString> classIntersection, Rule r){

        Set<ByteString> resultingSet = new HashSet<>();

        if (r.getTriples().get(0)[0].equals(r.getTriples().get(1)[0])){

            Set<ByteString> entities = kb.frequentBindingsOf(r.getTriples().get(1)[0],r.getTriples().get(1)[2],r.getBody());

            for (ByteString entity : classIntersection){
                if (entities.contains(entity)){
                    resultingSet.add(entity);
                }
            }

            return resultingSet;

        } else {

            Set<ByteString> entities = kb.frequentBindingsOf(r.getTriples().get(1)[2],r.getTriples().get(1)[0],r.getBody());

            for (ByteString entity : classIntersection){
                if (entities.contains(entity)){
                    resultingSet.add(entity);
                }
            }

            return resultingSet;

        }

    }

    public double getAttributeIntersectCount(Set<ByteString> classIntersection, Rule r){return getAttributeIntersect(classIntersection, r).size();}


    public double getOAScoreHybrid(Rule r){

        List<ByteString[]> body = r.getBody();
        ByteString[] head = r.getHead();

        ByteString relation = body.get(0)[1];

        int relId = embedding.getRelationId(relation);

        if (relId < 0){
            return 999999;
        }

        IntHashMap<ByteString> subjects = kb.resultsOneVariable(head);

        double totalSize = 0;

        double scoreSum = 0.0;

        double oaScore = 0.0;

        for (int varPos = 0; varPos <= 2; varPos += 2){

            if (!(head[0].equals(body.get(0)[varPos]))){
                continue;
            }

            for (ByteString subject: subjects) {
                //int subjId = Integer.parseInt(subject.toString());


                ByteString[] testingEdge = body.get(0);

                double maxScore = 0.0;

                double currentScore;

                if (varPos == 0){
                    testingEdge[0] = subject;
                    if (kb.resultsOneVariable(testingEdge).size() > 1){
                        continue;
                    }
                } else {
                    testingEdge[2] = subject;
                    if (kb.resultsOneVariable(testingEdge).size() > 1){
                        continue;
                    }
                }

                int subjId = embedding.getEntityId(subject);

                if (subjId < 0){
                    continue;
                }

                totalSize++;

                //for (ByteString object : kb.getAllEntities()){
                for (int i = 0; i <= 1000; i++){

                    int objId = ThreadLocalRandom.current().nextInt(0, 2952465);
                    if (objId < 0 || objId > 2952465){
                        continue;
                    }



                    //int objId = Integer.parseInt(object.toString());
                    //int objId = embedding.getEntityId(object);

                    if (varPos == 0){
                        //System.out.println("subject: "+subject.toString()+"\trelation: "+body.get(0)[1].toString()+"\tobject: "+object.toString()+"\nsubj: "+subjId+"\trel: "+relId+"\tobj: "+objId);
                        currentScore = embedding.getScore(subjId, relId, objId);

                        if (i == 0){
                            maxScore = currentScore;
                            continue;
                        }
                        if (maxScore > currentScore){
                            maxScore = currentScore;
                        }
                    } else {

                        currentScore = embedding.getScore(objId, relId, subjId);
                        if (i == 0){
                            maxScore = currentScore;
                            continue;
                        }
                        if (maxScore > currentScore){
                            maxScore = currentScore;
                        }
                    }
                }
                scoreSum = scoreSum + maxScore;
            }

            //System.out.println(subjects);


            oaScore = 1-((scoreSum/totalSize)/(1+Math.abs(scoreSum/totalSize)));

        }
        //System.out.println("Rule: "+r.toString()+"\tOAScore: "+oaScore);
        r.setOAScore(oaScore);
        return oaScore;
    }

    @Override
    public void evaluate(){

        LOGGER.info("Starting evaluation...");

        try{
            System.out.println("Evaluating TransE with L1");
            for (double i = 0; i <= 50; i += 1) {
                this.benchmark = new HashMap<>();
                this.result = new HashMap<>();
                BufferedReader benchmarkRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/wikidataVerif.tsv"))));
                BufferedReader resultRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/allRulesTransEL1new.txt"))));
                String line = "";

                while((line = benchmarkRead.readLine()) != null){
                    String[] rLine = line.split("\\s++");
                    if(rLine.length==2){
                        /*if (this.benchmark.containsKey(rLine[0])){
                            this.benchmark.get(rLine[0]).add(rLine[1]);
                            continue;
                        }
                        Set<String> classes = new HashSet<>();
                        classes.add(rLine[1]);
                        this.benchmark.put(rLine[0], classes);*/
                        continue;
                    }
                    if (this.benchmark.containsKey(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">"))){
                        this.benchmark.get(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">")).add(ByteString.of("<http://www.wikidata.org/entity/"+rLine[0].substring(1,rLine[0].length()-1)+">"));
                        continue;
                    }
                    Set<ByteString> classes = new HashSet<>();
                    classes.add(ByteString.of("<http://www.wikidata.org/entity/"+rLine[0]+">"));
                    this.benchmark.put(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">"), classes);


                }



                while((line = resultRead.readLine()) != null){
                    String[] rLine = line.split("\\s++");
                    if (Double.parseDouble(rLine[3]) > ((double)i)/2) {
                        continue;
                    }
                    if (this.result.containsKey(ByteString.of(rLine[2]))){
                        this.result.get(ByteString.of(rLine[2])).add(ByteString.of(rLine[0]));
                        continue;
                    }
                    Set<ByteString> classes = new HashSet<>();
                    classes.add(ByteString.of(rLine[0]));
                    this.result.put(ByteString.of(rLine[2]), classes);
                }

                /*for(String test : this.benchmark.get("P735")){
                System.out.println(test.substring(1,test.length()-2));
                }*/



                double intersectionCount = 0.0;
                for (ByteString property : embedding.relation2id){
                    //System.out.println("Checking: "+property.toString()+"\t"+"in benchmark: "+this.benchmark.containsKey(property)+" in result: "+this.result.containsKey(property));
                    if (this.benchmark.containsKey(property) && this.result.containsKey(property)){
                        //System.out.println("in");
                        Set<ByteString> benchmarkEntities = new HashSet<>();
                        for (ByteString kbClassBenchmark : this.benchmark.get(property)){

                            ByteString[] query = KB.triple(ByteString.of("?x"), concept, kbClassBenchmark);


                            Set<ByteString> entities = kb.resultsOneVariable(query);
                            if (i == 1){
                                //System.out.println("Benchmark entities: "+entities.size());
                            }


                            for (ByteString entity : entities){
                                if (!(benchmarkEntities.contains(entity))){
                                    benchmarkEntities.add(entity);
                                }
                            }
                        }

                        Set<ByteString> resultEntities = new HashSet<>();
                        for (ByteString kbClassResult : this.result.get(property)){

                            ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of(kbClassResult));

                            Set<ByteString> entities = kb.resultsOneVariable(query);
                            //System.out.println("Result entities: "+entities.size());
                            for (ByteString entity : entities){
                                if (!(resultEntities.contains(entity))){
                                    resultEntities.add(entity);
                                }
                            }
                        }

                        Set<ByteString> intersection = new HashSet<>(benchmarkEntities);
                        intersection.retainAll(resultEntities);

                        intersectionCount = intersectionCount + intersection.size();
                    }
                }
                double benchmarkSetSize = 0.0;
                double resultSetSize = 0.0;
                for (ByteString property : embedding.relation2id){
                    if (this.benchmark.containsKey(property)){
                        for (ByteString kbClassBenchmark : this.benchmark.get(property)){

                            ByteString[] query = KB.triple(ByteString.of("?x"), concept, kbClassBenchmark);

                            Set<ByteString> entities = kb.resultsOneVariable(query);
                            //System.out.println("No of entities benchmark: "+entities.size());
                            benchmarkSetSize = benchmarkSetSize + entities.size();
                        }
                    }
                    if (this.result.containsKey(property)){
                        for (ByteString kbClassResult : this.result.get(property)){

                            ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of(kbClassResult));

                            Set<ByteString> entities = kb.resultsOneVariable(query);
                            //System.out.println("No of entities result: "+entities.size());
                            resultSetSize = resultSetSize + entities.size();

                        }
                    }
                }

                double precision = intersectionCount/resultSetSize;

                double recall = intersectionCount/benchmarkSetSize;

                System.out.println("Precision with TH "+i+" : "+intersectionCount+" / "+resultSetSize+" = "+precision);
                System.out.println("Recall with TH "+i+" : "+intersectionCount+" / "+benchmarkSetSize+" = "+recall);
            }



            System.out.println("Evaluating TransE with L2");
            for (double i = 0; i <= 50; i += 1) {
                this.benchmark = new HashMap<>();
                this.result = new HashMap<>();
                BufferedReader benchmarkRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/wikidataVerif.tsv"))));
                BufferedReader resultRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/allRulesTransEL2new.txt"))));
                String line = "";

                while((line = benchmarkRead.readLine()) != null){
                    String[] rLine = line.split("\\s++");
                    if(rLine.length==2){
                        /*if (this.benchmark.containsKey(rLine[0])){
                            this.benchmark.get(rLine[0]).add(rLine[1]);
                            continue;
                        }
                        Set<String> classes = new HashSet<>();
                        classes.add(rLine[1]);
                        this.benchmark.put(rLine[0], classes);*/
                        continue;
                    }
                    if (this.benchmark.containsKey(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">"))){
                        this.benchmark.get(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">")).add(ByteString.of("<http://www.wikidata.org/entity/"+rLine[0].substring(1,rLine[0].length()-1)+">"));
                        continue;
                    }
                    Set<ByteString> classes = new HashSet<>();
                    classes.add(ByteString.of("<http://www.wikidata.org/entity/"+rLine[0]+">"));
                    this.benchmark.put(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">"), classes);


                }



                while((line = resultRead.readLine()) != null){
                    String[] rLine = line.split("\\s++");
                    if (Double.parseDouble(rLine[3])*10 > ((double)i)/2) {
                        continue;
                    }
                    if (this.result.containsKey(ByteString.of(rLine[2]))){
                        this.result.get(ByteString.of(rLine[2])).add(ByteString.of(rLine[0]));
                        continue;
                    }
                    Set<ByteString> classes = new HashSet<>();
                    classes.add(ByteString.of(rLine[0]));
                    this.result.put(ByteString.of(rLine[2]), classes);
                }

                /*for(String test : this.benchmark.get("P735")){
                System.out.println(test.substring(1,test.length()-2));
                }*/



                double intersectionCount = 0.0;
                for (ByteString property : embedding.relation2id){
                    //System.out.println("Checking: "+property.toString()+"\t"+"in benchmark: "+this.benchmark.containsKey(property)+" in result: "+this.result.containsKey(property));
                    if (this.benchmark.containsKey(property) && this.result.containsKey(property)){
                        //System.out.println("in");
                        Set<ByteString> benchmarkEntities = new HashSet<>();
                        for (ByteString kbClassBenchmark : this.benchmark.get(property)){

                            ByteString[] query = KB.triple(ByteString.of("?x"), concept, kbClassBenchmark);

                            Set<ByteString> entities = kb.resultsOneVariable(query);
                            //System.out.println("Benchmark entities: "+entities.size());
                            for (ByteString entity : entities){
                                if (!(benchmarkEntities.contains(entity))){
                                    benchmarkEntities.add(entity);
                                }
                            }
                        }

                        Set<ByteString> resultEntities = new HashSet<>();
                        for (ByteString kbClassResult : this.result.get(property)){

                            ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of(kbClassResult));

                            Set<ByteString> entities = kb.resultsOneVariable(query);
                            //System.out.println("Result entities: "+entities.size());
                            for (ByteString entity : entities){
                                if (!(resultEntities.contains(entity))){
                                    resultEntities.add(entity);
                                }
                            }

                        }

                        Set<ByteString> intersection = new HashSet<>(benchmarkEntities);
                        intersection.retainAll(resultEntities);

                        intersectionCount = intersectionCount + intersection.size();

                    }
                }
                double benchmarkSetSize = 0.0;
                double resultSetSize = 0.0;
                for (ByteString property : embedding.relation2id){
                    if (this.benchmark.containsKey(property)){
                        for (ByteString kbClassBenchmark : this.benchmark.get(property)){

                            ByteString[] query = KB.triple(ByteString.of("?x"), concept, kbClassBenchmark);

                            Set<ByteString> entities = kb.resultsOneVariable(query);
                            //System.out.println("No of entities benchmark: "+entities.size());
                            benchmarkSetSize = benchmarkSetSize + entities.size();
                        }
                    }
                    if (this.result.containsKey(property)){
                        for (ByteString kbClassResult : this.result.get(property)){

                            ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of(kbClassResult));

                            Set<ByteString> entities = kb.resultsOneVariable(query);
                            //System.out.println("No of entities result: "+entities.size());
                            resultSetSize = resultSetSize + entities.size();

                        }
                    }
                }

                double precision = intersectionCount/resultSetSize;

                double recall = intersectionCount/benchmarkSetSize;

                System.out.println("Precision with TH "+i+" : "+intersectionCount+" / "+resultSetSize+" = "+precision);
                System.out.println("Recall with TH "+i+" : "+intersectionCount+" / "+benchmarkSetSize+" = "+recall);
            }


            System.out.println("Evaluating TransE with L1 rules only");
            for (double i = 0; i <= 50; i += 1) {
                this.benchmark = new HashMap<>();
                this.result = new HashMap<>();
                BufferedReader benchmarkRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/wikidataVerif.tsv"))));
                BufferedReader resultRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/allRulesTransEL1new.txt"))));
                String line = "";

                while((line = benchmarkRead.readLine()) != null){
                    String[] rLine = line.split("\\s++");
                    if(rLine.length==2){
                        /*if (this.benchmark.containsKey(rLine[0])){
                            this.benchmark.get(rLine[0]).add(rLine[1]);
                            continue;
                        }
                        Set<String> classes = new HashSet<>();
                        classes.add(rLine[1]);
                        this.benchmark.put(rLine[0], classes);*/
                        continue;
                    }
                    if (this.benchmark.containsKey(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">"))){
                        this.benchmark.get(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">")).add(ByteString.of("<http://www.wikidata.org/entity/"+rLine[0].substring(1,rLine[0].length()-1)+">"));
                        continue;
                    }
                    Set<ByteString> classes = new HashSet<>();
                    classes.add(ByteString.of("<http://www.wikidata.org/entity/"+rLine[0]+">"));
                    this.benchmark.put(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">"), classes);


                }



                while((line = resultRead.readLine()) != null){
                    String[] rLine = line.split("\\s++");
                    if (Double.parseDouble(rLine[3]) > ((double)i)/2) {
                        continue;
                    }
                    if (this.result.containsKey(ByteString.of(rLine[2]))){
                        this.result.get(ByteString.of(rLine[2])).add(ByteString.of(rLine[0]));
                        continue;
                    }
                    Set<ByteString> classes = new HashSet<>();
                    classes.add(ByteString.of(rLine[0]));
                    this.result.put(ByteString.of(rLine[2]), classes);
                }

                /*for(String test : this.benchmark.get("P735")){
                System.out.println(test.substring(1,test.length()-2));
                }*/



                double intersectionCount = 0.0;
                for (ByteString property : embedding.relation2id){
                    //System.out.println("Checking: "+property.toString()+"\t"+"in benchmark: "+this.benchmark.containsKey(property)+" in result: "+this.result.containsKey(property));
                    if (this.benchmark.containsKey(property) && this.result.containsKey(property)){
                        //System.out.println("in");
                        Set<ByteString> benchmarkEntities = new HashSet<>();
                        for (ByteString kbClassBenchmark : this.benchmark.get(property)){

                            if (this.result.get(property).contains(kbClassBenchmark)) {
                                intersectionCount++;
                            }
                        }

                    }
                }
                double benchmarkSetSize = 0.0;
                double resultSetSize = 0.0;
                for (ByteString property : embedding.relation2id){
                    if (this.benchmark.containsKey(property)){
                        benchmarkSetSize = benchmarkSetSize + this.benchmark.get(property).size();
                    }
                    if (this.result.containsKey(property)){
                        resultSetSize = resultSetSize + this.result.get(property).size();
                    }
                }

                double precision = intersectionCount/resultSetSize;

                double recall = intersectionCount/benchmarkSetSize;

                System.out.println("Precision with TH "+i+" : "+intersectionCount+" / "+resultSetSize+" = "+precision);
                System.out.println("Recall with TH "+i+" : "+intersectionCount+" / "+benchmarkSetSize+" = "+recall);
            }

            System.out.println("Evaluating TransE with L2 rules only");
            for (double i = 0; i <= 50; i += 1) {
                this.benchmark = new HashMap<>();
                this.result = new HashMap<>();
                BufferedReader benchmarkRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/wikidataVerif.tsv"))));
                BufferedReader resultRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/allRulesTransEL2new.txt"))));
                String line = "";

                while((line = benchmarkRead.readLine()) != null){
                    String[] rLine = line.split("\\s++");
                    if(rLine.length==2){
                        /*if (this.benchmark.containsKey(rLine[0])){
                            this.benchmark.get(rLine[0]).add(rLine[1]);
                            continue;
                        }
                        Set<String> classes = new HashSet<>();
                        classes.add(rLine[1]);
                        this.benchmark.put(rLine[0], classes);*/
                        continue;
                    }
                    if (this.benchmark.containsKey(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">"))){
                        this.benchmark.get(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">")).add(ByteString.of("<http://www.wikidata.org/entity/"+rLine[0].substring(1,rLine[0].length()-1)+">"));
                        continue;
                    }
                    Set<ByteString> classes = new HashSet<>();
                    classes.add(ByteString.of("<http://www.wikidata.org/entity/"+rLine[0]+">"));
                    this.benchmark.put(ByteString.of("<http://www.wikidata.org/prop/direct/"+rLine[2]+">"), classes);


                }



                while((line = resultRead.readLine()) != null){
                    String[] rLine = line.split("\\s++");
                    if (Double.parseDouble(rLine[3])*10 > ((double)i)/2) {
                        continue;
                    }
                    if (this.result.containsKey(ByteString.of(rLine[2]))){
                        this.result.get(ByteString.of(rLine[2])).add(ByteString.of(rLine[0]));
                        continue;
                    }
                    Set<ByteString> classes = new HashSet<>();
                    classes.add(ByteString.of(rLine[0]));
                    this.result.put(ByteString.of(rLine[2]), classes);
                }

                /*for(String test : this.benchmark.get("P735")){
                System.out.println(test.substring(1,test.length()-2));
                }*/



                double intersectionCount = 0.0;
                for (ByteString property : embedding.relation2id){
                    //System.out.println("Checking: "+property.toString()+"\t"+"in benchmark: "+this.benchmark.containsKey(property)+" in result: "+this.result.containsKey(property));
                    if (this.benchmark.containsKey(property) && this.result.containsKey(property)){
                        //System.out.println("in");
                        Set<ByteString> benchmarkEntities = new HashSet<>();
                        for (ByteString kbClassBenchmark : this.benchmark.get(property)){

                            if (this.result.get(property).contains(kbClassBenchmark)) {
                                intersectionCount++;
                            }
                        }

                    }
                }
                double benchmarkSetSize = 0.0;
                double resultSetSize = 0.0;
                for (ByteString property : embedding.relation2id){
                    if (this.benchmark.containsKey(property)){
                        benchmarkSetSize = benchmarkSetSize + this.benchmark.get(property).size();
                    }
                    if (this.result.containsKey(property)){
                        resultSetSize = resultSetSize + this.result.get(property).size();
                    }
                }

                double precision = intersectionCount/resultSetSize;

                double recall = intersectionCount/benchmarkSetSize;

                System.out.println("Precision with TH "+i+" : "+intersectionCount+" / "+resultSetSize+" = "+precision);
                System.out.println("Recall with TH "+i+" : "+intersectionCount+" / "+benchmarkSetSize+" = "+recall);
            }



            System.out.println("Evaluating TransE with Hybrid and L2");
            for (int i = 0; i <= 10; i += 1) {
                for (double j = 5; j <= 15; j++) {


                    this.benchmark = new HashMap<>();
                    this.result = new HashMap<>();
                    BufferedReader benchmarkRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/wikidataVerif.tsv"))));
                    BufferedReader resultRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/allRulesTransEHybridL2new.txt"))));
                    String line = "";

                    while ((line = benchmarkRead.readLine()) != null) {
                        String[] rLine = line.split("\\s++");
                        if (rLine.length == 2) {
                        /*if (this.benchmark.containsKey(rLine[0])){
                            this.benchmark.get(rLine[0]).add(rLine[1]);
                            continue;
                        }
                        Set<String> classes = new HashSet<>();
                        classes.add(rLine[1]);
                        this.benchmark.put(rLine[0], classes);*/
                            continue;
                        }
                        if (this.benchmark.containsKey(ByteString.of("<http://www.wikidata.org/prop/direct/" + rLine[2] + ">"))) {
                            this.benchmark.get(ByteString.of("<http://www.wikidata.org/prop/direct/" + rLine[2] + ">")).add(ByteString.of("<http://www.wikidata.org/entity/" + rLine[0].substring(1, rLine[0].length() - 1) + ">"));
                            continue;
                        }
                        Set<ByteString> classes = new HashSet<>();
                        classes.add(ByteString.of("<http://www.wikidata.org/entity/" + rLine[0] + ">"));
                        this.benchmark.put(ByteString.of("<http://www.wikidata.org/prop/direct/" + rLine[2] + ">"), classes);


                    }


                    while ((line = resultRead.readLine()) != null) {
                        String[] rLine = line.split("\\s++");
                        if (Double.parseDouble(rLine[(int)j]) < ((double)i)/10) {
                            continue;
                        }
                        if (this.result.containsKey(ByteString.of(rLine[2]))) {
                            this.result.get(ByteString.of(rLine[2])).add(ByteString.of(rLine[0]));
                            continue;
                        }
                        Set<ByteString> classes = new HashSet<>();
                        classes.add(ByteString.of(rLine[0]));
                        this.result.put(ByteString.of(rLine[2]), classes);
                    }

                /*for(String test : this.benchmark.get("P735")){
                System.out.println(test.substring(1,test.length()-2));
                }*/


                    double intersectionCount = 0.0;
                    for (ByteString property : embedding.relation2id) {
                        //System.out.println("Checking: "+property.toString()+"\t"+"in benchmark: "+this.benchmark.containsKey(property)+" in result: "+this.result.containsKey(property));
                        if (this.benchmark.containsKey(property) && this.result.containsKey(property)) {
                            //System.out.println("in");
                            Set<ByteString> benchmarkEntities = new HashSet<>();
                            for (ByteString kbClassBenchmark : this.benchmark.get(property)) {

                                ByteString[] query = KB.triple(ByteString.of("?x"), concept, kbClassBenchmark);


                                Set<ByteString> entities = kb.resultsOneVariable(query);
                                if (i == 1) {
                                    //System.out.println("Benchmark entities: " + entities.size());
                                }


                                for (ByteString entity : entities) {
                                    if (!(benchmarkEntities.contains(entity))) {
                                        benchmarkEntities.add(entity);
                                    }
                                }
                            }

                            Set<ByteString> resultEntities = new HashSet<>();
                            for (ByteString kbClassResult : this.result.get(property)) {

                                ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of(kbClassResult));

                                Set<ByteString> entities = kb.resultsOneVariable(query);
                                //System.out.println("Result entities: "+entities.size());
                                for (ByteString entity : entities) {
                                    if (!(resultEntities.contains(entity))) {
                                        resultEntities.add(entity);
                                    }
                                }
                            }

                            Set<ByteString> intersection = new HashSet<>(benchmarkEntities);
                            intersection.retainAll(resultEntities);

                            intersectionCount = intersectionCount + intersection.size();
                        }
                    }
                    double benchmarkSetSize = 0.0;
                    double resultSetSize = 0.0;
                    for (ByteString property : embedding.relation2id) {
                        if (this.benchmark.containsKey(property)) {
                            for (ByteString kbClassBenchmark : this.benchmark.get(property)) {

                                ByteString[] query = KB.triple(ByteString.of("?x"), concept, kbClassBenchmark);

                                Set<ByteString> entities = kb.resultsOneVariable(query);
                                //System.out.println("No of entities benchmark: "+entities.size());
                                benchmarkSetSize = benchmarkSetSize + entities.size();
                            }
                        }
                        if (this.result.containsKey(property)) {
                            for (ByteString kbClassResult : this.result.get(property)) {

                                ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of(kbClassResult));

                                Set<ByteString> entities = kb.resultsOneVariable(query);
                                //System.out.println("No of entities result: "+entities.size());
                                resultSetSize = resultSetSize + entities.size();

                            }
                        }
                    }

                    double precision = intersectionCount / resultSetSize;

                    double recall = intersectionCount / benchmarkSetSize;

                    System.out.println("Precision with TH " + i + " and lambda "+(j-5)/10+": " + intersectionCount + " / " + resultSetSize + " = " + precision);
                    System.out.println("Recall with TH " + i + " and lambda "+(j-5)/10+": " + intersectionCount + " / " + benchmarkSetSize + " = " + recall);
                }
            }

            System.out.println("Evaluating TransE with Hybrid and L1");
            for (int i = 0; i <= 10; i += 1) {
                for (double j = 5; j <= 15; j++) {


                    this.benchmark = new HashMap<>();
                    this.result = new HashMap<>();
                    BufferedReader benchmarkRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/wikidataVerif.tsv"))));
                    BufferedReader resultRead = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/hessler/Documents/final/allRulesTransEHybridL1new.txt"))));
                    String line = "";

                    while ((line = benchmarkRead.readLine()) != null) {
                        String[] rLine = line.split("\\s++");
                        if (rLine.length == 2) {
                        /*if (this.benchmark.containsKey(rLine[0])){
                            this.benchmark.get(rLine[0]).add(rLine[1]);
                            continue;
                        }
                        Set<String> classes = new HashSet<>();
                        classes.add(rLine[1]);
                        this.benchmark.put(rLine[0], classes);*/
                            continue;
                        }
                        if (this.benchmark.containsKey(ByteString.of("<http://www.wikidata.org/prop/direct/" + rLine[2] + ">"))) {
                            this.benchmark.get(ByteString.of("<http://www.wikidata.org/prop/direct/" + rLine[2] + ">")).add(ByteString.of("<http://www.wikidata.org/entity/" + rLine[0].substring(1, rLine[0].length() - 1) + ">"));
                            continue;
                        }
                        Set<ByteString> classes = new HashSet<>();
                        classes.add(ByteString.of("<http://www.wikidata.org/entity/" + rLine[0] + ">"));
                        this.benchmark.put(ByteString.of("<http://www.wikidata.org/prop/direct/" + rLine[2] + ">"), classes);


                    }


                    while ((line = resultRead.readLine()) != null) {
                        String[] rLine = line.split("\\s++");
                        if (Double.parseDouble(rLine[(int)j]) < ((double)i)/10) {
                            continue;
                        }
                        if (this.result.containsKey(ByteString.of(rLine[2]))) {
                            this.result.get(ByteString.of(rLine[2])).add(ByteString.of(rLine[0]));
                            continue;
                        }
                        Set<ByteString> classes = new HashSet<>();
                        classes.add(ByteString.of(rLine[0]));
                        this.result.put(ByteString.of(rLine[2]), classes);
                    }

                /*for(String test : this.benchmark.get("P735")){
                System.out.println(test.substring(1,test.length()-2));
                }*/


                    double intersectionCount = 0.0;
                    for (ByteString property : embedding.relation2id) {
                        //System.out.println("Checking: "+property.toString()+"\t"+"in benchmark: "+this.benchmark.containsKey(property)+" in result: "+this.result.containsKey(property));
                        if (this.benchmark.containsKey(property) && this.result.containsKey(property)) {
                            //System.out.println("in");
                            Set<ByteString> benchmarkEntities = new HashSet<>();
                            for (ByteString kbClassBenchmark : this.benchmark.get(property)) {

                                ByteString[] query = KB.triple(ByteString.of("?x"), concept, kbClassBenchmark);


                                Set<ByteString> entities = kb.resultsOneVariable(query);
                                if (i == 1) {
                                    //System.out.println("Benchmark entities: " + entities.size());
                                }


                                for (ByteString entity : entities) {
                                    if (!(benchmarkEntities.contains(entity))) {
                                        benchmarkEntities.add(entity);
                                    }
                                }
                            }

                            Set<ByteString> resultEntities = new HashSet<>();
                            for (ByteString kbClassResult : this.result.get(property)) {

                                ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of(kbClassResult));

                                Set<ByteString> entities = kb.resultsOneVariable(query);
                                //System.out.println("Result entities: "+entities.size());
                                for (ByteString entity : entities) {
                                    if (!(resultEntities.contains(entity))) {
                                        resultEntities.add(entity);
                                    }
                                }
                            }

                            Set<ByteString> intersection = new HashSet<>(benchmarkEntities);
                            intersection.retainAll(resultEntities);

                            intersectionCount = intersectionCount + intersection.size();
                        }
                    }
                    double benchmarkSetSize = 0.0;
                    double resultSetSize = 0.0;
                    for (ByteString property : embedding.relation2id) {
                        if (this.benchmark.containsKey(property)) {
                            for (ByteString kbClassBenchmark : this.benchmark.get(property)) {

                                ByteString[] query = KB.triple(ByteString.of("?x"), concept, kbClassBenchmark);

                                Set<ByteString> entities = kb.resultsOneVariable(query);
                                //System.out.println("No of entities benchmark: "+entities.size());
                                benchmarkSetSize = benchmarkSetSize + entities.size();
                            }
                        }
                        if (this.result.containsKey(property)) {
                            for (ByteString kbClassResult : this.result.get(property)) {

                                ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of(kbClassResult));

                                Set<ByteString> entities = kb.resultsOneVariable(query);
                                //System.out.println("No of entities result: "+entities.size());
                                resultSetSize = resultSetSize + entities.size();

                            }
                        }
                    }

                    double precision = intersectionCount / resultSetSize;

                    double recall = intersectionCount / benchmarkSetSize;

                    System.out.println("Precision with TH " + i + " and lambda "+(j-5)/10+": " + intersectionCount + " / " + resultSetSize + " = " + precision);
                    System.out.println("Recall with TH " + i + " and lambda "+(j-5)/10+": " + intersectionCount + " / " + benchmarkSetSize + " = " + recall);
                }
            }

            LOGGER.info("Finished evaluation");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }



    }

}
