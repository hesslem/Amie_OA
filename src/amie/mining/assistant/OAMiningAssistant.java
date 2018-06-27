package amie.mining.assistant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import amie.mining.ConfidenceMetric;
import javatools.datatypes.ByteString;
import javatools.datatypes.IntHashMap;
import javatools.datatypes.Pair;
import amie.data.KB;
import amie.rules.Rule;

public class OAMiningAssistant extends MiningAssistant {

    public OAMiningAssistant(KB datasource){
        super(datasource);
        super.maxDepth = 2;




    }

    @Override
    public boolean testConfidenceThresholds(Rule candidate) {

        return true;
    }

    @Override
    public void getInitialAtomsFromSeeds(Collection<ByteString> relations, double minSupportThreshold, Collection<Rule> output) {

        Rule emptyQuery = new Rule();

    }

    @Override




}
