package amie.mining.assistant;

import amie.data.KB;
//import amie.mining.assistant.MiningAssistant;
//import amie.mining.assistant.MiningOperator;
import amie.rules.Rule;
//import javatools.database.Virtuoso;
import javatools.datatypes.ByteString;
import javatools.datatypes.IntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SchemaAttributeMiningAssistant extends MiningAssistant {

    ByteString concept;
    //static KB complete;
    //static Virtuoso virtuoso = new Virtuoso();
    //static Object myLock = new Object();
    double classSize;

    public SchemaAttributeMiningAssistant(KB dataSource, KB completeKB) {
        super(dataSource);
        //virtuoso = new Virtuoso();
        //bodyExcludedRelations = Arrays.asList(ByteString.of("<rdf:type>"));
        super.maxDepth = 2;
        super.setEnablePerfectRules(true);
        super.minPcaConfidence = 0.0;
        super.minStdConfidence = 0.0;
        //Class in Head Relationship that should be mined
        this.concept = ByteString.of("<http://dbpedia.org/ontology/City>");
        super.bodyExcludedRelations = Arrays.asList(ByteString.of("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"));
        //this.complete = completeKB;

    }


    public SchemaAttributeMiningAssistant(KB dataSource, String type) {
        super(dataSource);
        //virtuoso = new Virtuoso();
        //bodyExcludedRelations = Arrays.asList(ByteString.of("<rdf:type>"));
        super.maxDepth = 2;
        super.setEnablePerfectRules(true);
        super.minPcaConfidence = 0.0;
        super.minStdConfidence = 0.0;
        super.countAlwaysOnSubject = true;
        //Class in Head Relationship that should be mined
        this.concept = ByteString.of(type);
        super.bodyExcludedRelations = Arrays.asList(ByteString.of("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"), ByteString.of("<http://purl.org/dc/terms/subject>"));
        ByteString[] query = KB.triple(ByteString.of("?x"), concept, ByteString.of("?y"));
        this.classSize = kb.count(query);
        System.out.println(this.classSize);
        //this.classSize = virtuoso.getSubjectSize(this.concept.toString());


    }

    @Override
    public String getDescription() {
        ByteString test = ByteString.of("test");
        return "Rules of the form r(x,y) r(x,z) => type(x, C) or r(x,c1) r(x,c2) => type(x, C)"+test;
    }


    @Override
    public void getInitialAtomsFromSeeds(Collection<ByteString> relations,
                                                     double minSupportThreshold, Collection<Rule> output) {
        Rule emptyQuery = new Rule();

        ByteString relation = relations.iterator().next();

        ByteString[] newEdge = emptyQuery.fullyUnboundTriplePattern();

        emptyQuery.getTriples().add(newEdge);

        newEdge[1] = relation;
        int countVarPos = countAlwaysOnSubject ? 0 : findCountingVariable(newEdge);
        ByteString countingVariable = newEdge[countVarPos];
        long cardinality = kb.countDistinct(countingVariable, emptyQuery.getTriples());

        ByteString[] succedent = newEdge.clone();
        Rule candidate = new Rule(succedent, cardinality);
        System.out.println(candidate);
        candidate.setFunctionalVariablePosition(countVarPos);
        registerHeadRelation(candidate);


        ByteString[] danglingEdge = candidate.getTriples().get(0);
        IntHashMap<ByteString> constants = kb.frequentBindingsOf(danglingEdge[2], candidate.getFunctionalVariable(), candidate.getTriples());
        cardinality = constants.get(this.concept);

        Rule newCandidate = candidate.instantiateConstant(2, this.concept, cardinality);
        System.out.println(newCandidate);
        output.add(candidate);

    }

    @Override
    public void getInitialAtoms(double minSupportThreshold, Collection<Rule> output) {
        List<ByteString[]> newEdgeList = new ArrayList<ByteString[]>(1);
        ByteString[] newEdge = new ByteString[]{ByteString.of("?x"), ByteString.of(this.concept), ByteString.of("?z")};
        System.out.println(newEdge[1]);
        newEdgeList.add(newEdge);
        IntHashMap<ByteString> relations = kb.frequentBindingsOf(newEdge[1], newEdge[0], newEdgeList);
        buildInitialQueries(relations, minSupportThreshold, output);
    }


    @Override
    public void getDanglingAtoms(Rule rule, double minSupportThreshold, Collection<Rule> output) {
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
            rule.getTriples().add(newEdge);
//            System.out.println("JOIN VARIABLE " + joinVariable);
//            System.out.println("NEWEDGE " + newEdge[1]);
//            System.out.println("Functional Variable " + rule.getFunctionalVariable());
//            System.out.println("CurrentRule " + rule.toString());
            IntHashMap<ByteString> promisingRelations = kb.frequentBindingsOf(newEdge[1],
                    rule.getFunctionalVariable(), rule.getTriples());
            //System.out.println("HASHMAPSIZE " + promisingRelations.size());
            rule.getTriples().remove(nPatterns);

            int danglingPosition = (joinPosition == 0 ? 2 : 0);

            //System.out.println("Number of Promising Relations " + promisingRelations.size());
            for (ByteString relation : promisingRelations) {
                if (this.bodyExcludedRelations != null &&
                        this.bodyExcludedRelations.contains(relation))
                    continue;
                //Here we still have to make a redundancy check
                int cardinality = promisingRelations.get(relation);


                //check wether the cardinality of the joined relationship is above the minimum Threshold
                //Problems: Isnt the Support between 0 and 1, and the cardinality always above 1?
                if (cardinality >= minSupportThreshold) {
                    if (!rule.containsRelation(relation)) {
                        newEdge[1] = relation;
                        Rule candidate = rule.addAtom(newEdge, cardinality);
                        if (candidate.containsUnifiablePatterns()) {
                            //Verify whether dangling variable unifies to a single value (I do not like this hack)
                            if (kb.countDistinct(newEdge[danglingPosition], candidate.getTriples()) < 2)
                                continue;
                        }

                        candidate.setHeadCoverage((double) candidate.getSupport()
                                / headCardinalities.get(candidate.getHeadRelation()));
                        candidate.setSupportRatio((double) candidate.getSupport()
                                / (double) getTotalCount(candidate));
                        candidate.setParent(rule);
                        //candidate.setClassConfidence(getClassConfidence(candidate));
                        //candidate.setFrequency(cardinality/this.classSize);

                        //Here we add the generated candidate to the output collection
                        output.add(candidate);
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
        if (getOAConfidence(candidate) > 0.8) {
            System.out.println("Frequency tested");
            //candidate.setClassConfidence(this.getClassConfidence(candidate));
            return true;

        } else {

            return false;

        }
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

        frequency = support/this.classSize;
        return frequency;
    }



    /**
     * A method that computes the specificity of a rule regarding the input class.
     *
     * @param r
     * @return
     */

    public double getClassConfidence(Rule r){
        double classConfidence = 0.0;

        ByteString[] head = r.getHead();
        ByteString countVariable = null;
        countVariable = head[0];
        //r.setClassConfidence(this.getClassConfidence(r));
        long support = kb.countDistinct(countVariable, r.getTriples());
        //String query = r.bodyToSparql();
        //System.out.println(query);
        double supportComplete;
        /*synchronized (myLock) {
            supportComplete = virtuoso.getResultSize(query);

        }*/
        ByteString[] query1 = KB.triple(ByteString.of("?x"), concept, ByteString.of("?y"));
        supportComplete = kb.count(query1);
        //System.out.println("SUPPORT " + support);
        //System.out.println("ALL " + supportComplete);


        classConfidence = support/supportComplete;
        System.out.println(classConfidence);
        //System.out.println("CONFIDENCE " + classConfidence);
        return classConfidence;
    }

    public double getOAConfidence(Rule r){


    }

    @Override
    public String toString(){
        return "SchemaMining";
    }
}
