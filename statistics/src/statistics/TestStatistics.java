package statistics;

import java.util.concurrent.BlockingQueue;

import robusta.MPMCFactory;
import robusta.Robusta;
import robusta.RobustaClient;
import robusta.RobustaCommand;
import robusta.RobustaConfiguration;
import robusta.consumer.Pattern;
import robusta.consumer.PatternConsumer;
import robusta.lod.Triplestore;
import robusta.lod.TriplestoreSampler;
import robusta.producer.PatternProducer;
import statistics.MPMC.HistoryMonitor;
import statistics.MPMC.StatisticsConsumer;
import statistics.MPMC.StatisticsMonitor;
import statistics.MPMC.StatisticsProducer;
import statistics.MPMC.StatisticsSampler;

public class TestStatistics {

	public static void main(String[] args) {
		Robusta robusta = new Robusta(new MPMCFactory() {
			
			private TriplestoreSampler sampler = null;
			
			@Override
			public PatternProducer newProducer(BlockingQueue<Pattern> queue, TriplestoreSampler sampler) {
				return new StatisticsProducer(queue, sampler);
			}
			
			@Override
			public PatternConsumer newConsumer(BlockingQueue<Pattern> queue) {
				return new StatisticsConsumer(queue, "properties/statistics.properties");
			}

			@Override
			public TriplestoreSampler getTriplestoreSampler() {
				if (sampler == null)
					sampler = new StatisticsSampler("properties/statistics.properties");
				return sampler;
			}

		}, new RobustaConfiguration("properties/robusta_statistics.properties"));
		
		robusta.addTriplestore(new Triplestore("http://dbpedia.org/sparql"));

		robusta.addMonitor(new StatisticsMonitor("properties/statistics.properties"));
		robusta.addMonitor(new HistoryMonitor("properties/statistics.properties"));
		
		robusta.open();
		
		RobustaClient client = new RobustaClient(new RobustaConfiguration("properties/robusta_statistics.properties"));

		client.send(RobustaCommand.START);
		try {
			Thread.sleep(5 * 60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		client.send(RobustaCommand.STOP);
		client.send(RobustaCommand.CLOSE);
	}

}
