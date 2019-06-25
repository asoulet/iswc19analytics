package histogram;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import histogram.MPMC.HistogramMonitor;
import histogram.MPMC.RCCDatabaseConsumer;
import histogram.MPMC.RCCProducer;
import histogram.MPMC.RCCSampler;
import histogram.representativeness.RepresentativenessAnalyzer;
import robusta.MPMCFactory;
import robusta.MPMCMonitor;
import robusta.RobustaApplication;
import robusta.RobustaConfiguration;
import robusta.consumer.Pattern;
import robusta.consumer.PatternConsumer;
import robusta.lod.Triplestore;
import robusta.lod.TriplestoreSampler;
import robusta.producer.PatternProducer;

public class LODHistogramApplication extends RobustaApplication {

	private static final String [] ENDPOINTS = {
			"http://affymetrix.bio2rdf.org/sparql",
			"http://alaska.eagle-i.net/sparqler/sparql",
			"http://apps.morelab.deusto.es/labman/sparql",
			"http://biomodels.bio2rdf.org/sparql",
			"http://bioportal.bio2rdf.org/sparql",
			"http://cau.eagle-i.net/sparqler/sparql",
			"http://ccny-cuny.eagle-i.net/sparqler/sparql",
			"http://cdrewu.eagle-i.net/sparqler/sparql",
			"http://clinicaltrials.bio2rdf.org/sparql",
			"http://cr.eionet.europa.eu/sparql",
			"http://ctd.bio2rdf.org/sparql",
			"http://data.allie.dbcls.jp/sparql",
			"http://data.bnf.fr/sparql",
			"http://data.hnm.hu/sparql",
			"http://data.logainm.ie/sparql",
			"http://data.nobelprize.org/sparql",
			"http://data.open.ac.uk/query",
			"http://data.organic-edunet.eu/sparql",
			"http://data.persee.fr/sparql",
			"http://data.rism.info/sparql",
			"http://data.szepmuveszeti.hu/sparql",
			"http://data.utpl.edu.ec/ecuadorresearch/lod/sparql",
			"http://data.utpl.edu.ec/utpl/lod/sparql",
			"http://dati.camera.it/sparql",
			"http://dati.isprambiente.it/sparql",
			"http://datos.bcn.cl/sparql",
			"http://dbkwik.webdatacommons.org/sparql",
			"http://dbpedia.org/sparql",
			"http://dbsnp.bio2rdf.org/sparql",
			"http://dbtune.org/bbc/peel/sparql",
			"http://dbtune.org/classical/sparql",
			"http://dbtune.org/jamendo/sparql",
			"http://dbtune.org/magnatune/sparql",
			"http://dbtune.org/musicbrainz/sparql",
			"http://drugbank.bio2rdf.org/sparql",
			"http://enipedia.tudelft.nl/sparql",
			"http://en.openei.org/sparql",
			"http://environment.data.gov.uk/sparql/bwq/query",
			"http://es.dbpedia.org/sparql",
			"http://eu.dbpedia.org/sparql",
			"http://famu.eagle-i.net/sparqler/sparql",
			"http://fr.dbpedia.org/sparql",
			"http://genage.bio2rdf.org/sparql",
			"http://gendr.bio2rdf.org/sparql",
			"http://goa.bio2rdf.org/sparql",
			"http://greek-lod.auth.gr/police/sparql",
			"http://harvard.eagle-i.net/sparqler/sparql",
			"http://hawaii.eagle-i.net/sparqler/sparql",
			"http://hgnc.bio2rdf.org/sparql",
			"http://homologene.bio2rdf.org/sparql",
			"http://howard.eagle-i.net/sparqler/sparql",
			"http://hunter-cuny.eagle-i.net/sparqler/sparql",
			"http://id.sgcb.mcu.es/sparql",
			"http://interpro.bio2rdf.org/sparql",
			"http://irefindex.bio2rdf.org/sparql",
			"http://ja.dbpedia.org/sparql",
			"http://jsu.eagle-i.net/sparqler/sparql",
			"http://kent.zpr.fer.hr:8080/educationalProgram/sparql",
			"http://ldf.fi/warsa/sparql",
			"http://ldf.fi/ww1lod/sparql",
			"http://linkeddata.finki.ukim.mk/sparql",
			"http://linkedgeodata.org/sparql",
			"http://linked.opendata.cz/sparql",
			"http://lod.b3kat.de/sparql",
			"http://lod.xdams.org/sparql",
			"http://lsr.bio2rdf.org/sparql",
			"http://meharry.eagle-i.net/sparqler/sparql",
			"http://mesh.bio2rdf.org/sparql",
			"http://mgi.bio2rdf.org/sparql",
			"http://montana.eagle-i.net/sparqler/sparql",
			"http://msm.eagle-i.net/sparqler/sparql",
			"http://ncbigene.bio2rdf.org/sparql",
			"http://ndc.bio2rdf.org/sparql",
			"http://nl.dbpedia.org/sparql",
			"http://ohsu.eagle-i.net/sparqler/sparql",
			"http://omim.bio2rdf.org/sparql",
			"http://opendata.aragon.es/sparql",
			"http://orphanet.bio2rdf.org/sparql",
			"http://pharmgkb.bio2rdf.org/sparql",
			"http://psm.eagle-i.net/sparqler/sparql",
			"http://pt.dbpedia.org/sparql",
			"http://pubmed.bio2rdf.org/sparql",
			"http://resource.geolba.ac.at/PoolParty/sparql/GeologicUnit",
			"http://semantic.eea.europa.eu/sparql",
			"http://semanticweb.cs.vu.nl/dss/sparql",
			"http://semanticweb.cs.vu.nl/verrijktkoninkrijk/sparql",
			"http://serendipity.utpl.edu.ec/lod/sparql",
			"http://sgd.bio2rdf.org/sparql",
			"http://sider.bio2rdf.org/sparql",
			"https://linked.opendata.cz/sparql",
			"https://nkod.opendata.cz/sparql",
			"http://sparql.odw.tw",
			"http://sparql.openmobilenetwork.org",
			"https://ruian.linked.opendata.cz/sparql",
			"https://www.ebi.ac.uk/rdf/services/biomodels/sparql",
			"https://www.ebi.ac.uk/rdf/services/chembl/sparql",
			"https://www.ebi.ac.uk/rdf/services/reactome/sparql",
			"http://taxonomy.bio2rdf.org/sparql",
			"http://taxref.mnhn.fr/sparql",
			"http://tsu.eagle-i.net/sparqler/sparql",
			"http://tuskegee.eagle-i.net/sparqler/sparql",
			"http://uccaribe.eagle-i.net/sparqler/sparql",
			"http://upr.eagle-i.net/sparqler/sparql",
			"http://utep.eagle-i.net/sparqler/sparql",
			"http://utsa.eagle-i.net/sparqler/sparql",
			"http://vocabulary.semantic-web.at/PoolParty/sparql/AustrianSkiTeam",
			"http://vocabulary.semantic-web.at/PoolParty/sparql/OpenData",
			"http://vocabulary.semantic-web.at/PoolParty/sparql/semweb",
			"http://vocabulary.wolterskluwer.de/PoolParty/sparql/arbeitsrecht",
			"http://vocabulary.wolterskluwer.de/PoolParty/sparql/court",
			"http://webenemasuno.linkeddata.es/sparql",
			"http://webisa.webdatacommons.org/sparql",
			"http://wormbase.bio2rdf.org/sparql",
			"http://www.ebi.ac.uk/rdf/services/atlas/sparql",
			"http://www.icane.es/opendata/sparql",
			"http://www.imagesnippets.com/sparql/images",
			"http://www.influencetracker.com:8890/sparql",
			"http://www.linklion.org:8890/sparql",
			"http://www.lotico.com:3030/lotico/sparql",
			"http://xula.eagle-i.net/sparqler/sparql",
			"http://zbw.eu/beta/sparql/stw/query"
	};
	

