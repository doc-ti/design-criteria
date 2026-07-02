package edu.doc_ti.phivalidation.gendata.bitstreamstats;

import net.datafaker.providers.base.BaseFaker;

public class CustomFakerBitstreamStats extends BaseFaker {

	public ElementsBitstreamStats getElements() {
        return getProvider(ElementsBitstreamStats.class, ElementsBitstreamStats::new, this);
    }

}