	public static void main(String[] args) {
		LODHistogramApplication application = new LODHistogramApplication();
		/*String[] open = {"-a","open"};
		String[] start = {"-a","start"};
		String[] stop = {"-a","stop"};
		String[] close = {"-a","close"};*/
		//application.run(open);
		//application.run(start);
		//application.run(stop);
		//application.run(close);
		application.run(args);
	}

	@Override
	public ArrayList<Triplestore> getTriplestores() {
		String [] list = ENDPOINTS;
		ArrayList<Triplestore> triplestores = new ArrayList<>();
		for (int i = 0; i < ENDPOINTS.length; i++)
			triplestores.add(new Triplestore(list[i]));
		return triplestores;
	}

	@Override
	public RobustaConfiguration getRobustaConfiguration() {
		return new RobustaConfiguration("properties/robusta.properties");
	}

	@Override
	public MPMCFactory getMPMCFactory() {
		return new MPMCFactory() {
			
			private TriplestoreSampler sampler = null;
			
			@Override
			public PatternProducer newProducer(BlockingQueue<Pattern> queue, TriplestoreSampler sampler) {
				return new RCCProducer(queue, sampler);
			}
			
			@Override
			public PatternConsumer newConsumer(BlockingQueue<Pattern> queue) {
				return new RCCDatabaseConsumer(queue, "properties/histogram.properties");
			}

			@Override
			public TriplestoreSampler getTriplestoreSampler() {
				if (sampler == null)
					sampler = new RCCSampler("properties/histogram.properties");
				return sampler;
			}

		};
	}

	@Override
	public ArrayList<MPMCMonitor> getMPMCMonitors() {
		ArrayList<MPMCMonitor> monitors = new ArrayList<>();
		monitors.add(new HistogramMonitor("properties/histogram.properties"));
		monitors.add(new RepresentativenessAnalyzer("properties/histogram.properties"));
		return monitors;
	}

	
}
